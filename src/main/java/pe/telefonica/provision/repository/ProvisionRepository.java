package pe.telefonica.provision.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.query.Update;

import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.request.GetProvisionByOrderCodeRequest;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.Provision.StatusLog;
import pe.telefonica.provision.model.Queue;

public interface ProvisionRepository {

	Optional<List<Provision>> findAll(String documentType, String documentNumber);

	Optional<Provision> getOrder(String documentType, String documentNumber);

	Optional<Provision> getProvisionByXaRequest(String xaRequest);
	
	Provision getProvisionBySaleCode(String saleCode);
	
	Provision getProvisionByXaIdSt(String xaIdSt);
	
	Provision getProvisionByDummyStPsiCode(String dummyStPsiCode);

	Optional<Provision> getProvisionByXaRequestAndSt(String xaRequest, String xaIdSt);

	Optional<Provision> getStatus(String provisionId);

	boolean updateTrackingStatus(Provision provision, List<StatusLog> logStatus, boolean comesFromSchedule);

	Optional<List<Provision>> insertProvisionList(List<Provision> provisionRequestList);

	Optional<Provision> insertProvision(Provision provisionRequest);

	Boolean resetProvision(Provision provisionRequest);

	Optional<Provision> getProvisionById(String provisionId);

	// boolean updateContactInfoPsi(Provision provision);

	boolean updateProvision(Provision provision, Update update);

	// boolean updateCancelSchedule(CancelRequest cancelRequest);

	Optional<Queue> isQueueAvailable();

	// boolean sendCancelledMail(Provision provision, String name, String
	// idTemplate, String cancellationReason);

	Optional<List<Provision>> getAllInTimeRange(LocalDateTime startDate, LocalDateTime endDate);

	Provision getProvisionByOrderCode(ApiRequest<GetProvisionByOrderCodeRequest> request);
	
	Provision getByOrderCodeForUpdate(String orderCode);
	
	
}
