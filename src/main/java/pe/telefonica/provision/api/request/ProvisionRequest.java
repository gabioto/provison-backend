package pe.telefonica.provision.api.request;

public class ProvisionRequest {

	private String documentType;
	private String documentNumber;

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public ProvisionRequest(String documentType, String documentNumber) {
		super();
		this.documentType = documentType;
		this.documentNumber = documentNumber;
	}

	public ProvisionRequest() {
		super();
		// TODO Auto-generated constructor stub
	}	
}
