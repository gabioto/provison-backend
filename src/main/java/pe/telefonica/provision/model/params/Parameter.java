package pe.telefonica.provision.model.params;

import java.io.Serializable;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Document(collection = "collParams")
@JsonPropertyOrder({ "idParam" })
public class Parameter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2123641890148157692L;

	@Id
	@Field("_id")
	private String idParam;
		
	@Field("key")
	private String key;
	
	@Field("value")
	private String value;

	public String getIdParam() {
		return idParam;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public void setIdParam(String idParam) {
		this.idParam = idParam;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setValue(String value) {
		this.value = value;
	}
}