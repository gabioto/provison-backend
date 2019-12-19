package pe.telefonica.provision.controller.request;

public class CancelOrderRequest extends LogDataFrontendRequest {

	private String provisionId;
	private String cause;
	private String detail;

	public String getProvisionId() {
		return provisionId;
	}

	public void setProvisionId(String provisionId) {
		this.provisionId = provisionId;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}
}
