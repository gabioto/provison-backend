package pe.telefonica.provision.controller;

import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import pe.telefonica.provision.controller.common.ApiErrorResponse;
import pe.telefonica.provision.controller.common.ApiSimpliResponse;
import pe.telefonica.provision.controller.request.simpli.SetSimpliUrlRequest;
import pe.telefonica.provision.controller.response.simpli.ErrorResponse;
import pe.telefonica.provision.controller.response.simpli.SimpliUrlResponse;
import pe.telefonica.provision.service.SimpliService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("provision")
public class SimpliController {
	private static final Log log = LogFactory.getLog(SimpliController.class);

	@Autowired
	private SimpliService simpliService;

	@RequestMapping(value = {"liveTrackingTBX"}, method = RequestMethod.POST)
	public ResponseEntity<Object> setSimpliUrl(@RequestBody @Valid SetSimpliUrlRequest request,
			@RequestHeader(value = "UNICA-ServiceId", required=false) String UNICA_ServiceId,			
			@RequestHeader(value = "UNICA-PID", required=false) String UNICA_PID) {
		log.info(this.getClass().getName() + " - " + "liveTrackingTBX");
		
		ApiSimpliResponse<SimpliUrlResponse> simpliUrlResponse;
		
		HttpStatus status;
		
		ApiErrorResponse<ErrorResponse> apiErrorResponse;
		ErrorResponse responseError = null;		
		
		if (request.getTracking() == null || request.getTracking().isEmpty()) {
			status = HttpStatus.BAD_REQUEST;
			
			responseError = new ErrorResponse();
			responseError.setExceptionId("SVC1000");
			responseError.setExceptionText("Missing mandatory parameter: : Tracking");
			responseError.setMoreInfo("API Request without mandatory field");			
			responseError.setUserMessage("Missing mandatory parameter");
			
			apiErrorResponse = new ApiErrorResponse<ErrorResponse>(responseError);
			
			Gson gson = new Gson();
		    String json = gson.toJson(apiErrorResponse);
		    String cadena = "{\"body\":";
		    int posicion = json.indexOf(cadena);
		    if (posicion > -1) {
		    	json = json.substring(posicion + cadena.length());
		    	json = json.substring(0, json.length() - 1);	    
		    }
		    
		    return ResponseEntity.status(status)
					.header("UNICA-ServiceId", UNICA_ServiceId)
					.header("UNICA-PID", UNICA_PID)
					.header("Content-Type", "application/json;charset=UTF-8")
					.body(json);
		}
		
		if (request.getXa_peticion() == null || request.getXa_peticion().isEmpty()) {
			status = HttpStatus.BAD_REQUEST;
			
			responseError = new ErrorResponse();
			responseError.setExceptionId("SVC1000");
			responseError.setExceptionText("Missing mandatory parameter: : xa_peticion");
			responseError.setMoreInfo("API Request without mandatory field");			
			responseError.setUserMessage("Missing mandatory parameter");
			
			apiErrorResponse = new ApiErrorResponse<ErrorResponse>(responseError);
			
			Gson gson = new Gson();
		    String json = gson.toJson(apiErrorResponse);
		    String cadena = "{\"body\":";
		    int posicion = json.indexOf(cadena);
		    if (posicion > -1) {
		    	json = json.substring(posicion + cadena.length());
		    	json = json.substring(0, json.length() - 1);	    
		    }
		    
		    return ResponseEntity.status(status)
					.header("UNICA-ServiceId", UNICA_ServiceId)
					.header("UNICA-PID", UNICA_PID)
					.header("Content-Type", "application/json;charset=UTF-8")
					.body(json);			
		}
		
		if (request.getXa_activity_type() == null || request.getXa_activity_type().isEmpty()) {
			status = HttpStatus.BAD_REQUEST;
			
			responseError = new ErrorResponse();
			responseError.setExceptionId("SVC1000");
			responseError.setExceptionText("Missing mandatory parameter: : xa_activity_type");
			responseError.setMoreInfo("API Request without mandatory field");			
			responseError.setUserMessage("Missing mandatory parameter");
			
			apiErrorResponse = new ApiErrorResponse<ErrorResponse>(responseError);
			
			Gson gson = new Gson();
		    String json = gson.toJson(apiErrorResponse);
		    String cadena = "{\"body\":";
		    int posicion = json.indexOf(cadena);
		    if (posicion > -1) {
		    	json = json.substring(posicion + cadena.length());
		    	json = json.substring(0, json.length() - 1);	    
		    }
		    
		    return ResponseEntity.status(status)
					.header("UNICA-ServiceId", UNICA_ServiceId)
					.header("UNICA-PID", UNICA_PID)
					.header("Content-Type", "application/json;charset=UTF-8")
					.body(json);
		}
		
		if (request.getXa_requirement_number() == null || request.getXa_requirement_number().isEmpty()) {
			status = HttpStatus.BAD_REQUEST;
			
			responseError = new ErrorResponse();
			responseError.setExceptionId("SVC1000");
			responseError.setExceptionText("Missing mandatory parameter: : xa_requirement_number");
			responseError.setMoreInfo("API Request without mandatory field");			
			responseError.setUserMessage("Missing mandatory parameter");
			
			apiErrorResponse = new ApiErrorResponse<ErrorResponse>(responseError);
			
			Gson gson = new Gson();
		    String json = gson.toJson(apiErrorResponse);
		    String cadena = "{\"body\":";
		    int posicion = json.indexOf(cadena);
		    if (posicion > -1) {
		    	json = json.substring(posicion + cadena.length());
		    	json = json.substring(0, json.length() - 1);	    
		    }
		    
		    return ResponseEntity.status(status)
					.header("UNICA-ServiceId", UNICA_ServiceId)
					.header("UNICA-PID", UNICA_PID)
					.header("Content-Type", "application/json;charset=UTF-8")
					.body(json);
		}		
		
		if (request.getApptNumber() == null || request.getApptNumber().isEmpty()) {
			status = HttpStatus.BAD_REQUEST;
			
			responseError = new ErrorResponse();
			responseError.setExceptionId("SVC1000");
			responseError.setExceptionText("Missing mandatory parameter: : ApptNumber");
			responseError.setMoreInfo("API Request without mandatory field");			
			responseError.setUserMessage("Missing mandatory parameter");
			
			apiErrorResponse = new ApiErrorResponse<ErrorResponse>(responseError);
			
			Gson gson = new Gson();
		    String json = gson.toJson(apiErrorResponse);
		    String cadena = "{\"body\":";
		    int posicion = json.indexOf(cadena);
		    if (posicion > -1) {
		    	json = json.substring(posicion + cadena.length());
		    	json = json.substring(0, json.length() - 1);	    
		    }
		    
		    return ResponseEntity.status(status)
					.header("UNICA-ServiceId", UNICA_ServiceId)
					.header("UNICA-PID", UNICA_PID)
					.header("Content-Type", "application/json;charset=UTF-8")
					.body(json);
		}
		
		if (request.getETA() == null || request.getETA().isEmpty()) {
			status = HttpStatus.BAD_REQUEST;
			
			responseError = new ErrorResponse();
			responseError.setExceptionId("SVC1000");
			responseError.setExceptionText("Missing mandatory parameter: : ETA");
			responseError.setMoreInfo("API Request without mandatory field");			
			responseError.setUserMessage("Missing mandatory parameter");
			
			apiErrorResponse = new ApiErrorResponse<ErrorResponse>(responseError);
			
			Gson gson = new Gson();
		    String json = gson.toJson(apiErrorResponse);
		    String cadena = "{\"body\":";
		    int posicion = json.indexOf(cadena);
		    if (posicion > -1) {
		    	json = json.substring(posicion + cadena.length());
		    	json = json.substring(0, json.length() - 1);	    
		    }
		    
		    return ResponseEntity.status(status)
					.header("UNICA-ServiceId", UNICA_ServiceId)
					.header("UNICA-PID", UNICA_PID)
					.header("Content-Type", "application/json;charset=UTF-8")
					.body(json);
		}
		
		try {			
			SimpliUrlResponse objSimpliUrlResponse = simpliService.setSimpliUrl(request);
			if (objSimpliUrlResponse.getBody().getStatus().equals("OK")) {			
				simpliUrlResponse = new ApiSimpliResponse<SimpliUrlResponse>(objSimpliUrlResponse);
				status = HttpStatus.OK;
				
				Gson gson = new Gson();
			    String json = gson.toJson(simpliUrlResponse);
			    String cadena = "{\"body\":{\"body\":";
			    int posicion = json.indexOf(cadena);
			    if (posicion > -1) {
			    	json = json.substring(posicion + cadena.length());
			    	json = json.substring(0, json.length() - 2);	    
			    }
			    
			    return ResponseEntity.status(status)
						.header("UNICA-ServiceId", UNICA_ServiceId)
						.header("UNICA-PID", UNICA_PID)
						.header("Content-Type", "application/json;charset=UTF-8")
						.body(json);
			} else {
				responseError = new ErrorResponse();
				if (objSimpliUrlResponse.getBody().getStatus().equals("NOT_FOUND")) {			
					status = HttpStatus.NOT_FOUND;
				
					responseError.setExceptionId("SVC1006");
					responseError.setExceptionText("Resource " + request.getXa_activity_type() + " does not exist");
					responseError.setMoreInfo("Reference to a resource identifier which does not exist in the collection/repository referred");
					responseError.setUserMessage("Not existing Resource Id");					
				} else {
					status = HttpStatus.BAD_REQUEST;
					
					responseError.setExceptionId("SVC0001");
					responseError.setExceptionText("Generic Client Error");
					responseError.setMoreInfo("API Generic wildcard fault response");
					responseError.setUserMessage("Generic Client Error");
				}
				apiErrorResponse = new ApiErrorResponse<ErrorResponse>(responseError);
				
				Gson gson = new Gson();
			    String json = gson.toJson(apiErrorResponse);
			    String cadena = "{\"body\":";
			    int posicion = json.indexOf(cadena);
			    if (posicion > -1) {
			    	json = json.substring(posicion + cadena.length());
			    	json = json.substring(0, json.length() - 1);	    
			    }
			    
			    return ResponseEntity.status(status)
						.header("UNICA-ServiceId", UNICA_ServiceId)
						.header("UNICA-PID", UNICA_PID)
						.header("Content-Type", "application/json;charset=UTF-8")
						.body(json);
			}
		} catch (Exception ex) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			
			responseError = new ErrorResponse();
			responseError.setExceptionId("SVR1000");
			responseError.setExceptionText("Generic Server Error: " + ex.getMessage());
			responseError.setMoreInfo("There was a problem in the Service Providers network that prevented to carry out the request");			
			responseError.setUserMessage("Generic Server Fault");
			
			apiErrorResponse = new ApiErrorResponse<ErrorResponse>(responseError);
		}
		Gson gson = new Gson();
	    String json = gson.toJson(apiErrorResponse);
	    String cadena = "{\"body\":";
	    int posicion = json.indexOf(cadena);
	    if (posicion > -1) {
	    	json = json.substring(posicion + cadena.length());
	    	json = json.substring(0, json.length() - 1);	    
	    }
	    
		return ResponseEntity.status(status)
				.header("UNICA-ServiceId", UNICA_ServiceId)
				.header("UNICA-PID", UNICA_PID)
				.header("Content-Type", "application/json;charset=UTF-8")
				.body(json);
	}
}
