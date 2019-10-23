package pe.telefonica.provision.external;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pe.telefonica.provision.conf.ExternalApi;
import pe.telefonica.provision.conf.IBMSecurityAgendamiento;
import pe.telefonica.provision.controller.request.CancelRequest;
import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.util.constants.Constants;
import pe.telefonica.provision.util.exception.ServerNotFoundException;
import pe.telefonica.provision.util.exception.FunctionalErrorException;
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
		String urlSchedule = api.getScheduleUrl() + api.getUpdateSchedule();
		//String urlSchedule = "https://agendamiento-trazabilidad-dev.mybluemix.net/schedule/cancelSchedule";
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", iBMSecurityAgendamiento.getAuth());
		headersMap.add("X-IBM-Client-Id", iBMSecurityAgendamiento.getClientId());
		headersMap.add("X-IBM-Client-Secret", iBMSecurityAgendamiento.getClientSecret());
		
		ApiRequest<CancelRequest> apiRequest = new ApiRequest<CancelRequest>(Constants.APP_NAME_PROVISION, Constants.USER_PROVISION, Constants.OPER_CANCEL_SCHEDULE, cancelRequest);
		//HttpEntity<ApiRequest<LoginRequest>> entity = new HttpEntity<ApiRequest<LoginRequest>>(apiRequest, headersMap);

		HttpEntity<ApiRequest<CancelRequest>> entityProvision = new HttpEntity<ApiRequest<CancelRequest>>(apiRequest, headersMap);

		try {
			//ParameterizedTypeReference<ApiResponse<String>>  parameterizedTypeReference = new ParameterizedTypeReference<ApiResponse<String>>(){};
			
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(urlSchedule, entityProvision,
					String.class);
			
			log.info("responseEntity: " + responseEntity.getBody());

			return responseEntity.getStatusCode().equals(HttpStatus.OK);
		} catch (HttpClientErrorException ex) {
			log.info("Exception = " + ex.getMessage());
			log.info("Exception = " + ex.getResponseBodyAsString());
			
			JsonObject jsonDecode = new Gson().fromJson(ex.getResponseBodyAsString(), JsonObject.class);
			
			String errorCode = jsonDecode.getAsJsonObject("header").get("resultCode").getAsString();
			String message   = jsonDecode.getAsJsonObject("header").get("message").getAsString();
			
			throw new FunctionalErrorException(message, ex, String.valueOf(ex.getStatusCode().value() +"_"+ errorCode ));
			
			
			
		//hrow ew FunctionalErrrException(ex.getMessage(), ex, String.valueOf}<>)ex.getStatusCode());
		} catch (Exception ex) {
			
			log.info("Exception = " + ex.getMessage());
			throw new ServerNotFoundException(ex.getMessage());
		}
	}
}
