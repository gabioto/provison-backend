package pe.telefonica.provision.dto;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Document(collection = "oAuthToken")
@JsonPropertyOrder({"id"})
public class OAuthToken implements Serializable{

	private static final long serialVersionUID = 6788432646354973582L;
	
	@Id
	@Field("_id")
	private String id;
	@Field(value = "token_key")
	private String tokenKey;
	@Field("token_type")
	private String tokenType;
	@Field("access_token")
	private String accessToken;
	@Field("expires_in")
	private String expiresIn;
	@Field("consented_on")
	private String consentedOn;
	@Field("scope")
	private String scope;
	@Field("refresh_token")
	private String refreshToken;
	@Field("refresh_token_expires_in")
	private String refreshTokenExpiresIn;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTokenKey() {
		return tokenKey;
	}
	public void setTokenKey(String tokenKey) {
		this.tokenKey = tokenKey;
	}
	public String getTokenType() {
		return tokenType;
	}
	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getExpiresIn() {
		return expiresIn;
	}
	public void setExpiresIn(String expiresIn) {
		this.expiresIn = expiresIn;
	}
	public String getConsentedOn() {
		return consentedOn;
	}
	public void setConsentedOn(String consentedOn) {
		this.consentedOn = consentedOn;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public String getRefreshTokenExpiresIn() {
		return refreshTokenExpiresIn;
	}
	public void setRefreshTokenExpiresIn(String refreshTokenExpiresIn) {
		this.refreshTokenExpiresIn = refreshTokenExpiresIn;
	}
}
