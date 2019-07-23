package pe.telefonica.provision.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.query.Update;

import pe.telefonica.provision.api.request.ProvisionRequest;
import pe.telefonica.provision.dto.Provision;
import pe.telefonica.provision.dto.Queue;

public interface ProvisionRepository {

	Optional<List<Provision>> findAll(ProvisionRequest provisionRequest);

	Optional<Provision> getOrder(ProvisionRequest provisionRequest);
	
	Optional<String> getStatus(String provisionId);

	Optional<List<Provision>> insertProvisionList(List<Provision> provisionRequestList);

	Optional<Provision> getProvisionById(String provisionId);
	
	boolean updateContactInfoPsi(Provision provision);

	boolean updateProvision(Provision provision, Update update);
	
	Optional<Queue> isQueueAvailable();
}
