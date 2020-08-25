package pe.telefonica.provision.service;


import java.util.List;

import pe.telefonica.provision.controller.common.NotificationResponse;
import pe.telefonica.provision.controller.request.report.ReportByRegisterDateRequest;
import pe.telefonica.provision.model.Provision;

public interface ReportService {
	Long getProvisionByRegisterDate(ReportByRegisterDateRequest request);

	NotificationResponse countByEventNotication(ReportByRegisterDateRequest request);
	
	List<Provision> getAllProvisions(ReportByRegisterDateRequest request);
}
