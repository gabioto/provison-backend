package pe.telefonica.provision.controller.request;

public class UpdateStatusRequest extends LogDataFrontendRequest {

	private String stringSource;
	private String status;
	private String xaRequest;
	private String xaIdSt;

	public String getStringSource() {
		return stringSource;
	}

	public void setStringSource(String stringSource) {
		this.stringSource = stringSource;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getXaRequest() {
		return xaRequest;
	}

	public void setXaRequest(String xaRequest) {
		this.xaRequest = xaRequest;
	}

	public String getXaIdSt() {
		return xaIdSt;
	}

	public void setXaIdSt(String xaIdSt) {
		this.xaIdSt = xaIdSt;
	}
}
