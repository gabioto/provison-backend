package pe.telefonica.provision.dto;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Field;

public class HomePhone implements Serializable{

	private static final long serialVersionUID = 812258492568891478L;
	
	@Field("type")
	private String type;
	
	@Field("description")
	private String description;
	
	@Field("equipment")
	private String equipment;
	
	@Field("equipmenst_number")
	private Integer equipmenstNumber;
	
	@Field("sva_line")
	private String svaLine;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEquipment() {
		return equipment;
	}

	public void setEquipment(String equipment) {
		this.equipment = equipment;
	}

	public Integer getEquipmenstNumber() {
		return equipmenstNumber;
	}

	public void setEquipmenstNumber(Integer equipmenstNumber) {
		this.equipmenstNumber = equipmenstNumber;
	}

	public String getSvaLine() {
		return svaLine;
	}

	public void setSvaLine(String svaLine) {
		this.svaLine = svaLine;
	}
}
