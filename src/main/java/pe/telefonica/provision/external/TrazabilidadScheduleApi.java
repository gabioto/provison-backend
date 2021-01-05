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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pe.telefonica.provision.conf.ExternalApi;
import pe.telefonica.provision.conf.IBMSecurityAgendamiento;
import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.request.CancelRequest;
import pe.telefonica.provision.controller.request.ScheduleNotDoneRequest;
import pe.telefonica.provision.controller.request.ScheduleRequest;
import pe.telefonica.provision.external.request.ScheduleUpdateFicticiousRequest;
import pe.telefonica.provision.external.request.ScheduleUpdatePSICodeRealRequest;
import pe.telefonica.provision.external.request.schedule.GetTechnicianAvailableRequest;
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.util.constants.Constants;
import pe.telefonica.provision.util.exception.FunctionalErrorException;
import pe.telefonica.provision.util.exception.ServerNotFoundException;

@Component
public class TrazabilidadScheduleApi {
	private static final Log log = LogFactory.getLog(TrazabilidadScheduleApi.class);

	@Autowired
	private IBMSecurityAgendamiento iBMSecurityAgendamiento;

	@Autowired
	private ExternalApi api;

	public boolean cancelLocalSchedule(ScheduleNotDoneRequest scheduleNotDoneRequest) {
		//log.info("updateSchedule");
		RestTemplate restTemplate = new RestTemplate();
		String urlSchedule = api.getScheduleUrl() + api.getCancelLocalSchedule();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", iBMSecurityAgendamiento.getAuth());
		headersMap.add("X-IBM-Client-Id", iBMSecurityAgendamiento.getClientId());
		headersMap.add("X-IBM-Client-Secret", iBMSecurityAgendamiento.getClientSecret());
		
		ApiRequest<ScheduleNotDoneRequest> apiRequest = new ApiRequest<ScheduleNotDoneRequest>(Constants.APP_NAME_PROVISION, Constants.USER_PROVISION, Constants.OPER_NOTDONE_SCHEDULE, scheduleNotDoneRequest);

		HttpEntity<ApiRequest<ScheduleNotDoneRequest>> entityProvision = new HttpEntity<ApiRequest<ScheduleNotDoneRequest>>(apiRequest, headersMap);

		try {
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(urlSchedule, entityProvision,
					String.class);
			
			//log.info("responseEntity: " + responseEntity.getBody());

			return responseEntity.getStatusCode().equals(HttpStatus.OK);
		} catch (HttpClientErrorException ex) {
			log.error("Exception = " + ex.getMessage());
			log.error("Exception = " + ex.getResponseBodyAsString());
			if(ex.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
				throw new FunctionalErrorException(ex.getMessage(), ex, String.valueOf(ex.getStatusCode().value() +"_"+ "401" ));
			} else {
				
				JsonObject jsonDecode = new Gson().fromJson(ex.getResponseBodyAsString(), JsonObject.class);
				
				String errorCode = jsonDecode.getAsJsonObject("header").get("resultCode").getAsString();
				String message   = jsonDecode.getAsJsonObject("header").get("message").getAsString();
				
				throw new FunctionalErrorException(message, ex, String.valueOf(ex.getStatusCode().value() +"_"+ errorCode ));
				
			}
			
		} catch (Exception ex) {
			log.error("Exception = " + ex.getMessage());
			throw new ServerNotFoundException(ex.getMessage());
		}
	}
	
