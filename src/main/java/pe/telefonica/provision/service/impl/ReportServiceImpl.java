package pe.telefonica.provision.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.telefonica.provision.controller.request.report.ReportInviteMessageRequest;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.repository.ReportRepository;
import pe.telefonica.provision.service.ReportService;

@Service
@Transactional
public class ReportServiceImpl implements ReportService{

	@Autowired
	private ReportRepository reportRepository;
	
	@Override
	public List<Provision> getFaultsByInviteMessageDate(ReportInviteMessageRequest request) {
		return reportRepository.getProvisionsByInviteMessageDate(request);
	}

}
