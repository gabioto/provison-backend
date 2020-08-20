package pe.telefonica.provision.repository;

import java.util.List;

import pe.telefonica.provision.controller.common.NotificationResponse;
import pe.telefonica.provision.controller.request.report.ReportByRegisterDateRequest;
import pe.telefonica.provision.model.Provision;


public interface ReportRepository {
	
	Long getProvisionsByRegisterDate(ReportByRegisterDateRequest request);
	
	NotificationResponse getCountByEventNotication(ReportByRegisterDateRequest request);

	List<Provision> getAllProvision(ReportByRegisterDateRequest request);
}
