package pe.telefonica.provision.service;

import java.time.LocalDateTime;
import java.util.List;

import pe.telefonica.provision.controller.request.ProvisionRequest;
import pe.telefonica.provision.controller.request.SetContactInfoUpdateRequest;
import pe.telefonica.provision.controller.response.ProvisionArrayResponse;
import pe.telefonica.provision.controller.response.ProvisionResponse;
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.model.Provision;

public interface ProvisionService {

	ProvisionResponse<Customer> validateUser(ProvisionRequest provisionRequest);

	ProvisionArrayResponse<Provision> getAll(ProvisionRequest provisionRequest);
	
	ProvisionResponse<String> getStatus(String provisionId);

	ProvisionArrayResponse<Provision> insertProvisionList(List<Provision> provisionList);

	public Provision setContactInfoUpdate(String provisionId, String contactFullname, String contactCellphone,
			Boolean contactCellphoneIsMovistar);
	
	public ProvisionArrayResponse<Provision> setContactInfoUpdateNew(SetContactInfoUpdateRequest request);

	public Provision setProvisionIsValidated(String provisionId);

	public Provision requestAddressUpdate(String provisionId);
	

	public Boolean receiveAddressUpdateBO(String action, String provisionId, String newDepartment, String newProvince,
			String newDistrict, String newAddress, String newReference, boolean isSMSRequired);

	public Provision orderCancellation(String provisionId);
	
	ProvisionResponse<Boolean> validateQueue();
	
	public ProvisionResponse<Boolean> updateOrderSchedule(String provisionId);
	
	public List<Provision> getAllInTimeRange(LocalDateTime startDate, LocalDateTime endDate);
}
