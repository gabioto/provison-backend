package pe.telefonica.provision.controller.request.report;

import java.time.LocalDateTime;

public class ReportByRegisterDateRequest {
	private LocalDateTime startDate;
	private LocalDateTime endDate;

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}

	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}

}
