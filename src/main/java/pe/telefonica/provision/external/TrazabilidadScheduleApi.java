package pe.telefonica.provision.external;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import pe.telefonica.provision.conf.ExternalApi;
import pe.telefonica.provision.conf.IBMSecurityAgendamiento;
import pe.telefonica.provision.controller.request.CancelRequest;

@Component
public class TrazabilidadScheduleApi {
	private static final Log log = LogFactory.getLog(TrazabilidadScheduleApi.class);
	
	@Autowired
	private IBMSecurityAgendamiento iBMSecurityAgendamiento;
	
	@Autowired
	private ExternalApi api;
	
	public boolean updateCancelSchedule(CancelRequest cancelRequest) {
		log.info("updateCancelSchedule");
		RestTemplate restTemplate = new RestTemplate();
		String urlProvisionUser = api.getScheduleUrl() + api.getUpdateSchedule();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", iBMSecurityAgendamiento.getAuth());
		headersMap.add("X-IBM-Client-Id", iBMSecurityAgendamiento.getClientId());
		headersMap.add("X-IBM-Client-Secret", iBMSecurityAgendamiento.getClientSecret());

		HttpEntity<CancelRequest> entityProvision = new HttpEntity<CancelRequest>(cancelRequest, headersMap);

		try {
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(urlProvisionUser, entityProvision,
					String.class);
			log.info("responseEntity: " + responseEntity.getBody());

			return responseEntity.getStatusCode().equals(HttpStatus.OK);
		} catch (Exception e) {
			log.info("Exception = " + e.getMessage());
			return false;
		}
	}
}
