package pe.telefonica.provision.model;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Document(collection = "collStatus")
@JsonPropertyOrder({ "idStatus" })
public class Status {

	@Id
	@Field("_id")
	private String idStatus;

	@Field("status_name")
	private String statusName;

	@Field("status_id")
	private int statusId;

	@Field("description")
	private String description;

	@Field("front")
	private String front;

	@Field("generic_speech")
	private String genericSpeech;

	@Field("speech_without_schedule")
	private String speechWithoutSchedule;

	public String getIdStatus() {
		return idStatus;
	}

	public void setIdStatus(String idStatus) {
		this.idStatus = idStatus;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFront() {
		return front;
	}

	public void setFront(String front) {
		this.front = front;
	}

	public String getGenericSpeech() {
		return genericSpeech;
	}

	public void setGenericSpeech(String genericSpeech) {
		this.genericSpeech = genericSpeech;
	}

	public String getSpeechWithoutSchedule() {
		return speechWithoutSchedule;
	}

	public void setSpeechWithoutSchedule(String speechWithoutSchedule) {
		this.speechWithoutSchedule = speechWithoutSchedule;
	}

}
