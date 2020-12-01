package pe.telefonica.provision.service;

import pe.telefonica.provision.controller.request.simpli.SetSimpliUrlRequest;
import pe.telefonica.provision.controller.response.simpli.SimpliUrlResponse;

public interface SimpliService {

	public SimpliUrlResponse setSimpliUrl(SetSimpliUrlRequest request);
}
