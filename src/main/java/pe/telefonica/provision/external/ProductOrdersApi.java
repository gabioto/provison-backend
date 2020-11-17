package pe.telefonica.provision.external;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pe.telefonica.provision.conf.ExternalApi;
import pe.telefonica.provision.conf.IBMSecuritySeguridad;
import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.common.ApiResponse;
import pe.telefonica.provision.model.OAuthToken;
import pe.telefonica.provision.repository.OAuthTokenRepository;
import pe.telefonica.provision.service.request.PSIUpdateClientRequest;
import pe.telefonica.provision.service.response.PSIUpdateClientResponse;
import pe.telefonica.provision.util.DateUtil;
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

	public Boolean getProductOrders(String publicId, String orderCode, String customerCode) {
		
		String oAuthToken;
		LocalDateTime startHour = Constants.LOCAL_DATE_ZONE;
		LocalDateTime endHour;

		// Implementacion SSL
		RestTemplate restTemplate = new RestTemplate(initClientRestTemplate);
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		String requestUrl = api.getPsiUrl() + api.getPsiUpdateClient();
		log.info("updatePSIClient - URL: " + requestUrl);

		PSIUpdateClientRequest request = new PSIUpdateClientRequest();
		request.getHeaderIn().setCountry("PE");
		request.getHeaderIn().setLang("es");
		request.getHeaderIn().setEntity("TDP");

		request.getHeaderIn().setSystem("COLTRA");
		request.getHeaderIn().setSubsystem("COLTRA");
		request.getHeaderIn().setOriginator("PE:TDP:COLTRA:COLTRA");
		request.getHeaderIn().setSender("OracleServiceBus");
		request.getHeaderIn().setUserId("USERCOLTRA");
		request.getHeaderIn().setWsId("SistemColtra");
		request.getHeaderIn().setWsIp("10.10.10.10");
		request.getHeaderIn().setOperation("updateClient");
		request.getHeaderIn().setDestination("PE:TDP:COLTRA:COLTRA");
		request.getHeaderIn().setExecId("550e8400-e29b-41d4-a716-446655440000");
		request.getHeaderIn().setTimestamp(DateUtil.getNowPsi(Constants.TIMESTAMP_FORMAT_PSI));
		request.getHeaderIn().setMsgType("REQUEST");

		System.out.println(generateAuthString());

		request.getBodyUpdateClient().getUser().setNow(DateUtil.getNowPsi(Constants.TIMESTAMP_FORMAT_USER));
		request.getBodyUpdateClient().getUser().setLogin("appmovistar");
		request.getBodyUpdateClient().getUser().setCompany("telefonica-pe");
		request.getBodyUpdateClient().getUser().setAuth_string(generateAuthString());

		log.info("updatePSIClient - request: " + request.toString());

		oAuthToken = getAuthToken(request.getBodyUpdateClient().getNombre_completo());

		if (oAuthToken.isEmpty()) {
			return false;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + oAuthToken);
		headers.set("X-IBM-Client-Id", api.getApiClient());

		log.info("updatePSIClient - headers: " + headers.toString());

		HttpEntity<PSIUpdateClientRequest> entity = new HttpEntity<PSIUpdateClientRequest>(request, headers);

		System.out.println(entity);
		try {

			ResponseEntity<PSIUpdateClientResponse> responseEntity = restTemplate.postForEntity(requestUrl, entity,
					PSIUpdateClientResponse.class);

			/*
			 * ResponseEntity<PSIUpdateClientResponse> responseEntity =
			 * getRestTemplate().postForEntity(requestUrl, entity,
			 * PSIUpdateClientResponse.class);
			 */
			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("PSI", "updatePSIClient", new Gson().toJson(entity.getBody()),
					new Gson().toJson(responseEntity.getBody()).toString(), requestUrl, startHour, endHour,
					responseEntity.getStatusCodeValue());

			log.info("updatePSIClient - responseEntity.Body: " + responseEntity.getBody().toString());

			return responseEntity.getStatusCode().equals(HttpStatus.OK);
		} catch (HttpClientErrorException ex) {

			log.info("HttpClientErrorException = " + ex.getMessage());
			log.info("getResponseBodyAsString = " + ex.getResponseBodyAsString());

			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("PSI", "updatePSIClient", new Gson().toJson(entity.getBody()),
					ex.getResponseBodyAsString(), requestUrl, startHour, endHour, ex.getStatusCode().value());

			JsonObject jsonDecode = new Gson().fromJson(ex.getResponseBodyAsString(), JsonObject.class);
			System.out.println(jsonDecode);

			JsonObject appDetail = jsonDecode.getAsJsonObject("BodyOut").getAsJsonObject("ClientException")
					.getAsJsonObject("appDetail");
			String message = appDetail.get("exceptionAppMessage").toString();
			String codeError = appDetail.get("exceptionAppCode").toString();

			throw new FunctionalErrorException(message, ex, codeError);

		} catch (Exception ex) {
			log.info("Exception = " + ex.getMessage());
			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("PSI", "updatePSIClient", new Gson().toJson(entity.getBody()), ex.getMessage(),
					requestUrl, startHour, endHour, HttpStatus.INTERNAL_SERVER_ERROR.value());
			throw new ServerNotFoundException(ex.getMessage());
		}
	}

	private String getTokenFromPSI(String customerName, boolean toInsert) {
		RestTemplate restTemplate = new RestTemplate();
		boolean updated = true;
		String urlToken = api.getSecurityUrl() + api.getOauthToken();

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", security.getAuth());
		headersMap.add("X-IBM-Client-Id", security.getClientId());
		headersMap.add("X-IBM-Client-Secret", security.getClientSecret());

		ApiRequest<Object> request = new ApiRequest<Object>(Constants.APP_NAME_PROVISION, customerName,
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

			log.info("responseEntity: " + responseEntity.getBody());

			return updated ? ((OAuthToken) responseEntity.getBody().getBody()).getAccessToken() : "";
		} catch (Exception e) {
			log.info("Exception = " + e.getMessage());
			return "";
		}
	}

	// Util
	private String generateAuthString() {
		String passMD5 = stringToMD5("aPpM0v1S7@R");
		String authString = stringToMD5(DateUtil.getNowPsi(Constants.DATE_FORMAT_PSI_AUTH) + passMD5);
		return authString;
	}

	private String getAuthToken(String customerName) {
		String psiTokenGenerated = "";
		Optional<OAuthToken> optionalAuthToken = oAuthTokenRepository.getOAuthToke();

		if (optionalAuthToken.isPresent()) {
			OAuthToken oAuthToken = optionalAuthToken.get();
			Date now = new Date();
			long timeDiff = now.getTime() - (Long.parseLong(oAuthToken.getConsentedOn()) * 1000);

			if (timeDiff >= ((Integer.parseInt(oAuthToken.getExpiresIn()) - 5) * 1000)) {
				psiTokenGenerated = getTokenFromPSI(customerName, false);
			} else {
				psiTokenGenerated = oAuthToken.getAccessToken();
			}

		} else {
			psiTokenGenerated = getTokenFromPSI(customerName, true);
		}

		return psiTokenGenerated;
	}

	private String stringToMD5(String string) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(string.getBytes(Charset.forName("UTF8")));
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
			log.error(e.getMessage());
		}
		return null;
	}

}