	public boolean updateSchedule(ScheduleRequest scheduleRequest) {
		//log.info("updateSchedule");
		RestTemplate restTemplate = new RestTemplate();
		String urlSchedule = api.getScheduleUrl() + api.getUpdateScheduleDate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", iBMSecurityAgendamiento.getAuth());
		headersMap.add("X-IBM-Client-Id", iBMSecurityAgendamiento.getClientId());
		headersMap.add("X-IBM-Client-Secret", iBMSecurityAgendamiento.getClientSecret());
		
		ApiRequest<ScheduleRequest> apiRequest = new ApiRequest<ScheduleRequest>(Constants.APP_NAME_PROVISION, Constants.USER_PROVISION, Constants.OPER_UPDATE_RESCHEDULE, scheduleRequest);

		HttpEntity<ApiRequest<ScheduleRequest>> entityProvision = new HttpEntity<ApiRequest<ScheduleRequest>>(apiRequest, headersMap);

		try {
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(urlSchedule, entityProvision,
					String.class);
			
			//log.info("responseEntity: " + responseEntity.getBody());

			return responseEntity.getStatusCode().equals(HttpStatus.OK);
		} catch (HttpClientErrorException ex) {
			log.error("Exception = " + ex.getMessage());
			log.error("Exception = " + ex.getResponseBodyAsString());
			if(ex.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
				throw new FunctionalErrorException(ex.getMessage(), ex, String.valueOf(ex.getStatusCode().value() +"_"+ "401" ));
			} else {
				
				JsonObject jsonDecode = new Gson().fromJson(ex.getResponseBodyAsString(), JsonObject.class);
				
				String errorCode = jsonDecode.getAsJsonObject("header").get("resultCode").getAsString();
				String message   = jsonDecode.getAsJsonObject("header").get("message").getAsString();
				
				throw new FunctionalErrorException(message, ex, String.valueOf(ex.getStatusCode().value() +"_"+ errorCode ));
				
			}
			
		} catch (Exception ex) {
			log.error("Exception = " + ex.getMessage());
			throw new ServerNotFoundException(ex.getMessage());
		}
	}
	
	public boolean updateCancelSchedule(CancelRequest cancelRequest) {
		//log.info("updateCancelSchedule");
		RestTemplate restTemplate = new RestTemplate();
		String urlSchedule = api.getScheduleUrl() + api.getUpdateSchedule();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", iBMSecurityAgendamiento.getAuth());
		headersMap.add("X-IBM-Client-Id", iBMSecurityAgendamiento.getClientId());
		headersMap.add("X-IBM-Client-Secret", iBMSecurityAgendamiento.getClientSecret());

		ApiRequest<CancelRequest> apiRequest = new ApiRequest<CancelRequest>(Constants.APP_NAME_PROVISION,
				Constants.USER_PROVISION, Constants.OPER_CANCEL_SCHEDULE, cancelRequest);

		HttpEntity<ApiRequest<CancelRequest>> entityProvision = new HttpEntity<ApiRequest<CancelRequest>>(apiRequest,
				headersMap);

		try {
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(urlSchedule, entityProvision,
					String.class);

			//log.info("responseEntity: " + responseEntity.getBody());

			return responseEntity.getStatusCode().equals(HttpStatus.OK);
		} catch (HttpClientErrorException ex) {
			log.error("Exception = " + ex.getMessage());
			log.error("Exception = " + ex.getResponseBodyAsString());
			if (ex.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
				throw new FunctionalErrorException(ex.getMessage(), ex,
						String.valueOf(ex.getStatusCode().value() + "_" + "401"));
			} else {
				JsonObject jsonDecode = new Gson().fromJson(ex.getResponseBodyAsString(), JsonObject.class);

				String errorCode = jsonDecode.getAsJsonObject("header").get("resultCode").getAsString();
				String message = jsonDecode.getAsJsonObject("header").get("message").getAsString();

				throw new FunctionalErrorException(message, ex,
						String.valueOf(ex.getStatusCode().value() + "_" + errorCode));

			}
		} catch (Exception ex) {
			log.error("Exception = " + ex.getMessage());
			throw new ServerNotFoundException(ex.getMessage());
		}
	}

	public boolean updateFicticious(ScheduleUpdateFicticiousRequest request) {
		//log.info("updateCancelSchedule");
		RestTemplate restTemplate = new RestTemplate();
		String urlSchedule = api.getScheduleUrl() + api.getScheduleUpdateFicticious();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", iBMSecurityAgendamiento.getAuth());
		headersMap.add("X-IBM-Client-Id", iBMSecurityAgendamiento.getClientId());
		headersMap.add("X-IBM-Client-Secret", iBMSecurityAgendamiento.getClientSecret());

		ApiRequest<ScheduleUpdateFicticiousRequest> apiRequest = new ApiRequest<ScheduleUpdateFicticiousRequest>(
				Constants.APP_NAME_PROVISION, Constants.USER_PROVISION, Constants.OPER_SCHEDULE_UPDATE_CODE_FICT,
				request);

		HttpEntity<ApiRequest<ScheduleUpdateFicticiousRequest>> entityProvision = new HttpEntity<ApiRequest<ScheduleUpdateFicticiousRequest>>(
				apiRequest, headersMap);

		try {
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(urlSchedule, entityProvision,
					String.class);

			//log.info("responseEntity: " + responseEntity.getBody());

			return responseEntity.getStatusCode().equals(HttpStatus.OK);
		} catch (HttpClientErrorException ex) {
			log.error("Exception = " + ex.getMessage());
			log.error("Exception = " + ex.getResponseBodyAsString());

			return false;
		} catch (Exception ex) {
			log.error("Exception = " + ex.getMessage());
			return false;
		}
	}

