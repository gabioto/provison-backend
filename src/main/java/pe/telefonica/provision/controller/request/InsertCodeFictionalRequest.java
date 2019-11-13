package pe.telefonica.provision.controller.request;

import org.springframework.data.mongodb.core.mapping.Field;

public class InsertCodeFictionalRequest {
	private String saleCode;
	private String FictionalCode;
	private String scheduleDate;
	private String scheduleRange;

	public String getSaleCode() {
		return saleCode;
	}

	public void setSaleCode(String saleCode) {
		this.saleCode = saleCode;
	}

	public String getFictionalCode() {
		return FictionalCode;
	}

	public void setFictionalCode(String fictionalCode) {
		FictionalCode = fictionalCode;
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

}
