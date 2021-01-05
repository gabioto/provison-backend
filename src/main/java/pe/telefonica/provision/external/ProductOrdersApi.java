package pe.telefonica.provision.external;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pe.telefonica.provision.conf.ExternalApi;
import pe.telefonica.provision.conf.IBMSecuritySeguridad;
import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.common.ApiResponse;
import pe.telefonica.provision.external.response.ProductOrderResponse;
import pe.telefonica.provision.model.OAuthToken;
import pe.telefonica.provision.model.order.Order;
import pe.telefonica.provision.repository.OAuthTokenRepository;
import pe.telefonica.provision.util.constants.Constants;
import pe.telefonica.provision.util.exception.FunctionalErrorException;
import pe.telefonica.provision.util.exception.ServerNotFoundException;

@Component
public class ProductOrdersApi extends ConfigRestTemplate {
	private static final Log log = LogFactory.getLog(ProductOrdersApi.class);

	final Gson gson = new Gson();

	@Autowired
	private OAuthTokenRepository oAuthTokenRepository;

	@Autowired
	private ExternalApi api;

	@Autowired
	private IBMSecuritySeguridad security;

	@Autowired
	TrazabilidadSecurityApi loggerApi;

	@Autowired
	private HttpComponentsClientHttpRequestFactory initClientRestTemplate;

	public List<Order> getProductOrders(String publicId, String order, String orderCode, String customerCode) {

		String oAuthToken;
		UriComponentsBuilder builder;
		LocalDateTime startHour = LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE));

		// Implementacion SSL
		RestTemplate restTemplate = new RestTemplate(initClientRestTemplate);
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		String requestUrl = api.getProductOrders();
		//log.info("updatePSIClient - URL: " + requestUrl);

		oAuthToken = getAuthToken();

		HttpHeaders headers = new HttpHeaders();
		headers.set("UNICA-ServiceId", "");
		headers.set("UNICA-Application", "");
		headers.set("UNICA-PID", "");
		headers.set("UNICA-User", "");
		headers.set("Authorization", "Bearer " + oAuthToken);
		headers.set("X-IBM-Client-Id", api.getApiClient());
		headers.set("migrationIndicator", "2");
		headers.set("originSystem", "2");

		//log.info("updatePSIClient - headers: " + headers.toString());

		if (!order.isEmpty()) {
			builder = UriComponentsBuilder.fromHttpUrl(requestUrl).queryParam("id", order);
		} else {
			builder = UriComponentsBuilder.fromHttpUrl(requestUrl).queryParam("customerId", customerCode)
					.queryParam("accountId", orderCode).queryParam("productType", "").queryParam("publicId", publicId)
					.queryParam("relatedSource.name", "").queryParam("relatedSource.customerId", "")
					.queryParam("relatedSource.accountId", "").queryParam("relatedSource.serviceCode", "");
		}

		try {
			ResponseEntity<List<ProductOrderResponse>> responseEntity = restTemplate.exchange(builder.toUriString(),
					HttpMethod.GET, new HttpEntity<>(headers),
					new ParameterizedTypeReference<List<ProductOrderResponse>>() {
					});

			loggerApi.thirdLogEvent("CMS", "getProductOrders", new Gson().toJson(builder.toUriString()),
					new Gson().toJson(responseEntity.getBody()).toString(), requestUrl, startHour,
					LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)), responseEntity.getStatusCodeValue());

			//log.info("updatePSIClient - responseEntity.Body: " + responseEntity.getBody().toString());

			return responseEntity.getBody().parallelStream().map(response -> {
				return response.fromThis(publicId);
			}).collect(Collectors.toList());

		} catch (HttpClientErrorException ex) {
			log.error("HttpClientErrorException = " + ex.getMessage());
			log.error("getResponseBodyAsString = " + ex.getResponseBodyAsString());

			loggerApi.thirdLogEvent("CMS", "getProductOrders", new Gson().toJson(""), ex.getResponseBodyAsString(),
					requestUrl, startHour, LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)),
					ex.getStatusCode().value());

			JsonObject jsonDecode = new Gson().fromJson(ex.getResponseBodyAsString(), JsonObject.class);
			System.out.println(jsonDecode);

			JsonObject appDetail = jsonDecode.getAsJsonObject("BodyOut").getAsJsonObject("ClientException")
					.getAsJsonObject("appDetail");
			String message = appDetail.get("exceptionAppMessage").toString();
			String codeError = appDetail.get("exceptionAppCode").toString();

			throw new FunctionalErrorException(message, ex, codeError);

		} catch (Exception ex) {
			log.error("Exception = " + ex.getMessage());
			loggerApi.thirdLogEvent("CMS", "getProductOrders", new Gson().toJson(""), ex.getMessage(), requestUrl,
					startHour, LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)),
					HttpStatus.INTERNAL_SERVER_ERROR.value());
			throw new ServerNotFoundException(ex.getMessage());
		}
	}

	private String getTokenFromPSI(boolean toInsert) {
		RestTemplate restTemplate = new RestTemplate();
		boolean updated = true;
		String urlToken = api.getSecurityUrl() + api.getOauthToken();

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", security.getAuth());
		headersMap.add("X-IBM-Client-Id", security.getClientId());
		headersMap.add("X-IBM-Client-Secret", security.getClientSecret());

		ApiRequest<Object> request = new ApiRequest<Object>(Constants.APP_NAME_PROVISION, "",
				Constants.OPER_GET_OAUTH_TOKEN, null);

		HttpEntity<ApiRequest<Object>> entityToken = new HttpEntity<ApiRequest<Object>>(request, headersMap);

		try {
			ParameterizedTypeReference<ApiResponse<OAuthToken>> parameterizedTypeReference = new ParameterizedTypeReference<ApiResponse<OAuthToken>>() {
			};
			ResponseEntity<ApiResponse<OAuthToken>> responseEntity = restTemplate.exchange(urlToken, HttpMethod.POST,
					entityToken, parameterizedTypeReference);

			if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
				if (toInsert) {
					// insertToken(responseEntity.getBody().getBody());
					oAuthTokenRepository.insertToken(responseEntity.getBody().getBody());
				} else {
					updated = oAuthTokenRepository.updateToken(responseEntity.getBody());

					// updated = updateTokenInCollection(responseEntity.getBody());
				}
			} else {
				return "";
			}

			//log.info("responseEntity: " + responseEntity.getBody());

			return updated ? ((OAuthToken) responseEntity.getBody().getBody()).getAccessToken() : "";
		} catch (Exception e) {
			log.error("Exception = " + e.getMessage());
			return "";
		}
	}

	private String getAuthToken() {
		String psiTokenGenerated = "";
		Optional<OAuthToken> optionalAuthToken = oAuthTokenRepository.getOAuthToke();

		if (optionalAuthToken.isPresent()) {
			OAuthToken oAuthToken = optionalAuthToken.get();
			Date now = new Date();
			long timeDiff = now.getTime() - (Long.parseLong(oAuthToken.getConsentedOn()) * 1000);

			if (timeDiff >= ((Integer.parseInt(oAuthToken.getExpiresIn()) - 5) * 1000)) {
				psiTokenGenerated = getTokenFromPSI(false);
			} else {
				psiTokenGenerated = oAuthToken.getAccessToken();
			}

		} else {
			psiTokenGenerated = getTokenFromPSI(true);
		}

		return psiTokenGenerated;
	}

}