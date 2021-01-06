package pe.telefonica.provision.service;

import org.springframework.http.ResponseEntity;

public interface TokenService {

	ResponseEntity<Object> sendToken(String code);
}
