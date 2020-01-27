package pe.telefonica.provision.external.request;

public class ScheduleUpdateFicticiousRequest {
	private String originCode;
	private Integer fictitiousCode;
	private String orderCode;
	private String saleCode;
	private String requestName;
	private String requestId;

	public String getOriginCode() {
		return originCode;
	}

	public void setOriginCode(String originCode) {
		this.originCode = originCode;
	}

	public Integer getFictitiousCode() {
		return fictitiousCode;
	}

	public void setFictitiousCode(Integer fictitiousCode) {
		this.fictitiousCode = fictitiousCode;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public String getSaleCode() {
		return saleCode;
	}

	public void setSaleCode(String saleCode) {
		this.saleCode = saleCode;
	}

	public String getRequestName() {
		return requestName;
	}

	public void setRequestName(String requestName) {
		this.requestName = requestName;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	@Override
	public String toString() {
		return "ScheduleUpdateFicticiousRequest [originCode=" + originCode + ", fictitiousCode=" + fictitiousCode
				+ ", orderCode=" + orderCode + ", saleCode=" + saleCode + ", requestName=" + requestName
				+ ", requestId=" + requestId + "]";
	}

}
