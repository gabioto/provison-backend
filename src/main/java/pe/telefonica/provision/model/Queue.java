package pe.telefonica.provision.model;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Document(collection = "collContingencia")
@JsonPropertyOrder({"idQueue"})
public class Queue {

private static final long serialVersionUID = 4894729030347835498L;
	
	@Id
	@Field("idContingencia")
	private String idQueue;
	
	@Field("active")
	private Boolean active;

	public String getIdQueue() {
		return idQueue;
	}

	public void setIdQueue(String idQueue) {
		this.idQueue = idQueue;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
	
	
}
