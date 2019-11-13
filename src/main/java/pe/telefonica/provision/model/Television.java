package pe.telefonica.provision.model;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

public class Television implements Serializable{

	private static final long serialVersionUID = -5505560087293506272L;
	
	@Field("type")
	private String type;
	
	@Field("description")
	private String description;
	
	@Field("equipment")
	private String equipment;
	
	@Field("total_equipments_number")
	private Integer equipmentsNumber;
	
	@Field("additional_hd")
	private Integer additionalHd;
	
	@Field("additional_smart_hd")
	private Integer additionalSmartHd;
	
	@Field("tv_signal")
	private String tvSignal;
	
	@Field("technology")
	private String technology;
	
	@Field("tv_block")
	private List<TvBlock> tvBlocks;

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

	public Integer getEquipmentsNumber() {
		return equipmentsNumber;
	}

	public void setEquipmentsNumber(Integer equipmentsNumber) {
		this.equipmentsNumber = equipmentsNumber;
	}

	public Integer getAdditionalHd() {
		return additionalHd;
	}

	public void setAdditionalHd(Integer additionalHd) {
		this.additionalHd = additionalHd;
	}

	public Integer getAdditionalSmartHd() {
		return additionalSmartHd;
	}

	public void setAdditionalSmartHd(Integer additionalSmartHd) {
		this.additionalSmartHd = additionalSmartHd;
	}

	public String getTvSignal() {
		return tvSignal;
	}

	public void setTvSignal(String tvSignal) {
		this.tvSignal = tvSignal;
	}

	public String getTechnology() {
		return technology;
	}

	public void setTechnology(String technology) {
		this.technology = technology;
	}

	public List<TvBlock> getTvBlocks() {
		return tvBlocks;
	}

	public void setTvBlocks(List<TvBlock> tvBlocks) {
		this.tvBlocks = tvBlocks;
	} 
}
