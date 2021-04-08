package pe.telefonica.provision.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import pe.telefonica.provision.model.params.Parameter;
import pe.telefonica.provision.repository.ParamsRepository;

@Repository
public class ParamsRepositoryImpl implements ParamsRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Parameter getMessage(String key) {
		Criteria criteria = Criteria.where("key").is(key);
		
		return mongoOperations.findOne(new Query(criteria), Parameter.class);
	}
}
