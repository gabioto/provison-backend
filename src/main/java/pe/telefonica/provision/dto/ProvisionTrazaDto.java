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

import pe.telefonica.provision.model.Contacts;

import pe.telefonica.provision.model.UpFront;
import pe.telefonica.provision.model.rating.Rating;

@Document(collection = "collProvision")
@JsonPropertyOrder({ "idProvision" })
public class ProvisionTrazaDto implements Serializable {
	
	private static final long serialVersionUID = 379605915051581891L;

	@Field("action_not_done")
	private String actionNotDone;
	
	@Field("active_status")
	private String activeStatus;

	@Field("activity_type")
	private String activityType = "provision";
	
	@Field("components")
	private List<ComponentsDto> components = new ArrayList<ComponentsDto>();
	
	@Field("contacts")
	private List<Contacts> contacts= new ArrayList<Contacts>();
	
	@Field("customer")
	private CustomerDto customer;
	
	@Field("dummy_st_psi_code")
	private String dummyStPsiCode;

	@Field("front_speech")
	private String frontSpeech = "";
	
	@Field("generic_speech")
	private String genericSpeech = "";
	
	@Id
	@Field("_id")
	private String idProvision;
	
	@Field("is_up_front")
	private Boolean isUpFront = false;
	
	@Field("is_update_dummy_st_psi_code")
	private boolean isUpdatedummyStPsiCode;
	
	@Field("product_name")
	private String productName;
	
	@Field("product_type")
	private String productType;
	
	@Field("rating")
	private List<Rating> rating = new ArrayList<>();
	
	@Field("register_date")
	private LocalDateTime registerDate = LocalDateTime.now(ZoneOffset.of("-05:00"));
	
	@Field("sale_code")
	private String saleCode;
	
	@Field("show_location")
	private String showLocation;
	
	@Field("sub_reason_not_done")
	private String subReasonNotDone;
	
	@Field("up_front")
	private UpFront upFront;

	@Field("wo_prestart")
	private WoPreStartDto woPreStart;
	
	@Field("work_zone")
	private String workZone;
	
	@Field("xa_id_st")
	private String xaIdSt;
	
	@Field("xa_request")
	private String xaRequest;
	
	public String getActionNotDone() {
		return actionNotDone;
	}

	public String getActiveStatus() {
		return activeStatus;
	}

	public String getActivityType() {
		return activityType;
	}

	public List<ComponentsDto> getComponents() {
		return components;
	}

	public List<Contacts> getContacts() {
		return contacts;
	}

	public CustomerDto getCustomer() {
		return customer;
	}

	public String getDummyStPsiCode() {
		return dummyStPsiCode;
	}

	public String getFrontSpeech() {
		return frontSpeech;
	}

	public String getGenericSpeech() {
		return genericSpeech;
	}

	public String getIdProvision() {
		return idProvision;
	}

	public boolean getIsUpdatedummyStPsiCode() {
		return isUpdatedummyStPsiCode;
	}

	public String getProductName() {
		return productName;
	}

	public String getProductType() {
		return productType;
	}

	public List<Rating> getRating() {
		return rating;
	}

	public LocalDateTime getRegisterDate() {
		return registerDate;
	}

	public String getSaleCode() {
		return saleCode;
	}

	public String getShowLocation() {
		return showLocation;
	}

	public String getSubReasonNotDone() {
		return subReasonNotDone;
	}

	public UpFront getUpFront() {
		return upFront;
	}

	public WoPreStartDto getWoPreStart() {
		return woPreStart;
	}

	public String getWorkZone() {
		return workZone;
	}

	public String getXaIdSt() {
		return xaIdSt;
	}

	public String getXaRequest() {
		return xaRequest;
	}

	public void setActionNotDone(String actionNotDone) {
		this.actionNotDone = actionNotDone;
	}

	public void setActiveStatus(String activeStatus) {
		this.activeStatus = activeStatus;
	}

	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}

	public void setComponents(List<ComponentsDto> components) {
		this.components = components;
	}

	public void setContacts(List<Contacts> contacts) {
		this.contacts = contacts;
	}

	public void setCustomer(CustomerDto customer) {
		this.customer = customer;
	}

	public void setDummyStPsiCode(String dummyStPsiCode) {
		this.dummyStPsiCode = dummyStPsiCode;
	}

	public void setFrontSpeech(String frontSpeech) {
		this.frontSpeech = frontSpeech;
	}

	public void setGenericSpeech(String genericSpeech) {
		this.genericSpeech = genericSpeech;
	}

	public void setIdProvision(String idProvision) {
		this.idProvision = idProvision;
	}

	public void setUpdatedummyStPsiCode(boolean isUpdatedummyStPsiCode) {
		this.isUpdatedummyStPsiCode = isUpdatedummyStPsiCode;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public void setRating(List<Rating> rating) {
		this.rating = rating;
	}

	public void setRegisterDate(LocalDateTime registerDate) {
		this.registerDate = registerDate;
	}

	public void setSaleCode(String saleCode) {
		this.saleCode = saleCode;
	}

	public void setShowLocation(String showLocation) {
		this.showLocation = showLocation;
	}

	public void setSubReasonNotDone(String subReasonNotDone) {
		this.subReasonNotDone = subReasonNotDone;
	}

	public void setUpFront(UpFront upFront) {
		this.upFront = upFront;
	}

	public void setWoPreStart(WoPreStartDto woPreStart) {
		this.woPreStart = woPreStart;
	}

	public void setWorkZone(String workZone) {
		this.workZone = workZone;
	}

	public void setXaIdSt(String xaIdSt) {
		this.xaIdSt = xaIdSt;
	}

	public Boolean getIsUpFront() {
		return isUpFront;
	}

	public void setIsUpFront(Boolean isUpFront) {
		this.isUpFront = isUpFront;
	}

	public void setXaRequest(String xaRequest) {
		this.xaRequest = xaRequest;
	}

}