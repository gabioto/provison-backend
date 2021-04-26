package pe.telefonica.provision.repository.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.UpdateResult;

import pe.telefonica.provision.conf.ExternalApi;
import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.request.GetProvisionByOrderCodeRequest;
import pe.telefonica.provision.dto.ProvisionCustomerDto;
import pe.telefonica.provision.dto.ProvisionDto;
import pe.telefonica.provision.dto.ProvisionTrazaDto;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.Provision.StatusLog;
import pe.telefonica.provision.model.Queue;
import pe.telefonica.provision.model.ResendNotification;
import pe.telefonica.provision.repository.ProvisionRepository;
import pe.telefonica.provision.util.constants.Constants;
import pe.telefonica.provision.util.constants.Status;

@Repository
public class ProvisionRepositoryImpl implements ProvisionRepository {

	private static final Log log = LogFactory.getLog(ProvisionRepositoryImpl.class);
	private final MongoOperations mongoOperations;

	@Autowired
	private ExternalApi api;

	@Autowired
	public ProvisionRepositoryImpl(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}

	@Override
	public Optional<List<ProvisionDto>> findAll(String documentType, String documentNumber) {
		int diasVidaProvision = Integer.parseInt(api.getNroDiasVidaProvision()) + 1;
		diasVidaProvision = diasVidaProvision * -1;
		LocalDateTime dateStart = LocalDateTime.now().plusDays(diasVidaProvision);

		Query query = new Query(Criteria.where("customer.document_type").is(documentType)
				.and("customer.document_number").is(documentNumber).andOperator(Criteria.where("product_name").ne(null),
						Criteria.where("product_name").ne(""), Criteria.where("register_date").gte(dateStart)))
								.limit(3);
		query.with(new Sort(Direction.DESC, "register_date"));
		List<ProvisionDto> provisions = this.mongoOperations.find(query, ProvisionDto.class);

		Optional<List<ProvisionDto>> optionalProvisions = Optional.ofNullable(provisions);
		return optionalProvisions;
	}

	@Override
	public Optional<List<ProvisionTrazaDto>> findAllTraza(String documentType, String documentNumber) {
		int diasVidaProvision = Integer.parseInt(api.getNroDiasVidaProvision()) + 1;
		diasVidaProvision = diasVidaProvision * -1;
		LocalDateTime dateStart = LocalDateTime.now().plusDays(diasVidaProvision);

		Query query = new Query(Criteria.where("customer.document_type").is(documentType)
				.and("customer.document_number").is(documentNumber).andOperator(Criteria.where("product_name").ne(null),
						Criteria.where("product_name").ne(""), Criteria.where("register_date").gte(dateStart)))
								.limit(3);
		query.with(new Sort(Direction.DESC, "register_date"));

		List<ProvisionTrazaDto> provisions = this.mongoOperations.find(query, ProvisionTrazaDto.class);
		Optional<List<ProvisionTrazaDto>> optionalProvisions = Optional.ofNullable(provisions);
		return optionalProvisions;
	}

	@Override
	public Optional<Provision> getOrder(String documentType, String documentNumber) {
		int diasVidaProvision = Integer.parseInt(api.getNroDiasVidaProvision()) + 1;
		diasVidaProvision = diasVidaProvision * -1;
		LocalDateTime dateStart = LocalDateTime.now().plusDays(diasVidaProvision);

		Query query = new Query(Criteria.where("customer.document_type").is(documentType)
				.and("customer.document_number").is(documentNumber).andOperator(Criteria.where("product_name").ne(null),
						Criteria.where("product_name").ne(""), Criteria.where("register_date").gte(dateStart)))
								.limit(3);
		query.with(new Sort(Direction.DESC, "register_date"));
		Provision provision = this.mongoOperations.findOne(query, Provision.class);
		Optional<Provision> optionalOrder = Optional.ofNullable(provision);
		return optionalOrder;
	}

