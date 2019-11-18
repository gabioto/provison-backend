package pe.telefonica.provision.util.constants;

public enum Status {

	PENDIENTE("PENDIENTE", 1, "PENDIENTE"),
	INGRESADO("INGRESADO", 2, "INGRESADO"),
	CAIDO("CAIDO", 3, "CAIDO"),
	
	IN_TOA("IN_TOA", 4, "IN_TOA"), 
	WO_PRESTART("WO_PRESTART", 5, "WO_PRESTART"),
	WO_INIT("WO_INIT", 6, "WO_INIT"), 
	WO_COMPLETED("WO_COMPLETED", 7, "WO_COMPLETED"),
	WO_CANCEL("WO_CANCEL", 8, "WO_CANCEL"),
	
	SCHEDULED("SCHEDULED", 9, "SCHEDULED"), 
	FICTICIOUS_SCHEDULED("FICTICIOUS_SCHEDULED", 10, "FICTICIOUS_SCHEDULED");
	
	private String statusName;
	private int statusId;
	private String description;
	// EError.ERROR_CONNECTION.getMessage()

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
