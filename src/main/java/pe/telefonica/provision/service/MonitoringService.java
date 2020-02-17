package pe.telefonica.provision.service;

import pe.telefonica.provision.controller.request.monitoring.GetProvisionByStatusRequest;
import pe.telefonica.provision.controller.response.monitoring.GetProvisionByStatusResponse;

public interface MonitoringService {
	
	public GetProvisionByStatusResponse GetProvisionByStatus(GetProvisionByStatusRequest request);
	
}
