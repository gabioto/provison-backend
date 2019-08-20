package pe.telefonica.provision.service.response;

import pe.telefonica.provision.api.response.ResponseHeader;

public class GetPSITokenResponse {
	
	private ResponseHeader header;
	private PSIToken psiToken;
	
	public ResponseHeader getHeader() {
		return header;
	}
	public void setHeader(ResponseHeader header) {
		this.header = header;
	}
	public PSIToken getPSIToken() {
		return psiToken;
	}
	public void setPSIToken(PSIToken psiToken) {
		this.psiToken = psiToken;
	}

}
