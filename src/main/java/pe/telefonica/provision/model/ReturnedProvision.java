package pe.telefonica.provision.model;

import org.springframework.data.mongodb.core.mapping.Field;

public class ReturnedProvision {

	@Field("cod_reason")
	private String codReason;

	@Field("sub_reason")
	private String subReason;

	@Field("action")
	private String action;

	public String getCodReason() {
		return codReason;
	}

	public void setCodReason(String codReason) {
		this.codReason = codReason;
	}

	public String getSubReason() {
		return subReason;
	}

	public void setSubReason(String subReason) {
		this.subReason = subReason;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

}
