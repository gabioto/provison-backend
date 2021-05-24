package pe.telefonica.provision.controller.request;

public class ProvisionRequest extends LogDataFrontendRequest {

	private String idProvision;
	private String indicador;

	public String getIdProvision() {
		return idProvision;
	}

	public void setIdProvision(String idProvision) {
		this.idProvision = idProvision;
	}

	public String getIndicador() {
		return indicador;
	}

	public void setIndicador(String indicador) {
		this.indicador = indicador;
	}
	
}
