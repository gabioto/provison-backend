package pe.telefonica.provision.api.request;

public class ValidateDataRequest extends LogDataFrontendRequest {

	private String provisionId;

	public String getProvisionId() {
		return provisionId;
	}

	public void setProvisionId(String provisionId) {
		this.provisionId = provisionId;
	}

}
