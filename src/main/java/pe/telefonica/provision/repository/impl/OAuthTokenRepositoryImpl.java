package pe.telefonica.provision.repository.impl;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.UpdateResult;

import pe.telefonica.provision.controller.common.ApiResponse;
import pe.telefonica.provision.model.OAuthToken;
import pe.telefonica.provision.repository.OAuthTokenRepository;

@Repository
public class OAuthTokenRepositoryImpl implements OAuthTokenRepository {
	private static final Log log = LogFactory.getLog(OAuthTokenRepositoryImpl.class);
	private final MongoOperations mongoOperations;
	
	@Autowired
	public OAuthTokenRepositoryImpl(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}
	
	@Override
	public Optional<OAuthToken> getOAuthToke() {
		OAuthToken oAuthToken = null;
		try {
			oAuthToken = this.mongoOperations
					.findOne(new Query(Criteria.where("token_key").is("PARAM_KEY_OAUTH_TOKEN")), OAuthToken.class);
		} catch (Exception e) {
			log.info(e.getMessage());
		}

		Optional<OAuthToken> optionalPsiToken = Optional.ofNullable(oAuthToken);

		return optionalPsiToken;
	}
	
	@Override
	public Optional<OAuthToken> getOAuthTokeOnPremise() {
		OAuthToken oAuthToken = null;
		try {
			oAuthToken = this.mongoOperations
					.findOne(new Query(Criteria.where("token_key").is("PARAM_KEY_OAUTH_TOKEN_ON_PREMISE")), OAuthToken.class);
		} catch (Exception e) {
			log.info(e.getMessage());
		}

		Optional<OAuthToken> optionalPsiToken = Optional.ofNullable(oAuthToken);

		return optionalPsiToken;
	}

	@Override
	public void insertToken(OAuthToken oAuthToken) {
		
		this.mongoOperations.insert(oAuthToken);
	}

	@Override
	public boolean updateToken(ApiResponse<OAuthToken> apiResponse) {
		if (apiResponse.getBody() != null) {
			OAuthToken oAuthToken = apiResponse.getBody();
			Update update = new Update();
			update.set("token_type", oAuthToken.getTokenType());
			update.set("access_token", oAuthToken.getAccessToken());
			update.set("expires_in", oAuthToken.getExpiresIn());
			update.set("consented_on", oAuthToken.getConsentedOn());
			update.set("scope", oAuthToken.getScope());
			update.set("refresh_token", oAuthToken.getRefreshToken());
			update.set("refresh_token_expires_in", oAuthToken.getRefreshTokenExpiresIn());

			UpdateResult result = this.mongoOperations.updateFirst(
					new Query(Criteria.where("token_key").is("PARAM_KEY_OAUTH_TOKEN")), update, OAuthToken.class);

			return result.getMatchedCount() > 0;
		} else {
			return false;
		}
	}

}
