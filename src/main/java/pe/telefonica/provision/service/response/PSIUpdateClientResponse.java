package pe.telefonica.provision.service.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PSIUpdateClientResponse {
	
	private HeaderOut HeaderOut;
	private BodyOut BodyOut;
	
	@JsonProperty("HeaderOut")
	public HeaderOut getHeaderOut() {
		return HeaderOut;
	}

	public void setHeaderOut(HeaderOut headerOut) {
		HeaderOut = headerOut;
	}

	@JsonProperty("BodyOut")
	public BodyOut getBodyOut() {
		return BodyOut;
	}

	public void setBodyOut(BodyOut bodyOut) {
		BodyOut = bodyOut;
	}
	
	
	
	@Override
	public String toString() {
		return "PSIUpdateClientResponse [HeaderOut=" + HeaderOut + ", BodyOut=" + BodyOut + "]";
	}



	public class HeaderOut{
		private String originator;
		private String destination;
		private String execId;
		private String timestamp;
		private String msgType;
		
		public String getOriginator() {
			return originator;
		}
		public void setOriginator(String originator) {
			this.originator = originator;
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
		@Override
		public String toString() {
			return "HeaderOut [originator=" + originator + ", destination=" + destination + ", execId=" + execId
					+ ", timestamp=" + timestamp + ", msgType=" + msgType + "]";
		}
		
		
	}
	
	public class BodyOut{
		
		private String error;
		private String code_error;
		private String message;
		private String solicitud;
		
		public String getError() {
			return error;
		}
		public void setError(String error) {
			this.error = error;
		}
		public String getCode_error() {
			return code_error;
		}
		public void setCode_error(String code_error) {
			this.code_error = code_error;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public String getSolicitud() {
			return solicitud;
		}
		public void setSolicitud(String solicitud) {
			this.solicitud = solicitud;
		}
		@Override
		public String toString() {
			return "BodyOut [error=" + error + ", code_error=" + code_error + ", message=" + message + ", solicitud="
					+ solicitud + "]";
		}
		
		
	}
	
	
}
