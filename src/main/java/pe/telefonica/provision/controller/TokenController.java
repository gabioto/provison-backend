package pe.telefonica.provision.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import pe.telefonica.provision.service.TokenService;

@RestController
@Validated
@CrossOrigin(origins = "*")
@RequestMapping("provision/v1/token")

public class TokenController {

	@Autowired
	private TokenService tokenService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<Object> sendProvisionToken(
			@RequestHeader(value = "UNICA-ServiceId", required = false) String uServiceId,
			@RequestHeader(value = "UNICA-Application", required = false) String uApplication,
			@RequestHeader(value = "UNICA-PID", required = false) String uPid,
			@RequestHeader(value = "UNICA-User", required = false) String uUser,
			@RequestParam(required = true, defaultValue = "") String code) throws Exception {

		return tokenService.sendToken(code);
	}

}
