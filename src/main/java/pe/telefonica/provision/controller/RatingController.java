package pe.telefonica.provision.controller;

import javax.validation.Valid;

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
import pe.telefonica.provision.controller.request.rating.SetRatingRequest;
import pe.telefonica.provision.dto.ProvisionDetailTrazaDto;
import pe.telefonica.provision.service.RatingService;
import pe.telefonica.provision.util.constants.Constants;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("rating")
public class RatingController {
	// private static final Log log = LogFactory.getLog(RatingController.class);

	@Autowired
	private RatingService ratingService;

	@RequestMapping(value = "/setRating", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<ProvisionDetailTrazaDto>> setRating(@RequestBody @Valid ApiRequest<SetRatingRequest> request) {

		ApiResponse<ProvisionDetailTrazaDto> apiResponse;
		HttpStatus status;

		try {

			ProvisionDetailTrazaDto provison = ratingService.setRating(request.getBody());

			status = provison != null ? HttpStatus.OK : HttpStatus.NOT_FOUND;

			apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION, Constants.OPER_INSERT_RATING,
					String.valueOf(status.value()), status.getReasonPhrase(), null);
			apiResponse.setBody(provison);

		} catch (Exception ex) {

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION, Constants.OPER_INSERT_RATING,
					String.valueOf(status.value()), status.getReasonPhrase(), null);
			apiResponse.setBody(null);

		}

		return ResponseEntity.status(status).body(apiResponse);
	}

}
