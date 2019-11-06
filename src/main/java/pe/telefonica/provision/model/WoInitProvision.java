package pe.telefonica.provision.model;

public class WoInitProvision extends StatusProvision {

	private String resourceName;
	private String etaStartTime;
	private String asStatus;
	private String xaCreationDate;
	private String xaRequirementNumber;
	private String apptNumber;
	private String etaEndTime;
	private String aId;
	private String xaNumberWorkOrder;
	private String xaNumberServiceOrder;
	private String xaActivityType;
	private String xaNote;
	private String contactPhoneNumber2;
	private String contactName;
	private String xaDataReqCms;
	private String address;
	private String xaClientData;
	private String xrDni;
	private String pPhone;
	private String date;
	private String commentTechnician;

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getEtaStartTime() {
		return etaStartTime;
	}

	public void setEtaStartTime(String etaStartTime) {
		this.etaStartTime = etaStartTime;
	}

	public String getAsStatus() {
		return asStatus;
	}

	public void setAsStatus(String asStatus) {
		this.asStatus = asStatus;
	}

	public String getXaCreationDate() {
		return xaCreationDate;
	}

	public void setXaCreationDate(String xaCreationDate) {
		this.xaCreationDate = xaCreationDate;
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

	public String getEtaEndTime() {
		return etaEndTime;
	}

	public void setEtaEndTime(String etaEndTime) {
		this.etaEndTime = etaEndTime;
	}

	public String getaId() {
		return aId;
	}

	public void setaId(String aId) {
		this.aId = aId;
	}

	public String getXaNumberWorkOrder() {
		return xaNumberWorkOrder;
	}

	public void setXaNumberWorkOrder(String xaNumberWorkOrder) {
		this.xaNumberWorkOrder = xaNumberWorkOrder;
	}

	public String getXaNumberServiceOrder() {
		return xaNumberServiceOrder;
	}

	public void setXaNumberServiceOrder(String xaNumberServiceOrder) {
		this.xaNumberServiceOrder = xaNumberServiceOrder;
	}

	public String getXaActivityType() {
		return xaActivityType;
	}

	public void setXaActivityType(String xaActivityType) {
		this.xaActivityType = xaActivityType;
	}

	public String getXaNote() {
		return xaNote;
	}

	public void setXaNote(String xaNote) {
		this.xaNote = xaNote;
	}

	public String getContactPhoneNumber2() {
		return contactPhoneNumber2;
	}

	public void setContactPhoneNumber2(String contactPhoneNumber2) {
		this.contactPhoneNumber2 = contactPhoneNumber2;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getXaDataReqCms() {
		return xaDataReqCms;
	}

	public void setXaDataReqCms(String xaDataReqCms) {
		this.xaDataReqCms = xaDataReqCms;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getXaClientData() {
		return xaClientData;
	}

	public void setXaClientData(String xaClientData) {
		this.xaClientData = xaClientData;
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

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getCommentTechnician() {
		return commentTechnician;
	}

	public void setCommentTechnician(String commentTechnician) {
		this.commentTechnician = commentTechnician;
	}

	public WoInitProvision() {
		super();
	}

	public void mapObject(String stringObject) {

		try {
			String[] arrayWoInit = stringObject.split("\\|");
			this.label = arrayWoInit[0];
			this.externalId = arrayWoInit[1];
			this.resourceName = arrayWoInit[2];
			this.etaStartTime = arrayWoInit[3];
			this.asStatus = arrayWoInit[4];
			this.xaRequest = arrayWoInit[5];
			this.xaCreationDate = arrayWoInit[6];
			this.xaIdSt = arrayWoInit[7];
			this.xaRequirementNumber = arrayWoInit[8];
			this.apptNumber = arrayWoInit[9];
			this.etaEndTime = arrayWoInit[10];
			this.aId = arrayWoInit[11];
			this.xaNumberWorkOrder = arrayWoInit[12];
			this.xaNumberServiceOrder = arrayWoInit[13];
			this.xaActivityType = arrayWoInit[14];
			this.xaNote = arrayWoInit[15];
			this.contactPhoneNumber2 = arrayWoInit[16];
			this.contactName = arrayWoInit[17];
			this.xaDataReqCms = arrayWoInit[18];
			this.address = arrayWoInit[19];
			this.xaClientData = arrayWoInit[20];
			this.xrDni = arrayWoInit[21];
			this.pPhone = arrayWoInit[22];
			this.date = arrayWoInit[22];
			this.commentTechnician = arrayWoInit[23];

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
