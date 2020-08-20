package pe.telefonica.provision.controller.request.temporary;

public class GetSaleCodeByXaRequestAndSTRequest {
	private String xaRequest;
	private String stPsiCode;

	public String getXaRequest() {
		return xaRequest;
	}

	public String getStPsiCode() {
		return stPsiCode;
	}

	public void setXaRequest(String xaRequest) {
		this.xaRequest = xaRequest;
	}

	public void setStPsiCode(String stPsiCode) {
		this.stPsiCode = stPsiCode;
	}

}
