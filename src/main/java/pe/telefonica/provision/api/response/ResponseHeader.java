package pe.telefonica.provision.api.response;

import java.util.Calendar;

public class ResponseHeader {
	
	private String datetime;
	private String code;
	private String message;
	
	public String getDatetime() {
		return datetime;
	}
	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public ResponseHeader generateHeader(String code, String message) {
		ResponseHeader result = new ResponseHeader();
		
		Calendar calendar = Calendar.getInstance();
		
		result.setDatetime(calendar.getTime().toLocaleString());
		result.setCode(code);
		result.setMessage(message);
		
		return result;
	}

}
