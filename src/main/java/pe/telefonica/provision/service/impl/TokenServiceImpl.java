package pe.telefonica.provision.service.impl;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pe.telefonica.provision.controller.common.ApiResponse;
import pe.telefonica.provision.external.TrazabilidadSecurityApi;
import pe.telefonica.provision.external.response.TokenResponse;
import pe.telefonica.provision.model.Contacts;
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.Provision.StatusLog;
import pe.telefonica.provision.repository.ProvisionRepository;
import pe.telefonica.provision.service.TokenService;
import pe.telefonica.provision.util.constants.Constants;
import pe.telefonica.provision.util.exception.FunctionalErrorException;

@Service
public class TokenServiceImpl implements TokenService {

	@Autowired
	private ProvisionRepository provisionRepository;

	@Autowired
	TrazabilidadSecurityApi trazabilidadSecurityApi;

	@Override
	public ResponseEntity<Object> sendToken(String code) {
		Optional<Provision> optProvision = provisionRepository.getAllProvisionById(code);

		try {
			if (optProvision.isPresent()) {
				boolean validPhoneNumber = validatePhoneNumber(optProvision.get().getCustomer().getPhoneNumber());
				if (validPhoneNumber) {
					TokenResponse response = trazabilidadSecurityApi.sendLoginToken(optProvision.get().getCustomer());
				
				return new ResponseEntity<Object>(new ApiResponse<TokenResponse>(Constants.APP_NAME_PROVISION,
						Constants.OPER_SEND_TOKEN, String.valueOf(HttpStatus.OK.value()), "OK", response),
						HttpStatus.OK);
				} else {
					
				return new ResponseEntity<Object>(new ApiResponse<TokenResponse>(Constants.APP_NAME_PROVISION,
						Constants.OPER_SEND_TOKEN, String.valueOf(HttpStatus.NOT_ACCEPTABLE.value()),
						"El número de telefono no es válido", null), HttpStatus.NOT_ACCEPTABLE);				
				}
			} else {
				return new ResponseEntity<Object>(
						new ApiResponse<TokenResponse>(Constants.APP_NAME_PROVISION, Constants.OPER_SEND_TOKEN,
								String.valueOf(HttpStatus.BAD_REQUEST.value()), "No se encontró la provision", null),
						HttpStatus.BAD_REQUEST);
			}
		} catch (FunctionalErrorException e) {
			return new ResponseEntity<Object>(
					new ApiResponse<TokenResponse>(Constants.APP_NAME_PROVISION, Constants.OPER_SEND_TOKEN,
							String.valueOf(HttpStatus.FORBIDDEN.value()), e.getMessage(), null),
					HttpStatus.FORBIDDEN);
		}
	}

	private boolean validatePhoneNumber(String phone) {

		return phone.matches("[0-9]+") && phone.length() == 9 && phone.substring(0, 1).equals("9");
	}
}
