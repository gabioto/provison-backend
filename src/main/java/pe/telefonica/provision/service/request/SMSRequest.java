package pe.telefonica.provision.service.request;

public class SMSRequest {
	
	private String  customerPhone;
	private Boolean customerPhoneIsMovistar;
	private String  contactPhone;
	private Boolean contactPhoneIsMovistar;
	private String  message;
	private String  webURL;
	
	public String getCustomerPhone() {
		return customerPhone;
	}
	public void setCustomerPhone(String customerPhone) {
		this.customerPhone = customerPhone;
	}
	public Boolean getCustomerPhoneIsMovistar() {
		return customerPhoneIsMovistar;
	}
	public void setCustomerPhoneIsMovistar(Boolean customerPhoneIsMovistar) {
		this.customerPhoneIsMovistar = customerPhoneIsMovistar;
	}
	public String getContactPhone() {
		return contactPhone;
	}
	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}
	public Boolean getContactPhoneIsMovistar() {
		return contactPhoneIsMovistar;
	}
	public void setContactPhoneIsMovistar(Boolean contactPhoneIsMovistar) {
		this.contactPhoneIsMovistar = contactPhoneIsMovistar;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getWebURL() {
		return webURL;
	}
	public void setWebURL(String webURL) {
		this.webURL = webURL;
	}
}
