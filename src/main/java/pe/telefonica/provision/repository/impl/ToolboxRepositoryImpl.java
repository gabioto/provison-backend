package pe.telefonica.provision.repository.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import pe.telefonica.provision.model.Toolbox;
import pe.telefonica.provision.repository.ToolboxRepository;

@Repository
public class ToolboxRepositoryImpl implements ToolboxRepository {
	private final MongoOperations mongoOperations;

	@Autowired
	public ToolboxRepositoryImpl(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}
	
	@Override
	public void insertLog(Toolbox objToolbox)  {
		this.mongoOperations.insert(objToolbox);
	}
	
	@Override
	public Optional<Toolbox> getLog(String documentType, String documentNumber, String xaRequest, String chart)  {
		Query query = new Query(Criteria.where("document_type").is(documentType).and("document_number").is(documentNumber).and("xa_request").is(xaRequest).and("chart").ne(null));
		Toolbox objToolbox = this.mongoOperations.findOne(query, Toolbox.class);
		Optional<Toolbox> optionalFault = Optional.ofNullable(objToolbox);
		return optionalFault;
	}
}
