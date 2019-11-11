package pe.telefonica.provision.util.constants;

public final class Constants {

	public static final String ADDRESS_CHANGED = "addressChanged";
	public static final String ADDRESS_CANCELLED_BY_CUSTOMER = "cancelledByCustomer";
	public static final String ADDRESS_CANCELLED_BY_CHANGE = "cancelledByChange";
	public static final String ADDRESS_UNREACHABLE = "unreachable";

	public static final String PROVISION_STATUS_INCOMPLETE = "incomplete";
	public static final String PROVISION_STATUS_ACTIVE = "active";
	public static final String PROVISION_STATUS_ADDRESS_CHANGED = "addressChanged";
	public static final String PROVISION_STATUS_CANCELLED = "cancelled";

	public static final String DATE_FORMAT_WS = "dd/MM/yyyy";
	public static final String DATE_FORMAT_BO = "yyyy-MM-dd";
	public static final String DATE_FORMAT_PSI_AUTH = "yyyy-MM-dd";
	public static final String DATE_FORMAT_EMAILING = "dd' de 'MMMMM' de 'yyyy";
	public static final String TIMESTAMP_FORMAT_PSI = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public static final String TIMESTAMP_FORMAT_USER = "yyyy-MM-dd HH:mm:ss";

	public static final String ENVIROMENT_PROD = "prod";

	public static final String API_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	public static final String PARAM_KEY_OAUTH_TOKEN = "PARAM_KEY_OAUTH_TOKEN";

	public static final String OPER_GET_OAUTH_TOKEN = "OPER_GET_OAUTH_TOKEN";
	public static final String OPER_GET_ALL_IN_TIME_RANGE = "OPER_GET_ALL_IN_TIME_RANGE";

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

	// Codigos de los endpoints de PSI
	public static final String PSI_CODE_CAPACITY = "201";
	public static final String PSI_CODE_SCHEDULE = "202";
	public static final String PSI_CODE_CANCEL = "203";
	public static final String PSI_CODE_UPDATE_CONTACT = "204";

	public static final String OPER_SEND_SMS_BY_ID = "OPER_SEND_SMS_BY_ID";
	public static final String OPER_SEND_MAIL_BY_ID = "OPER_SEND_MAIL_BY_ID";

	public static final String OPER_CONTACT_INFO_UPDATE = "OPER_CONTACT_INFO_UPDATE";
	public static final String OPER_UPDATE_ADDRESS = "OPER_UPDATE_ADDRESS";
	public static final String OPER_UPDATE_ADDRESSBO = "OPER_UPDATE_ADDRESSBO";
	public static final String OPER_VALIDATE_DATA = "OPER_VALIDATE_DATA";
	public static final String OPER_VALIDATE_USER = "OPER_VALIDATE_USER";
	public static final String OPER_GET_PROVISION_ALL = "OPER_GET_PROVISION_ALL";
	public static final String OPER_INSERT_PROVISION = "OPER_INSERT_PROVISION";
	public static final String OPER_PROVISION_UPDATE_FROM_TOA = "OPER_PROVISION_UPDATE_FROM_TOA";
	public static final String OPER_INSERT_PROVISION_CODE_FICT = "OPER_INSERT_PROVISION_CODE_FICT";
	public static final String OPER_ORDER_CANCELLATION = "OPER_ORDER_CANCELLATION";
	public static final String OPER_CANCEL_SCHEDULE = "OPER_CANCEL_SCHEDULE";
	public static final String OPER_UPDATE_STATUS = "OPER_UPDATE_STATUS";
	public static final String OPER_GET_PROVISION_BY_ORDER_CODE = "OPER_GET_PROVISION_BY_ORDER_CODE";
}