package pe.telefonica.provision.controller.response;

import java.util.List;

import pe.telefonica.provision.model.Provision;

public class GetAllInTimeRangeResponse {

	private List<Provision> provisions;

	public List<Provision> getProvisions() {
		return provisions;
	}

	public void setProvisions(List<Provision> provisions) {
		this.provisions = provisions;
	}
}
