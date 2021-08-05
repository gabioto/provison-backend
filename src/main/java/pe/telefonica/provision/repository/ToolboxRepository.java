package pe.telefonica.provision.repository;

import java.util.Optional;

import pe.telefonica.provision.model.Toolbox;

public interface ToolboxRepository {
	void insertLog(Toolbox objToolbox);
	
	Optional<Toolbox> getLog(String documentType, String documentNumber, String xaRequest, String chart);
}
