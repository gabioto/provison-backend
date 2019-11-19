package pe.telefonica.provision.external.request;

public class ScheduleUpdateFicticiousRequest {
	private String originCode;
	private Integer fictitiousCode;
	private String orderCode;
	private String saleCode;

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

}
