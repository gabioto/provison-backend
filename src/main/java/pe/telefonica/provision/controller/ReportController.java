package pe.telefonica.provision.controller;

import java.util.List;

import javax.validation.Valid;

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
import pe.telefonica.provision.controller.request.report.ReportByRegisterDateRequest;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.service.ReportService;
import pe.telefonica.provision.util.constants.Constants;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("report")
@EnableAsync
public class ReportController {

	@Autowired
	private ReportService reportService;
	
	@RequestMapping(value = "/getCountProvisionByRegisterDate", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<Long>> getAllInTimeRange(
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
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<Long>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_PROVISION_BY_REGISTER_DATE, String.valueOf(status.value()), ex.getMessage().toString(), null);
		}
		return ResponseEntity.status(status).body(apiResponse);
	}
}
