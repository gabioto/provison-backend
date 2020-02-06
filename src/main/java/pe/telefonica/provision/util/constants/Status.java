package pe.telefonica.provision.util.constants;

public enum Status {

	PENDIENTE("PENDIENTE", 1, "PRE REGISTRADO",
			"Tu pedido se encuentra registrado y tu agenda pendiente de confirmación. En caso de algún inconveniente, nos comunicaremos contigo.\r\n"
					+ "Si el cliente desea reagendar la visita: Para cambiar la fecha de intalación te enviaremos un SMS cuando esté disponible",
			"Tu pedido se encuentra registrado. En caso de algún inconveniente, nos comunicaremos contigo. \r\n"
					+ "Si el cliente desea agendar la visita: Para agendar la fecha de intalación te enviaremos un SMS cuando esté disponible"),
	INGRESADO("INGRESADO", 2, "REGISTRADO",
			"Tu pedido se encuentra registrado y tu agenda pendiente de confirmación. En caso de algún inconveniente, nos comunicaremos contigo.\r\n"
					+ "Si el cliente desea reagendar la visita: Para cambiar la fecha de intalación te enviaremos un SMS cuando esté disponible",
			"\r\n" + "Tu pedido se encuentra registrado. En caso de algún inconveniente, nos comunicaremos contigo.\r\n"
					+ "Si el cliente desea agendar la visita: Para agendar la fecha de intalación te enviaremos un SMS cuando esté disponible"),
	CAIDA("CAIDA", 3, "CANCELADO", "Tu pedido ha sido cancelado. Lamentamos los inconvenientes", ""),
	DUMMY_IN_TOA("DUMMY_IN_TOA", 4, "PRE REGISTRADO",
			"Tu pedido se encuentra registrado y tu agenda pendiente de confirmación. En caso de algún inconveniente, nos comunicaremos contigo.\r\n"
					+ "Si el cliente desea reagendar la visita: Para cambiar la fecha de intalación te enviaremos un SMS cuando esté disponible",
			""),
	IN_TOA("IN_TOA", 5, "REGISTRADO OPCIÓN A AGENDAR",
			"Tu pedido se encuentra registrado. Puedes agendar la visita del técnico en este momento. En caso de algún inconveniente, nos comunicaremos contigo.",
			""),
	WO_PRESTART("WO_PRESTART", 6, "REGISTRADO TÉCNICO EN CAMINO",
			"Tu pedido se encuentra registrado y tu técnico se encuentra en camino. En caso de algún inconveniente, nos comunicaremos contigo.",
			""),
	WO_INIT("WO_INIT", 7, "REGISTRADO TÉCNICO EN CASA",
			"Tu pedido se encuentra registrado y el técnico se encuentra en tu domicilio. En caso de algún inconveniente, nos comunicaremos contigo.",
			""),
	WO_COMPLETED("WO_COMPLETED", 8, "INSTALACIÓN EXITOSA",
			"Tu pedido fue atendido e instalado.Gracias por confiar en Movistar.", ""),
	SCHEDULED("SCHEDULED", 9, "REGISTRADO OPCIÓN A REAGENDAR",
			"Tu pedido se encuentra registrado y tu agenda pendiente de confirmación. En caso de algún inconveniente, nos comunicaremos contigo.",
			""),
	FICTICIOUS_SCHEDULED("FICTICIOUS_SCHEDULED", 10, "PRE REGISTRADO",
			"Tu pedido se encuentra registrado y tu agenda pendiente de confirmación. En caso de algún inconveniente, nos comunicaremos contigo.\r\n"
					+ "Si el cliente desea reagendar la visita: Para cambiar la fecha de intalación te enviaremos un SMS cuando esté disponible",
			""),
	WO_CANCEL("WO_CANCEL", 11, "AGENDA CANCELADA"), WO_RESCHEDULE("WO_RESCHEDULE", 12, "REGISTRADO OPCIÓN A REAGENDAR"),
	WO_NOTDONE("WO_NOTDONE", 13, "PEDIDO NO INSTALADO"), CANCEL("CANCEL", 14, "CANCELADO POR CLIENTE");

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
