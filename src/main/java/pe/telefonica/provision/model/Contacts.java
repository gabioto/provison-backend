package pe.telefonica.provision.model;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Field;

public class Contacts implements Serializable {
	
	private static final long serialVersionUID = 3375714898258466530L;
	
	@Field("full_name")
	private String fullName;

	@Field("phone_number")
	private String phoneNumber;

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	
}
