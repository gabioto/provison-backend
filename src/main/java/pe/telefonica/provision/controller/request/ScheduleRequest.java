package pe.telefonica.provision.controller.request;

import pe.telefonica.provision.model.Customer;

public class ScheduleRequest extends LogDataFrontendRequest {

	private String requestId;
	private String requestType;
	private String requestName;
	private String selectedDate;
	private String selectedRange;
	private boolean isPilot;
	private String orderCode;
	private String stpsiCode;
	private String xaOrderCode;
	private Customer customer;

	/*public ScheduleRequest(String requestId, String requestType, String requestName, String selectedDate, String selectedRange, String isPilot, String orderCode, String stpsiCode) {
		super();
		this.requestId = requestId;
		this.requestType = requestType;
		this.requestName = requestName;
		this.selectedDate = selectedDate;
		this.selectedRange = selectedRange;
		this.isPilot = isPilot;
		this.orderCode = orderCode;
		this.stpsiCode = stpsiCode;
	}*/

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

	public String getRequestName() {
		return requestName;
	}

	public void setRequestName(String requestName) {
		this.requestName = requestName;
	}

	public String getSelectedDate() {
		return selectedDate;
	}

	public void setSelectedDate(String selectedDate) {
		this.selectedDate = selectedDate;
	}

	public String getSelectedRange() {
		return selectedRange;
	}

	public void setSelectedRange(String selectedRange) {
		this.selectedRange = selectedRange;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public String getStpsiCode() {
		return stpsiCode;
	}

	public void setStpsiCode(String stpsiCode) {
		this.stpsiCode = stpsiCode;
	}

	public boolean isPilot() {
		return isPilot;
	}

	public void setPilot(boolean isPilot) {
		this.isPilot = isPilot;
	}

	public String getXaOrderCode() {
		return xaOrderCode;
	}

	public void setXaOrderCode(String xaOrderCode) {
		this.xaOrderCode = xaOrderCode;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

}
