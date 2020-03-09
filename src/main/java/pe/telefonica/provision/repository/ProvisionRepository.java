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
import pe.telefonica.provision.model.Status;

public interface ProvisionRepository {

	Optional<List<Provision>> findAll(String documentType, String documentNumber);

	Optional<List<Provision>> findAllTraza(String documentType, String documentNumber);

	Optional<Provision> getOrder(String documentType, String documentNumber);

	Optional<Provision> getOrderTraza(String documentType, String documentNumber);

	Optional<Provision> getProvisionByXaRequest(String xaRequest);

	Provision getProvisionBySaleCode(String saleCode);

	Provision getProvisionByXaIdSt(String xaIdSt);

	Provision getProvisionByDummyStPsiCode(String dummyStPsiCode);

	Optional<Provision> getProvisionByXaRequestAndSt(String xaRequest, String xaIdSt);

	Optional<Provision> getStatus(String provisionId);

	boolean updateTrackingStatus(Provision provision, List<StatusLog> logStatus, String description, String speech,
			String frontSpeech, boolean comesFromSchedule);

	Optional<List<Provision>> insertProvisionList(List<Provision> provisionRequestList);

	Optional<Provision> insertProvision(Provision provisionRequest);

	Boolean resetProvision(Provision provisionRequest);

	Optional<Provision> getProvisionById(String provisionId);

	Optional<Provision> getProvisionByIdAndActiveStatus(String provisionId, String activeStatus);
	// boolean updateContactInfoPsi(Provision provision);

	boolean updateProvision(Provision provision, Update update);

	// boolean updateCancelSchedule(CancelRequest cancelRequest);

	Optional<Queue> isQueueAvailable();

	// boolean sendCancelledMail(Provision provision, String name, String
	// idTemplate, String cancellationReason);

	Optional<List<Provision>> getAllInTimeRange(LocalDateTime startDate, LocalDateTime endDate);

	Provision getProvisionByOrderCode(ApiRequest<GetProvisionByOrderCodeRequest> request);

	Provision getByOrderCodeForUpdate(String orderCode);

	Provision getByOrderCodeForUpdateFicticious(String xaRequirementNumber);

	Optional<List<Provision>> getOrderToNotify();

	void updateFlagDateNotify(List<Provision> listProvision);

	boolean updateShowLocation(Provision provision);

	Optional<Status> getInfoStatus(String statusName);

	Optional<List<Status>> getAllInfoStatus();
}
