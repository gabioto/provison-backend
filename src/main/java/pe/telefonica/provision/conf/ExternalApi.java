package pe.telefonica.provision.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "external.api")
public class ExternalApi {

	String securityUrl;
	String scheduleUrl;
	String provisionUrl;
	String boUrl;
	
	String sendSMS;
	String sendRequestToBO;
	
	public String getSendRequestToBO() {
		return sendRequestToBO;
	}
	public void setSendRequestToBO(String sendRequestToBO) {
		this.sendRequestToBO = sendRequestToBO;
	}
	public String getSendSMS() {
		return sendSMS;
	}
	public void setSendSMS(String sendSMS) {
		this.sendSMS = sendSMS;
	}
	public String getSecurityUrl() {
		return securityUrl;
	}
	public void setSecurityUrl(String securityUrl) {
		this.securityUrl = securityUrl;
	}
	public String getScheduleUrl() {
		return scheduleUrl;
	}
	public void setScheduleUrl(String scheduleUrl) {
		this.scheduleUrl = scheduleUrl;
	}
	public String getProvisionUrl() {
		return provisionUrl;
	}
	public void setProvisionUrl(String provisionUrl) {
		this.provisionUrl = provisionUrl;
	}
	public String getBoUrl() {
		return boUrl;
	}
	public void setBoUrl(String boUrl) {
		this.boUrl = boUrl;
	}
}
