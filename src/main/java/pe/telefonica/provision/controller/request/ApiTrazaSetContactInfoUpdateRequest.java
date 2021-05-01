package pe.telefonica.provision.controller.request;

import java.util.List;

public class ApiTrazaSetContactInfoUpdateRequest extends LogDataFrontendRequest {
	private String psiCode;
	private String email;
	private boolean holderWillReceive;
	private List<ContactRequest> contacts;
	private String scheduler;
	
	
	public String getScheduler() {
		return scheduler;
	}

	public void setScheduler(String scheduler) {
		this.scheduler = scheduler;
	}

	public String getPsiCode() {
		return psiCode;
	}

	public void setPsiCode(String psiCode) {
		this.psiCode = psiCode;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isHolderWillReceive() {
		return holderWillReceive;
	}

	public void setHolderWillReceive(boolean holderWillReceive) {
		this.holderWillReceive = holderWillReceive;
	}

	public List<ContactRequest> getContacts() {
		return contacts;
	}

	public void setContacts(List<ContactRequest> contacts) {
		this.contacts = contacts;
	}

}
