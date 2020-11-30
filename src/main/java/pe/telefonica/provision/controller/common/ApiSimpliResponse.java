package pe.telefonica.provision.controller.common;

public class ApiSimpliResponse<T> {
	private T body;

	public ApiSimpliResponse() {
		super();
	}

	public ApiSimpliResponse(T body) {
		setBody(body);
	}

	public T getBody() {
		return body;
	}

	public void setBody(T body) {
		this.body = body;
	}
}
