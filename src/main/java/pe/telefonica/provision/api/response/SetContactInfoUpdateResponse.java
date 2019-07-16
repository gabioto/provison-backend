package pe.telefonica.provision.api.response;

import pe.telefonica.provision.dto.Provision;

public class SetContactInfoUpdateResponse {

	private ResponseHeader header;
	private Provision result;

	public ResponseHeader getHeader() {
		return header;
	}

	public void setHeader(ResponseHeader header) {
		this.header = header;
	}

	public Provision getResult() {
		return result;
	}

	public void setResult(Provision result) {
		this.result = result;
	}

}