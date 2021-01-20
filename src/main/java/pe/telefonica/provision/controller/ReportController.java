package pe.telefonica.provision.controller;

import java.util.List;

import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.common.ApiResponse;
import pe.telefonica.provision.controller.common.NotificationResponse;
import pe.telefonica.provision.controller.request.report.ReportByRegisterDateRequest;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.service.ReportService;
import pe.telefonica.provision.util.constants.Constants;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("report")
@EnableAsync
public class ReportController {
	private static final Log log = LogFactory.getLog(RatingController.class);
	
	@Autowired
	private ReportService reportService;
	
	@RequestMapping(value = "/getCountProvisionByRegisterDate", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<Long>> getCountProvisionByRegisterDate(
			@RequestBody @Valid ApiRequest<ReportByRegisterDateRequest> request) {
		
		ApiResponse<Long> apiResponse;
		HttpStatus status;
		try {			
			Long provisions = reportService.getProvisionByRegisterDate(request.getBody());
			
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<Long>(Constants.APP_NAME_PROVISION,
						Constants.OPER_GET_PROVISION_BY_REGISTER_DATE, String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(provisions);			
			
		} catch (Exception ex) {
			log.error("Exception: "+ ex.getMessage());
			
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<Long>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_PROVISION_BY_REGISTER_DATE, String.valueOf(status.value()), ex.getMessage().toString(), null);
		}
		return ResponseEntity.status(status).body(apiResponse);
	}
	
	@RequestMapping(value = "/getCountByEventNotication", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<NotificationResponse>> getCountByEventNotication(
			@RequestBody @Valid ApiRequest<ReportByRegisterDateRequest> request) {
		
		ApiResponse<NotificationResponse> apiResponse = null;
		HttpStatus status;
		try {
						
			NotificationResponse notificationResponse= reportService.countByEventNotication(request.getBody());
			
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<NotificationResponse>(Constants.APP_NAME_PROVISION,
						Constants.OPER_GET_PROVISION_BY_REGISTER_DATE, String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(notificationResponse);
			
			
		} catch (Exception ex) {
			log.error("Exception: "+ ex.getMessage());
			
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			//apiResponse = new ApiResponse<Long>(Constants.APP_NAME_PROVISION,
			//		Constants.OPER_GET_PROVISION_BY_REGISTER_DATE, String.valueOf(status.value()), ex.getMessage().toString(), null);
		}
		return ResponseEntity.status(status).body(apiResponse);
	}
	
	@RequestMapping(value = "/getAllProvision", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<List<Provision>>> getAllProvisions(
			@RequestBody @Valid ApiRequest<ReportByRegisterDateRequest> request) {
		
		ApiResponse<List<Provision>> apiResponse;
		HttpStatus status;
		try {			
			List<Provision> provisions = reportService.getAllProvisions(request.getBody());
			
			if (provisions != null) {
				status = HttpStatus.OK;
				
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
						Constants.OPER_GET_ALL_PROVISION, String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(provisions);
			} else {				
				status = HttpStatus.NOT_FOUND;

				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
						Constants.OPER_GET_ALL_PROVISION, String.valueOf(status.value()), "No se encontraron registros", null);
				apiResponse.setBody(null);
			}			
		} catch (Exception ex) {
			log.error("Exception: "+ ex.getMessage());
			
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			
			apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_ALL_PROVISION, String.valueOf(status.value()), ex.getMessage().toString(), null);
		}
		return ResponseEntity.status(status).body(apiResponse);
	}
	
	
	
}
