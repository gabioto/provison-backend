package pe.telefonica.provision.controller.request;


public class InsertCodeFictionalRequest {
	private String saleCode;
	private String fictionalCode;
	private String scheduleDate;
	private String scheduleRange;
	private String bucket;

	public String getSaleCode() {
		return saleCode;
	}

	public void setSaleCode(String saleCode) {
		this.saleCode = saleCode;
	}

	public String getFictionalCode() {
		return fictionalCode;
	}

	public void setFictionalCode(String fictionalCode) {
		this.fictionalCode = fictionalCode;
	}

	public String getScheduleDate() {
		return scheduleDate;
	}

	public void setScheduleDate(String scheduleDate) {
		this.scheduleDate = scheduleDate;
	}

	public String getScheduleRange() {
		return scheduleRange;
	}

	public void setScheduleRange(String scheduleRange) {
		this.scheduleRange = scheduleRange;
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	
	

}
