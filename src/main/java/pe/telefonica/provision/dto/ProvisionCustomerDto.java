package pe.telefonica.provision.dto;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import pe.telefonica.provision.model.Customer;

@Document(collection = "collProvision")
@JsonPropertyOrder({ "idProvision" })
public class ProvisionCustomerDto {
	
	@Field("_id")
	private String idProvision;

	@Field("customer")
	private Customer customer;	
	

	public String getIdProvision() {
		return idProvision;
	}

	public void setIdProvision(String idProvision) {
		this.idProvision = idProvision;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

}
