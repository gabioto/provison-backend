package pe.telefonica.provision.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@Document(collection = "collProvision")
@JsonPropertyOrder({ "idProvision" })
public class ProvisionTrazaDto implements Serializable {

	private static final long serialVersionUID = 379605915051581891L;

	@Id
	@Field("_id")
	private String idProvision;

	@Field("active_status")
	private String activeStatus;

	@Field("activity_type")
	private String activityType = "provision";

	@Field("product_name")
	private String productName;

	@Field("product_type")
	private String productType;

	@Field("register_date")
	private LocalDateTime registerDate;

	public String getIdProvision() {
		return idProvision;
	}

	public void setIdProvision(String idProvision) {
		this.idProvision = idProvision;
	}

	public String getActiveStatus() {
		return activeStatus;
	}

	public void setActiveStatus(String activeStatus) {
		this.activeStatus = activeStatus;
	}

	public String getActivityType() {
		return activityType;
	}

	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public LocalDateTime getRegisterDate() {
		return registerDate;
	}

	public void setRegisterDate(LocalDateTime registerDate) {
		this.registerDate = registerDate;
	}

}