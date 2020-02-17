package pe.telefonica.provision.controller.request.monitoring;

import java.time.LocalDateTime;

public class GetProvisionByStatusRequest {
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private String[] status;

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}

	public String[] getStatus() {
		return status;
	}

	public void setStatus(String[] status) {
		this.status = status;
	}
	
	
	
}
