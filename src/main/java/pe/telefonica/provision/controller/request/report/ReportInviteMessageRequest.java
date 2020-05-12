package pe.telefonica.provision.controller.request.report;

import java.time.LocalDateTime;

public class ReportInviteMessageRequest {
	private LocalDateTime startDateStr;
	private LocalDateTime endDateStr;

	public LocalDateTime getStartDateStr() {
		return startDateStr;
	}

	public void setStartDateStr(LocalDateTime startDateStr) {
		this.startDateStr = startDateStr;
	}

	public LocalDateTime getEndDateStr() {
		return endDateStr;
	}

	public void setEndDateStr(LocalDateTime endDateStr) {
		this.endDateStr = endDateStr;
	}
}
