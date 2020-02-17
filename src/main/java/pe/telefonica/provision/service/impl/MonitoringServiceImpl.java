package pe.telefonica.provision.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pe.telefonica.provision.controller.request.monitoring.GetProvisionByStatusRequest;
import pe.telefonica.provision.controller.response.monitoring.GetProvisionByStatusResponse;
import pe.telefonica.provision.service.MonitoringService;
import pe.telefonica.provision.repository.MonitoringRepository;

@Service
public class MonitoringServiceImpl implements MonitoringService {

	@Autowired
	MonitoringRepository monitoringRepository;

	@Override
	public GetProvisionByStatusResponse GetProvisionByStatus(GetProvisionByStatusRequest request) {

		GetProvisionByStatusResponse response = new GetProvisionByStatusResponse();
		String mesague = "";
		for (String item : request.getStatus()) {
			long quantity = monitoringRepository.getQuantityRegisterByStatus(request.getStartDate(),
					request.getEndDate(), item);
			mesague += quantity + " " + " " + item + "; ";
		}

		response.setQuantity(mesague);

		// return response;
		return response;
	}

}
