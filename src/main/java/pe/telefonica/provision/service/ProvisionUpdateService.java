package pe.telefonica.provision.service;

import pe.telefonica.provision.controller.request.KafkaTOARequest;

public interface ProvisionUpdateService {

	boolean provisionUpdateFromTOA(KafkaTOARequest kafkaTOARequest) throws Exception;

	public interface ProvisionUpdateAsisService extends ProvisionUpdateService {

	}

	public interface ProvisionUpdateTobeService extends ProvisionUpdateService {

	}
}
