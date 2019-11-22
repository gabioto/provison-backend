package pe.telefonica.provision.external;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.util.constants.Constants;

@Component
public class TrazabilidadSecurityApi {
	private static final Log log = LogFactory.getLog(TrazabilidadSecurityApi.class);

	@Autowired
	private IBMSecuritySeguridad ibmSecuritySeguridad;

	@Autowired
	private ExternalApi api;

	public void saveLogData(String documentNumber, String documentType, String orderCode, String bucket, String typeLog,
			String request, String response, String action) {

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
		/*
		 * security.ibm.seguridad.clientId=ddcc9f10-166e-4643-bc40-1759901b54fe
		 * security.ibm.seguridad.clientSecret=e2eafa5e-2f92-497c-8d7c-fc4ae534898c
		 * security.ibm.seguridad.auth=Basic
		 * dHJhY2VhYmlsaXR5VXNlcjptMFYxc3RAUlMzY1VSaXQm
		 */

		String saveLogDataUrl = api.getSecurityUrl() + api.getSecuritySaveLogData();

		System.out.println(saveLogDataUrl);

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", ibmSecuritySeguridad.getAuth());
		headersMap.add("X-IBM-Client-Id", ibmSecuritySeguridad.getClientId());
		headersMap.add("X-IBM-Client-Secret", ibmSecuritySeguridad.getClientSecret());

		/*
		 * headersMap.add("Authorization",
		 * "Basic dHJhY2VhYmlsaXR5VXNlcjptMFYxc3RAUlMzY1VSaXQm");
		 * headersMap.add("X-IBM-Client-Id", "ddcc9f10-166e-4643-bc40-1759901b54fe");
		 * headersMap.add("X-IBM-Client-Secret",
		 * "e2eafa5e-2f92-497c-8d7c-fc4ae534898c");
		 */

		HttpEntity<LogDataRequest> entity = new HttpEntity<LogDataRequest>(logDataRequest, headersMap);

		try {

			ResponseEntity<String> responseEntity = restTemplate.postForEntity(saveLogDataUrl, entity, String.class);

		} catch (Exception ex) {

			System.out.println(ex.getMessage());
		}

	}

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

	public ApiResponse<SMSByIdResponse> sendSMS(Customer customer, String msgKey, MsgParameter[] msgParameters,
			String webURL) {
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

		System.out.println(webURL);

		List<Contact> contacts = new ArrayList<>();

		Contact contactCustomer = new Contact();
		contactCustomer.setPhoneNumber(customer.getPhoneNumber().toString()); // TODO: Cambiar integer a string
		contactCustomer.setIsMovistar(Boolean.valueOf(customer.getCarrier()));

		/*if (customer.getContactPhoneNumber() != null) {
			Contact contactContact = new Contact();
			contactContact.setPhoneNumber(customer.getContactPhoneNumber().toString()); 
			contactContact.setIsMovistar(Boolean.valueOf(customer.getContactCarrier()));
			contacts.add(contactContact);
		}*/

		contacts.add(contactCustomer);

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

}
