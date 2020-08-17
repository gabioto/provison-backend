package pe.telefonica.provision.model.provision;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.data.mongodb.core.mapping.Field;

public class PendienteDeAprobacion {
	
	@Field("request_date")
	private String requestDate;

	@Field("code_status_request")
	private String codeStatusRequest;

	@Field("change_date")
	private String changeDate;

	@Field("register_date")
	private LocalDateTime registerDate = LocalDateTime.now(ZoneOffset.of("-05:00"));

	public String getRequestDate() {
		return requestDate;
	}

	public String getCodeStatusRequest() {
		return codeStatusRequest;
	}

	public String getChangeDate() {
		return changeDate;
	}

	public LocalDateTime getRegisterDate() {
		return registerDate;
	}

	public void setRequestDate(String requestDate) {
		this.requestDate = requestDate;
	}

	public void setCodeStatusRequest(String codeStatusRequest) {
		this.codeStatusRequest = codeStatusRequest;
	}

	public void setChangeDate(String changeDate) {
		this.changeDate = changeDate;
	}

	public void setRegisterDate(LocalDateTime registerDate) {
		this.registerDate = registerDate;
	}

}
