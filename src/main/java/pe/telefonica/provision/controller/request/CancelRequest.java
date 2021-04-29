package pe.telefonica.provision.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CancelRequest extends LogDataFrontendRequest {

	private String requestId;
	private String requestType;
	private String stPsiCode;
	private boolean flgFicticious;
	private String scheduler;
	private String reason;
	private String reasonCode;
	private String origin;
	private String author;

}
