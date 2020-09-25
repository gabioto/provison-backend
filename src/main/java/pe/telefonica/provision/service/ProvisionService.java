package pe.telefonica.provision.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.common.ApiResponse;
import pe.telefonica.provision.controller.request.ApiTrazaSetContactInfoUpdateRequest;
import pe.telefonica.provision.controller.request.GetProvisionByOrderCodeRequest;
import pe.telefonica.provision.controller.request.InsertCodeFictionalRequest;
import pe.telefonica.provision.controller.request.InsertOrderRequest;
import pe.telefonica.provision.controller.request.ProvisionRequest;
import pe.telefonica.provision.controller.request.UpdateFromToaRequest;
import pe.telefonica.provision.controller.response.ProvisionResponse;
import pe.telefonica.provision.dto.ProvisionDto;
import pe.telefonica.provision.dto.ProvisionTrazaDto;
import pe.telefonica.provision.model.Contacts;
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.model.Provision;

public interface ProvisionService {

	Customer validateUser(ApiRequest<ProvisionRequest> provisionRequest);

	Provision getProvisionBySaleCode(String saleCode);

	List<ProvisionDto> getAll(ApiRequest<ProvisionRequest> provisionRequest);
	
	List<ProvisionTrazaDto> getAllTraza(ApiRequest<ProvisionRequest> provisionRequest);

	ProvisionResponse<String> getStatus(String provisionId);

	ApiResponse<List<Contacts>> getContactList(String provisionId);

	//List<Provision> insertProvisionList(List<Provision> provisionList);

	boolean insertProvision(InsertOrderRequest request);

	boolean provisionInsertCodeFictitious(InsertCodeFictionalRequest request);

	boolean provisionUpdateFromTOA(UpdateFromToaRequest request)
			throws Exception;

	public Provision setContactInfoUpdate(ApiTrazaSetContactInfoUpdateRequest request) throws Exception;

	public Boolean apiContactInfoUpdate(ApiTrazaSetContactInfoUpdateRequest request);

	public Provision setProvisionIsValidated(String provisionId);

	public Provision requestAddressUpdate(String provisionId);

	public Boolean receiveAddressUpdateBO(String action, String provisionId, String newDepartment, String newProvince,
			String newDistrict, String newAddress, String newReference, boolean isSMSRequired);

	public Provision orderCancellation(String provisionId, String cause, String detail);

	ProvisionResponse<Boolean> validateQueue();

	public ProvisionResponse<Boolean> updateOrderSchedule(String provisionId, LocalDate scheduledDate,
			String scheduledRange, Integer scheduledType);

	public List<Provision> getAllInTimeRange(LocalDateTime startDate, LocalDateTime endDate);

	Boolean updateTrackingStatus(String xaRequest, String xaIdSt, String status, boolean comesFromSchedule,
			LocalDate scheduledDate, String scheduledRange, Integer scheduleType, String description, String speech,
			String frontSpeech);

	public Provision getProvisionByOrderCode(ApiRequest<GetProvisionByOrderCodeRequest> request);

	boolean getBucketByProduct(String channel, String product, String bucket) throws Exception;

	List<Provision> getOrderToNotify();

	public boolean updateShowLocation(Provision provision);

	List<Provision> getUpFrontProvisions();

}
