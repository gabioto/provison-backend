package pe.telefonica.provision.util.constants;

public enum Status {

	PENDIENTE("PENDIENTE", 1, "PRE REGISTRADO",
			"Hola [$name], tu pedido Hogar está en evaluación. Cuando se confirme procederemos con la instalación el [INDICAR DIA y TURNO] según lo solicitaste",
			"Hola [$name], tu pedido Hogar está en evaluación. Cuando se  confirme te enviaremos un SMS para que agendes tu visita para la instalación",
			"Estamos evaluando tu pedido"),
	INGRESADO("INGRESADO", 2, "REGISTRADO",
			"Hola [$name], tu pedido Hogar está en evaluación. Cuando se confirme procederemos con la instalación el [INDICAR DIA y TURNO] según lo solicitaste",
			"Hola [$name], tu pedido Hogar está en evaluación. Cuando se  confirme te enviaremos un SMS para que agendes tu visita para la instalación",
			"Estamos evaluando tu pedido"),
	CAIDA("CAIDA", 3, "CANCELADO",
			"Hola, tu pedido ha sido cancelado, lamentamos el inconveniente. Por favor comunicate al 080011800 para reingresar tu pedido",
			"", "Tu pedido ha sido cancelado"),
	DUMMY_IN_TOA("DUMMY_IN_TOA", 4, "PRE REGISTRADO",
			"Hola [$name], tu pedido hogar está en evaluación. Cuando se confirme, la instalación se realizará el [INDICAR DIA Y TURNO] según lo solicitaste",
			"", "Estamos atendiendo tu pedido"),
	IN_TOA("IN_TOA", 5, "REGISTRADO OPCIÓN A AGENDAR",
			"Hola, estamos atendiendo tu pedido. Puedes hacer seguimiento desde nuestra App Hogar en Android o IOS",
			"Hola, falta agendar tu pedido. Puedes realizarlo desde nuestra App Hogar", "Pendiente de agendamiento"),
	WO_PRESTART("WO_PRESTART", 6, "REGISTRADO TÉCNICO EN CAMINO",
			"Hola, tu técnico está en camino, puedes hacer el seguimiento desde nuestra app Movistar Hogar", "",
			"El técnico está en camino a la instalación"),
	WO_INIT("WO_INIT", 7, "REGISTRADO TÉCNICO EN CASA",
			"Hola, tu pedido se está instalando. Puedes hacer el seguimiento desde la app Movistar Hogar", "",
			"Estamos instalando tu pedido"),
	WO_COMPLETED("WO_COMPLETED", 8, "INSTALACIÓN EXITOSA", "Hola, tu pedido se instaló con éxito", "",
			"Instalamos tu pedido con éxito"),
	SCHEDULED("SCHEDULED", 9, "REGISTRADO OPCIÓN A REAGENDAR",
			"Hola, tu pedido está en evaluación. Puedes hacer seguimiento desde nuestra app Movistar Hogar", "",
			"Estamos atendiendo tu pedido"),
	FICTICIOUS_SCHEDULED("FICTICIOUS_SCHEDULED", 10, "PRE REGISTRADO",
			"Hola [$name], tu pedido Hogar está en evaluación. Cuando tengamos la confirmación la instalación se realizará el [INDICAR DIA Y TURNO] según lo solicitaste",
			"", "Estamos atendiendo tu pedido"),
	WO_CANCEL("WO_CANCEL", 11, "AGENDA CANCELADA",
			"Hola, tu cita para la instalación de tu pedido fue cancelada, lamentamos el inconveniente.", "",
			"Tu cita de instalación fue cancelada"),
	WO_RESCHEDULE("WO_RESCHEDULE", 12, "REGISTRADO OPCIÓN A REAGENDAR",
			"Hola, tu pedido está en evaluación. Puedes hacer seguimiento desde nuestra app Movistar Hogar", "",
			"Estamos atendiendo tu pedido"),
	WO_NOTDONE("WO_NOTDONE", 13, "PEDIDO NO INSTALADO",
			"Hola [$name], tu pedido no pudo ser instalado en la fecha indicada. Lamentamos los inconvenientes.", "",
			"Tu pedido no fue instalado, lamentamos los inconvenientes"),
	CANCEL("CANCEL", 14, "CANCELADO POR CLIENTE",
			"Hola, la solicitud de cancelación de tu pedido Movistar se realizo con éxito. [Si el cliente menciona que él no solicitó la cancelación, sigue flujo regular de reingreso de pedido]",
			"", "Tu pedido ha sido cancelado"),
	PENDIENTE_PAGO("PENDIENTE-PAGO", 15, "PENDIENTE DE PAGO", "", "", "Pendiente de pago"),
	PAGADO("PAGADO", 16, "PAGADO", "", "", "Pagado"),
	
	FINALIZADO("FINALIZADO", 17, "FINALIZADO", "generic speech", "", "Tu nuevo servicio ya está disponible"),
	TERMINADA("TERMINADA", 18, "TERMINADA", "generic speech", "", "Tu nuevo servicio ya está disponible"),
	CANCELADA_ATIS("CANCELADA_ATIS", 19, "CANCELADA_ATIS", "generic speech", "", "Tu pedido ha sido cancelado"),
	PENDIENTE_DE_VALIDACION("PENDIENTE_DE_VALIDACION", 20, "PENDIENTE DE VALIDACION", "", "", "Pendiente de validacion"),
	CONFIGURADA("CONFIGURADA", 21, "CONFIGURADA", "", "", "Configurada"),
	PENDIENTE_DE_APROBACION("PENDIENTE_DE_APROBACION", 22, "PENDIENTE DE APROBACION", "", "", "Pendiente de aprovacion"),
	PETICION_PENDIENTE("PETICION_PENDIENTE", 23, "PETICION PENDIENTE", "", "", "Peticion Pendiente"),	
	WO_PRENOTDONE("WO_PRENOTDONE", 24, "PEDIDO NO INSTALADO", "", "", "Tu pedido no fue instalado, lamentamos los inconvenientes");
	
	private String statusName;
	private int statusId;
	private String description;
	private String genericSpeech;
	private String speechWithoutSchedule;
	private String frontSpeech;

	private Status(String statusName, int statusId, String description) {
		this.statusName = statusName;
		this.statusId = statusId;
		this.description = description;
	}

	private Status(String statusName, int statusId, String description, String genericSpeech,
			String speechWithoutSchedule, String frontSpeech) {
		this.statusName = statusName;
		this.statusId = statusId;
		this.description = description;
		this.genericSpeech = genericSpeech;
		this.speechWithoutSchedule = speechWithoutSchedule;
		this.frontSpeech = frontSpeech;
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

	public String getFrontSpeech() {
		return frontSpeech;
	}

}