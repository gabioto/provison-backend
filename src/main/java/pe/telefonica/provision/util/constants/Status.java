package pe.telefonica.provision.util.constants;

public enum Status {
	
	IN_TOA("IN_TOA", 1), 
	SCHEDULED("SCHEDULED", 2),
	WO_PRESTART("WO_PRESTART", 3), 
	WO_INIT("WO_INIT", 4),
	WO_COMPLETED("WO_COMPLETED", 5), 
	WO_CANCEL("WO_CANCEL", 6), 
	FICTICIOUS_SCHEDULED("FICTICIOUS_SCHEDULED",7);

	private String statusName;
	private int statusId;
	//EError.ERROR_CONNECTION.getMessage()

	private Status(String statusName, int statusId) {
		this.statusName = statusName;
		this.statusId = statusId;
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
}
