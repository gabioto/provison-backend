package pe.telefonica.provision.model;

public class StatusProvision {

	protected String label;
	protected String xaRequest;
	protected String xaIdSt;
	protected String externalId;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
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

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
}
