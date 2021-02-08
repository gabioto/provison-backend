package pe.telefonica.provision.model.params;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(collection = "collParams")
@JsonPropertyOrder({ "id" })
public class Params {

	@Id
	@Field("_id")
	private String id;
		
	@Field("key")
	private String key;
	
	@Field("value")
	private String value;
}