package pe.telefonica.provision.repository;

import pe.telefonica.provision.model.params.Params;

public interface ParamsRepository {
	Params getMessage(String key);
}
