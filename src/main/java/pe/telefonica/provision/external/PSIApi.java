package pe.telefonica.provision.external;

import java.nio.charset.Charset;
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
import org.springframework.http.client.ClientHttpRequestFactory;
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
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import pe.telefonica.provision.conf.ExternalApi;
import pe.telefonica.provision.conf.IBMSecuritySeguridad;
import pe.telefonica.provision.conf.SSLClientFactory;
import pe.telefonica.provision.conf.SSLClientFactory.HttpClientType;
import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.common.ApiResponse;
import pe.telefonica.provision.util.exception.ServerNotFoundException;
import pe.telefonica.provision.util.exception.FunctionalErrorException;
import pe.telefonica.provision.model.OAuthToken;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.repository.OAuthTokenRepository;
import pe.telefonica.provision.repository.impl.ProvisionRepositoryImpl;
import pe.telefonica.provision.service.request.PSIUpdateClientRequest;
import pe.telefonica.provision.service.response.PSIUpdateClientResponse;
import pe.telefonica.provision.util.DateUtil;
import pe.telefonica.provision.util.constants.Constants;

@Component
public class PSIApi extends ConfigRestTemplate {
	private static final Log log = LogFactory.getLog(PSIApi.class);

	@Autowired
	private OAuthTokenRepository oAuthTokenRepository;

	@Autowired
	private ExternalApi api;

	@Autowired
	private IBMSecuritySeguridad security;

