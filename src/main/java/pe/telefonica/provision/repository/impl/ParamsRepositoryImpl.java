package pe.telefonica.provision.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import pe.telefonica.provision.model.params.Params;
import pe.telefonica.provision.repository.ParamsRepository;

public class ParamsRepositoryImpl implements ParamsRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Params getMessage(String key) {
		Criteria criteria = Criteria.where("key").is(key);
		return mongoOperations.findOne(new Query(criteria), Params.class);
	}
}
