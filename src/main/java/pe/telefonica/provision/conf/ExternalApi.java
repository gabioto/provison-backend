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
	String productOrders;

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
	public String getPsiUrl() {
		return psiUrl;
	}
	public void setPsiUrl(String psiUrl) {
		this.psiUrl = psiUrl;
	}
	public String getCustomerUrl() {
		return customerUrl;
	}
	public void setCustomerUrl(String customerUrl) {
		this.customerUrl = customerUrl;
	}
	public String getCustomerUrlOnPremise() {
		return customerUrlOnPremise;
	}
	public void setCustomerUrlOnPremise(String customerUrlOnPremise) {
		this.customerUrlOnPremise = customerUrlOnPremise;
	}
	public String getOauthToken() {
		return oauthToken;
	}
	public void setOauthToken(String oauthToken) {
		this.oauthToken = oauthToken;
	}
	public String getCustomerSearchClient() {
		return customerSearchClient;
	}
	public void setCustomerSearchClient(String customerSearchClient) {
		this.customerSearchClient = customerSearchClient;
	}
	public String getCustomerSearchSecret() {
		return customerSearchSecret;
	}
	public void setCustomerSearchSecret(String customerSearchSecret) {
		this.customerSearchSecret = customerSearchSecret;
	}
	public String getApiClient() {
		return apiClient;
	}
	public void setApiClient(String apiClient) {
		this.apiClient = apiClient;
	}
	public String getSendSMS() {
		return sendSMS;
	}
	public void setSendSMS(String sendSMS) {
		this.sendSMS = sendSMS;
	}
	public String getSendMail() {
		return sendMail;
	}
	public void setSendMail(String sendMail) {
		this.sendMail = sendMail;
	}
	public String getSendRequestToBO() {
		return sendRequestToBO;
	}
	public void setSendRequestToBO(String sendRequestToBO) {
		this.sendRequestToBO = sendRequestToBO;
	}
	public String getPsiUpdateClient() {
		return psiUpdateClient;
	}
	public void setPsiUpdateClient(String psiUpdateClient) {
		this.psiUpdateClient = psiUpdateClient;
	}
	public String getUpdateSchedule() {
		return updateSchedule;
	}
	public void setUpdateSchedule(String updateSchedule) {
		this.updateSchedule = updateSchedule;
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
	public String getGetPSIToken() {
		return getPSIToken;
	}
	public void setGetPSIToken(String getPSIToken) {
		this.getPSIToken = getPSIToken;
	}
	public String getSecuritySaveLogData() {
		return securitySaveLogData;
	}
	public void setSecuritySaveLogData(String securitySaveLogData) {
		this.securitySaveLogData = securitySaveLogData;
	}
	public String getSaveThirdLogData() {
		return saveThirdLogData;
	}
	public void setSaveThirdLogData(String saveThirdLogData) {
		this.saveThirdLogData = saveThirdLogData;
	}
	public String getSendSMSById() {
		return sendSMSById;
	}
	public void setSendSMSById(String sendSMSById) {
		this.sendSMSById = sendSMSById;
	}
	public String getBucketsByProduct() {
		return bucketsByProduct;
	}
	public void setBucketsByProduct(String bucketsByProduct) {
		this.bucketsByProduct = bucketsByProduct;
	}
	public String getUpdateScheduleDate() {
		return updateScheduleDate;
	}
	public void setUpdateScheduleDate(String updateScheduleDate) {
		this.updateScheduleDate = updateScheduleDate;
	}
	public String getCancelLocalSchedule() {
		return cancelLocalSchedule;
	}
	public void setCancelLocalSchedule(String cancelLocalSchedule) {
		this.cancelLocalSchedule = cancelLocalSchedule;
	}
	public String getSearchCustomer() {
		return searchCustomer;
	}
	public void setSearchCustomer(String searchCustomer) {
		this.searchCustomer = searchCustomer;
	}
	public String getProductOrders() {
		return productOrders;
	}
	public void setProductOrders(String productOrders) {
		this.productOrders = productOrders;
	}
	public String getSearchCustomerOnPremise() {
		return searchCustomerOnPremise;
	}
	public void setSearchCustomerOnPremise(String searchCustomerOnPremise) {
		this.searchCustomerOnPremise = searchCustomerOnPremise;
	}
	public String getNroDiasVidaProvision() {
		return nroDiasVidaProvision;
	}
	public void setNroDiasVidaProvision(String nroDiasVidaProvision) {
		this.nroDiasVidaProvision = nroDiasVidaProvision;
	}
	public String getScheduleGetTechAvailable() {
		return scheduleGetTechAvailable;
	}
	public void setScheduleGetTechAvailable(String scheduleGetTechAvailable) {
		this.scheduleGetTechAvailable = scheduleGetTechAvailable;
	}
	public String getSecurityGetOAuthToken() {
		return securityGetOAuthToken;
	}
	public void setSecurityGetOAuthToken(String securityGetOAuthToken) {
		this.securityGetOAuthToken = securityGetOAuthToken;
	}
	public String getSimpliGetUrl() {
		return simpliGetUrl;
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
	public String getApiClientKeyAzure() {
		return apiClientKeyAzure;
	}
	public void setApiClientKeyAzure(String apiClientKeyAzure) {
		this.apiClientKeyAzure = apiClientKeyAzure;
	}
	public String getOauthTokenOnPremise() {
		return oauthTokenOnPremise;
	}
	public void setOauthTokenOnPremise(String oauthTokenOnPremise) {
		this.oauthTokenOnPremise = oauthTokenOnPremise;
	}
	public String getCustomerSearchClientOnPremise() {
		return customerSearchClientOnPremise;
	}
	public void setCustomerSearchClientOnPremise(String customerSearchClientOnPremise) {
		this.customerSearchClientOnPremise = customerSearchClientOnPremise;
	}
	public String getSecurityGetOAuthTokenAzure() {
		return securityGetOAuthTokenAzure;
	}
	public void setSecurityGetOAuthTokenAzure(String securityGetOAuthTokenAzure) {
		this.securityGetOAuthTokenAzure = securityGetOAuthTokenAzure;
	}


}
