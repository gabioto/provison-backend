package pe.telefonica.provision.controller.common;

public class ApiErrorResponse<T> {
	
	private T body;
	
	public ApiErrorResponse() {
		super();
	}

	public ApiErrorResponse(T body) {		
		setBody(body);
	}
	public T getBody() {
		return body;
	}
	public void setBody(T body) {
		this.body = body;
	}
}
