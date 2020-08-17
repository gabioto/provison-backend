package pe.telefonica.provision.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import pe.telefonica.provision.model.Contacts;
import pe.telefonica.provision.model.Customer;

import pe.telefonica.provision.model.UpFront;
import pe.telefonica.provision.model.Provision.StatusLog;

@Document(collection = "collProvision")
@JsonPropertyOrder({ "idProvision" })
public class ProvisionDto {
	@Field("_id")
	private String idProvision;

	@Field("xa_request")
	private String xaRequest;

	@Field("xa_id_st")
	private String xaIdSt;

	@Field("origin_code")
	private String originCode;

	@Field("product_name")
	private String productName;

	@Field("active_status")
	private String activeStatus;

	@Field("customer")
	private Customer customer;

	@Field("contacts")
	private List<Contacts> contacts = new ArrayList<Contacts>();

	@Field("work_zone")
	private String workZone;

	@Field("last_tracking_status")
	private String lastTrackingStatus;

	@Field("description_status")
	private String descriptionStatus;

	@Field("generic_speech")
	private String genericSpeech;

	@Field("log_status")
	private List<StatusLog> logStatus = new ArrayList<StatusLog>();

	@Field("up_front")
	private UpFront upFront;

	@Field("is_up_front")
	private Boolean isUpFront = false;

	public String getIdProvision() {
		return idProvision;
	}

	public void setIdProvision(String idProvision) {
		this.idProvision = idProvision;
	}

	public String getXaRequest() {
		return xaRequest;
	}

	public void setXaRequest(String xaRequest) {
		this.xaRequest = xaRequest;
	}

	public String getXaIdSt() {
		return xaIdSt;
	}

	public void setXaIdSt(String xaIdSt) {
		this.xaIdSt = xaIdSt;
	}

	public String getOriginCode() {
		return originCode;
	}

	public void setOriginCode(String originCode) {
		this.originCode = originCode;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getActiveStatus() {
		return activeStatus;
	}

	public void setActiveStatus(String activeStatus) {
		this.activeStatus = activeStatus;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public List<Contacts> getContacts() {
		return contacts;
	}

	public void setContacts(List<Contacts> contacts) {
		this.contacts = contacts;
	}

	public String getWorkZone() {
		return workZone;
	}

	public void setWorkZone(String workZone) {
		this.workZone = workZone;
	}

	public String getLastTrackingStatus() {
		return lastTrackingStatus;
	}

	public void setLastTrackingStatus(String lastTrackingStatus) {
		this.lastTrackingStatus = lastTrackingStatus;
	}

	public String getDescriptionStatus() {
		return descriptionStatus;
	}

	public void setDescriptionStatus(String descriptionStatus) {
		this.descriptionStatus = descriptionStatus;
	}

	public String getGenericSpeech() {
		return genericSpeech;
	}

	public void setGenericSpeech(String genericSpeech) {
		this.genericSpeech = genericSpeech;
	}

	public List<StatusLog> getLogStatus() {
		return logStatus;
	}

	public void setLogStatus(List<StatusLog> logStatus) {
		this.logStatus = logStatus;
	}

	public UpFront getUpFront() {
		return upFront;
	}

	public void setUpFront(UpFront upFront) {
		this.upFront = upFront;
	}

	public Boolean getIsUpFront() {
		return isUpFront;
	}

	public void setIsUpFront(Boolean isUpFront) {
		this.isUpFront = isUpFront;
	}
		
}
