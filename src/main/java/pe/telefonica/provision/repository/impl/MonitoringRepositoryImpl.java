package pe.telefonica.provision.repository.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.util.constants.ConstantsStatusMonitoring;
import pe.telefonica.provision.repository.MonitoringRepository;

@Repository
public class MonitoringRepositoryImpl implements MonitoringRepository {

	private static final Log log = LogFactory.getLog(MonitoringRepositoryImpl.class);
	private final MongoOperations mongoOperations;

	@Autowired
	public MonitoringRepositoryImpl(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}

	@Override
	public long getQuantityRegisterByStatus(LocalDateTime startDate, LocalDateTime endDate, String status) {

		if (status.equals(ConstantsStatusMonitoring.INGRESADO)) {
			Query query = new Query(Criteria.where("active_status").is("ingresado").andOperator(
					Criteria.where("register_date_update").gte(startDate),
					Criteria.where("register_date_update").lt(endDate)));

			long quantity = this.mongoOperations.count(query, Provision.class);
			return quantity;
		} else {
			Query query = new Query(Criteria.where("active_status").is("active").andOperator(
					Criteria.where("in_toa.register_date").gte(startDate),
					Criteria.where("in_toa.register_date").lt(endDate)));

			long quantity = this.mongoOperations.count(query, Provision.class);
			return quantity;
		}

	}

}
