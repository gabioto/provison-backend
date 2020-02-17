package pe.telefonica.provision.util.constants;

public enum Status {

	PENDIENTE("PENDIENTE", 1, "PRE REGISTRADO",
			"[Nombre Cliente], el servicio Hogar de tu pedido MT está en proceso de atención y la instalación se realizará el (indicar día) y (indicar nombre)",
			"[Nombre Cliente], el servicio Hogar de tu pedido MT está en proceso de atención y será entregado en un máximo de 7 días desde que realizaste tu solicitud."),
	INGRESADO("INGRESADO", 2, "REGISTRADO",
			"[Nombre Cliente], el servicio Hogar de tu pedido MT está en proceso de atención y la instalación se realizará el (indicar día) y (indicar nombre)",
			"[Nombre Cliente], el servicio Hogar de tu pedido MT está en proceso de atención y será entregado en un máximo de 7 días desde que realizaste tu solicitud."),
	CAIDA("CAIDA", 3, "CANCELADO",
			"Tu pedido ha sido cancelado, lamentamos los inconvenientes ocasionados. Te recomendamos comunicarte con los canales de venta para reingresar tu pedido (Revisar en sistemas comerciales)",
			""),
	DUMMY_IN_TOA("DUMMY_IN_TOA", 4, "PRE REGISTRADO",
			"[Nombre Cliente], el servicio Hogar de tu pedido MT está en proceso de atención y la instalación se realizará el (indicar día) y (indicar nombre)",
			""),
	IN_TOA("IN_TOA", 5, "REGISTRADO OPCIÓN A AGENDAR", "Tu pedido esta siendo atendido.",
			"Tu pedido esta pendiente de agendamiento."),
	WO_PRESTART("WO_PRESTART", 6, "REGISTRADO TÉCNICO EN CAMINO",
			"Tu técnico está en camino, puedes realizar el seguimiento desde la app Movistar Hogar", ""),
	WO_INIT("WO_INIT", 7, "REGISTRADO TÉCNICO EN CASA", "Tu pedido esta siendo instalado.", ""),
	WO_COMPLETED("WO_COMPLETED", 8, "INSTALACIÓN EXITOSA", "Tu pedido fue instalado con éxito.", ""),
	SCHEDULED("SCHEDULED", 9, "REGISTRADO OPCIÓN A REAGENDAR", "Tu pedido esta siendo atendido.",
			"Tu pedido esta siendo atendido."),
	FICTICIOUS_SCHEDULED("FICTICIOUS_SCHEDULED", 10, "PRE REGISTRADO",
			"[Nombre Cliente], el servicio Hogar de tu pedido MT está en proceso de atención y la instalación se realizará el (indicar día) y (indicar nombre)",
			""),
	WO_CANCEL("WO_CANCEL", 11, "AGENDA CANCELADA",
			"Tu cita de instalación fue cancelada, lamentamos los inconvenientes ocasionados. Estamos realizando las gestiones necesarias para continuar con el proceso de entrega en un plazo máximo de 11 días.",
			""),
	WO_RESCHEDULE("WO_RESCHEDULE", 12, "REGISTRADO OPCIÓN A REAGENDAR", "Tu pedido esta siendo atendido.", ""),
	WO_NOTDONE("WO_NOTDONE", 13, "PEDIDO NO INSTALADO",
			"[Nombre Cliente], hemos cancelado tu pedido debido a que no contamos con las facilidades de conectividad en tu zona para la instalación. Lamentamos los inconvenientes.",
			""),
	CANCEL("CANCEL", 14, "CANCELADO POR CLIENTE",
			"La solicitud de cancelación de tu pedido Movistar fue procesada exitosamente. [Si el cliente menciona que él no solicitó la cancelación, sigue flujo regular de reingreso de pedido]",
			"");

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
