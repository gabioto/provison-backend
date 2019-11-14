package pe.telefonica.provision.model;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Field;

public class Customer implements Serializable {

	private static final long serialVersionUID = 3275714898258466530L;

	@Field("document_type")
	private String documentType;

	@Field("document_number")
	private String documentNumber;

	@Field("name")
	private String name;

	@Field("carrier")
	private String carrier;

	@Field("contact_carrier")
	private String contactCarrier;

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

	@Field("origin_data")
	private String originData;

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

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
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

	public String getOriginData() {
		return originData;
	}

	public void setOriginData(String originData) {
		this.originData = originData;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

}
