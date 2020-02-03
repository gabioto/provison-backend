package pe.telefonica.provision.controller.request;

import java.time.LocalDate;

import pe.telefonica.provision.model.Customer;

public class InsertCodeFictionalRequest {
	private String saleCode;
	private Integer dummyXaRequest;
	private String dummyStPsiCode;
	private LocalDate scheduleDate;
	private String scheduleRange;
	private String bucket;
	private String originCode;
	private boolean isUpdatedummyStPsiCode;
	private Customer customer;
	private String commercialOp;
	private String productType;

	public String getSaleCode() {
		return saleCode;
	}

	public void setSaleCode(String saleCode) {
		this.saleCode = saleCode;
	}

	public Integer getDummyXaRequest() {
		return dummyXaRequest;
	}

	public void setDummyXaRequest(Integer dummyXaRequest) {
		this.dummyXaRequest = dummyXaRequest;
	}

	public String getDummyStPsiCode() {
		return dummyStPsiCode;
	}

	public void setDummyStPsiCode(String dummyStPsiCode) {
		this.dummyStPsiCode = dummyStPsiCode;
	}

	public LocalDate getScheduleDate() {
		return scheduleDate;
	}

	public void setScheduleDate(LocalDate scheduleDate) {
		this.scheduleDate = scheduleDate;
	}

	public String getScheduleRange() {
		return scheduleRange;
	}

	public void setScheduleRange(String scheduleRange) {
		this.scheduleRange = scheduleRange;
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getOriginCode() {
		return originCode;
	}

	public void setOriginCode(String originCode) {
		this.originCode = originCode;
	}

	public boolean isUpdatedummyStPsiCode() {
		return isUpdatedummyStPsiCode;
	}

	public void setUpdatedummyStPsiCode(boolean isUpdatedummyStPsiCode) {
		this.isUpdatedummyStPsiCode = isUpdatedummyStPsiCode;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public String getCommercialOp() {
		return commercialOp;
	}

	public void setCommercialOp(String commercialOp) {
		this.commercialOp = commercialOp;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

}
