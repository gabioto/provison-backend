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
	String customerUrl;
	String customerUrlOnPremise;
	String oauthToken;
	String customerSearchClient;
	String customerSearchSecret;
	String apiClient;

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
	String updateScheduleDate;
	String cancelLocalSchedule;
	String searchCustomer;
	String searchCustomerOnPremise;
	String nroDiasVidaProvision;
	String scheduleGetTechAvailable;
	String securityGetOAuthToken;
	String simpliGetUrl;
	String simpliBaseUrl;
	
	String simpliGetUrlAzure;
	String simpliBaseUrlAzure;
	String apiClientKeyAzure;
	
	String oauthTokenOnPremise;
	String customerSearchClientOnPremise;
	String securityGetOAuthTokenAzure;
	

	public String getSecurityGetOAuthTokenAzure() {
		return securityGetOAuthTokenAzure;
	}

	public void setSecurityGetOAuthTokenAzure(String securityGetOAuthTokenAzure) {
		this.securityGetOAuthTokenAzure = securityGetOAuthTokenAzure;
	}

	public String getApiClientKeyAzure() {
		return apiClientKeyAzure;
	}

	public void setApiClientKeyAzure(String apiClientKeyAzure) {
		this.apiClientKeyAzure = apiClientKeyAzure;
	}

	public String getSimpliGetUrlAzure() {
		return simpliGetUrlAzure;
	}

	public void setSimpliGetUrlAzure(String simpliGetUrlAzure) {
		this.simpliGetUrlAzure = simpliGetUrlAzure;
	}

	public String getSimpliBaseUrlAzure() {
		return simpliBaseUrlAzure;
	}

	public void setSimpliBaseUrlAzure(String simpliBaseUrlAzure) {
		this.simpliBaseUrlAzure = simpliBaseUrlAzure;
	}

	public String getCustomerSearchClientOnPremise() {
		return customerSearchClientOnPremise;
	}

	public void setCustomerSearchClientOnPremise(String customerSearchClientOnPremise) {
		this.customerSearchClientOnPremise = customerSearchClientOnPremise;
	}

	public String getOauthTokenOnPremise() {
		return oauthTokenOnPremise;
	}

	public void setOauthTokenOnPremise(String oauthTokenOnPremise) {
		this.oauthTokenOnPremise = oauthTokenOnPremise;
	}

	public String getNroDiasVidaProvision() {
		return nroDiasVidaProvision;
	}

	public void setNroDiasVidaProvision(String nroDiasVidaProvision) {
		this.nroDiasVidaProvision = nroDiasVidaProvision;
	}
	
	public String getSecurityUrl() {
		return securityUrl;
	}

	public String getScheduleUrl() {
		return scheduleUrl;
	}

	public String getProvisionUrl() {
		return provisionUrl;
	}

	public String getBoUrl() {
		return boUrl;
	}

	public String getPsiUrl() {
		return psiUrl;
	}

	public String getCustomerUrl() {
		return customerUrl;
	}

	public String getOauthToken() {
		return oauthToken;
	}

	public String getCustomerSearchClient() {
		return customerSearchClient;
	}

	public String getCustomerSearchSecret() {
		return customerSearchSecret;
	}

	public String getApiClient() {
		return apiClient;
	}

	public String getSendSMS() {
		return sendSMS;
	}

	public String getSendMail() {
		return sendMail;
	}

	public String getSendRequestToBO() {
		return sendRequestToBO;
	}

	public String getPsiUpdateClient() {
		return psiUpdateClient;
	}

	public String getUpdateSchedule() {
		return updateSchedule;
	}

	public String getScheduleUpdateFicticious() {
		return scheduleUpdateFicticious;
	}

	public String getScheduleUpdatePSICodeReal() {
		return scheduleUpdatePSICodeReal;
	}

	public String getGetPSIToken() {
		return getPSIToken;
	}

	public String getSecuritySaveLogData() {
		return securitySaveLogData;
	}

	public String getSaveThirdLogData() {
		return saveThirdLogData;
	}

	public String getSendSMSById() {
		return sendSMSById;
	}

	public String getBucketsByProduct() {
		return bucketsByProduct;
	}

	public String getUpdateScheduleDate() {
		return updateScheduleDate;
	}

	public String getCancelLocalSchedule() {
		return cancelLocalSchedule;
	}

	public String getSearchCustomer() {
		return searchCustomer;
	}

	public String getScheduleGetTechAvailable() {
		return scheduleGetTechAvailable;
	}

	public String getSecurityGetOAuthToken() {
		return securityGetOAuthToken;
	}

	public String getSimpliGetUrl() {
		return simpliGetUrl;
	}

	public void setSecurityUrl(String securityUrl) {
		this.securityUrl = securityUrl;
	}

	public void setScheduleUrl(String scheduleUrl) {
		this.scheduleUrl = scheduleUrl;
	}

	public void setProvisionUrl(String provisionUrl) {
		this.provisionUrl = provisionUrl;
	}

	public void setBoUrl(String boUrl) {
		this.boUrl = boUrl;
	}

	public void setPsiUrl(String psiUrl) {
		this.psiUrl = psiUrl;
	}

	public void setCustomerUrl(String customerUrl) {
		this.customerUrl = customerUrl;
	}

	public void setOauthToken(String oauthToken) {
		this.oauthToken = oauthToken;
	}

	public void setCustomerSearchClient(String customerSearchClient) {
		this.customerSearchClient = customerSearchClient;
	}

	public void setCustomerSearchSecret(String customerSearchSecret) {
		this.customerSearchSecret = customerSearchSecret;
	}

	public void setApiClient(String apiClient) {
		this.apiClient = apiClient;
	}

	public void setSendSMS(String sendSMS) {
		this.sendSMS = sendSMS;
	}

	public void setSendMail(String sendMail) {
		this.sendMail = sendMail;
	}

	public void setSendRequestToBO(String sendRequestToBO) {
		this.sendRequestToBO = sendRequestToBO;
	}

	public void setPsiUpdateClient(String psiUpdateClient) {
		this.psiUpdateClient = psiUpdateClient;
	}

	public void setUpdateSchedule(String updateSchedule) {
		this.updateSchedule = updateSchedule;
	}

	public void setScheduleUpdateFicticious(String scheduleUpdateFicticious) {
		this.scheduleUpdateFicticious = scheduleUpdateFicticious;
	}

	public void setScheduleUpdatePSICodeReal(String scheduleUpdatePSICodeReal) {
		this.scheduleUpdatePSICodeReal = scheduleUpdatePSICodeReal;
	}

	public void setGetPSIToken(String getPSIToken) {
		this.getPSIToken = getPSIToken;
	}

	public void setSecuritySaveLogData(String securitySaveLogData) {
		this.securitySaveLogData = securitySaveLogData;
	}

	public void setSaveThirdLogData(String saveThirdLogData) {
		this.saveThirdLogData = saveThirdLogData;
	}

	public void setSendSMSById(String sendSMSById) {
		this.sendSMSById = sendSMSById;
	}

	public void setBucketsByProduct(String bucketsByProduct) {
		this.bucketsByProduct = bucketsByProduct;
	}

	public void setUpdateScheduleDate(String updateScheduleDate) {
		this.updateScheduleDate = updateScheduleDate;
	}

	public void setCancelLocalSchedule(String cancelLocalSchedule) {
		this.cancelLocalSchedule = cancelLocalSchedule;
	}

	public void setSearchCustomer(String searchCustomer) {
		this.searchCustomer = searchCustomer;
	}

	public void setScheduleGetTechAvailable(String scheduleGetTechAvailable) {
		this.scheduleGetTechAvailable = scheduleGetTechAvailable;
	}

	public void setSecurityGetOAuthToken(String securityGetOAuthToken) {
		this.securityGetOAuthToken = securityGetOAuthToken;
	}

	public void setSimpliGetUrl(String simpliGetUrl) {
		this.simpliGetUrl = simpliGetUrl;
	}

	public String getSimpliBaseUrl() {
		return simpliBaseUrl;
	}

	public void setSimpliBaseUrl(String simpliBaseUrl) {
		this.simpliBaseUrl = simpliBaseUrl;
	}

	public String getCustomerUrlOnPremise() {
		return customerUrlOnPremise;
	}

	public void setCustomerUrlOnPremise(String customerUrlOnPremise) {
		this.customerUrlOnPremise = customerUrlOnPremise;
	}

	public String getSearchCustomerOnPremise() {
		return searchCustomerOnPremise;
	}

	public void setSearchCustomerOnPremise(String searchCustomerOnPremise) {
		this.searchCustomerOnPremise = searchCustomerOnPremise;
	}

}
