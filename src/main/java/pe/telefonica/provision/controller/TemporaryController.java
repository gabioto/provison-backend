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
import pe.telefonica.provision.controller.request.temporary.GetSaleCodeByXaRequestAndSTRequest;
import pe.telefonica.provision.service.TemporaryService;
import pe.telefonica.provision.util.constants.Constants;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("provision")
public class TemporaryController {
	private static final Log log = LogFactory.getLog(MonitoringController.class);

	@Autowired
	TemporaryService temporaryService;

	@RequestMapping(value = "/getSaleCodeByXaRequestAndST", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<String>> getProvisionByStatus(
			@RequestBody @Valid ApiRequest<GetSaleCodeByXaRequestAndSTRequest> request) {

		ApiResponse<String> apiResponse;
		HttpStatus status;

		try {

			String response = temporaryService.getSaleCode(request.getBody());

			status = HttpStatus.OK;
			apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_SALE_CODE, String.valueOf(status.value()), status.getReasonPhrase(),
					null);
			apiResponse.setBody(response);

		} catch (Exception ex) {
			log.error("Exception: "+ ex.getMessage());
			
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_SALE_CODE, String.valueOf(status.value()), status.getReasonPhrase(),
					null);
			apiResponse.setBody(null);

		}

		return ResponseEntity.status(status).body(apiResponse);
	}
}
