package pe.telefonica.provision.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.telefonica.provision.model.Customer;

@Getter
@Setter
@NoArgsConstructor
public class CustomerRequest {

	private String documentType;

	private String documentNumber;

	private String fullName;

	private String patSurname;

	private String matSurname;

	private String email;

	private String cellphone;

	private Boolean cellphoneIsMovistar;

	private String address;

	private String productName;
	
	public CustomerRequest fromCustomer(Customer customer) {
		this.fullName = customer.getName();
		this.cellphone = customer.getPhoneNumber();
		this.documentNumber = customer.getDocumentNumber();
		this.documentType = customer.getDocumentType();
		this.email = customer.getMail();
		this.cellphoneIsMovistar = customer.getCarrier();
		this.address = customer.getAddress();
		return this;
	}
}