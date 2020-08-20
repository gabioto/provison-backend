package pe.telefonica.provision.external.request.simpli;

public class SimpliRequest {
	private String visitTitle;
	private String visitAddress;
	private String driverUserName;
	private String latitude;
	private String longitude;
	private String token;

	public String getVisitTitle() {
		return visitTitle;
	}

	public String getVisitAddress() {
		return visitAddress;
	}

	public String getDriverUserName() {
		return driverUserName;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setVisitTitle(String visitTitle) {
		this.visitTitle = visitTitle;
	}

	public void setVisitAddress(String visitAddress) {
		this.visitAddress = visitAddress;
	}

	public void setDriverUserName(String driverUserName) {
		this.driverUserName = driverUserName;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	
}
