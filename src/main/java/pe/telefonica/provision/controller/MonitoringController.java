package pe.telefonica.provision.controller;

import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.common.ApiResponse;
import pe.telefonica.provision.controller.request.monitoring.GetProvisionByStatusRequest;
import pe.telefonica.provision.controller.response.monitoring.GetProvisionByStatusResponse;
import pe.telefonica.provision.service.MonitoringService;
import pe.telefonica.provision.util.constants.Constants;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("monitoring")
public class MonitoringController {
	private static final Log log = LogFactory.getLog(MonitoringController.class);

	@Autowired
	MonitoringService monitoringService;

	@RequestMapping(value = "/getProvisionByStatus", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<GetProvisionByStatusResponse>> getProvisionByStatus(
			@RequestBody @Valid ApiRequest<GetProvisionByStatusRequest> request) {

		ApiResponse<GetProvisionByStatusResponse> apiResponse;
		HttpStatus status;

		try {

			GetProvisionByStatusResponse response = monitoringService.GetProvisionByStatus(request.getBody());

			status = HttpStatus.OK;
			apiResponse = new ApiResponse<GetProvisionByStatusResponse>(Constants.APP_NAME_PROVISION,
					Constants.OPER_MONITORING_PROVISION, String.valueOf(status.value()), status.getReasonPhrase(),
					null);
			apiResponse.setBody(response);

		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());
			
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<GetProvisionByStatusResponse>(Constants.APP_NAME_PROVISION,
					Constants.OPER_MONITORING_PROVISION, String.valueOf(status.value()), status.getReasonPhrase(),
					null);
			apiResponse.setBody(null);

		}

		return ResponseEntity.status(status).body(apiResponse);
	}
}
