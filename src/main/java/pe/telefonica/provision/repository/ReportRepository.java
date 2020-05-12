package pe.telefonica.provision.repository;

import java.util.List;

import pe.telefonica.provision.controller.request.report.ReportInviteMessageRequest;
import pe.telefonica.provision.model.Provision;

public interface ReportRepository {
	
	List<Provision> getProvisionsByInviteMessageDate(ReportInviteMessageRequest request);

}
