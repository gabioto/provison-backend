package pe.telefonica.provision.controller.response;

import java.util.ArrayList;
import java.util.Arrays;

public class SMSByIdResponse {
	
	private ContactResult[] contactResults;
	
	public SMSByIdResponse() {
		ArrayList<ContactResult> arrayList = new ArrayList<>();
		contactResults = arrayList.toArray(new ContactResult[0]);
	}
	
	public ContactResult[] getContactResults() {
		return contactResults;
	}
	public void setContactResults(ContactResult[] contactResults) {
		this.contactResults = contactResults;
	}
	
	@Override
	public String toString() {
		return "SMSByIdResponse [contactResults=" + Arrays.toString(contactResults) + "]";
	}

	public static class ContactResult{
		
		private String phoneNumber;
		private Boolean isMovistar;
		private Boolean wasSent;
		private String sentFrom;
		
		public String getPhoneNumber() {
			return phoneNumber;
		}
		public void setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}
		public Boolean getIsMovistar() {
			return isMovistar;
		}
		public void setIsMovistar(Boolean isMovistar) {
			this.isMovistar = isMovistar;
		}
		public Boolean getWasSent() {
			return wasSent;
		}
		public void setWasSent(Boolean wasSent) {
			this.wasSent = wasSent;
		}
		public String getSentFrom() {
			return sentFrom;
		}
		public void setSentFrom(String sentFrom) {
			this.sentFrom = sentFrom;
		}

		@Override
		public String toString() {
			return "Contact [phoneNumber=" + phoneNumber + ", isMovistar=" + isMovistar + ", wasSent=" + wasSent
					+ ", sentFrom=" + sentFrom + "]";
		}
	}
}
