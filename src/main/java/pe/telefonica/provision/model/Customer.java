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

	/*@Field("pat_surname")
	private String patSurname;

	@Field("mat_surname")
	private String matSurname;
	*/
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

	@Field("contact_name")
	private String contactName;

	@Field("contact_name1")
	private String contactName1;

	@Field("contact_name2")
	private String contactName2;

	@Field("contact_name3")
	private String contactName3;

	@Field("contact_phone_number")
	private Integer contactPhoneNumber;

	@Field("contact_phone_number1")
	private Integer contactPhoneNumber1;

	@Field("contact_phone_number2")
	private Integer contactPhoneNumber2;

	@Field("contact_phone_number3")
	private Integer contactPhoneNumber3;

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
	
	/*
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
*/
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

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactName1() {
		return contactName1;
	}

	public void setContactName1(String contactName1) {
		this.contactName1 = contactName1;
	}

	public String getContactName2() {
		return contactName2;
	}

	public void setContactName2(String contactName2) {
		this.contactName2 = contactName2;
	}

	public String getContactName3() {
		return contactName3;
	}

	public void setContactName3(String contactName3) {
		this.contactName3 = contactName3;
	}

	public Integer getContactPhoneNumber() {
		return contactPhoneNumber;
	}

	public void setContactPhoneNumber(Integer contactPhoneNumber) {
		this.contactPhoneNumber = contactPhoneNumber;
	}

	public Integer getContactPhoneNumber1() {
		return contactPhoneNumber1;
	}

	public void setContactPhoneNumber1(Integer contactPhoneNumber1) {
		this.contactPhoneNumber1 = contactPhoneNumber1;
	}

	public Integer getContactPhoneNumber2() {
		return contactPhoneNumber2;
	}

	public void setContactPhoneNumber2(Integer contactPhoneNumber2) {
		this.contactPhoneNumber2 = contactPhoneNumber2;
	}

	public Integer getContactPhoneNumber3() {
		return contactPhoneNumber3;
	}

	public void setContactPhoneNumber3(Integer contactPhoneNumber3) {
		this.contactPhoneNumber3 = contactPhoneNumber3;
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
