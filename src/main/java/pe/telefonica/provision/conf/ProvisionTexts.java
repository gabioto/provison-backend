package pe.telefonica.provision.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sms.text.provision")
public class ProvisionTexts {

	String webUrl;
	String addressUpdated;
	String unreachable;
	String cancelled;
	String cancelledByCustomer;
	String mainWeb;
	
	public String getCancelled() {
		return cancelled;
	}
	public void setCancelled(String cancelled) {
		this.cancelled = cancelled;
	}
	public String getWebUrl() {
		return webUrl;
	}
	public void setWebUrl(String webUrl) {
		this.webUrl = webUrl;
	}
	public String getAddressUpdated() {
		return addressUpdated;
	}
	public void setAddressUpdated(String addressUpdated) {
		this.addressUpdated = addressUpdated;
	}
	public String getUnreachable() {
		return unreachable;
	}
	public void setUnreachable(String unreachable) {
		this.unreachable = unreachable;
	}
	public String getCancelledByCustomer() {
		return cancelledByCustomer;
	}
	public void setCancelledByCustomer(String cancelledByCustomer) {
		this.cancelledByCustomer = cancelledByCustomer;
	}
	public String getMainWeb() {
		return mainWeb;
	}
	public void setMainWeb(String mainWeb) {
		this.mainWeb = mainWeb;
	}
	
}
