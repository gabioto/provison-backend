package pe.telefonica.provision.restclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import pe.telefonica.provision.conf.ExternalApi;
import pe.telefonica.provision.conf.IBMSecurity;
import pe.telefonica.provision.restclient.request.LogDataRequest;
import pe.telefonica.provision.service.request.SMSRequest;

@Service()
public class RestSecuritySaveLogData {
	@Autowired
	private IBMSecurity security;
	

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
		security.ibm.seguridad.clientId=ddcc9f10-166e-4643-bc40-1759901b54fe
		security.ibm.seguridad.clientSecret=e2eafa5e-2f92-497c-8d7c-fc4ae534898c
		security.ibm.seguridad.auth=Basic dHJhY2VhYmlsaXR5VXNlcjptMFYxc3RAUlMzY1VSaXQm*/
		
		String saveLogDataUrl = api.getSecurityUrl() + api.getSecuritySaveLogData();
		
		System.out.println(saveLogDataUrl);
		
		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", security.getAuth());
		headersMap.add("X-IBM-Client-Id", security.getClientId());
		headersMap.add("X-IBM-Client-Secret", security.getClientSecret());
		
		/*headersMap.add("Authorization", "Basic dHJhY2VhYmlsaXR5VXNlcjptMFYxc3RAUlMzY1VSaXQm");
		headersMap.add("X-IBM-Client-Id", "ddcc9f10-166e-4643-bc40-1759901b54fe");
		headersMap.add("X-IBM-Client-Secret", "e2eafa5e-2f92-497c-8d7c-fc4ae534898c");*/

		HttpEntity<LogDataRequest> entity = new HttpEntity<LogDataRequest>(logDataRequest, headersMap);
		
		try {
			
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(saveLogDataUrl, entity, String.class);
			
		} catch (Exception ex) {
			
			System.out.println(ex.getMessage());
		}
		// TODO Auto-generated constructor stub
	}

}
