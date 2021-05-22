package pe.telefonica.provision.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "external.api")
public class ExternalApi {

	private String securityUrl;
	private String scheduleUrl;
	private String provisionUrl;
	private String boUrl;
	private String psiUrl;
	private String customerUrl;
	private String customerUrlOnPremise;
	private String oauthToken;
	private String customerSearchClient;
	private String customerSearchSecret;
	private String apiClient;

	private String sendSMS;
	private String sendMail;
	private String sendRequestToBO;
	private String psiUpdateClient;
	private String updateSchedule;
	private String scheduleUpdateFicticious;
	private String scheduleUpdatePSICodeReal;
	private String getPSIToken;
	private String securitySaveLogData;
	private String saveThirdLogData;
	private String sendSMSById;
	private String bucketsByProduct;
	private String updateScheduleDate;
	private String cancelLocalSchedule;
	private String searchCustomer;
	private String productOrders;
	private String loginToken;
	private String insertSchedule;

	private String searchCustomerOnPremise;
	private String nroDiasVidaProvision;
	private String scheduleGetTechAvailable;
	private String securityGetOAuthToken;
	private String simpliGetUrl;
	private String simpliBaseUrl;

	private String simpliGetUrlAzure;
	private String simpliBaseUrlAzure;
	private String apiClientKeyAzure;

	private String oauthTokenOnPremise;
	private String customerSearchClientOnPremise;
	private String securityGetOAuthTokenAzure;
	//--//
	private String workOrderManagementUrl;
	private String workOrders;	
	private String unicaServiceId;
	private String unicaApplication;
	private String unicaPID;
	private String unicaUser;
	private String ocpApimSubscriptionKey;

}
