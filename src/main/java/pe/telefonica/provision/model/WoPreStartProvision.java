package pe.telefonica.provision.model;

public class WoPreStartProvision extends StatusProvision {

	private String resourceName;
	private String date;
	private String xaActivityType;
	private String xaRequirementNumber;
	private String apptNumber;
	private String xrDni;
	private String pPhone;
	private String contactPhoneNumber2;
	private String xaNumberWorkOrder;
	private String latitude;
	private String longitude;

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getXaActivityType() {
		return xaActivityType;
	}

	public void setXaActivityType(String xaActivityType) {
		this.xaActivityType = xaActivityType;
	}

	public String getXaRequirementNumber() {
		return xaRequirementNumber;
	}

	public void setXaRequirementNumber(String xaRequirementNumber) {
		this.xaRequirementNumber = xaRequirementNumber;
	}

	public String getApptNumber() {
		return apptNumber;
	}

	public void setApptNumber(String apptNumber) {
		this.apptNumber = apptNumber;
	}

	public String getXrDni() {
		return xrDni;
	}

	public void setXrDni(String xrDni) {
		this.xrDni = xrDni;
	}

	public String getpPhone() {
		return pPhone;
	}

	public void setpPhone(String pPhone) {
		this.pPhone = pPhone;
	}

	public String getContactPhoneNumber2() {
		return contactPhoneNumber2;
	}

	public void setContactPhoneNumber2(String contactPhoneNumber2) {
		this.contactPhoneNumber2 = contactPhoneNumber2;
	}

	public String getXaNumberWorkOrder() {
		return xaNumberWorkOrder;
	}

	public void setXaNumberWorkOrder(String xaNumberWorkOrder) {
		this.xaNumberWorkOrder = xaNumberWorkOrder;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public WoPreStartProvision() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void mapObject(String stringObject) {

		try {
			String[] arrayWoInit = stringObject.split("\\|");
			this.label = arrayWoInit[0];
			this.externalId = arrayWoInit[1];
			this.xaRequest = arrayWoInit[2];
			this.resourceName = arrayWoInit[3];
			this.date = arrayWoInit[4];
			this.xaActivityType = arrayWoInit[5];
			this.xaIdSt = arrayWoInit[6];
			this.xaRequirementNumber = arrayWoInit[7];
			this.apptNumber = arrayWoInit[8];
			this.xrDni = arrayWoInit[9];
			this.pPhone = arrayWoInit[10];
			this.contactPhoneNumber2 = arrayWoInit[11];
			this.xaNumberWorkOrder = arrayWoInit[12];
			this.longitude = arrayWoInit[13];
			this.longitude = arrayWoInit[14];
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
