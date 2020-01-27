package pe.telefonica.provision.external.request;

import pe.telefonica.provision.model.Customer;

public class ScheduleUpdatePSICodeRealRequest {
	private String orderCode;
	private String xaOrderCode;
	private String stPsiCode;
	private String requestId;
	private String requestType;
	private Customer customer;

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public String getXaOrderCode() {
		return xaOrderCode;
	}

	public void setXaOrderCode(String xaOrderCode) {
		this.xaOrderCode = xaOrderCode;
	}

	public String getStPsiCode() {
		return stPsiCode;
	}

	public void setStPsiCode(String stPsiCode) {
		this.stPsiCode = stPsiCode;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

}
