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
import pe.telefonica.provision.controller.request.report.ReportInviteMessageRequest;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.service.ReportService;
import pe.telefonica.provision.util.constants.Constants;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("provision")
@EnableAsync
public class ReportController {

	@Autowired
	private ReportService reportService;
	
	@RequestMapping(value = "/getAllInTimeRange", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<List<Provision>>> getAllInTimeRange(
			@RequestBody @Valid ApiRequest<ReportInviteMessageRequest> request) {
		
		ApiResponse<List<Provision>> apiResponse;
		HttpStatus status;
		try {
			
			List<Provision> provisions = reportService.getFaultsByInviteMessageDate(request.getBody());
			
			if (provisions != null) {
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_AVERIA,
						Constants.OPER_GET_PROVISION_ALL, String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(provisions);
			} else {
				
				status = HttpStatus.NOT_FOUND;

				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_AVERIA,
						Constants.OPER_GET_PROVISION_ALL, String.valueOf(status.value()), "No se encontraron registros", null);
				apiResponse.setBody(null);
			}
			
		} catch (Exception ex) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_AVERIA,
					Constants.OPER_GET_PROVISION_ALL, String.valueOf(status.value()), ex.getMessage().toString(), null);
		}
		return ResponseEntity.status(status).body(apiResponse);
	}
}
