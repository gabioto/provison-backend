package pe.telefonica.provision.controller.request;


public class SetContactInfoUpdateRequest extends LogDataFrontendRequest{
	
	private String provisionId;
	private String contactFullname;
	private String contactCellphone;
	private Boolean contactCellphoneIsMovistar;
	
	public String getProvisionId() {
		return provisionId;
	}
	public void setProvisionId(String provisionId) {
		this.provisionId = provisionId;
	}
	public String getContactFullname() {
		return contactFullname;
	}
	public void setContactFullname(String contactFullname) {
		this.contactFullname = contactFullname;
	}
	public String getContactCellphone() {
		return contactCellphone;
	}
	public void setContactCellphone(String contactCellphone) {
		this.contactCellphone = contactCellphone;
	}
	public Boolean getContactCellphoneIsMovistar() {
		return contactCellphoneIsMovistar;
	}
	public void setContactCellphoneIsMovistar(Boolean contactCellphoneIsMovistar) {
		this.contactCellphoneIsMovistar = contactCellphoneIsMovistar;
	}
}