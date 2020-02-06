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
	private String commercialOp;
	private String customerName;
	private String customerDocumentType;
	private String customerDocumentNumber;
	private String customerLatitude;
	private String customerLongitude;

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

	public boolean getIsUpdatedummyStPsiCode() {
		return isUpdatedummyStPsiCode;
	}

	public void setUpdatedummyStPsiCode(boolean isUpdatedummyStPsiCode) {
		this.isUpdatedummyStPsiCode = isUpdatedummyStPsiCode;
	}

	public String getCommercialOp() {
		return commercialOp;
	}

	public void setCommercialOp(String commercialOp) {
		this.commercialOp = commercialOp;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getCustomerDocumentType() {
		return customerDocumentType;
	}

	public void setCustomerDocumentType(String customerDocumentType) {
		this.customerDocumentType = customerDocumentType;
	}

	public String getCustomerDocumentNumber() {
		return customerDocumentNumber;
	}

	public void setCustomerDocumentNumber(String customerDocumentNumber) {
		this.customerDocumentNumber = customerDocumentNumber;
	}

	public String getCustomerLatitude() {
		return customerLatitude;
	}

	public void setCustomerLatitude(String customerLatitude) {
		this.customerLatitude = customerLatitude;
	}

	public String getCustomerLongitude() {
		return customerLongitude;
	}

	public void setCustomerLongitude(String customerLongitude) {
		this.customerLongitude = customerLongitude;
	}

}
