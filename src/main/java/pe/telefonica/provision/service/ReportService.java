package pe.telefonica.provision.service;


import pe.telefonica.provision.controller.common.NotificationResponse;
import pe.telefonica.provision.controller.request.report.ReportByRegisterDateRequest;

public interface ReportService {
	Long getProvisionByRegisterDate(ReportByRegisterDateRequest request);

	NotificationResponse countByEventNotication(ReportByRegisterDateRequest request);
}
