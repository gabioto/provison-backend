package pe.telefonica.provision.api.request;

public class MailRequest {

	private String mailTemplateId;
	private MailParameter[] mailParameters;
	
	public String getMailTemplateId() {
		return mailTemplateId;
	}
	public void setMailTemplateId(String mailTemplateId) {
		this.mailTemplateId = mailTemplateId;
	}
	public MailParameter[] getMailParameters() {
		return mailParameters;
	}
	public void setMailParameters(MailParameter[] mailParameters) {
		this.mailParameters = mailParameters;
	}

	public static class MailParameter{
		
		private String paramKey;
		private String paramValue;
		
		public String getParamKey() {
			return paramKey;
		}
		public void setParamKey(String paramKey) {
			this.paramKey = paramKey;
		}
		public String getParamValue() {
			return paramValue;
		}
		public void setParamValue(String paramValue) {
			this.paramValue = paramValue;
		}
		
	}
	
}
