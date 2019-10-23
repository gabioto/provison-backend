package pe.telefonica.provision.controller.request;

public class CancelRequest extends LogDataFrontendRequest {

	private String requestId;
	private String requestType;
	private String stPsiCode;

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public String getStPsiCode() {
		return stPsiCode;
	}

	public void setStPsiCode(String stPsiCode) {
		this.stPsiCode = stPsiCode;
	}

	public CancelRequest(String requestId, String requestType, String stPsiCode) {
		super();
		this.requestId = requestId;
		this.requestType = requestType;
		this.stPsiCode = stPsiCode;
	}

}
