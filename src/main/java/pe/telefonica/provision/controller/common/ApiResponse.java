package pe.telefonica.provision.controller.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import pe.telefonica.provision.conf.Constants;

public class ApiResponse<T> {

	private ResponseHeader header;
	private T body;

	public ApiResponse(String appName, String operation, String resultCode, String message, T body) {
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.API_TIMESTAMP_FORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT-5:00"));

		ResponseHeader header = new ResponseHeader();
		header.setAppName(appName);
		header.setDateTime(sdf.format(Calendar.getInstance().getTime()));
		header.setResultCode(resultCode);
		header.setMessage(message);

		setHeader(header);
		setBody(body);
	}

	public ResponseHeader getHeader() {
		return header;
	}

	public void setHeader(ResponseHeader header) {
		this.header = header;
	}

	public T getBody() {
		return body;
	}

	public void setBody(T body) {
		this.body = body;
	}

	public ApiResponse() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
