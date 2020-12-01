package pe.telefonica.provision.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {

	private String exceptionId;
	
	private String exceptionText;
	
	private String moreInfo;
	
	private String userMessage;
}
