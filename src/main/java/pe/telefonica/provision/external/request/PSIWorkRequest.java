package pe.telefonica.provision.external.request;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import pe.telefonica.provision.service.request.PSIUpdateClientRequest;

public class PSIWorkRequest {

	private String correlationId;
	private String startDate;
	private List<Detail> details = new ArrayList<>();

	public String getCorrelationId() {
		return correlationId;
	}

	public String getStartDate() {
		return startDate;
	}

	public List<Detail> getDetails() {
		return details;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public void setDetails(List<Detail> details) {
		this.details = details;
	}

	public PSIWorkRequest() {
		super();
	}

	public PSIWorkRequest(PSIUpdateClientRequest request, String date) {
		correlationId = request.getBodyUpdateClient().getSolicitud();
		startDate = date;

		List<ContactMedia> contactMedia = new ArrayList<>();
		//public ContactMedia(String _type, String type, String email, String number) {
		contactMedia.add(new ContactMedia("email", "", request.getBodyUpdateClient().getCorreo(), ""));
		contactMedia.add(new ContactMedia("phone", "mobile", "", request.getBodyUpdateClient().getTelefono1()));

		Appointment appointment = new Appointment("", date, null, contactMedia);

		details.add(new Detail(new Work(request.getBodyUpdateClient().getNombre_completo(), null, null, appointment)));
	}

	public class Detail {
		private Work work;

		public Work getWork() {
			return work;
		}

		public void setWork(Work work) {
			this.work = work;
		}

		public Detail(Work work) {
			super();
			this.work = work;
		}

	}

	public class Work {
		private String name;
		//private List<Note> notes = new ArrayList<>();
		//private List<RelatedInformation> relatedInformation = new ArrayList<>();
		private Appointment appointment;
		
		public Appointment getAppointment() {
			return appointment;
		}

		public void setAppointment(Appointment appointment) {
			this.appointment = appointment;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		
		public Work(String name, List<Note> notes, List<RelatedInformation> relatedInformation,
				Appointment appointment) {
			super();
			this.name = name;
			//this.notes = notes;
			//this.relatedInformation = relatedInformation;
			this.appointment = appointment;
		}

		public Work() {
			super();
		}

	}

	public class Note {
		private String text;

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public Note(String text) {
			super();
			this.text = text;
		}
	}

	public class Appointment {
		private String activityType;
		private String startDate;
		//private List<String> timeSlots = new ArrayList<>();
		private List<ContactMedia> contactMedia = new ArrayList<>();

		public String getActivityType() {
			return activityType;
		}

		public String getStartDate() {
			return startDate;
		}

		

		public void setActivityType(String activityType) {
			this.activityType = activityType;
		}

		public void setStartDate(String startDate) {
			this.startDate = startDate;
		}

		
		public List<ContactMedia> getContactMedia() {
			return contactMedia;
		}

		public void setContactMedia(List<ContactMedia> contactMedia) {
			this.contactMedia = contactMedia;
		}

		public Appointment(String activityType, String startDate, List<String> timeSlots,
				List<ContactMedia> contactMedia) {
			super();
			//this.activityType = activityType;
			//this.startDate = startDate;
			//this.timeSlots = timeSlots;
			this.contactMedia = contactMedia;
		}

		public Appointment() {
			super();
		}

	}

	public class ContactMedia {
		
		@SerializedName("@type")
		private String _type;

		private String type;

		private String email;

		private String number;
		
		
		@Override
		public String toString() {
			return "ContactMedia [@type=" + _type + ", type=" + type + ", email=" + email + ", number=" + number + "]";
		}

		public String getNumber() {
			return number;
		}

		public void setNumber(String number) {
			this.number = number;
		}

		public String getType() {
			return type;
		}

		public String getEmail() {
			return email;
		}

		public void setType(String type) {
			this.type = type;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String get_type() {
			return _type;
		}

		public void set_type(String _type) {
			this._type = _type;
		}

		public ContactMedia(String _type, String type, String email, String number) {
			super();
			this._type = _type;
			this.type = type;
			this.email = email;
			this.number = number;
		}

	}

	public class RelatedInformation {

		private List<Characteristic> characteristics = new ArrayList<>();

		public List<Characteristic> getCharacteristics() {
			return characteristics;
		}

		public void setCharacteristics(List<Characteristic> characteristics) {
			this.characteristics = characteristics;
		}

		public RelatedInformation(List<Characteristic> characteristics) {
			super();
			this.characteristics = characteristics;
		}

	}

	public class Characteristic {
		private String name;
		private String value;

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public Characteristic(String name, String value) {
			super();
			this.name = name;
			this.value = value;
		}

	}
}
