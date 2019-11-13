package pe.telefonica.provision.model.provision;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Field;

public class WoInit implements Serializable {
	
	private static final long serialVersionUID = 3775714898258466530L;
	
	@Field("nameResource")
	private String nameResource;
	
	@Field("xa_creation_date")
	private String xaCreationDate;
	
	@Field("eta_start_time")
	private String etaStartTime;
	
	@Field("eta_end_time")
	private String etaEndTime;
	
	@Field("xa_note")
	private String xaNote;
	
	@Field("date")
	private String date;
	
	@Field("comment_Technician")
	private String commentTechnician;

	public String getNameResource() {
		return nameResource;
	}

	public void setNameResource(String nameResource) {
		this.nameResource = nameResource;
	}

	public String getXaCreationDate() {
		return xaCreationDate;
	}

	public void setXaCreationDate(String xaCreationDate) {
		this.xaCreationDate = xaCreationDate;
	}

	public String getEtaStartTime() {
		return etaStartTime;
	}

	public void setEtaStartTime(String etaStartTime) {
		this.etaStartTime = etaStartTime;
	}

	public String getEtaEndTime() {
		return etaEndTime;
	}

	public void setEtaEndTime(String etaEndTime) {
		this.etaEndTime = etaEndTime;
	}

	public String getXaNote() {
		return xaNote;
	}

	public void setXaNote(String xaNote) {
		this.xaNote = xaNote;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getCommentTechnician() {
		return commentTechnician;
	}

	public void setCommentTechnician(String commentTechnician) {
		this.commentTechnician = commentTechnician;
	}
	
	
	
	
}
