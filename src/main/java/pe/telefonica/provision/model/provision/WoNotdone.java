package pe.telefonica.provision.model.provision;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Field;

public class WoNotdone implements Serializable {
	
	private static final long serialVersionUID = 3775714898258466530L;
	
	@Field("a_not_done_type_install")
	private String aNotDoneTypeInstall;
	
	@Field("a_not_done_reason_install")
	private String aNotDoneReasonInstall;

	@Field("a_not_done_sub_reason_install")
	private String aNotDoneSubReasonInstall;
	
	@Field("a_not_done_type_repair")
	private String aNotDoneTypeRepair;
	
	@Field("a_not_done_area")
	private String aNotDoneArea;
	
	@Field("a_not_done_reason_repair")
	private String aNotDoneReasonRepair;
	
	@Field("a_observation")
	private String aObservation;
	
	@Field("user_notdone")
	private String userNotdone;
	
	public String getaNotDoneReasonInstall() {
		return aNotDoneReasonInstall;
	}

	public void setaNotDoneReasonInstall(String aNotDoneReasonInstall) {
		this.aNotDoneReasonInstall = aNotDoneReasonInstall;
	}

	public String getaNotDoneSubReasonInstall() {
		return aNotDoneSubReasonInstall;
	}

	public void setaNotDoneSubReasonInstall(String aNotDoneSubReasonInstall) {
		this.aNotDoneSubReasonInstall = aNotDoneSubReasonInstall;
	}

	public String getaNotDoneTypeRepair() {
		return aNotDoneTypeRepair;
	}

	public void setaNotDoneTypeRepair(String aNotDoneTypeRepair) {
		this.aNotDoneTypeRepair = aNotDoneTypeRepair;
	}

	public String getaNotDoneArea() {
		return aNotDoneArea;
	}

	public void setaNotDoneArea(String aNotDoneArea) {
		this.aNotDoneArea = aNotDoneArea;
	}

	public String getaNotDoneReasonRepair() {
		return aNotDoneReasonRepair;
	}

	public void setaNotDoneReasonRepair(String aNotDoneReasonRepair) {
		this.aNotDoneReasonRepair = aNotDoneReasonRepair;
	}

	public String getaObservation() {
		return aObservation;
	}

	public void setaObservation(String aObservation) {
		this.aObservation = aObservation;
	}

	public String getUserNotdone() {
		return userNotdone;
	}

	public void setUserNotdone(String userNotdone) {
		this.userNotdone = userNotdone;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getaNotDoneTypeInstall() {
		return aNotDoneTypeInstall;
	}

	public void setaNotDoneTypeInstall(String aNotDoneTypeInstall) {
		this.aNotDoneTypeInstall = aNotDoneTypeInstall;
	}

}
