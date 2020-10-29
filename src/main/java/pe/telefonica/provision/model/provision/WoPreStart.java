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
	private Boolean availableTracking = false;

	@Field("eta")
	private String eta;

	@Field("register_date")
	private LocalDateTime registerDate = LocalDateTime.now(ZoneOffset.of("-05:00"));

	public String getNameResource() {
		return nameResource;
	}

	public void setNameResource(String nameResource) {
		this.nameResource = nameResource;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTechnicalId() {
		return technicalId;
	}

	public void setTechnicalId(String technicalId) {
		this.technicalId = technicalId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getTrackingUrl() {
		return trackingUrl;
	}

	public void setTrackingUrl(String trackingUrl) {
		this.trackingUrl = trackingUrl;
	}

	public Boolean getAvailableTracking() {
		return availableTracking;
	}

	public void setAvailableTracking(Boolean availableTracking) {
		this.availableTracking = availableTracking;
	}

	public String getEta() {
		return eta;
	}

	public void setEta(String eta) {
		this.eta = eta;
	}

	public LocalDateTime getRegisterDate() {
		return registerDate;
	}

	public void setRegisterDate(LocalDateTime registerDate) {
		this.registerDate = registerDate;
	}

}
