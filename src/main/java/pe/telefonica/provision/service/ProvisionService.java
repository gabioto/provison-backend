package pe.telefonica.provision.service;

import java.util.List;

import pe.telefonica.provision.api.ProvisionRequest;
import pe.telefonica.provision.api.ProvisionResponse;
import pe.telefonica.provision.dto.Provision;

public interface ProvisionService {
	
	ProvisionResponse<Provision> getAll(ProvisionRequest provisionRequest);
	ProvisionResponse<Provision> insertProvisionList(List<Provision> provisionList);
	
	public Boolean setContactInfoUpdate(String provisionId, String contactFullname, String contactCellphone, Boolean contactCellphoneIsMovistar);
	public Boolean setProvisionIsValidated(String provisionId);
	public Boolean requestAddressUpdate(String provisionId);
	public Boolean receiveAddressUpdateBO(String action, String provisionId, String newDepartment, String newProvince,  String newDistrict, String newAddress, String newReference, boolean isSMSRequired);
	public Boolean orderCancellation(String provisionId);	
}
