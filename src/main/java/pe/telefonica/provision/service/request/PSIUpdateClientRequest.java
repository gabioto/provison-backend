package pe.telefonica.provision.service.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PSIUpdateClientRequest {

	private HeaderIn HeaderIn;
	private BodyUpdateClient BodyUpdateClient; 
	
	public PSIUpdateClientRequest() {
		HeaderIn = new HeaderIn();
		BodyUpdateClient = new BodyUpdateClient();
	}
	
	@JsonProperty("HeaderIn")
	public HeaderIn getHeaderIn() {
		return HeaderIn;
	}

	public void setHeaderIn(HeaderIn headerIn) {
		HeaderIn = headerIn;
	}

	@JsonProperty("BodyUpdateClient")
	public BodyUpdateClient getBodyUpdateClient() {
		return BodyUpdateClient;
	}

	public void setBodyUpdateClient(BodyUpdateClient bodyUpdateClient) {
		BodyUpdateClient = bodyUpdateClient;
	}

	public class HeaderIn{
		
		private String country;
		private String lang;
		private String entity;
		private String system;
		private String subsystem;
		private String originator;
		private String sender;
		private String userId;
		private String wsId;
		private String wsIp;
		private String operation;
		private String destination;
		private String execId;
		private String timestamp;
		private String msgType;
		
		public String getCountry() {
			return country;
		}
		public void setCountry(String country) {
			this.country = country;
		}
		public String getLang() {
			return lang;
		}
		public void setLang(String lang) {
			this.lang = lang;
		}
		public String getEntity() {
			return entity;
		}
		public void setEntity(String entity) {
			this.entity = entity;
		}
		public String getSystem() {
			return system;
		}
		public void setSystem(String system) {
			this.system = system;
		}
		public String getSubsystem() {
			return subsystem;
		}
		public void setSubsystem(String subsystem) {
			this.subsystem = subsystem;
		}
		public String getOriginator() {
			return originator;
		}
		public void setOriginator(String originator) {
			this.originator = originator;
		}
		public String getSender() {
			return sender;
		}
		public void setSender(String sender) {
			this.sender = sender;
		}
		public String getUserId() {
			return userId;
		}
		public void setUserId(String userId) {
			this.userId = userId;
		}
		public String getWsId() {
			return wsId;
		}
		public void setWsId(String wsId) {
			this.wsId = wsId;
		}
		public String getWsIp() {
			return wsIp;
		}
		public void setWsIp(String wsIp) {
			this.wsIp = wsIp;
		}
		public String getOperation() {
			return operation;
		}
		public void setOperation(String operation) {
			this.operation = operation;
		}
		public String getDestination() {
			return destination;
		}
		public void setDestination(String destination) {
			this.destination = destination;
		}
		public String getExecId() {
			return execId;
		}
		public void setExecId(String execId) {
			this.execId = execId;
		}
		public String getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}
		public String getMsgType() {
			return msgType;
		}
		public void setMsgType(String msgType) {
			this.msgType = msgType;
		}
	}
	
	public class BodyUpdateClient{
		
		private User user;
		private String solicitud;
		private String nombre_completo;
		private String correo;
		private String telefono1;
		
		public BodyUpdateClient() {
			user = new User();
		}
		
		public User getUser() {
			return user;
		}
		public void setUser(User user) {
			this.user = user;
		}
		public String getSolicitud() {
			return solicitud;
		}
		public void setSolicitud(String solicitud) {
			this.solicitud = solicitud;
		}
		public String getNombre_completo() {
			return nombre_completo;
		}
		public void setNombre_completo(String nombre_completo) {
			this.nombre_completo = nombre_completo;
		}
		public String getCorreo() {
			return correo;
		}
		public void setCorreo(String correo) {
			this.correo = correo;
		}
		public String getTelefono1() {
			return telefono1;
		}
		public void setTelefono1(String telefono1) {
			this.telefono1 = telefono1;
		}

		public class User{
			private String now;
			private String login;
			private String company;
			private String auth_string;
			
			public String getNow() {
				return now;
			}
			public void setNow(String now) {
				this.now = now;
			}
			public String getLogin() {
				return login;
			}
			public void setLogin(String login) {
				this.login = login;
			}
			public String getCompany() {
				return company;
			}
			public void setCompany(String company) {
				this.company = company;
			}
			public String getAuth_string() {
				return auth_string;
			}
			public void setAuth_string(String auth_string) {
				this.auth_string = auth_string;
			}
		}
		
		
	}
	
}
