package pe.telefonica.provision.dto;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Field;

public class TvBlock implements Serializable{

	private static final long serialVersionUID = -5850949473352504549L;
	
	@Field("name")
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
