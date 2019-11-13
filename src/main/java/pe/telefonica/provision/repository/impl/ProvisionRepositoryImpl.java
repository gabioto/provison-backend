package pe.telefonica.provision.repository.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.mongodb.client.result.UpdateResult;

import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.request.GetProvisionByOrderCodeRequest;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.Queue;
import pe.telefonica.provision.model.Provision.StatusLog;
import pe.telefonica.provision.repository.ProvisionRepository;
import pe.telefonica.provision.util.constants.Constants;
import pe.telefonica.provision.util.constants.Status;

@Repository
public class ProvisionRepositoryImpl implements ProvisionRepository {

	private static final Log log = LogFactory.getLog(ProvisionRepositoryImpl.class);
	private final MongoOperations mongoOperations;

	@Autowired
	public ProvisionRepositoryImpl(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}

	@Override
	public Optional<List<Provision>> findAll(String documentType, String documentNumber) {
		List<Provision> provisions = this.mongoOperations.find(
				new Query(Criteria.where("customer.document_type").is(documentType).and("customer.document_number")
						.is(documentNumber)
						.and("xa_request").ne("")
						.and("work_zone").ne("")
						.and("xa_id_st").ne("")
						.orOperator(Criteria.where("active_status").is(Constants.PROVISION_STATUS_ACTIVE),
								Criteria.where("active_status").is(Constants.PROVISION_STATUS_ADDRESS_CHANGED))),
				Provision.class);
		Optional<List<Provision>> optionalProvisions = Optional.ofNullable(provisions);
		return optionalProvisions;
	}

	@Override
	public Optional<Provision> getOrder(String documentType, String documentNumber) {
		Provision provision = this.mongoOperations
				.findOne(
						new Query(
								Criteria.where("customer.document_type").is(documentType)
										.and("customer.document_number").is(documentNumber).orOperator(
												Criteria.where("active_status").is(Constants.PROVISION_STATUS_ACTIVE),
												Criteria.where("active_status")
														.is(Constants.PROVISION_STATUS_ADDRESS_CHANGED))),
						Provision.class);
		Optional<Provision> optionalOrder = Optional.ofNullable(provision);
		return optionalOrder;
	}

	@Override
	public Optional<Provision> getStatus(String provisionId) {
		Provision provision = this.mongoOperations
				.findOne(new Query(Criteria.where("_id").is(new ObjectId(provisionId))), Provision.class);
		Optional<Provision> optionalOrder = Optional.ofNullable(provision);
		return optionalOrder;
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
			provision = this.mongoOperations.findOne(
					new Query(Criteria.where("idProvision").is(new ObjectId(provisionId)).orOperator(
							Criteria.where("active_status").is(Constants.PROVISION_STATUS_ACTIVE),
							Criteria.where("active_status").is(Constants.PROVISION_STATUS_ADDRESS_CHANGED))),
					Provision.class);
		} catch (Exception e) {
			log.info(e.getMessage());
		}

		Optional<Provision> optionalSchedule = Optional.ofNullable(provision);

