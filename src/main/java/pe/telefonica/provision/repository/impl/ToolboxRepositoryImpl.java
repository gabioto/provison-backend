package pe.telefonica.provision.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;

import pe.telefonica.provision.model.Toolbox;
import pe.telefonica.provision.repository.ToolboxRepository;

public class ToolboxRepositoryImpl implements ToolboxRepository {
	private final MongoOperations mongoOperations;

	@Autowired
	public ToolboxRepositoryImpl(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}
	
	public void insertLog(Toolbox objToolbox)  {
		this.mongoOperations.insert(objToolbox);
	}
}
