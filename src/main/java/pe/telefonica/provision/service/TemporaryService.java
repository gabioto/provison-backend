package pe.telefonica.provision.service;

import pe.telefonica.provision.controller.request.temporary.GetSaleCodeByXaRequestAndSTRequest;

public interface TemporaryService {
	
	String getSaleCode(GetSaleCodeByXaRequestAndSTRequest request);

}
