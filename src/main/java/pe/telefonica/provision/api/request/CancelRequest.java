package pe.telefonica.provision.api.request;

public class CancelRequest {

	private String requestId;
	private String requestType;

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

	public CancelRequest(String requestId, String requestType) {
		super();
		this.requestId = requestId;
		this.requestType = requestType;
	}

}
