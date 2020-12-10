package pe.telefonica.provision.external;

import java.time.LocalDateTime;
import java.util.List;

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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import pe.telefonica.provision.conf.ExternalApi;
import pe.telefonica.provision.conf.IBMSecuritySeguridad;
import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.common.ApiResponse;
import pe.telefonica.provision.controller.request.MailRequest;
import pe.telefonica.provision.controller.request.MailRequest.MailParameter;
import pe.telefonica.provision.controller.request.SMSByIdRequest;
import pe.telefonica.provision.controller.request.SMSByIdRequest.Contact;
import pe.telefonica.provision.controller.request.SMSByIdRequest.Message;
import pe.telefonica.provision.controller.request.SMSByIdRequest.Message.MsgParameter;
import pe.telefonica.provision.controller.response.SMSByIdResponse;
import pe.telefonica.provision.external.request.LogDataRequest;
import pe.telefonica.provision.external.request.security.GetTokenExternalRequest;
import pe.telefonica.provision.util.constants.Constants;

@Component
public class TrazabilidadSecurityApi {
	private static final Log log = LogFactory.getLog(TrazabilidadSecurityApi.class);

	@Autowired
	private IBMSecuritySeguridad ibmSecuritySeguridad;

	@Autowired
	private ExternalApi api;

	@Async
	public void saveLogData(String documentNumber, String documentType, String orderCode, String bucket, String typeLog,
			String request, String response, String action, String messageId, String firstDate, String lastDate,
			String activityType, String channel) {

		RestTemplate restTemplate = new RestTemplate();

		LogDataRequest logDataRequest = new LogDataRequest();

		logDataRequest.setDocumentNumber(documentNumber);
		logDataRequest.setDocumentType(documentType);
		logDataRequest.setOrderCode(orderCode);
		logDataRequest.setBucket(bucket);
		logDataRequest.setLogType(typeLog);
		logDataRequest.setRequest(request);
		logDataRequest.setResponse(response);
		logDataRequest.setAction(action);
		logDataRequest.setMessageId(messageId);
		logDataRequest.setFirstDate(firstDate);
		logDataRequest.setLastDate(lastDate);
		logDataRequest.setActivityType(activityType);
		logDataRequest.setChannel(channel);

		String saveLogDataUrl = api.getSecurityUrl() + api.getSecuritySaveLogData();

		System.out.println(saveLogDataUrl);

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", ibmSecuritySeguridad.getAuth());
		headersMap.add("X-IBM-Client-Id", ibmSecuritySeguridad.getClientId());
		headersMap.add("X-IBM-Client-Secret", ibmSecuritySeguridad.getClientSecret());

		HttpEntity<LogDataRequest> entity = new HttpEntity<LogDataRequest>(logDataRequest, headersMap);

		try {

//			ResponseEntity<String> responseEntity = 
			restTemplate.postForEntity(saveLogDataUrl, entity, String.class);

		} catch (Exception ex) {

			System.out.println(ex.getMessage());
		}

	}
	
