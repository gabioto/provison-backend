package pe.telefonica.provision.util.constants;

public enum Status {

	PENDIENTE("PENDIENTE", 1, "PRE REGISTRADO",
			"Tu pedido ha sido registrado. Tu cita de instalación se encuentra pendiente de confirmación.",
			"Tu pedido ha sido registrado. Te avisaremos cuando puedas agendar la visita del técnico."),
	INGRESADO("INGRESADO", 2, "REGISTRADO",
			"Tu pedido ha sido registrado. Tu cita de instalación se encuentra pendiente de confirmación.",
			"Tu pedido ha sido registrado. Te avisaremos cuando puedas agendar la visita del técnico."),
	CAIDA("CAIDA", 3, "CANCELADO", "Tu pedido fue cancelado, lamentamos los inconvenientes.", ""),
	DUMMY_IN_TOA("DUMMY_IN_TOA", 4, "PRE REGISTRADO",
			"Tu pedido ha sido registrado y se encuentra pendiente de confirmación.",
			""),
	IN_TOA("IN_TOA", 5, "REGISTRADO OPCIÓN A AGENDAR", "Tu pedido esta siendo atendido.",
			"Tu pedido esta pendiente de agendamiento."),
	WO_PRESTART("WO_PRESTART", 6, "REGISTRADO TÉCNICO EN CAMINO", "Tu técnico está en camino.", ""),
	WO_INIT("WO_INIT", 7, "REGISTRADO TÉCNICO EN CASA", "Tu pedido esta siendo instalado.", ""),
	WO_COMPLETED("WO_COMPLETED", 8, "INSTALACIÓN EXITOSA", "Tu pedido fue instalado con éxito.", ""),
	SCHEDULED("SCHEDULED", 9, "REGISTRADO OPCIÓN A REAGENDAR", "Tu pedido esta siendo atendido.", "Tu pedido esta siendo atendido."),
	FICTICIOUS_SCHEDULED("FICTICIOUS_SCHEDULED", 10, "PRE REGISTRADO",
			"Tu pedido ha sido registrado. Tu cita de instalación se encuentra pendiente de confirmación.", ""),
	WO_CANCEL("WO_CANCEL", 11, "AGENDA CANCELADA", "Tu cita de instalación fue cancelada.", ""),
	WO_RESCHEDULE("WO_RESCHEDULE", 12, "REGISTRADO OPCIÓN A REAGENDAR", "Tu pedido esta siendo atendido.", ""),
	WO_NOTDONE("WO_NOTDONE", 13, "PEDIDO NO INSTALADO",
			"Tu pedido no pudo ser instalado. Lamentamos los inconvenientes.", ""),
	CANCEL("CANCEL", 14, "CANCELADO POR CLIENTE",
			"La solicitud de cancelación de tu pedido Movistar fue procesada exitosamente.", "");

	private String statusName;
	private int statusId;
	private String description;
	private String genericSpeech;
	private String speechWithoutSchedule;
	// EError.ERROR_CONNECTION.getMessage()

	private Status(String statusName, int statusId, String description) {
		this.statusName = statusName;
		this.statusId = statusId;
		this.description = description;
	}

	private Status(String statusName, int statusId, String description, String genericSpeech,
			String speechWithoutSchedule) {
		this.statusName = statusName;
		this.statusId = statusId;
		this.description = description;
		this.genericSpeech = genericSpeech;
		this.speechWithoutSchedule = speechWithoutSchedule;
	}

	public String getStatusName() {
		return statusName;
	}

	public int getStatusId() {
		return statusId;
	}

	public String getDescription() {
		return description;
	}

	public String getGenericSpeech() {
		return genericSpeech;
	}

	public String getSpeechWithoutSchedule() {
		return speechWithoutSchedule;
	}

}
