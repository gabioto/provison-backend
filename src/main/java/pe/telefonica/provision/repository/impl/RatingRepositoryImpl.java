package pe.telefonica.provision.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import pe.telefonica.provision.model.rating.Rating;
import pe.telefonica.provision.repository.RatingRepository;

@Repository
public class RatingRepositoryImpl implements RatingRepository {
	
	private static final Log log = LogFactory.getLog(RatingRepositoryImpl.class);
	private final MongoOperations mongoOperations;

	@Autowired
	public RatingRepositoryImpl(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}
	
	@Override
	public Rating getRatingByKeyName(String keyName) {
		Query query = new Query(Criteria.where("key_name").is(keyName));
		 Rating rating = this.mongoOperations.findOne(query, Rating.class);
		return rating;
	}

}
