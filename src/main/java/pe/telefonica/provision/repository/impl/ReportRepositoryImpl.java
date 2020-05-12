package pe.telefonica.provision.repository.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import pe.telefonica.provision.controller.request.report.ReportInviteMessageRequest;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.repository.ReportRepository;

@Repository
public class ReportRepositoryImpl implements ReportRepository{

	private final MongoOperations mongoOperations;
	
	@Autowired
	public ReportRepositoryImpl(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}
	
	@Override
	public List<Provision> getProvisionsByInviteMessageDate(ReportInviteMessageRequest request) {
		Query query = new Query(Criteria.where("invite_message_date").gte(request.getStartDateStr()).andOperator(
						Criteria.where("invite_message_date").lte(request.getEndDateStr())));
		List<Provision> provisions = this.mongoOperations.find(query, Provision.class);
		
		return provisions;
	}

}