		return optionalSchedule;
	}

	@Override
	public boolean updateProvision(Provision provision, Update update) {
		UpdateResult result = this.mongoOperations.updateFirst(
				new Query(Criteria.where("idProvision").is(new ObjectId(provision.getIdProvision()))), update,
				Provision.class);

		return result.getMatchedCount() > 0;
	}

	@Override
	public Optional<Queue> isQueueAvailable() {
		Queue queue = null;
		try {
			queue = this.mongoOperations.findOne(new Query(Criteria.where("idContingencia").is("1")), Queue.class);
		} catch (Exception e) {
			log.info(e.getMessage());
		}

		Optional<Queue> optionalQueue = Optional.ofNullable(queue);

		return optionalQueue;
	}

	@Override
	public Optional<List<Provision>> getAllInTimeRange(LocalDateTime startDate, LocalDateTime endDate) {
		Query query = new Query(Criteria.where("productName").ne(null)
				.andOperator(Criteria.where("updatedDate").gte(startDate), Criteria.where("updatedDate").lt(endDate)));
		List<Provision> provisions = this.mongoOperations.find(query, Provision.class);

		Optional<List<Provision>> optionalProvisions = Optional.ofNullable(provisions);
		return optionalProvisions;
	}

	@Override
	public Optional<Provision> getProvisionByXaRequest(String xaRequest) {
		Provision provision = this.mongoOperations.findOne(new Query(Criteria.where("xaRequest").is(xaRequest)),
				Provision.class);
		Optional<Provision> optionalOrder = Optional.ofNullable(provision);
		return optionalOrder;
	}

	@Override
	public Optional<Provision> getProvisionByXaRequestAndSt(String xaRequest, String xaIdSt) {
		Provision provision = this.mongoOperations.findOne(
				new Query(Criteria.where("xaRequest").is(xaRequest).and("xaIdSt").is(xaIdSt)), Provision.class);
		Optional<Provision> optionalOrder = Optional.ofNullable(provision);
		return optionalOrder;
	}

	@Override
	public Optional<Provision> insertProvision(Provision provisionRequest) {
		Provision provision = this.mongoOperations.insert(provisionRequest);

		Optional<Provision> optionalProvision = Optional.ofNullable(provision);
		return optionalProvision;
	}

	@Override
	public Boolean resetProvision(Provision provisionRequest) {
		Update update = new Update();
		StatusLog statusLog = new StatusLog();
		statusLog.setStatus(Status.IN_TOA.getStatusName());
		List<StatusLog> listStatusLogs = new ArrayList<>();
		listStatusLogs.add(statusLog);

		update.set("xa_id_st", provisionRequest.getXaIdSt());
		update.set("has_schedule", false);
		update.set("active_status", Constants.PROVISION_STATUS_INCOMPLETE);
		update.set("status_toa", "IN_TOA");
		update.set("last_tracking_status", Status.IN_TOA.getStatusName());
		update.set("log_status", listStatusLogs);

		UpdateResult result = this.mongoOperations.updateFirst(
				new Query(Criteria.where("idProvision").is(new ObjectId(provisionRequest.getIdProvision()))), update,
				Provision.class);

		return result.getMatchedCount() > 0;
	}

	@Override
	public boolean updateTrackingStatus(Provision provision, List<StatusLog> logStatus, boolean comesFromSchedule) {
		Update update = new Update();
		update.set("last_tracking_status", provision.getLastTrackingStatus());
		update.set("log_status", logStatus);

		if (comesFromSchedule) {
			update.set("has_schedule", true);
		}

		UpdateResult result = this.mongoOperations.updateFirst(
				new Query(Criteria.where("idProvision").is(new ObjectId(provision.getIdProvision()))), update,
				Provision.class);

		return result.getMatchedCount() > 0;
	}

	@Override
	public Provision getProvisionByOrderCode(ApiRequest<GetProvisionByOrderCodeRequest> request) {
		Query query = new Query(Criteria.where("xaRequest").is(request.getBody().getOrderCode())
				.andOperator(Criteria.where("status_toa").is("done")));

		query.with(new Sort(new Order(Direction.DESC, "register_date")));

		List<Provision> provisions = this.mongoOperations.find(query, Provision.class);

		if (provisions.size() > 0) {
			Provision provision = provisions.get(0);
			return provision;
		}
		// Provision provision = this.mongoOperations.findOne(new
		// Query(Criteria.where("xaRequest").is(request.getOrdercode()).with( new
		// Sort.Direction.DESC, "sortField"))), Provision.class);

		return null;
	}

	@Override
	public Provision getProvisionByXaIdSt(String xaIdSt) {
		
		Provision provision = this.mongoOperations.findOne(
				new Query(Criteria.where("xaIdSt").is(xaIdSt)), Provision.class);
		
		return provision;
	}

	@Override
	public Provision getProvisionBySaleCode(String saleCode) {
		
		Provision provision = this.mongoOperations.findOne(
				new Query(Criteria.where("sale_code").is(saleCode)), Provision.class);
		
		return provision;
	}

	@Override
	public Provision getByOrderCodeForUpdate(String orderCode) {
		Provision provision = this.mongoOperations.findOne(
				new Query(Criteria.where("xaRequest").is(orderCode)), Provision.class);
		
		return provision;
	}

	@Override
	public Provision getProvisionByDummyStPsiCode(String dummyStPsiCode) {
		
		Provision provision = this.mongoOperations.findOne(
				new Query(Criteria.where("dummy_st_psi_code").is(dummyStPsiCode)), Provision.class);
		
		return provision;
	}
}
