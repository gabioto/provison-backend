package pe.telefonica.provision.api.response;

import java.util.List;

import pe.telefonica.provision.dto.Provision;

public class GetAllInTimeRangeResponse {

	private List<Provision> provisions;

	public List<Provision> getProvisions() {
		return provisions;
	}

	public void setProvisions(List<Provision> provisions) {
		this.provisions = provisions;
	}
}