	public Boolean updatePSIClient(PSIUpdateClientRequest requestx) {
		String oAuthToken;

		RestTemplate restTemplate = new RestTemplate(
				SSLClientFactory.getClientHttpRequestFactory(HttpClientType.OkHttpClient));
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		/* RestTemplate restTemplate = new RestTemplate(); */
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		// RestTemplate test = new RestTemplate(this.getClientHttpRequestFactory());

		String requestUrl = api.getPsiUrl() + api.getPsiUpdateClient();
		log.info("updatePSIClient - URL: " + requestUrl);

		PSIUpdateClientRequest request = requestx;
		request.getHeaderIn().setCountry("PE");
		request.getHeaderIn().setLang("es");
		request.getHeaderIn().setEntity("TDP");

		request.getHeaderIn().setSystem("SIVADAC");
		request.getHeaderIn().setSubsystem("SIVADAC");
		request.getHeaderIn().setOriginator("PE:TDP:SIVADAC:SIVADAC");
		request.getHeaderIn().setSender("OracleServiceBus");
		request.getHeaderIn().setUserId("USERSIVADAC");
		request.getHeaderIn().setWsId("SistemSivadac");
		request.getHeaderIn().setWsIp("10.10.10.10");
		request.getHeaderIn().setOperation("updateClient");
		request.getHeaderIn().setDestination("PE:SIVADAC:SIVADAC:SIVADAC");
		request.getHeaderIn().setExecId("550e8400-e29b-41d4-a716-446655440000");
		request.getHeaderIn().setTimestamp(DateUtil.getNowPsi(Constants.TIMESTAMP_FORMAT_PSI));
		request.getHeaderIn().setMsgType("REQUEST");
		
		
		/*
		 * request.getHeaderIn().setSystem("COL");
		 * request.getHeaderIn().setSubsystem("TRA");
		 * request.getHeaderIn().setOriginator("PE:TDP:COL:TRA");
		 * request.getHeaderIn().setSender("OracleServiceBus");
		 * request.getHeaderIn().setUserId("USERTRA");
		 * request.getHeaderIn().setWsId("SistemTRA");
		 * request.getHeaderIn().setWsIp("192.168.100.1");
		 * request.getHeaderIn().setOperation("updateClient");
		 * request.getHeaderIn().setDestination("PE:TDP:COL:TRA");
		 * request.getHeaderIn().setExecId("550e8400-e29b-41d4-a716-446655440000");
		 * request.getHeaderIn().setTimestamp(DateUtil.getNowPsi(Constants.
		 * TIMESTAMP_FORMAT_PSI)); request.getHeaderIn().setMsgType("REQUEST");
		 */
		System.out.println(generateAuthString());

		request.getBodyUpdateClient().getUser().setNow(DateUtil.getNowPsi(Constants.TIMESTAMP_FORMAT_USER));
		request.getBodyUpdateClient().getUser().setLogin("appmovistar");
		request.getBodyUpdateClient().getUser().setCompany("telefonica-pe");
		request.getBodyUpdateClient().getUser().setAuth_string(generateAuthString());
		
		
		/*request.getBodyUpdateClient().setSolicitud(provision.getXaIdSt());
		request.getBodyUpdateClient().setCorreo(provision.getCustomer().getMail());
		
		request.getBodyUpdateClient().setNombre_completo(provision.getCustomer().getContactName());
		request.getBodyUpdateClient().setNombre_completo2(provision.getCustomer().getContactName1());
		request.getBodyUpdateClient().setNombre_completo3(provision.getCustomer().getContactName2());
		request.getBodyUpdateClient().setNombre_completo4(provision.getCustomer().getContactName3());
		
		request.getBodyUpdateClient().setTelefono1(String.valueOf(provision.getCustomer().getContactPhoneNumber()));
		request.getBodyUpdateClient().setTelefono2(provision.getCustomer().getContactPhoneNumber1() != null ? String.valueOf(provision.getCustomer().getContactPhoneNumber1()): "");
		request.getBodyUpdateClient().setTelefono3(provision.getCustomer().getContactPhoneNumber2() != null ? String.valueOf(provision.getCustomer().getContactPhoneNumber2()): "");
		request.getBodyUpdateClient().setTelefono4(provision.getCustomer().getContactPhoneNumber2() != null ? String.valueOf(provision.getCustomer().getContactPhoneNumber3()): "");
		*/
		log.info("updatePSIClient - request: " + request.toString());
		
		oAuthToken = getAuthToken(request.getBodyUpdateClient().getNombre_completo());
		//log.info("updatePSIClient - oAuthToken: " + oAuthToken);

		if (oAuthToken.isEmpty()) {
			return false;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + oAuthToken);
		headers.set("X-IBM-Client-Id", api.getOauth2Client());

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

			log.info("updatePSIClient - responseEntity.Body: " + responseEntity.getBody().toString());

			return responseEntity.getStatusCode().equals(HttpStatus.OK);
		} catch (HttpClientErrorException ex) {

			log.info("HttpClientErrorException = " + ex.getMessage());
			log.info("getResponseBodyAsString = " + ex.getResponseBodyAsString());

			// JsonObject jobj = new Gson().fromJson(jsonString, JsonObject.class);
			JsonObject jsonDecode = new Gson().fromJson(ex.getResponseBodyAsString(), JsonObject.class);
			System.out.println(jsonDecode);

			JsonObject appDetail = jsonDecode.getAsJsonObject("BodyOut").getAsJsonObject("ClientException")
					.getAsJsonObject("appDetail");
			String message = appDetail.get("exceptionAppMessage").toString();
			String codeError = appDetail.get("exceptionAppCode").toString();

			throw new FunctionalErrorException(message, ex, codeError);
			// throw new ServerNotFoundException(ex.getResponseBodyAsString());
			// return false;
		} catch (Exception ex) {
			log.info("Exception = " + ex.getMessage());
			throw new ServerNotFoundException(ex.getMessage());
			// return false;
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
	
	public boolean getCarrier(String phoneNumber) {
		try {
			Client client = Client.create();
			WebResource webResource = client.resource("https://api.us-east.apiconnect.ibmcloud.com/telefonica-del-peru-development/ter/customerinformation/v2/searchCustomer");

			JsonObject jsonBody = new JsonObject();
			JsonObject jsonTefHeaderReq = new JsonObject();
			JsonObject jsonSearchCustomer = new JsonObject();
			JsonObject jsonCustomerId = new JsonObject();
			JsonObject jsonTelephoneNumber = new JsonObject();
			jsonTefHeaderReq.addProperty("userLogin", "10223928");
			jsonTefHeaderReq.addProperty("serviceChannel", "MS");
			jsonTefHeaderReq.addProperty("sessionCode", "83e478c1-84a4-496d-8497-582657011f80");
			jsonTefHeaderReq.addProperty("application", "APPVENTAS");
			jsonTefHeaderReq.addProperty("idMessage", "57f33f81-57f3-57f3-57f3-57f33f811e0b");
			jsonTefHeaderReq.addProperty("ipAddress", "169.54.245.69");
			jsonTefHeaderReq.addProperty("functionalityCode", "CustomerService");
			jsonTefHeaderReq.addProperty("transactionTimestamp", "2019-05-28T15:08:31.960");
			jsonTefHeaderReq.addProperty("serviceName", "searchCustomer");
			jsonTefHeaderReq.addProperty("version", "1.0");

			jsonTelephoneNumber.addProperty("number", phoneNumber);
			jsonCustomerId.add("telephoneNumber", jsonTelephoneNumber);
			jsonSearchCustomer.add("customerIdentification", jsonCustomerId);

			jsonBody.add("TefHeaderReq", jsonTefHeaderReq);
			jsonBody.add("SearchCustomerRequestData", jsonSearchCustomer);

			Gson gson = new Gson();
			String input = gson.toJson(jsonBody);

			// GENESIS
			ClientResponse clientResponse = webResource.accept(new String[] { "application/json" })
					.header("Content-type", "application/json")
					.header("X-IBM-Client-Id", "42eac6bc-c8a8-4faf-b96b-6650a929a28d")
					.header("X-IBM-Client-Secret", "kY1vH1tQ8iX2uO7nX7sP5tD5mR0cO5cM6qD8cK3bW5vK3eG8gE")
					.post(ClientResponse.class, input);
			String output = (String) clientResponse.getEntity(String.class);

			JsonElement jsonElement = gson.fromJson(output, JsonElement.class);
			JsonObject jsonOutput = jsonElement.getAsJsonObject();

			JsonObject joOutputSearchData = jsonOutput.getAsJsonObject("SearchCustomerResponseData");
			JsonObject joOutputSearchList = joOutputSearchData.getAsJsonObject("searchCustomerResultsList");
			JsonArray jaCustomerResults = joOutputSearchList.getAsJsonArray("searchCustomerResults");
			
			
			return jaCustomerResults.size() > 0;
		} catch (Exception e) {
			// Se detecta error, por lo tanto se considera otro operador.
			System.out.println("Se detecta error, por lo tanto se considera otro operador, error: " + e.getMessage());
			return false;
		}
	}
	
}