	@Async
	public void thirdLogEvent(String third, String operation, String request, String response, String serviceUrl,
			LocalDateTime startHour, LocalDateTime endHour) {
		log.info(this.getClass().getName() + " - " + "logEvent");

		String url = api.getSecurityUrl() + api.getSaveThirdLogData();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("X-IBM-Client-Id", ibmSecuritySeguridad.getClientId());
		headers.add("X-IBM-Client-Secret", ibmSecuritySeguridad.getClientSecret());
		headers.add("Authorization", ibmSecuritySeguridad.getAuth());

		LogDataRequest logRequest = new LogDataRequest();
		logRequest.setThird(third);
		logRequest.setOperation(operation);
		logRequest.setRequest(request);
		logRequest.setResponse(response);
		logRequest.setUrl(serviceUrl);
		logRequest.setStartHour(startHour);
		logRequest.setEndHour(endHour);

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		HttpEntity<LogDataRequest> entity = new HttpEntity<LogDataRequest>(logRequest, headers);

		try {
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, entity, String.class);

			if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
				log.info(this.getClass().getName() + " - " + "logEvent - OK");
			} else {
				log.info(this.getClass().getName() + " - " + "logEvent - ERR");
			}
		} catch (Exception e) {
			log.info(this.getClass().getName() + " - " + "logEvent - EXCEPTION : " + e.getMessage());
		}
	}

	@Async
	public Boolean sendMail(String templateId, MailParameter[] mailParameters) {
		RestTemplate restTemplate = new RestTemplate();

		String sendMailUrl = api.getSecurityUrl() + api.getSendMail();
		MailRequest mailRequest = new MailRequest();

		mailRequest.setMailTemplateId(templateId);
		mailRequest.setMailParameters(mailParameters);

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();

		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", ibmSecuritySeguridad.getAuth());
		headersMap.add("X-IBM-Client-Id", ibmSecuritySeguridad.getClientId());
		headersMap.add("X-IBM-Client-Secret", ibmSecuritySeguridad.getClientSecret());

		ApiRequest<MailRequest> apiRequest = new ApiRequest<MailRequest>(Constants.APP_NAME_PROVISION,
				Constants.USER_PROVISION, Constants.OPER_SEND_MAIL_BY_ID, mailRequest);

		HttpEntity<ApiRequest<MailRequest>> entity = new HttpEntity<ApiRequest<MailRequest>>(apiRequest, headersMap);

		try {
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(sendMailUrl, entity, String.class);
			if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {

			System.out.println(ex);
		}
		return false;

	}
	
	@Async
	public ApiResponse<SMSByIdResponse> sendSMS(List<Contact> contacts, String msgKey, MsgParameter[] msgParameters,
			String webURL, String webContactURL) {
		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("X-IBM-Client-Id", ibmSecuritySeguridad.getClientId());
		headersMap.add("X-IBM-Client-Secret", ibmSecuritySeguridad.getClientSecret());
		headersMap.add("Authorization", ibmSecuritySeguridad.getAuth());
		headersMap.add("Content-Type", "application/json");

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		String url = api.getSecurityUrl() + api.getSendSMSById();

		SMSByIdRequest smsByIdRequest = new SMSByIdRequest();

		Message message = new Message();
		message.setMsgKey(msgKey);
		message.setMsgParameters(msgParameters);
		message.setWebURL(webURL);
		message.setWebContactURL(webContactURL);
		

		System.out.println(webURL);

		// List<Contact> contacts = new ArrayList<>();

		/*
		 * Contact contactCustomer = new Contact();
		 * contactCustomer.setPhoneNumber(customer.getPhoneNumber().toString()); //
		 * TODO: Cambiar integer a string
		 * contactCustomer.setIsMovistar(Boolean.valueOf(customer.getCarrier()));
		 */

		/*
		 * if (customer.getContactPhoneNumber() != null) { Contact contactContact = new
		 * Contact();
		 * contactContact.setPhoneNumber(customer.getContactPhoneNumber().toString());
		 * contactContact.setIsMovistar(Boolean.valueOf(customer.getContactCarrier()));
		 * contacts.add(contactContact); }
		 */

		// contacts.add(contactCustomer);

		smsByIdRequest.setContacts(contacts.toArray(new Contact[0]));
		smsByIdRequest.setMessage(message);

		ApiRequest<SMSByIdRequest> apiRequest = new ApiRequest<SMSByIdRequest>(Constants.APP_NAME_PROVISION,
				Constants.USER_PROVISION, Constants.OPER_SEND_SMS_BY_ID, smsByIdRequest);
		HttpEntity<ApiRequest<SMSByIdRequest>> entity = new HttpEntity<ApiRequest<SMSByIdRequest>>(apiRequest,
				headersMap);

		ParameterizedTypeReference<ApiResponse<SMSByIdResponse>> parameterizedTypeReference = new ParameterizedTypeReference<ApiResponse<SMSByIdResponse>>() {
		};

		try {

			ResponseEntity<ApiResponse<SMSByIdResponse>> responseEntity = restTemplate.exchange(url, HttpMethod.POST,
					entity, parameterizedTypeReference);

			return responseEntity.getBody();

		} catch (Exception e) {
			return null;
		}

	}
	
	public String gerateToken() {

		log.info(this.getClass().getName() + " - " + "gerateToken");
		// RestTemplate restTemplate = new
		// RestTemplate(SSLClientFactory.getClientHttpRequestFactory(HttpClientType.OkHttpClient));
		// restTemplate.getMessageConverters().add(new
		// MappingJackson2HttpMessageConverter());
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		String requestUrl = api.getSecurityUrl() + api.getSecurityGetOAuthToken();
		
		GetTokenExternalRequest getTokenExternalRequest = new  GetTokenExternalRequest();

		getTokenExternalRequest.setTokenKey("");
		
		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();

		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", ibmSecuritySeguridad.getAuth());
		headersMap.add("X-IBM-Client-Id", ibmSecuritySeguridad.getClientId());
		headersMap.add("X-IBM-Client-Secret", ibmSecuritySeguridad.getClientSecret());

		ApiRequest<GetTokenExternalRequest> apiRequest = new ApiRequest<GetTokenExternalRequest>(Constants.APP_NAME_PROVISION,
				Constants.USER_SEGURIDAD, Constants.OPER_GET_TOKEN_EXTERNAL, getTokenExternalRequest);

		HttpEntity<ApiRequest<GetTokenExternalRequest>> entity = new HttpEntity<ApiRequest<GetTokenExternalRequest>>(apiRequest, headersMap);

		try {
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(requestUrl, entity, String.class);
			if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
				JsonObject jsonObject = new JsonParser().parse(responseEntity.getBody().toString()).getAsJsonObject();
				String token = jsonObject.get("body").getAsJsonObject().get("accessToken").toString().toString().replaceAll("\"",
						"");
				
				System.out.println(responseEntity);
				return token;
			} else {
				return null;
			}
		} catch (HttpClientErrorException e) {
			log.info("HttpClientErrorException = " + e.getMessage());
			log.info("HttpClientErrorException = " + e.getResponseBodyAsString());
			return null;
		
		} catch (Exception ex) {

			System.out.println(ex.getMessage());
			return null;
		}

	}
	
	public String gerateTokenAzure() {

		log.info(this.getClass().getName() + " - " + "gerateToken");
		// RestTemplate restTemplate = new
		// RestTemplate(SSLClientFactory.getClientHttpRequestFactory(HttpClientType.OkHttpClient));
		// restTemplate.getMessageConverters().add(new
		// MappingJackson2HttpMessageConverter());
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		String requestUrl = api.getSecurityUrl() + api.getSecurityGetOAuthTokenAzure();
		
		GetTokenExternalRequest getTokenExternalRequest = new  GetTokenExternalRequest();
		
		getTokenExternalRequest.setTokenKey("");
		
		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();

		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", ibmSecuritySeguridad.getAuth());
		headersMap.add("X-IBM-Client-Id", ibmSecuritySeguridad.getClientId());
		headersMap.add("X-IBM-Client-Secret", ibmSecuritySeguridad.getClientSecret());

		ApiRequest<GetTokenExternalRequest> apiRequest = new ApiRequest<GetTokenExternalRequest>(Constants.APP_NAME_PROVISION,
				Constants.USER_SEGURIDAD, Constants.OPER_GET_TOKEN_EXTERNAL, getTokenExternalRequest);

		HttpEntity<ApiRequest<GetTokenExternalRequest>> entity = new HttpEntity<ApiRequest<GetTokenExternalRequest>>(apiRequest, headersMap);

		try {
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(requestUrl, entity, String.class);
			if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
				JsonObject jsonObject = new JsonParser().parse(responseEntity.getBody().toString()).getAsJsonObject();
				String token = jsonObject.get("body").getAsJsonObject().get("accessToken").toString().toString().replaceAll("\"",
						"");
				
				System.out.println(responseEntity);
				return token;
			} else {
				return null;
			}
		} catch (HttpClientErrorException e) {
			log.info("HttpClientErrorException = " + e.getMessage());
			log.info("HttpClientErrorException = " + e.getResponseBodyAsString());
			return null;
		
		} catch (Exception ex) {

			System.out.println(ex.getMessage());
			return null;
		}

	}

}
