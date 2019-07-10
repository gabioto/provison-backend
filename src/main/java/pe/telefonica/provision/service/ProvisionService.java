package pe.telefonica.provision.service;

import java.util.List;

import pe.telefonica.provision.api.request.ProvisionRequest;
import pe.telefonica.provision.api.response.ProvisionArrayResponse;
import pe.telefonica.provision.api.response.ProvisionResponse;
import pe.telefonica.provision.dto.Customer;
import pe.telefonica.provision.dto.Provision;

public interface ProvisionService {

	ProvisionResponse<Customer> validateUser(ProvisionRequest provisionRequest);

	ProvisionArrayResponse<Provision> getAll(ProvisionRequest provisionRequest);

	ProvisionArrayResponse<Provision> insertProvisionList(List<Provision> provisionList);

	public Boolean setContactInfoUpdate(String provisionId, String contactFullname, String contactCellphone,
			Boolean contactCellphoneIsMovistar);

	public Boolean setProvisionIsValidated(String provisionId);

	public Boolean requestAddressUpdate(String provisionId);

	public Boolean receiveAddressUpdateBO(String action, String provisionId, String newDepartment, String newProvince,
			String newDistrict, String newAddress, String newReference, boolean isSMSRequired);

	public Boolean orderCancellation(String provisionId);
}
