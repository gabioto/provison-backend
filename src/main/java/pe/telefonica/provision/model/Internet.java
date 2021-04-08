package pe.telefonica.provision.model;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Field;

public class Internet implements Serializable {

	private static final long serialVersionUID = -6987474157579085838L;

	@Field("description")
	private String description;

	@Field("internet_equipment")
	private String equipment;

	@Field("technology")
	private String technology;

	@Field("network_technology")
	private String networkTechnology;

	@Field("speed")
	private String speed;

	@Field("promo_speed")
	private String promoSpeed;

	@Field("time_promo_speed")
	private String timePromoSpeed;

	@Field("repeater_smart_wifi")
	private String smartWifi;

	@Field("sva_internet")
	private String svaInternet;

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

	public String getTechnology() {
		return technology;
	}

	public void setTechnology(String technology) {
		this.technology = technology;
	}

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getPromoSpeed() {
		return promoSpeed;
	}

	public void setPromoSpeed(String promoSpeed) {
		this.promoSpeed = promoSpeed;
	}

	public String getTimePromoSpeed() {
		return timePromoSpeed;
	}

	public void setTimePromoSpeed(String timePromoSpeed) {
		this.timePromoSpeed = timePromoSpeed;
	}

	public String getSmartWifi() {
		return smartWifi;
	}

	public void setSmartWifi(String smartWifi) {
		this.smartWifi = smartWifi;
	}

	public String getSvaInternet() {
		return svaInternet;
	}

	public void setSvaInternet(String svaInternet) {
		this.svaInternet = svaInternet;
	}

	public String getNetworkTechnology() {
		return networkTechnology;
	}

	public void setNetworkTechnology(String networkTechnology) {
		this.networkTechnology = networkTechnology;
	}

}
