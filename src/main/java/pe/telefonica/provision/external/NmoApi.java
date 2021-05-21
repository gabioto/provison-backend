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
	public boolean updateActivity(String idActivity, SetNmoRequest request, String tokenExternal) throws Exception {
		RestTemplate restTemplate = new RestTemplate(initClientRestTemplate);
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		String url = "https://apimngr-genesis-dev.azure-api.net/api-ne-traceability-trazabilidadtoa-oc/v1/rest/ofscCore/v1/activities/" + idActivity;
		
		LocalDateTime startHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
		LocalDateTime endHour;

		SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.TIMESTAMP_FORMAT_USER);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-5:00"));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("UNICA-ServiceId", "");
		headers.set("UNICA-Application", "trazabilidad");
		headers.set("UNICA-PID", "");
		headers.set("UNICA-User", "user_trazabilidad");
		headers.set("Ocp-Apim-Subscription-Key", externalApi.getApiClientKeyAzure());
		headers.set("Authorization", "Bearer " + tokenExternal);		

		HttpEntity<SetNmoRequest> entity = new HttpEntity<>(request, headers);

		try {
			ResponseEntity<PSIWorkResponse> responseEntity = restTemplate.exchange(url, HttpMethod.PATCH, entity,
					PSIWorkResponse.class);

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
}