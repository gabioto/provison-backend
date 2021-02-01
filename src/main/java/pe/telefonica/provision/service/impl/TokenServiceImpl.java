package pe.telefonica.provision.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pe.telefonica.provision.controller.common.ApiResponse;
import pe.telefonica.provision.external.TrazabilidadSecurityApi;
import pe.telefonica.provision.external.response.TokenResponse;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.repository.ProvisionRepository;
import pe.telefonica.provision.service.TokenService;
import pe.telefonica.provision.util.constants.Constants;

@Service
public class TokenServiceImpl implements TokenService {

	@Autowired
	private ProvisionRepository provisionRepository;

	@Autowired
	TrazabilidadSecurityApi trazabilidadSecurityApi;

	@Override
	public ResponseEntity<Object> sendToken(String code) {
		Optional<Provision> optProvision = provisionRepository.getProvisionById(code);

		if (optProvision.isPresent()) {
			TokenResponse response = trazabilidadSecurityApi.sendLoginToken(optProvision.get().getCustomer());
			return new ResponseEntity<Object>(new ApiResponse<TokenResponse>(Constants.APP_NAME_PROVISION,
					Constants.OPER_SEND_TOKEN, String.valueOf(HttpStatus.OK.value()), "OK", response), HttpStatus.OK);
		} else {
			return new ResponseEntity<Object>(
					new ApiResponse<TokenResponse>(Constants.APP_NAME_PROVISION, Constants.OPER_SEND_TOKEN,
							String.valueOf(HttpStatus.NO_CONTENT.value()), "No se encontró la provision", null),
					HttpStatus.NO_CONTENT);
		}
	}

}