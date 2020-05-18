package pe.telefonica.provision.controller.common;


public class NotificationResponse {
	
	private Long sms_into;
	private Long sms_prestart;
	private Long sms_notdone;
	private Long sms_completed;

	@Override
	public String toString() {
		return "NotificationResponse [sms_into=" + sms_into + ", sms_prestart=" + sms_prestart + ", sms_notdone="
				+ sms_notdone + ", sms_completed=" + sms_completed + "]";
	}
	
	public Long getSms_into() {
		return sms_into;
	}
	public void setSms_into(Long sms_into) {
		this.sms_into = sms_into;
	}
	public Long getSms_prestart() {
		return sms_prestart;
	}
	public void setSms_prestart(Long sms_prestart) {
		this.sms_prestart = sms_prestart;
	}
	public Long getSms_notdone() {
		return sms_notdone;
	}
	public void setSms_notdone(Long sms_notdone) {
		this.sms_notdone = sms_notdone;
	}
	public Long getSms_completed() {
		return sms_completed;
	}
	public void setSms_completed(Long sms_completed) {
		this.sms_completed = sms_completed;
	}
	
	
	

}
