package pe.telefonica.provision.api.response;

public class RequestAddressUpdateResponse {

	private ResponseHeader header;
	private Boolean result;
	
	public ResponseHeader getHeader() {
		return header;
	}
	public void setHeader(ResponseHeader header) {
		this.header = header;
	}
	public Boolean getResult() {
		return result;
	}
	public void setResult(Boolean result) {
		this.result = result;
	}
	
}
