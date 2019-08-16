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
	String psiUrl;
	String oauth2Url;

	String sendSMS;
	String sendMail;
	String sendRequestToBO;
	String psiUpdateClient;
	String updateSchedule;
	String oauth2Token;

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

	public String getPsiUpdateClient() {
		return psiUpdateClient;
	}

	public void setPsiUpdateClient(String psiUpdateClient) {
		this.psiUpdateClient = psiUpdateClient;
	}

	public String getPsiUrl() {
		return psiUrl;
	}

	public void setPsiUrl(String psiUrl) {
		this.psiUrl = psiUrl;
	}

	public String getUpdateSchedule() {
		return updateSchedule;
	}

	public void setUpdateSchedule(String updateSchedule) {
		this.updateSchedule = updateSchedule;
	}

	public String getSendMail() {
		return sendMail;
	}

	public void setSendMail(String sendMail) {
		this.sendMail = sendMail;
	}
}
