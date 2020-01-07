package pe.telefonica.provision.util.constants;

public enum Status {

	PENDIENTE("PENDIENTE", 1, "PENDIENTE"),
	INGRESADO("INGRESADO", 2, "INGRESADO"),
	CAIDO("CAIDO", 3, "CAIDO"),
	
	DUMMY_IN_TOA("DUMMY_IN_TOA", 4, "DUMMY_IN_TOA"),
	IN_TOA("IN_TOA", 5, "IN_TOA"), 
	WO_PRESTART("WO_PRESTART", 6, "WO_PRESTART"),
	WO_INIT("WO_INIT", 7, "WO_INIT"), 
	WO_COMPLETED("WO_COMPLETED", 8, "WO_COMPLETED"),
	
	SCHEDULED("SCHEDULED", 9, "SCHEDULED"), 
	FICTICIOUS_SCHEDULED("FICTICIOUS_SCHEDULED", 10, "FICTICIOUS_SCHEDULED"),
	
	WO_CANCEL("WO_CANCEL", 11, "WO_CANCEL"),
	WO_RESCHEDULE("WO_RESCHEDULE", 12, "WO_RESCHEDULE"),
	WO_NOTDONE("WO_NOTDONE", 13, "WO_NOTDONE");
	
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
