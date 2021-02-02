package pe.telefonica.provision.model;

public enum StatusAtis {

	FINALIZADO("FI", "FINALIZADO"), 
	TERMINADA("TE", "TERMINADA"),
	CANCELADA_ATIS("CG", "CANCELADA_ATIS"),
	PENDIENTE_DE_VALIDACION("PV", "PENDIENTE_DE_VALIDACION"), 
	CONFIGURADA("CE", "CONFIGURADA"),
	PENDIENTE_DE_APROBACION("PD", "PENDIENTE_DE_APROBACION");

	private String statusNameAtis;
	private String statusNameTraza;

	private StatusAtis(String statusName, String statusTraza) {
		this.statusNameAtis = statusName;
		this.statusNameTraza = statusTraza;
	}

	public String getStatusNameAtis() {
		return statusNameAtis;
	}

	public String getStatusNameTraza() {
		return statusNameTraza;
	}

	public void setStatusNameAtis(String statusNameAtis) {
		this.statusNameAtis = statusNameAtis;
	}

	public void setStatusNameTraza(String statusNameTraza) {
		this.statusNameTraza = statusNameTraza;
	}

}
