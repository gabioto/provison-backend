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

	private String scheduler;

	private String priority;

	private String customerType;

	private String customerSubType;

	private String phoneNetworkTechnology;

	private String phoneTechnology;

	private String broadbandNetworkTechnology;

	private String broadbandTechnology;

	private String tvNetworkTechnology;

	private String tvTechnology;

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

	public String getScheduler() {
		return scheduler;
	}

	public String getPriority() {
		return priority;
	}

	public String getCustomerType() {
		return customerType;
	}

	public String getCustomerSubType() {
		return customerSubType;
	}

	public String getPhoneNetworkTechnology() {
		return phoneNetworkTechnology;
	}

	public String getPhoneTechnology() {
		return phoneTechnology;
	}

	public String getBroadbandNetworkTechnology() {
		return broadbandNetworkTechnology;
	}

	public String getBroadbandTechnology() {
		return broadbandTechnology;
	}

	public String getTvNetworkTechnology() {
		return tvNetworkTechnology;
	}

	public String getTvTechnology() {
		return tvTechnology;
	}

	public void setScheduler(String scheduler) {
		this.scheduler = scheduler;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public void setCustomerType(String customerType) {
		this.customerType = customerType;
	}

	public void setCustomerSubType(String customerSubType) {
		this.customerSubType = customerSubType;
	}

	public void setPhoneNetworkTechnology(String phoneNetworkTechnology) {
		this.phoneNetworkTechnology = phoneNetworkTechnology;
	}

	public void setPhoneTechnology(String phoneTechnology) {
		this.phoneTechnology = phoneTechnology;
	}

	public void setBroadbandNetworkTechnology(String broadbandNetworkTechnology) {
		this.broadbandNetworkTechnology = broadbandNetworkTechnology;
	}

	public void setBroadbandTechnology(String broadbandTechnology) {
		this.broadbandTechnology = broadbandTechnology;
	}

	public void setTvNetworkTechnology(String tvNetworkTechnology) {
		this.tvNetworkTechnology = tvNetworkTechnology;
	}

	public void setTvTechnology(String tvTechnology) {
		this.tvTechnology = tvTechnology;
	}

}
