package pe.telefonica.provision.repository;

import java.util.Optional;

import pe.telefonica.provision.controller.common.ApiResponse;
import pe.telefonica.provision.model.OAuthToken;

public interface OAuthTokenRepository {
	Optional<OAuthToken> getOAuthToke();
	void insertToken(OAuthToken oAuthToken);
	boolean updateToken(ApiResponse<OAuthToken> apiResponse);
	
}
