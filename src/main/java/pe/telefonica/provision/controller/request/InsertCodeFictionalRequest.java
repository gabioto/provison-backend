package pe.telefonica.provision.controller.request;

import java.time.LocalDate;

public class InsertCodeFictionalRequest {
	private String saleCode;
	private String fictionalCode;
	private LocalDate scheduleDate;
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

	public LocalDate getScheduleDate() {
		return scheduleDate;
	}

	public void setScheduleDate(LocalDate scheduleDate) {
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
