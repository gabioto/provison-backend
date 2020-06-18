package pe.telefonica.provision.controller.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pe.telefonica.provision.model.Contacts;

public class SMSByIdRequest {

	private Contact[] contacts;
	private Message message;

	public SMSByIdRequest() {
		ArrayList<Contact> arrayList = new ArrayList<>();
		contacts = arrayList.toArray(new Contact[0]);
	}

	public Contact[] getContacts() {
		return contacts;
	}

	public void setContacts(Contact[] contacts) {
		this.contacts = contacts;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "SMSByIdRequest [contacts=" + Arrays.toString(contacts) + ", message=" + message + "]";
	}

	public static class Contact {

		private String phoneNumber;
		private Boolean isMovistar;
		private String fullName;
		private Boolean holder;

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

		public String getFullName() {
			return fullName;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

		public Boolean getHolder() {
			return holder;
		}

		public void setHolder(Boolean holder) {
			this.holder = holder;
		}

		@Override
		public String toString() {
			return "Contact [phoneNumber=" + phoneNumber + ", isMovistar=" + isMovistar + "]";
		}
	}

	public static class Message {

		private String msgKey;
		private MsgParameter[] msgParameters;
		private String webURL;
		private String webContactURL;

		public Message() {
			ArrayList<MsgParameter> arrayList = new ArrayList<>();
			msgParameters = arrayList.toArray(new MsgParameter[0]);
		}

		public String getMsgKey() {
			return msgKey;
		}

		public void setMsgKey(String msgKey) {
			this.msgKey = msgKey;
		}

		public MsgParameter[] getMsgParameters() {
			return msgParameters;
		}

		public void setMsgParameters(MsgParameter[] msgParameters) {
			this.msgParameters = msgParameters;
		}

		public String getWebURL() {
			return webURL;
		}

		public void setWebURL(String webURL) {
			this.webURL = webURL;
		}

		public String getWebContactURL() {
			return webContactURL;
		}

		public void setWebContactURL(String webContactURL) {
			this.webContactURL = webContactURL;
		}

		@Override
		public String toString() {
			return "Message [msgKey=" + msgKey + ", msgParameters=" + Arrays.toString(msgParameters) + ", webURL="
					+ webURL + "]";
		}

		public static class MsgParameter {

			private String key;
			private String value;

			public String getKey() {
				return key;
			}

			public void setKey(String key) {
				this.key = key;
			}

			public String getValue() {
				return value;
			}

			public void setValue(String value) {
				this.value = value;
			}

			@Override
			public String toString() {
				return "Parameter [key=" + key + ", value=" + value + "]";
			}
		}

	}

	public static List<Contact> mapContacts(List<Contacts> contacts) {
		List<Contact> contactList = new ArrayList<>();

		if (contacts != null) {
			for (Contacts contact : contacts) {
				Contact cont = new Contact();
				cont.setFullName(contact.getFullName());
				cont.setHolder(contact.isHolder());
				cont.setIsMovistar(contact.getCarrier());
				cont.setPhoneNumber(contact.getPhoneNumber());
				contactList.add(cont);
			}
		}

		return contactList;
	}

}
