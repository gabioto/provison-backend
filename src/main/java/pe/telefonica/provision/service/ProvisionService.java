package pe.telefonica.provision.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.request.GetProvisionByOrderCodeRequest;
import pe.telefonica.provision.controller.request.ProvisionRequest;
import pe.telefonica.provision.controller.response.ProvisionResponse;
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.model.Provision;

public interface ProvisionService {

	Customer validateUser(ApiRequest<ProvisionRequest> provisionRequest);

	List<Provision> getAll(ApiRequest<ProvisionRequest> provisionRequest);
	
	ProvisionResponse<String> getStatus(String provisionId);

	List<Provision> insertProvisionList(List<Provision> provisionList);

	public Provision setContactInfoUpdate(String provisionId, String contactFullname, String contactCellphone,
			Boolean contactCellphoneIsMovistar);
	
	
	public Provision setProvisionIsValidated(String provisionId);

	public Provision requestAddressUpdate(String provisionId);
	

	public Boolean receiveAddressUpdateBO(String action, String provisionId, String newDepartment, String newProvince,
			String newDistrict, String newAddress, String newReference, boolean isSMSRequired);

	public Provision orderCancellation(String provisionId);
	
	ProvisionResponse<Boolean> validateQueue();
	
	public ProvisionResponse<Boolean> updateOrderSchedule(String provisionId,
			                                              LocalDate scheduledDate,
			  											  String scheduledRange);
	
	public List<Provision> getAllInTimeRange(LocalDateTime startDate, LocalDateTime endDate);
	
	Boolean updateTrackingStatus(String xaRequest, String xaIdSt,  String status, boolean comesFromSchedule, LocalDate scheduledDate,String scheduledRange);
	
	public Provision getProvisionByOrderCode(ApiRequest<GetProvisionByOrderCodeRequest> request);
}
