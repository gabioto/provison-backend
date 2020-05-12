package pe.telefonica.provision.repository.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import pe.telefonica.provision.controller.request.report.ReportByRegisterDateRequest;
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
	public Long getProvisionsByRegisterDate(ReportByRegisterDateRequest request) {
		Query query = new Query(Criteria.where("register_date").gte(request.getStartDate()).andOperator(
						Criteria.where("register_date").lte(request.getEndDate())));
		
		//List<Provision> provisions = this.mongoOperations.find(query, Provision.class);
		return this.mongoOperations.count(query, Provision.class);
	}

}
