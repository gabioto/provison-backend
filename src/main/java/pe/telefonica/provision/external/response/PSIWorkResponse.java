package pe.telefonica.provision.external.response;

//import com.fasterxml.jackson.annotation.JsonProperty;

public class PSIWorkResponse {

	//@JsonProperty("solicitud")
	//private String request;
	
	private String id;
	private String correlationId;
	private String startDate;
	private String endDate;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCorrelationId() {
		return correlationId;
	}
	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
	

//	public String getRequest() {
//		return request;
//	}
//
//	public void setRequest(String request) {
//		this.request = request;
//	}

}
