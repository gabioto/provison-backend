package pe.telefonica.provision.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import pe.telefonica.provision.controller.request.simpli.SetSimpliUrlRequest;
import pe.telefonica.provision.controller.response.simpli.SimpliUrlResponse;
import pe.telefonica.provision.model.Provision;

import pe.telefonica.provision.service.SimpliService;
import pe.telefonica.provision.repository.RatingRepository;
import pe.telefonica.provision.repository.ProvisionRepository;

@Service
public class SimpliServiceImpl implements SimpliService {

	@Autowired
	private RatingRepository ratingRepository;

	@Autowired
	private ProvisionRepository provisionRepository;


	@Override
	public SimpliUrlResponse setSimpliUrl(SetSimpliUrlRequest request) {
		
		SimpliUrlResponse simpliUrlResponse = new SimpliUrlResponse();
		SimpliUrlResponse.Body body = new SimpliUrlResponse.Body();
		
		Provision provision = provisionRepository.getProvisionByXaRequest(request.getXa_peticion());
		
		body.setAPPT_Numer(request.getAppt_number());
		if(provision != null) {
			Update update = new Update();
			update.set("wo_prestart.tracking_url", request.getTracking());
			update.set("wo_prestart.available_tracking", true);
			
			boolean updated = provisionRepository.updateProvision(provision, update);
			if(updated) {
				body.setStatus("OK");
			} else {
				body.setStatus("FAILED_UPDATE");
			}
		} else {
			body.setStatus("REGISTER_DOES_NOT_ EXIST");
		}
		
		simpliUrlResponse.setBody(body);
		return simpliUrlResponse;
		
	}

}
