package pe.telefonica.provision.model;

import java.io.Serializable;
import java.time.LocalDate;

import org.springframework.data.mongodb.core.mapping.Field;

public class ProvisionScheduler implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4277394474450626062L;

	@Field("idProvision")
	private String idProvision;
	
	@Field("scheduleDate")
	private String scheduleDate;
	
	@Field("scheduleRange")
	private String scheduleRange;

	public String getIdProvision() {
		return idProvision;
	}

	public void setIdProvision(String idProvision) {
		this.idProvision = idProvision;
	}

	public String getScheduleDate() {
		return scheduleDate;
	}

	public void setScheduleDate(String scheduleDate) {
		this.scheduleDate = scheduleDate;
	}

	public String getScheduleRange() {
		return scheduleRange;
	}

	public void setScheduleRange(String scheduleRange) {
		this.scheduleRange = scheduleRange;
	}
	
	
}
