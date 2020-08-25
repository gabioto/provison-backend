package pe.telefonica.provision.external.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PSIWorkResponse {

	@JsonProperty("solicitud")
	private String request;

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

}