	@Override
	public Optional<Provision> getOrderTraza(String documentType, String documentNumber) {

		Provision provision = this.mongoOperations
				.findOne(
						new Query(Criteria.where("customer.document_type").is(documentType)
								.and("customer.document_number").is(documentNumber)
								.orOperator(Criteria.where("active_status").is(Constants.PROVISION_STATUS_CANCELLED),
										Criteria.where("active_status").is(Constants.PROVISION_STATUS_ACTIVE),
										Criteria.where("active_status").is(Constants.PROVISION_STATUS_ADDRESS_CHANGED),
										Criteria.where("active_status")
												.is(Constants.PROVISION_STATUS_SCHEDULE_IN_PROGRESS),
										Criteria.where("active_status").is(Constants.PROVISION_STATUS_WOINIT),
										Criteria.where("active_status").is(Constants.PROVISION_STATUS_NOTDONE),
										Criteria.where("active_status").is(Constants.PROVISION_STATUS_COMPLETED))),
						Provision.class);

		return Optional.ofNullable(provision);
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
							Criteria.where("active_status").is(Constants.PROVISION_STATUS_ADDRESS_CHANGED),
							Criteria.where("active_status").is(Constants.PROVISION_STATUS_SCHEDULE_IN_PROGRESS))),
					Provision.class);
		} catch (Exception e) {
			log.error(this.getClass().getName() + " - Exception: " + e.getMessage());
		}

		Optional<Provision> optionalSchedule = Optional.ofNullable(provision);

		return optionalSchedule;
	}

	@Override
	public Optional<Provision> getAllProvisionById(String provisionId) {
		Provision provision = null;
		try {
			provision = this.mongoOperations
					.findOne(new Query(Criteria.where("idProvision").is(new ObjectId(provisionId))), Provision.class);
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
			log.error(this.getClass().getName() + " - Exception: " + e.getMessage());
		}

		Optional<Queue> optionalQueue = Optional.ofNullable(queue);

		return optionalQueue;
	}

	@Override
	public Optional<List<Provision>> getAllInTimeRange(LocalDateTime startDate, LocalDateTime endDate) {
		Query query = new Query(Criteria.where("productName").ne(null).and("xa_request").ne(null).and("xa_id_st")
				.ne(null).andOperator(Criteria.where("notifications.into_send_date").gte(startDate),
						Criteria.where("notifications.into_send_date").lte(endDate)));

		List<Provision> provisions = this.mongoOperations.find(query, Provision.class);

		Optional<List<Provision>> optionalProvisions = Optional.ofNullable(provisions);
		return optionalProvisions;
	}

	@Override
	public Optional<List<ProvisionCustomerDto>> getAllResendNotification(LocalDateTime startDate,
			LocalDateTime endDate) {

		Query query = new Query(Criteria.where("log_status.status").ne("SCHEDULED").and("active_status").is("active")
				.and("notifications.into_send_notify").is(true).and("resend_intoa.intoa_count").exists(false)
				.andOperator(Criteria.where("notifications.into_send_date").gte(startDate),
						Criteria.where("notifications.into_send_date").lte(endDate)));

		List<ProvisionCustomerDto> provisions = this.mongoOperations.find(query, ProvisionCustomerDto.class);

		Optional<List<ProvisionCustomerDto>> optionalProvisions = Optional.ofNullable(provisions);
		return optionalProvisions;

	}

	@Override
	public Provision getProvisionByXaRequest(String xaRequest) {
		Provision provision = this.mongoOperations.findOne(new Query(Criteria.where("xaRequest").is(xaRequest)),
				Provision.class);
		return provision;
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
	public boolean updateTrackingStatus(Provision provision, List<StatusLog> logStatus, String description,
			String speech, String frontSpeech, boolean comesFromSchedule) {
		Update update = new Update();
		update.set("last_tracking_status", provision.getLastTrackingStatus());
		update.set("log_status", logStatus);
		update.set("description_status", description);
		update.set("generic_speech", speech);
		update.set("front_speech", frontSpeech);

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

		query.with(new Sort(Direction.DESC, "register_date"));

		List<Provision> provisions = this.mongoOperations.find(query, Provision.class);

		if (provisions.size() > 0) {
			Provision provision = provisions.get(0);
			return provision;
		}
		return null;
	}

	@Override
	public Provision getProvisionByXaIdSt(String xaIdSt) {

		Provision provision = this.mongoOperations.findOne(new Query(Criteria.where("xaIdSt").is(xaIdSt)),
				Provision.class);

		return provision;
	}

	@Override
	public Provision getProvisionBySaleCode(String saleCode) {

		Provision provision = this.mongoOperations.findOne(new Query(Criteria.where("sale_code").is(saleCode)),
				Provision.class);

		return provision;
	}

	@Override
	public Provision getByOrderCodeForUpdate(String orderCode) {
		Provision provision = this.mongoOperations.findOne(new Query(Criteria.where("xaRequest").is(orderCode)),
				Provision.class);

		return provision;
	}

	@Override
	public Provision getByOrderCodeForUpdateFicticious(String xaRequirementNumber) {
		Provision provision = this.mongoOperations
				.findOne(new Query(Criteria.where("dummyStPsiCode").is(xaRequirementNumber)), Provision.class);
		return provision;
	}

	@Override
	public Provision getProvisionByDummyStPsiCode(String dummyStPsiCode) {

		Provision provision = this.mongoOperations
				.findOne(new Query(Criteria.where("dummy_st_psi_code").is(dummyStPsiCode)), Provision.class);

		return provision;
	}

	@Override
	public Optional<List<Provision>> getOrderToNotify() {
		ArrayList<String> status = new ArrayList<String>();
		status.add(Status.IN_TOA.getStatusName());
		status.add(Status.WO_CANCEL.getStatusName());
		status.add(Status.SCHEDULED.getStatusName());
		status.add(Status.CAIDA.getStatusName());
		status.add(Status.WO_NOTDONE.getStatusName());
		status.add(Status.PAGADO.getStatusName());

		Criteria criteria = new Criteria();
		criteria.orOperator(
				Criteria.where("notifications.pagado_send_notify").is(false).and("last_tracking_status")
						.is(Status.PAGADO.getStatusName()),
				Criteria.where("notifications.into_send_notify").is(false).and("last_tracking_status")
						.is(Status.IN_TOA.getStatusName()),
				Criteria.where("notifications.into_send_notify").is(false).and("last_tracking_status")
						.is(Status.SCHEDULED.getStatusName()),
				Criteria.where("notifications.notdone_send_notify").is(false).and("last_tracking_status")
						.is(Status.WO_NOTDONE.getStatusName()),
				Criteria.where("notifications.cancel_send_notify").is(false).and("last_tracking_status")
						.is(Status.WO_CANCEL.getStatusName()),
				Criteria.where("notifications.completed_send_notify").is(false).and("last_tracking_status")
						.is(Status.WO_COMPLETED.getStatusName()),
				Criteria.where("notifications.finalizado_send_notify").is(false).and("last_tracking_status")
						.is(Status.FINALIZADO.getStatusName()),

				Criteria.where("notifications.cancelada_atis_send_notify").is(false).and("last_tracking_status")
						.is(Status.CANCELADA_ATIS.getStatusName())

		).and("customer").ne(null).and("notifications").ne(null);

		Query query = new Query(criteria).limit(15);

		query.with(new Sort(Direction.ASC, "_id"));

		List<Provision> provision = this.mongoOperations.find(query, Provision.class);

		Optional<List<Provision>> optionalOrder = Optional.ofNullable(provision);
		return optionalOrder;
	}

	@Override
	public void updateFlagDateNotify(List<Provision> listProvision) {
		
		for (int i = 0; i < listProvision.size(); i++) {
			Update update = new Update();
			
			if (Status.CAIDA.getStatusName().equals(listProvision.get(i).getLastTrackingStatus())) {
				update.set("notifications.caida_send_notify", true);
				if (Boolean.valueOf(System.getenv("TDP_MESSAGE_PROVISION_ENABLE"))) {
					update.set("notifications.caida_send_date", LocalDateTime.now(ZoneOffset.of("-05:00")));
				}
			}

			if (Status.PAGADO.getStatusName().equals(listProvision.get(i).getLastTrackingStatus())) {
				update.set("notifications.pagado_send_notify", true);
				if (Boolean.valueOf(System.getenv("TDP_MESSAGE_PROVISION_ENABLE"))) {
					update.set("notifications.pagado_send_date", LocalDateTime.now(ZoneOffset.of("-05:00")));
				}
			}

			if (Status.IN_TOA.getStatusName().equals(listProvision.get(i).getLastTrackingStatus())
					|| Status.SCHEDULED.getStatusName().equals(listProvision.get(i).getLastTrackingStatus())) {
				update.set("notifications.into_send_notify", true);
				if (Boolean.valueOf(System.getenv("TDP_MESSAGE_PROVISION_ENABLE"))) {
					update.set("notifications.into_send_date", LocalDateTime.now(ZoneOffset.of("-05:00")));
				}
			}

			if (Status.WO_NOTDONE.getStatusName().equals(listProvision.get(i).getLastTrackingStatus())) {
				update.set("notifications.notdone_send_notify", true);
				if (Boolean.valueOf(System.getenv("TDP_MESSAGE_PROVISION_ENABLE"))) {
					update.set("notifications.notdone_send_date", LocalDateTime.now(ZoneOffset.of("-05:00")));
				}

			}

			if (Status.WO_COMPLETED.getStatusName().equals(listProvision.get(i).getLastTrackingStatus())) {
				update.set("notifications.completed_send_notify", true);
				update.set("notifications.finalizado_send_notify", true);
				if (Boolean.valueOf(System.getenv("TDP_MESSAGE_PROVISION_ENABLE"))) {
					update.set("notifications.completed_send_date", LocalDateTime.now(ZoneOffset.of("-05:00")));
				}
			}

			if (Status.WO_CANCEL.getStatusName().equals(listProvision.get(i).getLastTrackingStatus())) {
				update.set("notifications.cancel_send_notify", true);
				if (Boolean.valueOf(System.getenv("TDP_MESSAGE_PROVISION_ENABLE"))) {
					update.set("notifications.cancel_send_date", LocalDateTime.now(ZoneOffset.of("-05:00")));
				}
			}

			if (Status.FINALIZADO.getStatusName().equals(listProvision.get(i).getLastTrackingStatus())) {
				update.set("notifications.finalizado_send_notify", true);
				if (Boolean.valueOf(System.getenv("TDP_MESSAGE_PROVISION_ENABLE"))) {
					update.set("notifications.finalizado_send_date", LocalDateTime.now(ZoneOffset.of("-05:00")));
				}
			}

			if (Status.CANCELADA_ATIS.getStatusName().equals(listProvision.get(i).getLastTrackingStatus())) {
				update.set("notifications.cancelada_atis_send_notify", true);
				if (Boolean.valueOf(System.getenv("TDP_MESSAGE_PROVISION_ENABLE"))) {
					update.set("notifications.cancelada_atis_send_date", LocalDateTime.now(ZoneOffset.of("-05:00")));
				}
			}

			this.mongoOperations.updateFirst(
					new Query(Criteria.where("idProvision").is(new ObjectId(listProvision.get(i).getIdProvision()))),
					update, Provision.class);
		}
	}

	@Override
	public void updateResendNotification(List<ProvisionCustomerDto> listProvision) {
		Update update = new Update();
		ResendNotification resendNotification = new ResendNotification();
		List<ResendNotification> listResendNotification = new ArrayList<ResendNotification>();
		for (int i = 0; i < listProvision.size(); i++) {
			resendNotification = new ResendNotification();
			listResendNotification = new ArrayList<ResendNotification>();
			resendNotification.setIntoaCount(1);
			resendNotification.setIntoaSendDate(LocalDateTime.now(ZoneOffset.of("-05:00")));
			resendNotification.setIntoaSendNotify(true);
			listResendNotification.add(resendNotification);
			update.set("resend_intoa", listResendNotification);

			this.mongoOperations.updateFirst(
					new Query(Criteria.where("idProvision").is(new ObjectId(listProvision.get(i).getIdProvision()))),
					update, Provision.class);
		}

	}

	@Override
	public boolean updateShowLocation(Provision provision) {
		Update update = new Update();
		update.set("show_location", true);
		UpdateResult result = this.mongoOperations.updateFirst(
				new Query(Criteria.where("idProvision").is(new ObjectId(provision.getIdProvision()))), update,
				Provision.class);
		return result.getMatchedCount() > 0;
	}

	@Override
	public Optional<Provision> getProvisionByIdAndActiveStatus(String provisionId, String activeStatus) {

		Provision provision = this.mongoOperations.findOne(new Query(Criteria.where("idProvision")
				.is(new ObjectId(provisionId)).andOperator(Criteria.where("active_status").is(activeStatus))),
				Provision.class);
		Optional<Provision> optionalProvision = Optional.ofNullable(provision);

		return optionalProvision;

	}

	@Override
	public Optional<pe.telefonica.provision.model.Status> getInfoStatus(String statusName) {
		pe.telefonica.provision.model.Status status = this.mongoOperations.findOne(
				new Query(Criteria.where("status_name").is(statusName)), pe.telefonica.provision.model.Status.class);
		Optional<pe.telefonica.provision.model.Status> optionalStatus = Optional.ofNullable(status);

		return optionalStatus;
	}

	@Override
	public Optional<List<pe.telefonica.provision.model.Status>> getAllInfoStatus() {
		List<pe.telefonica.provision.model.Status> statusList = this.mongoOperations
				.findAll(pe.telefonica.provision.model.Status.class);
		Optional<List<pe.telefonica.provision.model.Status>> optionalStatus = Optional.ofNullable(statusList);

		return optionalStatus;
	}

	public Provision getProvisionByIdNotFilter(String provisionId) {

		Provision provision = this.mongoOperations
				.findOne(new Query(Criteria.where("idProvision").is(new ObjectId(provisionId))), Provision.class);

		return provision;
	}

	@Override
	public Optional<List<Provision>> getUpFrontProvisionsOnDay() {
		LocalDate today = LocalDate.now(ZoneOffset.of("-05:00")).minusDays(1);
		LocalDate yesterday = today.minusDays(1);

		List<Provision> provisions = this.mongoOperations
				.find(new Query(Criteria.where("is_up_front").is(true).and("up_front_read").is(false).andOperator(
						Criteria.where("register_date").gt(yesterday), Criteria.where("register_date").lt(today),
						Criteria.where("dummy_st_psi_code").ne(null), Criteria.where("dummy_st_psi_code").ne("")))
								.limit(10),
						Provision.class);
		Optional<List<Provision>> optionalProvisions = Optional.ofNullable(provisions);
		return optionalProvisions;
	}

	@Override
	public void updateUpFrontProvisionRead(List<Provision> provisions) {
		Update update = new Update();
		update.set("up_front_read", true);

		for (int i = 0; i < provisions.size(); i++) {
			this.mongoOperations.updateFirst(
					new Query(Criteria.where("idProvision").is(new ObjectId(provisions.get(i).getIdProvision()))),
					update, Provision.class);
		}

	}

	@Override
	public Provision getProvisionDetailById(String provisionId) {

		Provision provision = this.mongoOperations
				.findOne(new Query(Criteria.where("_id").is(new ObjectId(provisionId))), Provision.class);

		return provision;
	}
}
