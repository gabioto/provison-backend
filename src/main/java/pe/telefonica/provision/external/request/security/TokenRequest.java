package pe.telefonica.provision.external.request.security;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRequest {

	private String phoneNumber;

	private String carrier;

	private String token;

	private String customerName;

	private String productName;

	private String customerEmail;

	private String customerIDType;

	private String customerIDNumber;

	private String requestType;

}
