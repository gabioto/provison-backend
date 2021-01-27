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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import pe.telefonica.provision.conf.ExternalApi;
import pe.telefonica.provision.conf.IBMSecuritySeguridad;
import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.common.ApiResponse;
import pe.telefonica.provision.external.request.BucketRequest;
import pe.telefonica.provision.external.response.ResponseBucket;
import pe.telefonica.provision.model.OAuthToken;
import pe.telefonica.provision.repository.OAuthTokenRepository;
import pe.telefonica.provision.service.request.PSIUpdateClientRequest;
import pe.telefonica.provision.service.response.PSIUpdateClientResponse;
import pe.telefonica.provision.util.DateUtil;
import pe.telefonica.provision.util.constants.Constants;
import pe.telefonica.provision.util.exception.FunctionalErrorException;
import pe.telefonica.provision.util.exception.ServerNotFoundException;

@Component
public class PSIApi extends ConfigRestTemplate {
	private static final Log log = LogFactory.getLog(PSIApi.class);

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

	public Boolean updatePSIClient(PSIUpdateClientRequest requestx) {
		String oAuthToken;
		LocalDateTime startHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
		LocalDateTime endHour;
		// Implementacion SSL
		RestTemplate restTemplate = new RestTemplate(initClientRestTemplate);
		// bypaseo
		// RestTemplate restTemplate = new
		// RestTemplate(SSLClientFactory.getClientHttpRequestFactory(HttpClientType.OkHttpClient));
		/* RestTemplate restTemplate = new RestTemplate(); */
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		String requestUrl = api.getPsiUrl() + api.getPsiUpdateClient();
		
		PSIUpdateClientRequest request = requestx;
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

		request.getBodyUpdateClient().getUser().setNow(DateUtil.getNowPsi(Constants.TIMESTAMP_FORMAT_USER));
		request.getBodyUpdateClient().getUser().setLogin("appmovistar");
		request.getBodyUpdateClient().getUser().setCompany("telefonica-pe");
		request.getBodyUpdateClient().getUser().setAuth_string(generateAuthString());

		oAuthToken = getAuthToken(request.getBodyUpdateClient().getNombre_completo());

		if (oAuthToken.isEmpty()) {
			return false;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + oAuthToken);
		headers.set("X-IBM-Client-Id", api.getApiClient());

		HttpEntity<PSIUpdateClientRequest> entity = new HttpEntity<PSIUpdateClientRequest>(request, headers);

		try {

			ResponseEntity<PSIUpdateClientResponse> responseEntity = restTemplate.postForEntity(requestUrl, entity,
					PSIUpdateClientResponse.class);

			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("PSI", "updatePSIClient", new Gson().toJson(entity.getBody()),
					new Gson().toJson(responseEntity.getBody()).toString(), requestUrl, startHour, endHour,
					responseEntity.getStatusCodeValue());

			return responseEntity.getStatusCode().equals(HttpStatus.OK);
		} catch (HttpClientErrorException ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("PSI", "updatePSIClient", new Gson().toJson(entity.getBody()),
					ex.getLocalizedMessage(), requestUrl, startHour, endHour, ex.getStatusCode().value());

			JsonObject jsonDecode = new Gson().fromJson(ex.getResponseBodyAsString(), JsonObject.class);
			
			JsonObject appDetail = jsonDecode.getAsJsonObject("BodyOut").getAsJsonObject("ClientException")
					.getAsJsonObject("appDetail");
			String message = appDetail.get("exceptionAppMessage").toString();
			String codeError = appDetail.get("exceptionAppCode").toString();

			throw new FunctionalErrorException(message, ex, codeError);

		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());
			String error = ex.getLocalizedMessage().substring(0, ex.getLocalizedMessage().indexOf(" "));
			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("PSI", "updatePSIClient", new Gson().toJson(entity.getBody()), ex.getLocalizedMessage(),
					requestUrl, startHour, endHour, Integer.parseInt(error));
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
					oAuthTokenRepository.insertToken(responseEntity.getBody().getBody());
				} else {
					updated = oAuthTokenRepository.updateToken(responseEntity.getBody());
				}
			} else {
				return "";
			}

			return updated ? ((OAuthToken) responseEntity.getBody().getBody()).getAccessToken() : "";
		} catch (Exception e) {
			log.error(this.getClass().getName() + " - Exception: " + e.getMessage());
			
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
			log.error(this.getClass().getName() + " - Exception: " + e.getMessage());
		}
		return null;
	}
	
	public boolean getCarrier(String phoneNumber) {

		RestTemplate restTemplate = new RestTemplate(initClientRestTemplate);
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		String requestUrl = api.getCustomerUrlOnPremise() + api.getSearchCustomerOnPremise();

		String input = "";
		String oAuthToken;
		LocalDateTime startHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
		LocalDateTime endHour;
		
		JsonObject jsonBody = new JsonObject();
		JsonObject jsonTefHeaderReq = new JsonObject();
		JsonObject jsonSearchCustomer = new JsonObject();
		JsonObject jsonCustomerId = new JsonObject();
		JsonObject jsonTelephoneNumber = new JsonObject();
		jsonTefHeaderReq.addProperty("userLogin", "10223928");
		jsonTefHeaderReq.addProperty("serviceChannel", "MS");
		jsonTefHeaderReq.addProperty("sessionCode", "83e478c1-84a4-496d-8497-582657011f80");
		jsonTefHeaderReq.addProperty("application", "COLTRA");
		jsonTefHeaderReq.addProperty("idMessage", "57f33f81-57f3-57f3-57f3-57f33f811e0b");
		jsonTefHeaderReq.addProperty("ipAddress", "169.54.245.69");
		jsonTefHeaderReq.addProperty("functionalityCode", "CustomerService");
		jsonTefHeaderReq.addProperty("transactionTimestamp",
				DateUtil.getNowPsi(Constants.TIMESTAMP_FORMAT_CMS_ATIS_NO_ZONE));
		jsonTefHeaderReq.addProperty("serviceName", "searchCustomer");
		jsonTefHeaderReq.addProperty("version", "1.0");

		jsonTelephoneNumber.addProperty("number", phoneNumber);
		jsonCustomerId.add("telephoneNumber", jsonTelephoneNumber);
		jsonSearchCustomer.add("customerIdentification", jsonCustomerId);

		jsonBody.add("TefHeaderReq", jsonTefHeaderReq);
		jsonBody.add("SearchCustomerRequestData", jsonSearchCustomer);

		Gson gson = new Gson();
		input = gson.toJson(jsonBody);
		oAuthToken = getAuthToken("PARAM_KEY_OAUTH_TOKEN");

		if (oAuthToken.isEmpty()) {
			return false;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-IBM-Client-Id", api.getCustomerSearchClientOnPremise());
		headers.set("Authorization", "Bearer " + oAuthToken);

		HttpEntity<String> entity = new HttpEntity<>(input, headers);
		
		try {
			ResponseEntity<String> output = restTemplate.postForEntity(requestUrl, entity, String.class);

			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("SEARCH_CUSTOMER_ONPREMISE", "getCarrierOnPremise", entity.getBody(),
					output.getBody(), requestUrl, startHour, endHour, output.getStatusCodeValue());

			JsonElement jsonElement = gson.fromJson(output.getBody(), JsonElement.class);
			JsonObject jsonOutput = jsonElement.getAsJsonObject();

			JsonObject joOutputSearchData = jsonOutput.getAsJsonObject("SearchCustomerResponseData");
			JsonObject joOutputSearchList = joOutputSearchData.getAsJsonObject("searchCustomerResultsList");
			JsonArray jaCustomerResults = joOutputSearchList.getAsJsonArray("searchCustomerResults");

			return jaCustomerResults.size() > 0;
		} catch (Exception e) {
			// Se detecta error, por lo tanto se considera otro operador.
			log.error(this.getClass().getName() + " - Exception: " + e.getMessage());			
			String message = e.getLocalizedMessage().substring(0, e.getLocalizedMessage().indexOf(" "));
			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("SEARCH_CUSTOMER_ONPREMISE", "getCarrierOnPremise", entity.getBody(), e.getLocalizedMessage(), requestUrl, startHour,
					endHour, Integer.parseInt(message));
			return false;
		}
	}

	public boolean getCarrierOld(String phoneNumber) {

		RestTemplate restTemplate = new RestTemplate(initClientRestTemplate);
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		String requestUrl = api.getCustomerUrl() + api.getSearchCustomer();
		log.info("getCarrier - URL: " + requestUrl);

		String input = "";
		LocalDateTime startHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
		LocalDateTime endHour;

		JsonObject jsonBody = new JsonObject();
		JsonObject jsonTefHeaderReq = new JsonObject();
		JsonObject jsonSearchCustomer = new JsonObject();
		JsonObject jsonCustomerId = new JsonObject();
		JsonObject jsonTelephoneNumber = new JsonObject();
		jsonTefHeaderReq.addProperty("userLogin", "10223928");
		jsonTefHeaderReq.addProperty("serviceChannel", "MS");
		jsonTefHeaderReq.addProperty("sessionCode", "83e478c1-84a4-496d-8497-582657011f80");
		jsonTefHeaderReq.addProperty("application", "COLTRA");
		jsonTefHeaderReq.addProperty("idMessage", "57f33f81-57f3-57f3-57f3-57f33f811e0b");
		jsonTefHeaderReq.addProperty("ipAddress", "169.54.245.69");
		jsonTefHeaderReq.addProperty("functionalityCode", "CustomerService");
		jsonTefHeaderReq.addProperty("transactionTimestamp", DateUtil.getNowPsi(Constants.TIMESTAMP_FORMAT_CMS_ATIS_NO_ZONE));
		jsonTefHeaderReq.addProperty("serviceName", "searchCustomer");
		jsonTefHeaderReq.addProperty("version", "1.0");

		jsonTelephoneNumber.addProperty("number", phoneNumber);
		jsonCustomerId.add("telephoneNumber", jsonTelephoneNumber);
		jsonSearchCustomer.add("customerIdentification", jsonCustomerId);

		jsonBody.add("TefHeaderReq", jsonTefHeaderReq);
		jsonBody.add("SearchCustomerRequestData", jsonSearchCustomer);

		Gson gson = new Gson();
		input = gson.toJson(jsonBody);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-IBM-Client-Id", api.getCustomerSearchClient());
		headers.set("X-IBM-Client-Secret", api.getCustomerSearchSecret());
			
		HttpEntity<String> entity = new HttpEntity<>(input, headers);

		try {
			ResponseEntity<String> output = restTemplate.postForEntity(requestUrl, entity, String.class);

			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("SEARCH_CUSTOMER", "getCarrier", entity.getBody(),
					output.getBody(), requestUrl, startHour, endHour, output.getStatusCodeValue());

			JsonElement jsonElement = gson.fromJson(output.getBody(), JsonElement.class);
			JsonObject jsonOutput = jsonElement.getAsJsonObject();

			JsonObject joOutputSearchData = jsonOutput.getAsJsonObject("SearchCustomerResponseData");
			JsonObject joOutputSearchList = joOutputSearchData.getAsJsonObject("searchCustomerResultsList");
			JsonArray jaCustomerResults = joOutputSearchList.getAsJsonArray("searchCustomerResults");

			return jaCustomerResults.size() > 0;
		} catch (Exception e) {
			// Se detecta error, por lo tanto se considera otro operador.
			log.error(this.getClass().getName() + " - Exception: " + e.getMessage());
			String message = e.getLocalizedMessage().substring(0, e.getLocalizedMessage().indexOf(" "));
			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("SEARCH_CUSTOMER", "getCarrier", entity.getBody(), e.getLocalizedMessage(),
					requestUrl, startHour, endHour, Integer.parseInt(message));
			return false;
		}
	}

	public boolean getBucketByProduct(String bucket, String product, String channel) {
		RestTemplate restTemplate = new RestTemplate();
		BucketRequest bucketRequest = new BucketRequest();

		bucketRequest.setBucket(bucket);
		bucketRequest.setChannel(channel);
		bucketRequest.setProduct(product);

		String bucketUrl = api.getSecurityUrl() + api.getBucketsByProduct();

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", security.getAuth());
		headersMap.add("X-IBM-Client-Id", security.getClientId());
		headersMap.add("X-IBM-Client-Secret", security.getClientSecret());

		HttpEntity<BucketRequest> entity = new HttpEntity<>(bucketRequest, headersMap);

		try {
			ResponseEntity<ResponseBucket> responseEntity = restTemplate.postForEntity(bucketUrl, entity,
					ResponseBucket.class);

			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				if (responseEntity.getBody() != null && responseEntity.getBody().getBody() != null) {
					return responseEntity.getBody().getBody().isContent();
				}
			}
			return false;
		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());
			throw ex;
		}
	}

}