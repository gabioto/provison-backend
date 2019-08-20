package pe.telefonica.provision.service.response;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Document(collection = "psiToken")
@JsonPropertyOrder({"id"})
public class PSIToken implements Serializable{

	private static final long serialVersionUID = 6458500171831568645L;
	
	@Id
	@Field("_id")
	private String id;
	@Field(value = "psi_key")
	private String psiKey;
	@Field(value = "ouath2_token")
	private String ouath2Token;
	@Field(value = "refresh_token")
	private String refreshToken;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPsiKey() {
		return psiKey;
	}
	public void setPsiKey(String psiKey) {
		this.psiKey = psiKey;
	}
	public String getOuath2Token() {
		return ouath2Token;
	}
	public void setOuath2Token(String ouath2Token) {
		this.ouath2Token = ouath2Token;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
