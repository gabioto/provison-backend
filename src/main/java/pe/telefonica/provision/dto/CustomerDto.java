package pe.telefonica.provision.dto;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Field;

import pe.telefonica.provision.model.Customer;

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

	public CustomerDto fromCustomer(Customer customer) {
		this.documentType = customer.getDocumentType();
		this.documentNumber = customer.getDocumentNumber();
		this.name = customer.getName();
		this.carrier = customer.getCarrier();
		this.mail = customer.getMail();
		this.phoneNumber = customer.getPhoneNumber();
		this.district = customer.getDistrict();
		this.province = customer.getProvince();
		this.department = customer.getDepartment();
		this.address = customer.getAddress();
		this.reference = customer.getReference();
		return this;
	}
}