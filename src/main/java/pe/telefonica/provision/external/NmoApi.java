package pe.telefonica.provision.external;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import pe.telefonica.provision.conf.ExternalApi;
import pe.telefonica.provision.controller.request.simpli.SetNmoRequest;
import pe.telefonica.provision.controller.request.simpli.SetServiceRequest;
import pe.telefonica.provision.external.response.PSIWorkResponse;
import pe.telefonica.provision.util.constants.Constants;

@Component
public class NmoApi {

	private static final Log log = LogFactory.getLog(NmoApi.class);

	@Autowired
	TrazabilidadSecurityApi loggerApi;

	@Autowired
	private HttpComponentsClientHttpRequestFactory initClientRestTemplate;

	@Autowired
	private ExternalApi externalApi;
	
	/**
	 * 
	 * @param reference
	 * @return
	 * @throws Exception
	 */
	public boolean updateActivity(String idActivity, String tokenExternal) throws Exception {
		RestTemplate restTemplate = new RestTemplate(initClientRestTemplate);
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		SetNmoRequest request = new SetNmoRequest();
		request.setA_FORM_NMO("3");
				
		String url = "https://apimngr-genesis-cert.azure-api.net/api-ne-traceability-trazabilidadtoa-oc/v1/rest/ofscCore/v1/activities/" + idActivity;
		
		LocalDateTime startHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
		LocalDateTime endHour;

		SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.TIMESTAMP_FORMAT_USER);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-5:00"));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("UNICA-ServiceId", externalApi.getUnicaServiceId());
		headers.set("UNICA-Application", externalApi.getUnicaApplication());
		headers.set("UNICA-PID", externalApi.getUnicaPID());
		headers.set("UNICA-User", externalApi.getUnicaUser());
		headers.set("Ocp-Apim-Subscription-Key", externalApi.getOcpApimSubscriptionKey());
		headers.set("Authorization", "Bearer " + tokenExternal);		

		HttpEntity<SetNmoRequest> entity = new HttpEntity<>(request, headers);

		log.info(new Gson().toJson(entity));
		
		try {
			ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.PATCH, entity,
					String.class);
			
			log.info(new Gson().toJson(responseEntity));

			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("NMO", "updateActivity", new Gson().toJson(entity.getBody()), new Gson().toJson(responseEntity.getBody()), url,
					startHour, endHour, responseEntity.getStatusCodeValue());

			return true;
		} catch (HttpClientErrorException ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());
			
			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("NMO", "updateActivity", new Gson().toJson(entity.getBody()), ex.getLocalizedMessage(), url,
					startHour, endHour, ex.getStatusCode().value());
			
			return false;
		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			String error = ex.getLocalizedMessage().substring(0, ex.getLocalizedMessage().indexOf(" "));
			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("NMO", "updateActivity", new Gson().toJson(entity.getBody()), ex.getLocalizedMessage(), url, startHour,
					endHour, Integer.parseInt(error));
			
			return false;
		}
	}
	
	public boolean serviceRequest(String idActivity, String tokenExternal) throws Exception {
		RestTemplate restTemplate = new RestTemplate(initClientRestTemplate);
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		SetServiceRequest request = new SetServiceRequest();
		request.setActivityId(Integer.parseInt(idActivity));
		request.setRequestType("REJECTION");
		
		String url = "https://apimngr-genesis-cert.azure-api.net/api-ne-traceability-trazabilidadtoa-oc/v1/rest/ofscCore/v1/serviceRequests";
		
		LocalDateTime startHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
		LocalDateTime endHour;

		SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.TIMESTAMP_FORMAT_USER);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-5:00"));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("UNICA-ServiceId", externalApi.getUnicaServiceId());
		headers.set("UNICA-Application", externalApi.getUnicaApplication());
		headers.set("UNICA-PID", externalApi.getUnicaPID());
		headers.set("UNICA-User", externalApi.getUnicaUser());
		headers.set("Ocp-Apim-Subscription-Key", externalApi.getOcpApimSubscriptionKey());
		headers.set("Authorization", "Bearer " + tokenExternal);		

		HttpEntity<SetServiceRequest> entity = new HttpEntity<>(request, headers);

		log.info(new Gson().toJson(entity));
		
		try {
			ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity,
					String.class);
			
			log.info(new Gson().toJson(responseEntity));

			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("NMO", "serviceRequest", new Gson().toJson(entity.getBody()), new Gson().toJson(responseEntity.getBody()), url,
					startHour, endHour, responseEntity.getStatusCodeValue());

			return true;
		} catch (HttpClientErrorException ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());
			
			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("NMO", "serviceRequest", new Gson().toJson(entity.getBody()), ex.getLocalizedMessage(), url,
					startHour, endHour, ex.getStatusCode().value());
			
			return false;
		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			String error = ex.getLocalizedMessage().substring(0, ex.getLocalizedMessage().indexOf(" "));
			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("NMO", "serviceRequest", new Gson().toJson(entity.getBody()), ex.getLocalizedMessage(), url, startHour,
					endHour, Integer.parseInt(error));
			
			return false;
		}
	}
}