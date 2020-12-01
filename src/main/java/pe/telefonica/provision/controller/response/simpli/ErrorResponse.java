package pe.telefonica.provision.controller.response.simpli;

public class ErrorResponse {

	private String exceptionId;
	private String exceptionText;
	private String moreInfo;
	private String userMessage;
	
	public String getExceptionId() {
		return exceptionId;
	}
	public String getExceptionText() {
		return exceptionText;
	}
	public String getMoreInfo() {
		return moreInfo;
	}
	public String getUserMessage() {
		return userMessage;
	}
	public void setExceptionId(String exceptionId) {
		this.exceptionId = exceptionId;
	}
	public void setExceptionText(String exceptionText) {
		this.exceptionText = exceptionText;
	}
	public void setMoreInfo(String moreInfo) {
		this.moreInfo = moreInfo;
	}
	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}
}