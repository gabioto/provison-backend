package pe.telefonica.provision.service;

import pe.telefonica.provision.model.Provision;

import java.util.List;

import pe.telefonica.provision.controller.request.report.ReportInviteMessageRequest;

public interface ReportService {
	List<Provision> getFaultsByInviteMessageDate(ReportInviteMessageRequest request);

}
