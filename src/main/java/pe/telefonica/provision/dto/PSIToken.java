package pe.telefonica.provision.dto;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "psiToken")
public class PSIToken implements Serializable{

	private static final long serialVersionUID = 6458500171831568645L;
	
	@Id
	@Field("_id")
	private String id;
	@Field(value = "psi_key")
	private String psiKey;
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
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
