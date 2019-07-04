package pe.telefonica.provision.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.UpdateResult;

import pe.telefonica.provision.api.ProvisionRequest;
import pe.telefonica.provision.conf.Constants;
import pe.telefonica.provision.dto.Provision;
import pe.telefonica.provision.repository.ProvisionRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@Repository
public class ProvisionRepositoryImpl implements ProvisionRepository{

	private static final Log log = LogFactory.getLog(ProvisionRepositoryImpl.class);
    private final MongoOperations mongoOperations;

    @Autowired
    public ProvisionRepositoryImpl(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

	@Override
	public Optional<List<Provision>> findAll(ProvisionRequest provisionRequest) {
		List<Provision> provisions = this.mongoOperations.find(new Query(Criteria.where("customer.document_type")
				.is(provisionRequest.getDocumentType()).and("customer.document_number").
				is(provisionRequest.getDocumentNumber()).orOperator(Criteria.where("active_status").
						is(Constants.PROVISION_STATUS_ACTIVE), Criteria.where("active_status").
						is(Constants.PROVISION_STATUS_ADDRESS_CHANGED))), Provision.class);
        Optional<List<Provision>> optionalProvisions = Optional.ofNullable(provisions);
        return optionalProvisions;
	}

	@Override
	public Optional<List<Provision>> insertProvisionList(List<Provision> provisionRequestList) {
		List<Provision> provisions = (List<Provision>) this.mongoOperations.insertAll(provisionRequestList);
		Optional<List<Provision>> optionalProvisions = Optional.ofNullable(provisions);
		return optionalProvisions;
	}

	@Override
	public Optional<Provision> getProvisionById(String provisionId) {
		Provision provision = null;
		try {
			provision = this.mongoOperations.findOne(new Query(Criteria.where("idProvision").is(new ObjectId(provisionId)).
					orOperator(Criteria.where("active_status").is(Constants.PROVISION_STATUS_ACTIVE),
					Criteria.where("active_status").is(Constants.PROVISION_STATUS_ADDRESS_CHANGED))), Provision.class);
		} catch (Exception e) {
			log.info(e.getMessage());
		}
		
		Optional<Provision> optionalSchedule = Optional.ofNullable(provision);
		
		return optionalSchedule;
	}

	@Override
	public boolean updateProvision(Provision provision, Update update) {
		UpdateResult result = this.mongoOperations.updateFirst(new Query(Criteria.where("idProvision")
				.is(new ObjectId(provision.getIdProvision()))), update, Provision.class);
		
		return result.getMatchedCount() > 0;
	}
}
