package pe.telefonica.provision.external;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

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
import pe.telefonica.provision.external.request.PSIWorkRequest;
import pe.telefonica.provision.external.response.PSIWorkResponse;
import pe.telefonica.provision.model.OAuthToken;
import pe.telefonica.provision.repository.OAuthTokenRepository;
import pe.telefonica.provision.service.request.PSIUpdateClientRequest;
import pe.telefonica.provision.util.constants.Constants;
import pe.telefonica.provision.util.exception.FunctionalErrorException;
import pe.telefonica.provision.util.exception.ServerNotFoundException;

@Component
public class ScheduleApi {

	private static final Log log = LogFactory.getLog(PSIApi.class);

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

	/**
	 * 
	 * @param reference
	 * @return
	 * @throws Exception
	 */
	public String modifyWork(PSIUpdateClientRequest requestx) throws Exception {

		RestTemplate restTemplate = new RestTemplate(initClientRestTemplate);
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		String url = "https://apisd.telefonica.com.pe/vp-tecnologia/bss/workOrderManagement/v1/workOrders"; // api.getPsiUrl()
																											// +
																											// api.getPsiGetCapacityFicticious();
		LocalDateTime startHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
		LocalDateTime endHour;

		String oAuthToken = getAuthToken(requestx.getBodyUpdateClient().getNombre_completo());

		log.info("modifyWork - URL: " + url);

		Calendar calendar = Calendar.getInstance();
		Date now = calendar.getTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.TIMESTAMP_FORMAT_USER);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-5:00"));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("UNICA-ServiceId", "");
		headers.set("UNICA-Application", "");
		headers.set("UNICA-PID", "");
		headers.set("UNICA-Application", "appmovistar");
		headers.set("UNICA-User", "");
		headers.set("Destination", "Agendador");
		headers.set("auth_string", generateAuthString());
		headers.set("Authorization", "Bearer " + oAuthToken);
		headers.set("X-IBM-Client-Id", "88d1769b-521f-49d9-bfc9-35f15c336698");// api.getApiClient());

		PSIWorkRequest request = new PSIWorkRequest(requestx, dateFormat.format(now));

		HttpEntity<PSIWorkRequest> entity = new HttpEntity<>(request, headers);

		try {

			log.info("modifyWork - requestUrl: " + new Gson().toJson(request));

			ResponseEntity<PSIWorkResponse> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, entity,
					PSIWorkResponse.class);

			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("PSI", "modifyWork", new Gson().toJson(entity.getBody()), new Gson().toJson(responseEntity.getBody()), url,
					startHour, endHour, responseEntity.getStatusCodeValue());

			log.info("modifyWork - Body: " + responseEntity.getBody().toString());

//			return responseEntity.getBody().getBodyOut().getCapacity();

			return null;

		} catch (HttpClientErrorException ex) {

			log.info("HttpClientErrorException = " + ex.getMessage());
			log.info("getResponseBodyAsString = " + ex.getResponseBodyAsString());

			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("PSI", "modifyWork", new Gson().toJson(entity.getBody()), ex.getLocalizedMessage(), url,
					startHour, endHour, ex.getStatusCode().value());

			JsonObject jsonDecode = new Gson().fromJson(ex.getResponseBodyAsString(), JsonObject.class);
			System.out.println(jsonDecode);

			JsonObject appDetail = jsonDecode.getAsJsonObject("BodyOut").getAsJsonObject("ClientException")
					.getAsJsonObject("appDetail");
			String message = appDetail.get("exceptionAppMessage").toString();
			String codeError = appDetail.get("exceptionAppCode").toString();

			throw new FunctionalErrorException(message, ex, codeError);

		} catch (Exception ex) {
			log.info("Exception = " + ex.getMessage());
			String message = ex.getLocalizedMessage().substring(0, ex.getLocalizedMessage().indexOf(" "));
			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("PSI", "modifyWork", new Gson().toJson(entity.getBody()), ex.getLocalizedMessage(), url, startHour,
					endHour, Integer.parseInt(message));
			throw new ServerNotFoundException(ex.getMessage());
		}
	}

	/**
	 * 
	 * @param customerName
	 * @return
	 */
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

	/**
	 * 
	 * @param customerName
	 * @param toInsert
	 * @return
	 */
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

	// PSI
	/**
	 * 
	 * @param string
	 * @return
	 */
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

	private String generateAuthString() {
		Calendar calendar = Calendar.getInstance();
		Date now = calendar.getTime();

		String passMD5 = stringToMD5("pS1D3v3L0p3R");

		SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_PSI_AUTH);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-5:00"));
		String authString = stringToMD5(dateFormat.format(now) + passMD5);

		return authString;
	}
}
