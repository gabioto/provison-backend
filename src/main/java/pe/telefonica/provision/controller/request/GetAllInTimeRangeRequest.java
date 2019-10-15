package pe.telefonica.provision.controller.request;

public class GetAllInTimeRangeRequest {
	
	private String startDateStr;
	private String endDateStr;
	
	public String getStartDateStr() {
		return startDateStr;
	}
	public void setStartDateStr(String startDateStr) {
		this.startDateStr = startDateStr;
	}
	public String getEndDateStr() {
		return endDateStr;
	}
	public void setEndDateStr(String endDateStr) {
		this.endDateStr = endDateStr;
	}
}
