package pe.telefonica.provision.repository;

import pe.telefonica.provision.controller.request.report.ReportByRegisterDateRequest;


public interface ReportRepository {
	
	Long getProvisionsByRegisterDate(ReportByRegisterDateRequest request);

}
