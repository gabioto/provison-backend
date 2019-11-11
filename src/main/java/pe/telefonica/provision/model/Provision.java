package pe.telefonica.provision.model;

import java.io.Serializable;
import java.time.LocalDate;
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

	@Field("xa_id_st_fict")
	private String xaIdStFict;

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

	@Field("sale_code")
	private String saleCode;
	
	@Field("sale_request_date")
	private String saleRequestDate;
	
	@Field("sale_register_date")
	private String saleRegisterDate;
	
	@Field("sale_channel")
	private String saleChannel;
	
	@Field("protected_data")
	private String protectedData;
	
	@Field("kafka_date_send")
	private String kafkaDateSend;

	@Field("product_name")
	private String productName;
	
	@Field("product_name_source")
	private String productNameSource;
	
	@Field("product_type")
	private String productType;
	
	@Field("product_sub")
	private String productSub;
	
	@Field("legacies")
	private String legacies;
	
	@Field("commercial_op")
	private String commercialOp;
	
	@Field("product_code")
	private String productCode;
	
	@Field("payment_method")
	private String paymentMethod;
	
	@Field("campaign")
	private String campaign;

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

	@Field("register_date")
	private LocalDateTime registerDate = LocalDateTime.now(ZoneOffset.of("-05:00"));

	@Field("has_schedule")
	private Boolean hasSchedule = false;

	@Field("internet_detail")
	private Internet internetDetail;

	@Field("television_detail")
	private Television tvDetail;

	@Field("home_phone_detail")
	private HomePhone homePhoneDetail;

	@Field("customer")
	private Customer customer;

	@Field("contacts")
	private List<Contacts> contacts;

	@Field("work_zone")
	private String workZone;

	@Field("registro_actualizado")
	private LocalDateTime updatedDate = LocalDateTime.now(ZoneOffset.of("-05:00"));

	@Field("last_tracking_status")
	private String lastTrackingStatus;

	@Field("log_status")
	private List<StatusLog> logStatus = new ArrayList<StatusLog>();

	
	
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

	public String getXaIdStFict() {
		return xaIdStFict;
	}

	public void setXaIdStFict(String xaIdStFict) {
		this.xaIdStFict = xaIdStFict;
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

	public String getSaleCode() {
		return saleCode;
	}

	public void setSaleCode(String saleCode) {
		this.saleCode = saleCode;
	}

	public String getSaleRequestDate() {
		return saleRequestDate;
	}

	public void setSaleRequestDate(String saleRequestDate) {
		this.saleRequestDate = saleRequestDate;
	}

	public String getSaleRegisterDate() {
		return saleRegisterDate;
	}

	public void setSaleRegisterDate(String saleRegisterDate) {
		this.saleRegisterDate = saleRegisterDate;
	}

	public String getSaleChannel() {
		return saleChannel;
	}

	public void setSaleChannel(String saleChannel) {
		this.saleChannel = saleChannel;
	}

	public String getProtectedData() {
		return protectedData;
	}

	public void setProtectedData(String protectedData) {
		this.protectedData = protectedData;
	}

	public String getKafkaDateSend() {
		return kafkaDateSend;
	}

	public void setKafkaDateSend(String kafkaDateSend) {
		this.kafkaDateSend = kafkaDateSend;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductNameSource() {
		return productNameSource;
	}

	public void setProductNameSource(String productNameSource) {
		this.productNameSource = productNameSource;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getProductSub() {
		return productSub;
	}

	public void setProductSub(String productSub) {
		this.productSub = productSub;
	}

	public String getLegacies() {
		return legacies;
	}

	public void setLegacies(String legacies) {
		this.legacies = legacies;
	}

	public String getCommercialOp() {
		return commercialOp;
	}

	public void setCommercialOp(String commercialOp) {
		this.commercialOp = commercialOp;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getCampaign() {
		return campaign;
	}

	public void setCampaign(String campaign) {
		this.campaign = campaign;
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

	public LocalDateTime getRegisterDate() {
		return registerDate;
	}

	public void setRegisterDate(LocalDateTime registerDate) {
		this.registerDate = registerDate;
	}

	public Boolean getHasSchedule() {
		return hasSchedule;
	}

	public void setHasSchedule(Boolean hasSchedule) {
		this.hasSchedule = hasSchedule;
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

	public List<Contacts> getContacts() {
		return contacts;
	}

	public void setContacts(List<Contacts> contacts) {
		this.contacts = contacts;
	}

	public String getWorkZone() {
		return workZone;
	}

	public void setWorkZone(String workZone) {
		this.workZone = workZone;
	}

	public LocalDateTime getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(LocalDateTime updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getLastTrackingStatus() {
		return lastTrackingStatus;
	}

	public void setLastTrackingStatus(String lastTrackingStatus) {
		this.lastTrackingStatus = lastTrackingStatus;
	}

	public List<StatusLog> getLogStatus() {
		return logStatus;
	}

	public void setLogStatus(List<StatusLog> logStatus) {
		this.logStatus = logStatus;
	}

	public Provision() {

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
				+ statusToa + ", validatedAddress=" + validatedAddress + ", registerDate=" + registerDate
				+ ", hasSchedule=" + hasSchedule + ", internetDetail=" + internetDetail + ", tvDetail=" + tvDetail
				+ ", homePhoneDetail=" + homePhoneDetail + ", customer=" + customer + ", workZone=" + workZone + "]";
	}

	public static class StatusLog {

		@Field("status")
		private String status;

		@Field("inserted_date")
		private LocalDateTime insertedDate = LocalDateTime.now(ZoneOffset.of("-05:00"));

		@Field("scheduled_date")
		private LocalDate scheduledDate = LocalDate.now(ZoneOffset.of("-05:00"));

		@Field("scheduled_range")
		private String scheduledRange;

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public LocalDateTime getInsertedDate() {
			return insertedDate;
		}

		public void setInsertedDate(LocalDateTime insertedDate) {
			this.insertedDate = insertedDate;
		}

		public LocalDate getScheduledDate() {
			return scheduledDate;
		}

		public void setScheduledDate(LocalDate scheduledDate) {
			this.scheduledDate = scheduledDate;
		}

		public String getScheduledRange() {
			return scheduledRange;
		}

		public void setScheduledRange(String scheduledRange) {
			this.scheduledRange = scheduledRange;
		}

	}

}
