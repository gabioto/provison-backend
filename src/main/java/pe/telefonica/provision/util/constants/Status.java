package pe.telefonica.provision.util.constants;

public enum Status {

	IN_TOA("IN_TOA", 1,"IN_TOA"), SCHEDULED("SCHEDULED", 2,"SCHEDULED"), WO_PRESTART("WO_PRESTART", 3,"WO_PRESTART"), WO_INIT("WO_INIT", 4,"WO_INIT"),
	WO_COMPLETED("WO_COMPLETED", 5,"WO_COMPLETED"), WO_CANCEL("WO_CANCEL", 6,"WO_CANCEL"), FICTICIOUS_SCHEDULED("FICTICIOUS_SCHEDULED",7,"FICTICIOUS_SCHEDULED");

	private String statusName;
	private int statusId;
	private String description;
	//EError.ERROR_CONNECTION.getMessage()

	private Status(String statusName, int statusId, String description) {
		this.statusName = statusName;
		this.statusId = statusId;
		this.description = description;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
