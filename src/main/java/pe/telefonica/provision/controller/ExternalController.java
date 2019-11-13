package pe.telefonica.provision.controller;

import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.common.ApiResponse;
import pe.telefonica.provision.controller.request.InsertOrderRequest;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.util.constants.Constants;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("external")
public class ExternalController {
	private static final Log log = LogFactory.getLog(ExternalController.class);
	
	
	
}
