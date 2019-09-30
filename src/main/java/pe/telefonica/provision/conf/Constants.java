package pe.telefonica.provision.conf;

public final class Constants {

	public static final String ADDRESS_CHANGED = "addressChanged";
	public static final String ADDRESS_CANCELLED_BY_CUSTOMER = "cancelledByCustomer";
	public static final String ADDRESS_CANCELLED_BY_CHANGE   = "cancelledByChange";
	public static final String ADDRESS_UNREACHABLE  		 = "unreachable";
	
	public static final String PROVISION_STATUS_INCOMPLETE = "incomplete";
	public static final String PROVISION_STATUS_ACTIVE = "active";
	public static final String PROVISION_STATUS_ADDRESS_CHANGED = "addressChanged";
	public static final String PROVISION_STATUS_CANCELLED = "cancelled";
	
	public static final String DATE_FORMAT_WS = "dd/MM/yyyy";
	public static final String DATE_FORMAT_BO = "yyyy-MM-dd";
	public static final String DATE_FORMAT_PSI_AUTH = "yyyy-MM-dd";
	public static final String DATE_FORMAT_EMAILING = "dd' de 'MMMMM' de 'yyyy";
	public static final String TIMESTAMP_FORMAT_PSI  = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public static final String TIMESTAMP_FORMAT_USER = "yyyy-MM-dd HH:mm:ss";
	
	public static final String ENVIROMENT_PROD = "prod";
	
	public static final String API_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	
	public static final String PARAM_KEY_OAUTH_TOKEN = "PARAM_KEY_OAUTH_TOKEN";
	
	public static final String OPER_GET_OAUTH_TOKEN	 = "OPER_GET_OAUTH_TOKEN";
	
	public static final String APP_NAME_FRONT_END	 = "APP_FRONT_END";
	public static final String APP_NAME_PROVISION 	 = "APP_PROVISION";
	public static final String APP_NAME_AGENDAMIENTO = "APP_AGENDAMIENTO";
	public static final String APP_NAME_SEGURIDAD 	 = "APP_SEGURIDAD";
	public static final String APP_NAME_AVERIA 		 = "APP_AVERIA";
	
	public static final String USER_PROVISION 	 = "USER_PROVISION";
	public static final String USER_AGENDAMIENTO = "USER_AGENDAMIENTO";
	public static final String USER_SEGURIDAD 	 = "USER_SEGURIDAD";
	public static final String USER_AVERIA 		 = "USER_AVERIA";
	
	public static final String TEXT_NAME_REPLACE 	= "TEXT_NAME_REPLACE";
	public static final String TEXT_PRODUCT_REPLACE = "TEXT_PRODUCT_REPLACE";
	
	public static final String MSG_CONTACT_UPDATED_KEY 		= "MSG_CONTACT_UPDATED_KEY";
	public static final String MSG_ADDRESS_UPDATED_KEY 		= "MSG_ADDRESS_UPDATED_KEY";
	
	//Cuando el BO no se logra contactar con el cliente 
	public static final String MSG_PRO_CUSTOMER_UNREACHABLE_KEY  = "MSG_PRO_CUSTOMER_UNREACHABLE_KEY";
	//Cuando el BO cancela la orden
	public static final String MSG_PRO_CANCELLED_BY_BO_KEY 		 = "MSG_PRO_CANCELLED_BY_BO_KEY";
	//Cuando el cliente cancela desde la web
	public static final String MSG_PRO_CANCELLED_BY_CUSTOMER_KEY = "MSG_PRO_CANCELLED_BY_CUSTOMER_KEY";
	
	public static final String OPER_SEND_SMS_BY_ID	 = "OPER_SEND_SMS_BY_ID";
}