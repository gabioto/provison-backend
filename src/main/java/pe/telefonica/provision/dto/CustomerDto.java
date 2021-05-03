package pe.telefonica.provision.dto;

import java.io.Serializable;
import org.springframework.data.mongodb.core.mapping.Field;

public class CustomerDto implements Serializable {
	
	private static final long serialVersionUID = -8640547575248724968L;

	@Field("document_type")
	private String documentType;

	@Field("document_number")
	private String documentNumber;

	@Field("name")
	private String name;

	@Field("carrier")
	private boolean carrier;

	@Field("mail")
	private String mail;

	@Field("phone_number")
	private String phoneNumber;

	@Field("district")
	private String district;

	@Field("province")
	private String province;

	@Field("department")
	private String department;

	@Field("address")
	private String address;

	@Field("reference")
	private String reference;
	
	@Field("latitude")
	private String latitude;

	@Field("longitude")
	private String longitude;

	@Field("product_name")
	private String productName;

	@Field("contact_carrier")
	private String contactCarrier;
	
	@Field("contact_name")
	private String contactName;
	
	@Field("contact_phone_number")
	private String contactPhoneNumber;
	
	private Boolean hasProvisions;
	
	private Boolean hasFaults;

	public String getDocumentType() {
		return documentType;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public String getName() {
		return name;
	}

	public boolean isCarrier() {
		return carrier;
	}

	public String getMail() {
		return mail;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getDistrict() {
		return district;
	}

	public String getProvince() {
		return province;
	}

	public String getDepartment() {
		return department;
	}

	public String getAddress() {
		return address;
	}

	public String getReference() {
		return reference;
	}

	public String getProductName() {
		return productName;
	}

	public String getContactCarrier() {
		return contactCarrier;
	}

	public String getContactName() {
		return contactName;
	}

	public String getContactPhoneNumber() {
		return contactPhoneNumber;
	}

	public Boolean getHasProvisions() {
		return hasProvisions;
	}

	public Boolean getHasFaults() {
		return hasFaults;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCarrier(boolean carrier) {
		this.carrier = carrier;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public void setContactCarrier(String contactCarrier) {
		this.contactCarrier = contactCarrier;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public void setContactPhoneNumber(String contactPhoneNumber) {
		this.contactPhoneNumber = contactPhoneNumber;
	}

	public void setHasProvisions(Boolean hasProvisions) {
		this.hasProvisions = hasProvisions;
	}

	public void setHasFaults(Boolean hasFaults) {
		this.hasFaults = hasFaults;
	}
		
}