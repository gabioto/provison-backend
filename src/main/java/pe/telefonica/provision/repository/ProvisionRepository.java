package pe.telefonica.provision.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.query.Update;

import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.request.CancelRequest;
import pe.telefonica.provision.controller.request.ProvisionRequest;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.Queue;

public interface ProvisionRepository {

	Optional<List<Provision>> findAll(String documentType, String documentNumber);

	Optional<Provision> getOrder(String documentType, String documentNumber);
	
	Optional<Provision> getStatus(String provisionId);

	Optional<List<Provision>> insertProvisionList(List<Provision> provisionRequestList);

	Optional<Provision> getProvisionById(String provisionId);
	
	//boolean updateContactInfoPsi(Provision provision);

	boolean updateProvision(Provision provision, Update update);
	
	//boolean updateCancelSchedule(CancelRequest cancelRequest);
	
	Optional<Queue> isQueueAvailable();
	
	//boolean sendCancelledMail(Provision provision, String name, String idTemplate, String cancellationReason);
	
	Optional<List<Provision>> getAllInTimeRange(LocalDateTime startDate, LocalDateTime endDate);
}
