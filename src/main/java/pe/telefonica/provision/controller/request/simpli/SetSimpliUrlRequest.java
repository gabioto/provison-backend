package pe.telefonica.provision.controller.request.simpli;

import java.time.LocalTime;

public class SetSimpliUrlRequest {
	private String tracking;
	private String xa_peticion;
	private String xa_activity_type;
	private String xa_requirement_number;
	private String appt_number;
	private String eta;

	public String getTracking() {
		return tracking;
	}

	public void setTracking(String tracking) {
		this.tracking = tracking;
	}

	public String getXa_peticion() {
		return xa_peticion;
	}

	public void setXa_peticion(String xa_peticion) {
		this.xa_peticion = xa_peticion;
	}

	public String getXa_activity_type() {
		return xa_activity_type;
	}

	public void setXa_activity_type(String xa_activity_type) {
		this.xa_activity_type = xa_activity_type;
	}

	public String getXa_requirement_number() {
		return xa_requirement_number;
	}

	public void setXa_requirement_number(String xa_requirement_number) {
		this.xa_requirement_number = xa_requirement_number;
	}

	public String getAppt_number() {
		return appt_number;
	}

	public void setAppt_number(String appt_number) {
		this.appt_number = appt_number;
	}

	public String getEta() {
		return eta;
	}

	public void setEta(String eta) {
		this.eta = eta;
	}

}
