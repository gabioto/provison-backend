package pe.telefonica.provision.external.request;

public class ScheduleUpdatePSICodeRealRequest {
	private String orderCode;
	private String stPsiCode;
	private String requestId;

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public String getStPsiCode() {
		return stPsiCode;
	}

	public void setStPsiCode(String stPsiCode) {
		this.stPsiCode = stPsiCode;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	

}
