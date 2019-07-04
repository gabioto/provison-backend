package pe.telefonica.provision.dto;

import java.io.Serializable;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Document(collection = "collProvision")
@JsonPropertyOrder({"idProvision"})
public class Provision implements Serializable {
	
	private static final long serialVersionUID = 4894729030347835498L;
	
	@Id
	@Field("_id")
	private String idProvision;
	
	@Field("external_id")
	private String externalId;
	
	@Field("xa_request")
	private String xaRequest;
	
	@Field("xa_creation_date")
	private String xaCreationDate;
	
	@Field("xa_id_st")
	private String xaIdSt;
	
	@Field("xa_requirement_number")
	private String xaRequirementNumber;
	
	@Field("appt_number")
	private String apptNumber;
	
	@Field("purchase_date")
	private String purchaseDate;
	
	@Field("purchase_hour")
	private String purchaseHour;
	
	@Field("order_code")
	private String orderCode;
	
	@Field("product_name")
	private String productName;
	
	@Field("commercial_op")
	private String commercialOp;
	
	@Field("payment_method")
	private String paymentMethod;
	
	@Field("regular_price")
	private Double regularPrice;
	
	@Field("promo_price")
	private Double promoPrice;
	
	@Field("time_promo_price")
	private Integer timePromoPrice;
	
	@Field("currency")
	private String currency;
	
	@Field("install_price")
	private String installPrice;
	
	@Field("active_status")
	private String activeStatus;
	
	@Field("status_toa")
	private String statusToa;
	
	@Field("validated_address")
	private String validatedAddress;
	
	@Field("internet_detail")
	private Internet internetDetail;
	
	@Field("television_detail")
	private Television tvDetail;
	
	@Field("home_phone_detail")
	private HomePhone homePhoneDetail;
	
	@Field("customer")
	private Customer customer;

	public String getIdProvision() {
		return idProvision;
	}

	public void setIdProvision(String idProvision) {
		this.idProvision = idProvision;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getXaRequest() {
		return xaRequest;
	}

	public void setXaRequest(String xaRequest) {
		this.xaRequest = xaRequest;
	}

	public String getXaCreationDate() {
		return xaCreationDate;
	}

	public void setXaCreationDate(String xaCreationDate) {
		this.xaCreationDate = xaCreationDate;
	}

	public String getXaIdSt() {
		return xaIdSt;
	}

	public void setXaIdSt(String xaIdSt) {
		this.xaIdSt = xaIdSt;
	}

	public String getXaRequirementNumber() {
		return xaRequirementNumber;
	}

	public void setXaRequirementNumber(String xaRequirementNumber) {
		this.xaRequirementNumber = xaRequirementNumber;
	}

	public String getApptNumber() {
		return apptNumber;
	}

	public void setApptNumber(String apptNumber) {
		this.apptNumber = apptNumber;
	}

	public String getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(String purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	public String getPurchaseHour() {
		return purchaseHour;
	}

	public void setPurchaseHour(String purchaseHour) {
		this.purchaseHour = purchaseHour;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getCommercialOp() {
		return commercialOp;
	}

	public void setCommercialOp(String commercialOp) {
		this.commercialOp = commercialOp;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public Double getRegularPrice() {
		return regularPrice;
	}

	public void setRegularPrice(Double regularPrice) {
		this.regularPrice = regularPrice;
	}

	public Double getPromoPrice() {
		return promoPrice;
	}

	public void setPromoPrice(Double promoPrice) {
		this.promoPrice = promoPrice;
	}

	public Integer getTimePromoPrice() {
		return timePromoPrice;
	}

	public void setTimePromoPrice(Integer timePromoPrice) {
		this.timePromoPrice = timePromoPrice;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getInstallPrice() {
		return installPrice;
	}

	public void setInstallPrice(String installPrice) {
		this.installPrice = installPrice;
	}

	public String getActiveStatus() {
		return activeStatus;
	}

	public void setActiveStatus(String activeStatus) {
		this.activeStatus = activeStatus;
	}

	public String getStatusToa() {
		return statusToa;
	}

	public void setStatusToa(String statusToa) {
		this.statusToa = statusToa;
	}

	public String getValidatedAddress() {
		return validatedAddress;
	}

	public void setValidatedAddress(String validatedAddress) {
		this.validatedAddress = validatedAddress;
	}

	public Internet getInternetDetail() {
		return internetDetail;
	}

	public void setInternetDetail(Internet internetDetail) {
		this.internetDetail = internetDetail;
	}

	public Television getTvDetail() {
		return tvDetail;
	}

	public void setTvDetail(Television tvDetail) {
		this.tvDetail = tvDetail;
	}

	public HomePhone getHomePhoneDetail() {
		return homePhoneDetail;
	}

	public void setHomePhoneDetail(HomePhone homePhoneDetail) {
		this.homePhoneDetail = homePhoneDetail;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Provision(){
		
	}

	@Override
	public String toString() {
		return "Provision [idProvision=" + idProvision + ", externalId=" + externalId + ", xaRequest=" + xaRequest
				+ ", xaCreationDate=" + xaCreationDate + ", xaIdSt=" + xaIdSt + ", xaRequirementNumber="
				+ xaRequirementNumber + ", apptNumber=" + apptNumber + ", purchaseDate=" + purchaseDate
				+ ", purchaseHour=" + purchaseHour + ", orderCode=" + orderCode + ", productName=" + productName
				+ ", commercialOp=" + commercialOp + ", paymentMethod=" + paymentMethod + ", regularPrice="
				+ regularPrice + ", promoPrice=" + promoPrice + ", timePromoPrice=" + timePromoPrice + ", currency="
				+ currency + ", installPrice=" + installPrice + ", activeStatus=" + activeStatus + ", statusToa="
				+ statusToa + ", validatedAddress=" + validatedAddress + ", internetDetail=" + internetDetail
				+ ", tvDetail=" + tvDetail + ", homePhoneDetail=" + homePhoneDetail + ", customer=" + customer + "]";
	}
}
