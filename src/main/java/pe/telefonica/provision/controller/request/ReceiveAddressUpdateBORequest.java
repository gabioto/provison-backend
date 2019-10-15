package pe.telefonica.provision.controller.request;

public class ReceiveAddressUpdateBORequest {

	private String action;
	private Boolean isSMSRequired;
	private String provisionId;
	private String newDepartment;
	private String newProvince;
	private String newDistrict;
	private String newAddress;
	private String newReference;
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getProvisionId() {
		return provisionId;
	}
	public void setProvisionId(String provisionId) {
		this.provisionId = provisionId;
	}
	public String getNewDepartment() {
		return newDepartment;
	}
	public void setNewDepartment(String newDepartment) {
		this.newDepartment = newDepartment;
	}
	public String getNewProvince() {
		return newProvince;
	}
	public void setNewProvince(String newProvince) {
		this.newProvince = newProvince;
	}
	public String getNewDistrict() {
		return newDistrict;
	}
	public void setNewDistrict(String newDistrict) {
		this.newDistrict = newDistrict;
	}
	public String getNewAddress() {
		return newAddress;
	}
	public void setNewAddress(String newAddress) {
		this.newAddress = newAddress;
	}
	public String getNewReference() {
		return newReference;
	}
	public void setNewReference(String newReference) {
		this.newReference = newReference;
	}
	public Boolean getIsSMSRequired() {
		return isSMSRequired;
	}
	public void setIsSMSRequired(Boolean isSMSRequired) {
		this.isSMSRequired = isSMSRequired;
	}
	@Override
	public String toString() {
		return "ReceiveAddressUpdateBORequest [action=" + action + ", isSMSRequired=" + isSMSRequired + ", provisionId="
				+ provisionId + ", newDepartment=" + newDepartment + ", newProvince=" + newProvince + ", newDistrict="
				+ newDistrict + ", newAddress=" + newAddress + ", newReference=" + newReference + "]";
	}
}
