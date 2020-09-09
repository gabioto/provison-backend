package pe.telefonica.provision.dto;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Field;

public class WoPreStartDto implements Serializable {

	private static final long serialVersionUID = 3119564011070588284L;

	@Field("available_tracking")
	private Boolean availableTracking = false;

	@Field("document_number")
	private String documentNumber;

	@Field("full_name")
	private String fullName;

	@Field("tracking_url")
	private String trackingUrl;

	public Boolean getAvailableTracking() {
		return availableTracking;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public String getFullName() {
		return fullName;
	}

	public String getTrackingUrl() {
		return trackingUrl;
	}

	public void setAvailableTracking(Boolean availableTracking) {
		this.availableTracking = availableTracking;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setTrackingUrl(String trackingUrl) {
		this.trackingUrl = trackingUrl;
	}

}

