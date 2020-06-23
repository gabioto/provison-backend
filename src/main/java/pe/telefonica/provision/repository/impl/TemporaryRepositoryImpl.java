package pe.telefonica.provision.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import pe.telefonica.provision.controller.request.temporary.GetSaleCodeByXaRequestAndSTRequest;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.repository.TemporaryRepository;

@Repository
public class TemporaryRepositoryImpl implements TemporaryRepository {
	
	private final MongoOperations mongoOperations;
	
	@Autowired
	public TemporaryRepositoryImpl(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}
	@Override
	public Provision getSaleCode(GetSaleCodeByXaRequestAndSTRequest request) {
		Query query = new Query(Criteria.where("xa_request").is(request.getXaRequest()).and("xa_id_st").is(request.getStPsiCode()));
		Provision provision = this.mongoOperations.findOne(query, Provision.class);
		return provision;
	}

}
