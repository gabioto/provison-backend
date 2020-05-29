package pe.telefonica.provision.model.provision;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.data.mongodb.core.mapping.Field;

public class WoPreStart implements Serializable {

	private static final long serialVersionUID = 3375714898258466530L;

	@Field("nameResource")
	private String nameResource;

	@Field("date")
	private String date;

	// Tech Info

	@Field("technical_id")
	private String technicalId;

	@Field("full_name")
	private String fullName;

	@Field("document_number")
	private String documentNumber;

	@Field("phone_number")
	private String phoneNumber;

	@Field("latitude")
	private String latitude;

	@Field("longitude")
	private String longitude;

	// live tracking
	@Field("tracking_url")
	private String trackingUrl;

	@Field("available_tracking")
	private Boolean availableTracking;

	@Field("register_date")
	private LocalDateTime registerDate = LocalDateTime.now(ZoneOffset.of("-05:00"));

	public String getNameResource() {
		return nameResource;
	}

	public String getDate() {
		return date;
	}

	public String getTechnicalId() {
		return technicalId;
	}

	public String getFullName() {
		return fullName;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public String getTrackingUrl() {
		return trackingUrl;
	}

	public Boolean getAvailableTracking() {
		return availableTracking;
	}

	public LocalDateTime getRegisterDate() {
		return registerDate;
	}

	public void setNameResource(String nameResource) {
		this.nameResource = nameResource;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setTechnicalId(String technicalId) {
		this.technicalId = technicalId;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public void setTrackingUrl(String trackingUrl) {
		this.trackingUrl = trackingUrl;
	}

	public void setAvailableTracking(Boolean availableTracking) {
		this.availableTracking = availableTracking;
	}

	public void setRegisterDate(LocalDateTime registerDate) {
		this.registerDate = registerDate;
	}

}
