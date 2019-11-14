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

import pe.telefonica.provision.model.provision.InToa;
import pe.telefonica.provision.model.provision.WoComplete;
import pe.telefonica.provision.model.provision.WoInit;
import pe.telefonica.provision.model.provision.WoPreStart;

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

	@Field("xa_requirement_number")
	private String xaRequirementNumber;

	@Field("appt_number")
	private String apptNumber;

	@Field("xa_number_service_order")
	private String xaNumberServiceOrder;

	@Field("xa_number_work_order")
	private String xaNumberWorkOrder;

	@Field("activity_type")
	private String activityType;

	@Field("xa_creation_date")
	private String xaCreationDate;

	@Field("xa_id_st")
	private String xaIdSt;

	@Field("dummy_st_psi_code")
	private String dummyStPsiCode;

	@Field("back")
	private String back;

	@Field("sale_source")
	private String saleSource;

	@Field("sale_code")
	private String saleCode;

	@Field("sale_request_date")
	private String saleRequestDate;

	@Field("sale_register_date")
	private String saleRegisterDate;

	@Field("channel_entered")
	private String channelEntered;

	@Field("protected_data")
	private String protectedData;

	@Field("code_ps_code")
	private String codePsCode;

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

	@Field("product_internal_equipment")
	private String productInternalEquipment;

	@Field("product_signal")
	private String productSignal;

	@Field("product_ps_admin")
	private List<String> productPsAdmin;

	@Field("sva_code")
	private List<String> svaCode;

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
	private String regularPrice;

	@Field("promo_price")
	private String promoPrice;

	@Field("time_promo_price")
	private Integer timePromoPrice;

	@Field("currency")
	private String currency;

	@Field("install_price")
	private String installPrice;

	@Field("install_price_month")
	private String installPriceMonth;

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
	
	
	@Field("in_toa")
	private InToa inToa;
	
	@Field("wo_prestart")
	private WoPreStart woPreStart;
	
	@Field("in_init")
	private WoInit woInit;
	
	@Field("wo_complete")
	private WoComplete woComplete;
	
	
	
	
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

	public String getXaNumberServiceOrder() {
		return xaNumberServiceOrder;
	}

	public void setXaNumberServiceOrder(String xaNumberServiceOrder) {
		this.xaNumberServiceOrder = xaNumberServiceOrder;
	}

	public String getXaNumberWorkOrder() {
		return xaNumberWorkOrder;
	}

	public void setXaNumberWorkOrder(String xaNumberWorkOrder) {
		this.xaNumberWorkOrder = xaNumberWorkOrder;
	}

	public String getActivityType() {
		return activityType;
	}

	public void setActivityType(String activityType) {
		this.activityType = activityType;
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

	public String getDummyStPsiCode() {
		return dummyStPsiCode;
	}

	public void setDummyStPsiCode(String dummyStPsiCode) {
		this.dummyStPsiCode = dummyStPsiCode;
	}

	public String getBack() {
		return back;
	}

	public void setBack(String back) {
		this.back = back;
	}

	public String getSaleSource() {
		return saleSource;
	}

	public void setSaleSource(String saleSource) {
		this.saleSource = saleSource;
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

	public String getChannelEntered() {
		return channelEntered;
	}

	public void setChannelEntered(String channelEntered) {
		this.channelEntered = channelEntered;
	}

	public String getProtectedData() {
		return protectedData;
	}

	public void setProtectedData(String protectedData) {
		this.protectedData = protectedData;
	}

	public String getCodePsCode() {
		return codePsCode;
	}

	public void setCodePsCode(String codePsCode) {
		this.codePsCode = codePsCode;
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

	public String getProductInternalEquipment() {
		return productInternalEquipment;
	}

	public void setProductInternalEquipment(String productInternalEquipment) {
		this.productInternalEquipment = productInternalEquipment;
	}

	public String getProductSignal() {
		return productSignal;
	}

	public void setProductSignal(String productSignal) {
		this.productSignal = productSignal;
	}

	public List<String> getProductPsAdmin() {
		return productPsAdmin;
	}

	public void setProductPsAdmin(List<String> productPsAdmin) {
		this.productPsAdmin = productPsAdmin;
	}

	public List<String> getSvaCode() {
		return svaCode;
	}

	public void setSvaCode(List<String> svaCode) {
		this.svaCode = svaCode;
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

	public String getRegularPrice() {
		return regularPrice;
	}

	public void setRegularPrice(String regularPrice) {
		this.regularPrice = regularPrice;
	}

	public String getPromoPrice() {
		return promoPrice;
	}

	public void setPromoPrice(String promoPrice) {
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

	public String getInstallPriceMonth() {
		return installPriceMonth;
	}

	public void setInstallPriceMonth(String installPriceMonth) {
		this.installPriceMonth = installPriceMonth;
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
	
	
	public InToa getInToa() {
		return inToa;
	}

	public void setInToa(InToa inToa) {
		this.inToa = inToa;
	}

	public WoPreStart getWoPrestart() {
		return woPreStart;
	}

	public void setWoPrestart(WoPreStart woPrestart) {
		this.woPreStart = woPrestart;
	}

	public WoInit getWoInit() {
		return woInit;
	}

	public void setWoInit(WoInit woInit) {
		this.woInit = woInit;
	}

	public WoComplete getWoComplete() {
		return woComplete;
	}

	public void setWoComplete(WoComplete woComplete) {
		this.woComplete = woComplete;
	}

	public Provision() {

	}

	@Override
	public String toString() {
		return "Provision [idProvision=" + idProvision + ", externalId=" + externalId + ", xaRequest=" + xaRequest
				+ ", xaCreationDate=" + xaCreationDate + ", xaIdSt=" + xaIdSt + ", xaRequirementNumber="
				+ xaRequirementNumber + ", apptNumber=" + apptNumber + ", productName=" + productName
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
		private String scheduledDate; /*= LocalDate.now(ZoneOffset.of("-05:00"));*/

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

		public String getScheduledDate() {
			return scheduledDate;
		}

		public void setScheduledDate(String scheduledDate) {
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
