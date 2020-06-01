package pe.telefonica.provision.repository.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import pe.telefonica.provision.controller.common.NotificationResponse;
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

	@Override
	public NotificationResponse getCountByEventNotication(ReportByRegisterDateRequest request) {
		Query queryInToa = new Query(Criteria.where("notifications.into_send_date").gte(request.getStartDate()).andOperator(
				Criteria.where("notifications.into_send_date").lte(request.getEndDate())));
		
		Long contadorInToa=this.mongoOperations.count(queryInToa, Provision.class);
		
		Query queryWoPrestart = new Query(Criteria.where("notifications.prestart_send_date").gte(request.getStartDate()).andOperator(
				Criteria.where("notifications.prestart_send_date").lte(request.getEndDate())));
		
		Long contadorWoPrestart=this.mongoOperations.count(queryWoPrestart, Provision.class);
		
		Query queryWoCompleted = new Query(Criteria.where("notifications.notdone_send_date").gte(request.getStartDate()).andOperator(
				Criteria.where("notifications.notdone_send_date").lte(request.getEndDate())));
		
		Long contadorWoCompleted=this.mongoOperations.count(queryWoCompleted, Provision.class);
		
		Query queryWoNotdone = new Query(Criteria.where("notifications.completed_send_date").gte(request.getStartDate()).andOperator(
				Criteria.where("notifications.completed_send_date").lte(request.getEndDate())));
		
		Long contadorWoNotdone=this.mongoOperations.count(queryWoNotdone, Provision.class);
		
		NotificationResponse notification=new NotificationResponse();
		notification.setSms_into(contadorInToa);
		notification.setSms_prestart(contadorWoPrestart);
		notification.setSms_completed(contadorWoCompleted);
		notification.setSms_notdone(contadorWoNotdone);
		
		return notification;
	}

}
