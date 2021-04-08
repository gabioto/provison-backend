package pe.telefonica.provision.repository;

import pe.telefonica.provision.model.params.Parameter;

public interface ParamsRepository {
	Parameter getMessage(String key);
}
