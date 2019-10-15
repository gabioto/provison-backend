package pe.telefonica.provision.service.response;

import pe.telefonica.provision.controller.response.ResponseHeader;

public class GetPSITokenResponse {
	
	private ResponseHeader header;
	private String accessToken;
	
	public ResponseHeader getHeader() {
		return header;
	}
	public void setHeader(ResponseHeader header) {
		this.header = header;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

}
