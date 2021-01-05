package pe.telefonica.provision.external.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class TokenResponse {

	private String idParam;

	private String key;

	private String value;
}
