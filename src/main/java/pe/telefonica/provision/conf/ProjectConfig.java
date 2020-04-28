package pe.telefonica.provision.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "project.configuration")
public class ProjectConfig {

	private String functionsProvisionEnable;
	private String functionsFaultEnable;

	public String getFunctionsProvisionEnable() {
		return functionsProvisionEnable;
	}

	public void setFunctionsProvisionEnable(String functionsProvisionEnable) {
		this.functionsProvisionEnable = functionsProvisionEnable;
	}

	public String getFunctionsFaultEnable() {
		return functionsFaultEnable;
	}

	public void setFunctionsFaultEnable(String functionsFaultEnable) {
		this.functionsFaultEnable = functionsFaultEnable;
	}

}