	public boolean updatePSICodeReal(String idProvision, String orderCode, String stPsiCode, String requestType, Customer customer) {

		//log.info("updatePSICodeReal");
		RestTemplate restTemplate = new RestTemplate();
		String urlSchedule = api.getScheduleUrl() + api.getScheduleUpdatePSICodeReal();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", iBMSecurityAgendamiento.getAuth());
		headersMap.add("X-IBM-Client-Id", iBMSecurityAgendamiento.getClientId());
		headersMap.add("X-IBM-Client-Secret", iBMSecurityAgendamiento.getClientSecret());

		ScheduleUpdatePSICodeRealRequest updateStPsiCodeRequest = new ScheduleUpdatePSICodeRealRequest();
		updateStPsiCodeRequest.setOrderCode(orderCode);
		updateStPsiCodeRequest.setXaOrderCode(orderCode);
		updateStPsiCodeRequest.setStPsiCode(stPsiCode);
		updateStPsiCodeRequest.setRequestId(idProvision);
		updateStPsiCodeRequest.setRequestType(requestType);
		updateStPsiCodeRequest.setCustomer(customer);

		ApiRequest<ScheduleUpdatePSICodeRealRequest> apiRequest = new ApiRequest<ScheduleUpdatePSICodeRealRequest>(
				Constants.APP_NAME_PROVISION, Constants.USER_PROVISION, Constants.OPER_SCHEDULE_UPDATE_CODE_FICT,
				updateStPsiCodeRequest);

		HttpEntity<ApiRequest<ScheduleUpdatePSICodeRealRequest>> entityProvision = new HttpEntity<ApiRequest<ScheduleUpdatePSICodeRealRequest>>(
				apiRequest, headersMap);

		try {
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(urlSchedule, entityProvision,
					String.class);

			//log.info("responseEntity: " + responseEntity.getBody());

			return responseEntity.getStatusCode().equals(HttpStatus.OK);
		} catch (HttpClientErrorException ex) {
			log.error("Exception = " + ex.getMessage());
			log.error("Exception = " + ex.getResponseBodyAsString());

			return false;

		} catch (Exception ex) {

			log.error("Exception = " + ex.getMessage());
			return false;
		}

	}
	
	
	public String getTechAvailable(GetTechnicianAvailableRequest request) {
		//log.info("updateCancelSchedule");
		
		
		RestTemplate restTemplate = new RestTemplate();
		String urlSchedule = api.getScheduleUrl() + api.getScheduleGetTechAvailable();
		
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", iBMSecurityAgendamiento.getAuth());
		headersMap.add("X-IBM-Client-Id", iBMSecurityAgendamiento.getClientId());
		headersMap.add("X-IBM-Client-Secret", iBMSecurityAgendamiento.getClientSecret());

		ApiRequest<GetTechnicianAvailableRequest> apiRequest = new ApiRequest<GetTechnicianAvailableRequest>(
				Constants.APP_NAME_AGENDAMIENTO, Constants.APP_NAME_AGENDAMIENTO, Constants.OPER_GET_TECH_AVAILABLE,
				request);

		HttpEntity<ApiRequest<GetTechnicianAvailableRequest>> entityProvision = new HttpEntity<ApiRequest<GetTechnicianAvailableRequest>>(
				apiRequest, headersMap);

		try {
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(urlSchedule, entityProvision,
					String.class);

			//log.info("responseEntity: " + responseEntity.getBody());
			if(responseEntity.getStatusCode().equals(HttpStatus.OK)) {
				JsonObject jsonObject = new JsonParser().parse(responseEntity.getBody().toString()).getAsJsonObject();
				String driverUserName = jsonObject.get("body").getAsJsonObject().get("driverUsername").toString().replaceAll("\"",
						"");
				
				return driverUserName;
			}
			return null;
		} catch (HttpClientErrorException ex) {
			log.error("Exception = " + ex.getMessage());
			log.error("Exception = " + ex.getResponseBodyAsString());

			return null;
		} catch (Exception ex) {
			log.error("Exception = " + ex.getMessage());
			return null;
		}
	}

}
