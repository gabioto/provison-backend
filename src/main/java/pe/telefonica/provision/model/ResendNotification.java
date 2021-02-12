package pe.telefonica.provision.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Field;

public class ResendNotification implements Serializable {

	private static final long serialVersionUID = 3275714898258466523L;
	
	@Field("intoa_count")
	private Integer intoaCount;

	@Field("intoa_send_date")
	private LocalDateTime intoaSendDate;

	@Field("intoa_send_notify")
	private boolean intoaSendNotify;

	
	public Integer getIntoaCount() {
		return intoaCount;
	}

	public void setIntoaCount(Integer intoaCount) {
		this.intoaCount = intoaCount;
	}

	public LocalDateTime getIntoaSendDate() {
		return intoaSendDate;
	}

	public void setIntoaSendDate(LocalDateTime intoaSendDate) {
		this.intoaSendDate = intoaSendDate;
	}

	public boolean isIntoaSendNotify() {
		return intoaSendNotify;
	}

	public void setIntoaSendNotify(boolean intoaSendNotify) {
		this.intoaSendNotify = intoaSendNotify;
	}

	public ResendNotification() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "ResendNotification [intoaCount=" + intoaCount + ", intoaSendDate=" + intoaSendDate
				+ ", intoaSendNotify=" + intoaSendNotify + "]";
	}
	
	
	
	

}
