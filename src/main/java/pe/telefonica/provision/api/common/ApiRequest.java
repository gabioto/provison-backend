package pe.telefonica.provision.api.common;

public class ApiRequest<T> {
	
	private RequestHeader header;
	private T body;
	
	public ApiRequest(String appName, String user, String operation, T body) {
		RequestHeader header = new RequestHeader();
		header.setAppName(appName);
		header.setUser(user);
		header.setOperation(operation);
		
		setHeader(header);
		setBody(body);
	}
	
	public RequestHeader getHeader() {
		return header;
	}
	public void setHeader(RequestHeader header) {
		this.header = header;
	}
	public T getBody() {
		return body;
	}
	public void setBody(T body) {
		this.body = body;
	}
}
