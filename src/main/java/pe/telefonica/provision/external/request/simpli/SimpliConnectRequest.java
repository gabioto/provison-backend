package pe.telefonica.provision.external.request.simpli;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;



public class SimpliConnectRequest implements Serializable  {


	private static final long serialVersionUID = -5185708381940257080L;

	private HeaderIn HeaderIn;
	private BodyIn BodyIn;

	@JsonProperty("HeaderIn")
	public HeaderIn getHeaderIn() {
		return HeaderIn;
	}

	public void setHeaderIn(HeaderIn headerIn) {
		HeaderIn = headerIn;
	}

	@JsonProperty("BodyIn")
	public BodyIn getBodyIn() {
		return BodyIn;
	}

	public void setBodyIn(BodyIn bodyIn) {
		BodyIn = bodyIn;
	}

	public SimpliConnectRequest generateRequest(Double latitude, Double longitude, String visit_title, String visit_address,
			String driver_username) {
		SimpliConnectRequest request = new SimpliConnectRequest();

		HeaderIn headerIn = new HeaderIn();
		headerIn.setCountry("PE");
		headerIn.setLang("es");
		headerIn.setEntity("TDP");
		headerIn.setSystem("COLTRA");
		headerIn.setSubsystem("COLTRA");
		headerIn.setOriginator("PE:TDP:COLTRA:COLTRA");
		headerIn.setSender("OracleServiceBus");
		headerIn.setUserId("USERCOLTRA");
		headerIn.setWsId("SistemTRAZA");
		headerIn.setWsIp("192.168.100.1");
		headerIn.setWsIpv6("0101:ca75:0101:ca75:0101:ca75:0101:ca75");
		headerIn.setOperation("LiveTracking");
		headerIn.setDestination("PE:TDP:COLTRA:COLTRA");
		headerIn.setPid("550e8400-e29b-41d4-a716-446655440000");
		headerIn.setExecld("550e8400-e29b-41d4-a716-446655440000");
		headerIn.setMsgld("550e8400-e29b-41d4-a716-446655440021");
		headerIn.setTimestamp("2015-07-15T14:53:47.233-05:00");
		headerIn.setMsgType("REQUEST");

		VarArg varArgDatos = new VarArg();

		List<KeyValues> listkeyValues = new ArrayList<KeyValues>();
		KeyValues keyValues = new KeyValues();
		keyValues.setKey("token");
		ValuesToken valuesToken = new ValuesToken();
		valuesToken.setValue("token");
		keyValues.setValues(valuesToken);
		listkeyValues.add(keyValues);
		varArgDatos.setArg(listkeyValues);

		headerIn.setVarArg(varArgDatos);

		BodyIn bodyIn = new BodyIn();
		bodyIn.setStart_lat(-33.429372);
		bodyIn.setStart_lon(-70.618796);
		bodyIn.setEnd_lat(latitude);
		bodyIn.setEnd_lon(longitude);
		bodyIn.setVisit_title(visit_title);
		bodyIn.setVisit_address(visit_address);
		bodyIn.setDriver_username(driver_username);

		request.setHeaderIn(headerIn);
		request.setBodyIn(bodyIn);

		return request;
	}

	public class HeaderIn {
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
		private String wsIpv6;
		private String operation;
		private String destination;
		private String pid;
		private String execld;
		private String msgld;
		private String timestamp;
		private String msgType;
		private VarArg varArg;

		@Override
		public String toString() {
			return "HeaderIn [country=" + country + ", lang=" + lang + ", entity=" + entity + ", system=" + system
					+ ", subsystem=" + subsystem + ", originator=" + originator + ", sender=" + sender + ", userId="
					+ userId + ", wsId=" + wsId + ", wsIp=" + wsIp + ", wsIpv6=" + wsIpv6 + ", operation=" + operation
					+ ", destination=" + destination + ", pid=" + pid + ", execld=" + execld + ", msgld=" + msgld
					+ ", timestamp=" + timestamp + ", msgType=" + msgType + ", varArg=" + varArg + "]";
		}

		public VarArg getVarArg() {
			return varArg;
		}

		public void setVarArg(VarArg varArg) {
			this.varArg = varArg;
		}

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

		public String getWsIpv6() {
			return wsIpv6;
		}

		public void setWsIpv6(String wsIpv6) {
			this.wsIpv6 = wsIpv6;
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

		public String getPid() {
			return pid;
		}

		public void setPid(String pid) {
			this.pid = pid;
		}

		public String getExecld() {
			return execld;
		}

		public void setExecld(String execld) {
			this.execld = execld;
		}

		public String getMsgld() {
			return msgld;
		}

		public void setMsgld(String msgld) {
			this.msgld = msgld;
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

	public class VarArg {
		private List<KeyValues> arg;

		@Override
		public String toString() {
			return "VarArg [arg=" + arg + "]";
		}

		public List<KeyValues> getArg() {
			return arg;
		}

		public void setArg(List<KeyValues> arg) {
			this.arg = arg;
		}

	}

	public class KeyValues {
		private String key;
		private ValuesToken values;

		@Override
		public String toString() {
			return "KeyValues [key=" + key + ", values=" + values + "]";
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public ValuesToken getValues() {
			return values;
		}

		public void setValues(ValuesToken values) {
			this.values = values;
		}

	}

	public class ValuesToken {
		private String value;

		@Override
		public String toString() {
			return "ValuesToken [value=" + value + "]";
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}

	public class BodyIn {

		private Double start_lat;
		private Double start_lon;
		private Double end_lat;
		private Double end_lon;
		private String visit_title;
		private String visit_address;
		private String driver_username;

		@Override
		public String toString() {
			return "BodyIn [start_lat=" + start_lat + ", start_lon=" + start_lon + ", end_lat=" + end_lat + ", end_lon="
					+ end_lon + ", visit_title=" + visit_title + ", visit_address=" + visit_address
					+ ", driver_username=" + driver_username + "]";
		}

		public Double getStart_lat() {
			return start_lat;
		}

		public void setStart_lat(Double start_lat) {
			this.start_lat = start_lat;
		}

		public Double getStart_lon() {
			return start_lon;
		}

		public void setStart_lon(Double start_lon) {
			this.start_lon = start_lon;
		}

		public Double getEnd_lat() {
			return end_lat;
		}

		public void setEnd_lat(Double end_lat) {
			this.end_lat = end_lat;
		}

		public Double getEnd_lon() {
			return end_lon;
		}

		public void setEnd_lon(Double end_lon) {
			this.end_lon = end_lon;
		}

		public String getVisit_title() {
			return visit_title;
		}

		public void setVisit_title(String visit_title) {
			this.visit_title = visit_title;
		}

		public String getVisit_address() {
			return visit_address;
		}

		public void setVisit_address(String visit_address) {
			this.visit_address = visit_address;
		}

		public String getDriver_username() {
			return driver_username;
		}

		public void setDriver_username(String driver_username) {
			this.driver_username = driver_username;
		}

	}


}
