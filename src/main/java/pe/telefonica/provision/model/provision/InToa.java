package pe.telefonica.provision.model.provision;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Field;

public class InToa implements Serializable {
	private static final long serialVersionUID = 3775714898258466530L;

	@Field("xa_note")
	private String xaNote;

	@Field("date")
	private String date;

	@Field("xa_scheduler")
	private String xaScheduler;

	@Field("xa_creation_date")
	private String xaCreationDate;

	@Field("longitude")
	private String longitude;

	@Field("latitude")
	private String latitude;

	public String getXaNote() {
		return xaNote;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setXaNote(String xaNote) {
		this.xaNote = xaNote;
	}

	public String getXaScheduler() {
		return xaScheduler;
	}

	public void setXaScheduler(String xaScheduler) {
		this.xaScheduler = xaScheduler;
	}

	public String getXaCreationDate() {
		return xaCreationDate;
	}

	public void setXaCreationDate(String xaCreationDate) {
		this.xaCreationDate = xaCreationDate;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

}
