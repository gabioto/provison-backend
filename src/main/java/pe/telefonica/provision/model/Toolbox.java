package pe.telefonica.provision.model;

import java.io.Serializable;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Document(collection = "collToolbox")
@JsonPropertyOrder({ "idParam" })
public class Toolbox implements Serializable {

	private static final long serialVersionUID = 4894729030347812498L;

	@Id
	@Field("_id")
	private String idToolbox;
		
	@Field("document_type")
	private String documentType;
	
	@Field("document_number")
	private String documentNumber;
	
	@Field("phone_number")
	private String phoneNumber;
	
	@Field("carrier")
	private boolean carrier;
	
	@Field("chart")
	private boolean chart;

	public String getIdToolbox() {
		return idToolbox;
	}

	public void setIdToolbox(String idToolbox) {
		this.idToolbox = idToolbox;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
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

	public boolean isCarrier() {
		return carrier;
	}

	public void setCarrier(boolean carrier) {
		this.carrier = carrier;
	}

	public boolean isChart() {
		return chart;
	}

	public void setChart(boolean chart) {
		this.chart = chart;
	}
}
