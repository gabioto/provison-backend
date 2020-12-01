package pe.telefonica.provision.controller.request.simpli;

public class SetSimpliUrlRequest {
	private String Tracking;
	private String xa_peticion;
	private String xa_activity_type;
	private String xa_requirement_number;
	private String apptNumber;
	private String ETA;

	public String getTracking() {
		return Tracking;
	}

	public String getXa_peticion() {
		return xa_peticion;
	}

	public String getXa_activity_type() {
		return xa_activity_type;
	}

	public String getXa_requirement_number() {
		return xa_requirement_number;
	}

	public String getApptNumber() {
		return apptNumber;
	}

	public String getETA() {
		return ETA;
	}

	public void setTracking(String tracking) {
		Tracking = tracking;
	}

	public void setXa_peticion(String xa_peticion) {
		this.xa_peticion = xa_peticion;
	}

	public void setXa_activity_type(String xa_activity_type) {
		this.xa_activity_type = xa_activity_type;
	}

	public void setXa_requirement_number(String xa_requirement_number) {
		this.xa_requirement_number = xa_requirement_number;
	}

	public void setApptNumber(String apptNumber) {
		this.apptNumber = apptNumber;
	}

	public void setETA(String eta) {
		ETA = eta;
	}
}
