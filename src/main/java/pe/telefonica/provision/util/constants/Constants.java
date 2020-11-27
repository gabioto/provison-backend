package pe.telefonica.provision.util.constants;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Constants {

	public static final String ADDRESS_CHANGED = "addressChanged";
	public static final String ADDRESS_CANCELLED_BY_CUSTOMER = "cancelledByCustomer";
	public static final String ADDRESS_CANCELLED_BY_CHANGE = "cancelledByChange";
	public static final String ADDRESS_UNREACHABLE = "unreachable";

	public static final String PROVISION_STATUS_INCOMPLETE = "incomplete";
	public static final String PROVISION_STATUS_ACTIVE = "active";
	public static final String PROVISION_STATUS_SCHEDULE_IN_PROGRESS = "scheduleInProgress";
	public static final String PROVISION_STATUS_ADDRESS_CHANGED = "addressChanged";
	public static final String PROVISION_STATUS_CAIDA = "caida";
	public static final String PROVISION_STATUS_CANCELLED = "cancelled";

	public static final String PROVISION_STATUS_COMPLETED = "completed";
	public static final String PROVISION_STATUS_DONE = "done";
	public static final String PROVISION_STATUS_RESCHEDULE = "schedule";
	public static final String PROVISION_STATUS_NOTDONE = "notDone";
	public static final String PROVISION_STATUS_WOINIT = "init";

	public static final String COMPONENTS_NAME_TV = "TV";
	public static final String COMPONENTS_NAME_INTERNET = "INTERNET";
	public static final String COMPONENTS_NAME_LINE = "LINEA";
	public static final String COMPONENTS_TITLE_TV = "Televisión HD";
	public static final String COMPONENTS_TITLE_INTERNET = "Internet Ilimitado";
	public static final String COMPONENTS_TITLE_LINE = "Línea Fija";
	public static final String COMPONENTS_DESC_TV = "Canales exclusivos Movistar";
	public static final String COMPONENTS_DESC_INTERNET = "Incluye Movistar Play Full";
	public static final String COMPONENTS_DESC_LINE = "Llamadas ilimitadas a fijos Movistar";

	public static final String DATE_FORMAT_WS = "dd/MM/yyyy";
	public static final String DATE_FORMAT_BO = "yyyy-MM-dd";
	public static final String DATE_FORMAT_PSI_AUTH = "yyyy-MM-dd";
	public static final String DATE_FORMAT_EMAILING = "dd' de 'MMMMM' de 'yyyy";
	public static final String TIMESTAMP_FORMAT_PSI = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public static final String TIMESTAMP_FORMAT_USER = "yyyy-MM-dd HH:mm:ss";
	public static final String TIMESTAMP_FORMAT_ORDERS = "dd/MM/yyyy HH:mm:ss";
	public static final String TIMESTAMP_FORMAT_CMS_ATIS = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public static final String TIME_ZONE_LOCALE = "-05:00";

	public static final String ENVIROMENT_PROD = "prod";

	public static final String API_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	public static final String PARAM_KEY_OAUTH_TOKEN = "PARAM_KEY_OAUTH_TOKEN";

	public static final String OPER_GET_TECH_AVAILABLE = "OPER_GET_TECH_AVAILABLE";
	public static final String OPER_GET_TOKEN_EXTERNAL = "OPER_GET_TOKEN_EXTERNAL";
	public static final String OPER_GET_OAUTH_TOKEN = "OPER_GET_OAUTH_TOKEN";
	public static final String OPER_GET_ALL_IN_TIME_RANGE = "OPER_GET_ALL_IN_TIME_RANGE";
	public static final String OPER_GET_ORDER_TO_NOTIFY = "OPER_GET_ORDER_TO_NOTIFY";

	public static final String APP_NAME_FRONT_END = "APP_FRONT_END";
	public static final String APP_NAME_PROVISION = "APP_PROVISION";
	public static final String APP_NAME_AGENDAMIENTO = "APP_AGENDAMIENTO";
	public static final String APP_NAME_SEGURIDAD = "APP_SEGURIDAD";
	public static final String APP_NAME_AVERIA = "APP_AVERIA";

	public static final String USER_PROVISION = "USER_PROVISION";
	public static final String USER_AGENDAMIENTO = "USER_AGENDAMIENTO";
	public static final String USER_SEGURIDAD = "USER_SEGURIDAD";
	public static final String USER_AVERIA = "USER_AVERIA";

	public static final String TEXT_NAME_REPLACE = "[$name]";
	public static final String TEXT_PRODUCT_REPLACE = "[$product]";

	public static final String MSG_CONTACT_UPDATED_KEY = "MSG_CONTACT_UPDATED_KEY";
	public static final String MSG_ADDRESS_UPDATED_KEY = "MSG_ADDRESS_UPDATED_KEY";

	// Cuando el BO no se logra contactar con el cliente
	public static final String MSG_PRO_CUSTOMER_UNREACHABLE_KEY = "MSG_PRO_CUSTOMER_UNREACHABLE_KEY";
	// Cuando el BO cancela la orden
	public static final String MSG_PRO_CANCELLED_BY_BO_KEY = "MSG_PRO_CANCELLED_BY_BO_KEY";
	// Cuando el cliente cancela desde la web
	public static final String MSG_PRO_CANCELLED_BY_CUSTOMER_KEY = "MSG_PRO_CANCELLED_BY_CUSTOMER_KEY";
	public static final String MSG_FAULT_WOPRESTART = "MSG_FAULT_WOPRESTART";
	public static final String MSG_PRO_SCHEDULE_TECHNICIAN_KEY = "MSG_PRO_SCHEDULE_TECHNICIAN_KEY";

	// Codigos de los endpoints de PSI
	public static final String PSI_CODE_CAPACITY = "201";
	public static final String PSI_CODE_SCHEDULE = "202";
	public static final String PSI_CODE_CANCEL = "203";
	public static final String PSI_CODE_UPDATE_CONTACT = "204";

	public static final String GET_ORDERS = "205";

	public static final String OPER_SEND_SMS_BY_ID = "OPER_SEND_SMS_BY_ID";
	public static final String OPER_SEND_MAIL_BY_ID = "OPER_SEND_MAIL_BY_ID";

	public static final String OPER_CONTACT_INFO_UPDATE = "OPER_CONTACT_INFO_UPDATE";
	public static final String OPER_UPDATE_ADDRESS = "OPER_UPDATE_ADDRESS";
	public static final String OPER_UPDATE_ADDRESSBO = "OPER_UPDATE_ADDRESSBO";
	public static final String OPER_VALIDATE_DATA = "OPER_VALIDATE_DATA";
	public static final String OPER_VALIDATE_USER = "OPER_VALIDATE_USER";
	public static final String OPER_MONITORING_PROVISION = "OPER_MONITORING_PROVISION";
	public static final String OPER_GET_SALE_CODE = "OPER_GET_SALE_CODE";
	public static final String OPER_GET_PROVISION_ALL = "OPER_GET_PROVISION_ALL";
	public static final String OPER_GET_PROVISION_BY_REGISTER_DATE = "OPER_GET_PROVISION_BY_REGISTER_DATE";
	public static final String OPER_INSERT_PROVISION = "OPER_INSERT_PROVISION";
	public static final String OPER_PROVISION_UPDATE_FROM_TOA = "OPER_PROVISION_UPDATE_FROM_TOA";
	public static final String OPER_INSERT_PROVISION_CODE_FICT = "OPER_INSERT_PROVISION_CODE_FICT";
	public static final String OPER_ORDER_CANCELLATION = "OPER_ORDER_CANCELLATION";
	public static final String OPER_CANCEL_SCHEDULE = "OPER_CANCEL_SCHEDULE";
	public static final String OPER_SCHEDULE_UPDATE_CODE_FICT = "OPER_SCHEDULE_UPDATE_CODE_FICT";
	public static final String OPER_UPDATE_RESCHEDULE = "OPER_UPDATE_RESCHEDULE";
	public static final String OPER_NOTDONE_SCHEDULE = "OPER_NOTDONE_SCHEDULE";
	public static final String OPER_SHOW_LOCATION = "OPER_UPDATE_SHOW_LOCATION";
	public static final String OPER_INSERT_RATING = "OPER_INSERT_RATING";

	public static final String OPER_UPDATE_STATUS = "OPER_UPDATE_STATUS";
	public static final String OPER_GET_PROVISION_BY_ORDER_CODE = "OPER_GET_PROVISION_BY_ORDER_CODE";
	public static final String OPER_GET_PROVISION_BY_SALE_CODE = "OPER_GET_PROVISION_BY_SALE_CODE";

	public static final String OPER_GET_ALL_PROVISION = "OPER_GET_ALL_PROVISION";

	public static final String TIPO_RUC = "ruc";
	public static final String RUC_NATURAL = "10";

	public static final String STATUS_IN_TOA = "IN_TOA";
	public static final String STATUS_WO_PRESTART = "WO_PRESTART";
	public static final String STATUS_WO_INIT = "WO_INIT";
	public static final String STATUS_WO_COMPLETED = "WO_COMPLETED";
	public static final String STATUS_WO_PRE_NOTDONE = "WO_PRE_NOTDONE";
	public static final String STATUS_WO_NOTDONE = "WO_NOTDONE";
	public static final String STATUS_WO_CANCEL = "WO_CANCEL";
	public static final String STATUS_WO_RESCHEDULE = "WO_RESCHEDULE";
	public static final String FICTICIOUS_SCHEDULED = "FICTICIOUS_SCHEDULED";

	public static final String ACTIVITY_TYPE_PROVISION = "PROVISION";
	public static final String BARRA_VERTICAL = "|";

	public static final String DEFAULT_NOTDONE_SUBREASON = "[$name], tu pedido no pudo ser instalado en la fecha indicada. Lamentamos los inconvenientes.";
	public static final String DEFAULT_NOTDONE_ACTION = "Nos comunicaremos contigo en breve.";

	// ORDERS
	public static final String SOURCE_ORDERS_ORDENES = "ORDENES";
	public static final String SOURCE_ORDERS_VENTAS_FIJA = "VENTASFIJA_PARKUR";
	public static final String SOURCE_ORDERS_ATIS = "ATIS";
	public static final String SOURCE_ORDERS_CMS = "CMS";

	public static final Map<String, String> ATIS_CMS_STATUS = Stream
			.of(new String[][] { { "new", "EN PROCESO" }, { "rejeted", "EN PROCESO" }, { "pending", "EN PROCESO" },
					{ "completed", "COMPLETADO" }, { "rejected", "CANCELADO" }, { "CE", "EN PROCESO" },
					{ "PV", "EN PROCESO" }, { "PD", "EN PROCESO" }, { "PE", "EN PROCESO" }, { "FI", "COMPLETADO" },
					{ "TE", "COMPLETADO" }, { "CG", "CANCELADO" } })
			.collect(Collectors.toMap(data -> data[0], data -> data[1]));

}