package pe.telefonica.provision.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.repository.ProvisionRepository;
import pe.telefonica.provision.service.TokenService;

@Service
public class TokenServiceImpl implements TokenService{

	@Autowired
	private ProvisionRepository provisionRepository;
	
	@Override
	public ResponseEntity<Object> sendToken(String code) {
		Optional<Provision> optProvision = provisionRepository.getProvisionById(code);
		return null;
	}

}
