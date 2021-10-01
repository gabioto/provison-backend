package pe.telefonica.provision.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
		
	@Field("xa_request")
	private String xaRequest;
	
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

	@Field("url")
	private String url;
	
	@Field("register_date")
	private LocalDateTime registerDate = LocalDateTime.now(ZoneOffset.of("-05:00"));
	
	public String getXaRequest() {
		return xaRequest;
	}

	public void setXaRequest(String xaRequest) {
		this.xaRequest = xaRequest;
	}

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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}