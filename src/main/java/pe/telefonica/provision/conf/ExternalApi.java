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
	String oauthToken;

	String oauth2Client;
	String oauth2RefreshToken;

	String sendSMS;
	String sendMail;
	String sendRequestToBO;
	String psiUpdateClient;
	String updateSchedule;
	String scheduleUpdateFicticious;
	String scheduleUpdatePSICodeReal;
	String getPSIToken;
	String securitySaveLogData;
	String saveThirdLogData;
	String sendSMSById;
	String bucketsByProduct;

	public String getSendSMSById() {
		return sendSMSById;
	}

	public void setSendSMSById(String sendSMSById) {
		this.sendSMSById = sendSMSById;
	}

	public String getOauth2Client() {
		return oauth2Client;
	}

	public void setOauth2Client(String oauth2Client) {
		this.oauth2Client = oauth2Client;
	}

	public String getOauth2RefreshToken() {
		return oauth2RefreshToken;
	}

	public void setOauth2RefreshToken(String oauth2RefreshToken) {
		this.oauth2RefreshToken = oauth2RefreshToken;
	}

	public String getGetPSIToken() {
		return getPSIToken;
	}

	public void setGetPSIToken(String getPSIToken) {
		this.getPSIToken = getPSIToken;
	}

	public String getScheduleUpdateFicticious() {
		return scheduleUpdateFicticious;
	}

	public void setScheduleUpdateFicticious(String scheduleUpdateFicticious) {
		this.scheduleUpdateFicticious = scheduleUpdateFicticious;
	}

	public String getScheduleUpdatePSICodeReal() {
		return scheduleUpdatePSICodeReal;
	}

	public void setScheduleUpdatePSICodeReal(String scheduleUpdatePSICodeReal) {
		this.scheduleUpdatePSICodeReal = scheduleUpdatePSICodeReal;
	}

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

	public String getSecuritySaveLogData() {
		return securitySaveLogData;
	}

	public void setSecuritySaveLogData(String securitySaveLogData) {
		this.securitySaveLogData = securitySaveLogData;
	}

	public String getOauthToken() {
		return oauthToken;
	}

	public void setOauthToken(String oauthToken) {
		this.oauthToken = oauthToken;
	}

	public String getBucketsByProduct() {
		return bucketsByProduct;
	}

	public void setBucketsByProduct(String bucketsByProduct) {
		this.bucketsByProduct = bucketsByProduct;
	}

	public String getSaveThirdLogData() {
		return saveThirdLogData;
	}

	public void setSaveThirdLogData(String saveThirdLogData) {
		this.saveThirdLogData = saveThirdLogData;
	}

}
