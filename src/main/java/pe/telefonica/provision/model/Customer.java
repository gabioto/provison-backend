package pe.telefonica.provision.model;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Field;

public class Customer implements Serializable{
	
	private static final long serialVersionUID = 3275714898258466530L;
	
	@Field("document_type")
	private String documentType;
	
	@Field("document_number")
	private String documentNumber;
	
	@Field("name")
	private String name;
	
	@Field("pat_surname")
	private String patSurname;
	
	@Field("mat_surname")
	private String matSurname;
	
	@Field("carrier")
	private String carrier;
	
	@Field("contact_carrier")
	private String contactCarrier;
	
	@Field("mail")
	private String mail;
	
	@Field("phone_number")
	private Integer phoneNumber;
	
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
	
	@Field("contact_name")
	private String contactName;
	
	@Field("contact_phone_number")
	private Integer contactPhoneNumber;
	
	@Field("latitude")
	private Double latitude;
	
	@Field("longitude")
	private Double longitude;
	
	private String productName;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPatSurname() {
		return patSurname;
	}

	public void setPatSurname(String patSurname) {
		this.patSurname = patSurname;
	}

	public String getMatSurname() {
		return matSurname;
	}

	public void setMatSurname(String matSurname) {
		this.matSurname = matSurname;
	}

	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public String getContactCarrier() {
		return contactCarrier;
	}

	public void setContactCarrier(String contactCarrier) {
		this.contactCarrier = contactCarrier;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public Integer getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(Integer phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public Integer getContactPhoneNumber() {
		return contactPhoneNumber;
	}

	public void setContactPhoneNumber(Integer contactPhoneNumber) {
		this.contactPhoneNumber = contactPhoneNumber;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}
}
