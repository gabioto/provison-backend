package pe.telefonica.provision.repository;

import pe.telefonica.provision.controller.request.temporary.GetSaleCodeByXaRequestAndSTRequest;
import pe.telefonica.provision.model.Provision;

public interface TemporaryRepository {
	Provision getSaleCode(GetSaleCodeByXaRequestAndSTRequest request);
}
