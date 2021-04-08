package pe.telefonica.provision.external.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class TokenResponse {

	@JsonProperty("sendCount")
	private String value;

	private String phone;
}
