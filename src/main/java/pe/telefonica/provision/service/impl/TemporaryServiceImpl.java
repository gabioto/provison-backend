package pe.telefonica.provision.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pe.telefonica.provision.controller.request.temporary.GetSaleCodeByXaRequestAndSTRequest;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.service.TemporaryService;
import pe.telefonica.provision.repository.TemporaryRepository;

@Service
public class TemporaryServiceImpl implements TemporaryService {
	
	@Autowired
	private TemporaryRepository temporaryRepository;
	@Override
	public String getSaleCode(GetSaleCodeByXaRequestAndSTRequest request) {
		
		Provision provision = temporaryRepository.getSaleCode(request);
		
		if(provision != null) {
			return provision.getSaleCode();
		} else {
			return null;
		}

	}

}
