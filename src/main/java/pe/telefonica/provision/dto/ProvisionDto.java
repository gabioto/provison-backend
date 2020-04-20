package pe.telefonica.provision.dto;

import java.util.ArrayList;
import java.util.List;

import pe.telefonica.provision.model.Contacts;
import pe.telefonica.provision.model.Customer;

import pe.telefonica.provision.model.UpFront;
import pe.telefonica.provision.model.Provision.StatusLog;

public class ProvisionDto {

	private String idProvision;

	private String xaRequest;

	private String xaIdSt;

	private String originCode;

	private String productName;

	private String activeStatus;

	private Customer customer;

	private List<Contacts> contacts;

	private String workZone;

	private String lastTrackingStatus;

	private String descriptionStatus;

	private String genericSpeech;

	private List<StatusLog> logStatus = new ArrayList<StatusLog>();

	private UpFront upFront;

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
