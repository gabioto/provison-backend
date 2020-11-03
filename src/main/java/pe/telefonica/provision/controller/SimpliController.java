package pe.telefonica.provision.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pe.telefonica.provision.controller.request.simpli.SetSimpliUrlRequest;
import pe.telefonica.provision.controller.response.simpli.SimpliUrlResponse;
import pe.telefonica.provision.service.SimpliService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("provision")
public class SimpliController {
	// private static final Log log = LogFactory.getLog(RatingController.class);

	@Autowired
	private SimpliService simpliService;

	@RequestMapping(value = "liveTrackingTBX", method = RequestMethod.POST)
	public ResponseEntity<SimpliUrlResponse> setSimpliUrl(@RequestBody @Valid SetSimpliUrlRequest request,
			@RequestHeader(value = "UNICA-ServiceId") String UNICA_ServiceId,
			@RequestHeader(value = "UNICA-Application") String UNICA_Application,
			@RequestHeader(value = "UNICA-PID") String UNICA_PID, @RequestHeader HttpHeaders headers) {
		
		//set header by
		SimpliUrlResponse simpliUrlResponse = new SimpliUrlResponse();
		SimpliUrlResponse.Body body = new SimpliUrlResponse.Body();
		SimpliUrlResponse.Header header =  new SimpliUrlResponse.Header();
		
		HttpStatus status;
		
		header.setUNICA_ServiceId(UNICA_ServiceId.toString());
		header.setUNICA_PID(UNICA_PID.toString());
		
		
		if (request.getAppt_number() == null || request.getAppt_number().isEmpty()) {
			status = HttpStatus.BAD_REQUEST;
			
			body.setAPPT_Numer(request.getAppt_number());
			body.setStatus("appt_number obligatorio");
			simpliUrlResponse.setBody(body);
			return ResponseEntity.status(status).body(simpliUrlResponse);
		}
		
		if (request.getTracking() == null || request.getTracking().isEmpty()) {
			status = HttpStatus.BAD_REQUEST;
			
			body.setAPPT_Numer(request.getAppt_number());
			body.setStatus("Url tracking obligatorio");
			simpliUrlResponse.setBody(body);
			return ResponseEntity.status(status).body(simpliUrlResponse);
		}
		
		if (request.getXa_peticion() == null || request.getXa_peticion().isEmpty()) {
			status = HttpStatus.BAD_REQUEST;
			
			body.setAPPT_Numer(request.getAppt_number());
			body.setStatus("xa_peticion obligatorio");
			simpliUrlResponse.setBody(body);
			return ResponseEntity.status(status).body(simpliUrlResponse);
		}
		
		if (request.getXa_activity_type() == null || request.getXa_activity_type().isEmpty()) {
			status = HttpStatus.BAD_REQUEST;
			
			body.setAPPT_Numer(request.getAppt_number());
			body.setStatus("xa_activity_type obligatorio");
			simpliUrlResponse.setBody(body);
			return ResponseEntity.status(status).body(simpliUrlResponse);
		}
		
		if (request.getXa_requirement_number() == null || request.getXa_requirement_number().isEmpty()) {
			status = HttpStatus.BAD_REQUEST;
			
			body.setAPPT_Numer(request.getAppt_number());
			body.setStatus("xa_requirement_number obligatorio");
			simpliUrlResponse.setBody(body);
			return ResponseEntity.status(status).body(simpliUrlResponse);
		}
		
		if (request.getEta() == null || request.getEta().isEmpty()) {
			status = HttpStatus.BAD_REQUEST;
			
			body.setAPPT_Numer(request.getAppt_number());
			body.setStatus("ETA obligatorio");
			simpliUrlResponse.setBody(body);
			return ResponseEntity.status(status).body(simpliUrlResponse);
		}
		

		try {

			simpliUrlResponse = simpliService.setSimpliUrl(request);

			status = HttpStatus.OK;

		} catch (Exception ex) {

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			body.setAPPT_Numer(request.getAppt_number());
			body.setStatus(status.name());
			simpliUrlResponse.setBody(body);
		}
		simpliUrlResponse.setHeader(header);
		return ResponseEntity.status(status).body(simpliUrlResponse);
	}

}
