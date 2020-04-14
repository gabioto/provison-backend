package pe.telefonica.provision.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "project.configuration")
public class ProjectConfig {

	private String functionsProvisionEnable;

	public String getFunctionsProvisionEnable() {
		return functionsProvisionEnable;
	}

	public void setFunctionsProvisionEnable(String functionsProvisionEnable) {
		this.functionsProvisionEnable = functionsProvisionEnable;
	}

}
