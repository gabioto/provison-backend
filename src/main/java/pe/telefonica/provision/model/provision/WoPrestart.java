package pe.telefonica.provision.model.provision;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Field;

public class WoPrestart implements Serializable {

	private static final long serialVersionUID = 3375714898258466530L;

	@Field("nameResource")
	private String nameResource;

	@Field("date")
	private String date;

	public String getNameResource() {
		return nameResource;
	}

	public void setNameResource(String nameResource) {
		this.nameResource = nameResource;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

}
