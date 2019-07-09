package pe.telefonica.provision.api.response;

import java.util.Date;

public class ProvisionHeaderResponse {
	
	private int code;
	private String datetime;
	private String message;

	public int getCode() {
		return code;
	}
	public ProvisionHeaderResponse setCode(int code) {
		this.code = code;
		return this;
	}
	public String getDatetime() {
		return datetime;
	}
	public ProvisionHeaderResponse setDatetime(String datetime) {
		this.datetime = datetime;
		return this;
	}
	public String getMessage() {
		return message;
	}
	public ProvisionHeaderResponse setMessage(String message) {
		this.message = message;
		return this;
	}
	public ProvisionHeaderResponse() {
		super();
		this.datetime = new Date().toString();
	}
	
}
