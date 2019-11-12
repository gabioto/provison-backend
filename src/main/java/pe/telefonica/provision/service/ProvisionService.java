package pe.telefonica.provision.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.request.ApiTrazaSetContactInfoUpdateRequest;
import pe.telefonica.provision.controller.request.GetProvisionByOrderCodeRequest;
import pe.telefonica.provision.controller.request.InsertCodeFictionalRequest;
import pe.telefonica.provision.controller.request.InsertOrderRequest;
import pe.telefonica.provision.controller.request.ProvisionRequest;
import pe.telefonica.provision.controller.request.UpdateFromToaRequest;
import pe.telefonica.provision.controller.response.ProvisionResponse;
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.model.Provision;

public interface ProvisionService {

	Customer validateUser(ApiRequest<ProvisionRequest> provisionRequest);
	
	Customer getCustomerByOrderCode(String orderCode);

	List<Provision> getAll(ApiRequest<ProvisionRequest> provisionRequest);

	ProvisionResponse<String> getStatus(String provisionId);

	List<Provision> insertProvisionList(List<Provision> provisionList);

	boolean insertProvision(InsertOrderRequest request);

	boolean provisionInsertCodeFictional(InsertCodeFictionalRequest request);

	boolean provisionUpdateFromTOA(UpdateFromToaRequest request);

	public Provision setContactInfoUpdate(String provisionId, String contactFullname, String contactCellphone,
			Boolean contactCellphoneIsMovistar);

	public Boolean apiContactInfoUpdate(ApiTrazaSetContactInfoUpdateRequest request);

	public Provision setProvisionIsValidated(String provisionId);

	public Provision requestAddressUpdate(String provisionId);

	public Boolean receiveAddressUpdateBO(String action, String provisionId, String newDepartment, String newProvince,
			String newDistrict, String newAddress, String newReference, boolean isSMSRequired);

	public Provision orderCancellation(String provisionId);

	ProvisionResponse<Boolean> validateQueue();

	public ProvisionResponse<Boolean> updateOrderSchedule(String provisionId, LocalDate scheduledDate,
			String scheduledRange, Integer scheduledType);

	public List<Provision> getAllInTimeRange(LocalDateTime startDate, LocalDateTime endDate);

	Boolean updateTrackingStatus(String xaRequest, String xaIdSt, String status, boolean comesFromSchedule,
			LocalDate scheduledDate, String scheduledRange, Integer scheduleType);

	public Provision getProvisionByOrderCode(ApiRequest<GetProvisionByOrderCodeRequest> request);
}
