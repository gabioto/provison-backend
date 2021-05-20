package pe.telefonica.provision.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.telefonica.provision.controller.request.CustomerRequest;
import pe.telefonica.provision.controller.request.KafkaTOARequest;
import pe.telefonica.provision.controller.request.KafkaTOARequest.Event.Appointment;
import pe.telefonica.provision.controller.request.ScheduleNotDoneRequest;
import pe.telefonica.provision.controller.request.ScheduleRequest;
import pe.telefonica.provision.external.SimpliConnectApi;
import pe.telefonica.provision.external.TrazabilidadScheduleApi;
import pe.telefonica.provision.external.TrazabilidadSecurityApi;
import pe.telefonica.provision.external.request.schedule.GetTechnicianAvailableRequest;
import pe.telefonica.provision.external.request.simpli.SimpliRequest;
import pe.telefonica.provision.model.Contacts;
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.model.HomePhone;
import pe.telefonica.provision.model.Internet;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.Provision.StatusLog;
import pe.telefonica.provision.model.ReturnedProvision;
import pe.telefonica.provision.model.Television;
import pe.telefonica.provision.model.provision.InToa;
import pe.telefonica.provision.model.provision.Notifications;
import pe.telefonica.provision.model.provision.WoCancel;
import pe.telefonica.provision.model.provision.WoCompleted;
import pe.telefonica.provision.model.provision.WoInit;
import pe.telefonica.provision.model.provision.WoNotdone;
import pe.telefonica.provision.model.provision.WoPreNotdone;
import pe.telefonica.provision.model.provision.WoPreStart;
import pe.telefonica.provision.model.provision.WoReshedule;
import pe.telefonica.provision.repository.ProvisionRepository;
import pe.telefonica.provision.service.ProvisionUpdateService.ProvisionUpdateTobeService;
import pe.telefonica.provision.util.constants.Constants;
import pe.telefonica.provision.util.constants.Status;

@Service
@Transactional
public class ProvisionUpdateTobeServiceImpl extends ProvisionUpdateServiceImpl implements ProvisionUpdateTobeService {

	private static final Log log = LogFactory.getLog(ProvisionUpdateTobeServiceImpl.class);

	@Autowired
	private ProvisionRepository provisionRepository;

	@Autowired
	private TrazabilidadScheduleApi trazabilidadScheduleApi;

	@Autowired
	private TrazabilidadSecurityApi trazabilidadSecurityApi;

	@Autowired
	private SimpliConnectApi simpliConnectApi;

	@Override
	public boolean provisionUpdateFromTOA(KafkaTOARequest kafkaTOARequest) throws Exception {

		boolean updated = false;

		Provision provision = provisionRepository
				.getProvisionByXaIdSt(kafkaTOARequest.getEvent().getAppointment().getId());

		updated = provision != null ? updateProvision(provision, kafkaTOARequest) : insertProvision(kafkaTOARequest);

		return updated;
	}

	private boolean insertProvision(KafkaTOARequest kafkaTOARequest) {

		Optional<List<pe.telefonica.provision.model.Status>> statusListOptional = provisionRepository
				.getAllInfoStatus();

		List<pe.telefonica.provision.model.Status> statusList = statusListOptional.get();

		Appointment appointment = kafkaTOARequest.getEvent().getAppointment();

		if (kafkaTOARequest.getEventType().equalsIgnoreCase(Status.IN_TOA.getStatusName())) {

			pe.telefonica.provision.model.Status inToaStatus = getInfoStatus(Status.IN_TOA.getStatusName(), statusList);

			String productName = (appointment.getRelatedObject().get(2).getReference() != null
					&& !appointment.getRelatedObject().get(2).getReference().isEmpty())
							? appointment.getRelatedObject().get(2).getReference()
							: "Movistar Hogar";

			log.info("productName" + productName);

			Provision provision = new Provision();
			provision.setProductName(productName);
			provision.setCustomerType(appointment.getRelatedParty().get(0).getAdditionalData().get(0).getValue());
			provision.setCustomerSubType(appointment.getRelatedParty().get(0).getAdditionalData().get(1).getValue());
			provision.setPriority(appointment.getRelatedParty().get(0).getAdditionalData().get(2).getValue());
			provision.setApptNumber(appointment.getId());
			provision.setXaIdSt(appointment.getId());
			provision.setScheduler(appointment.getScheduler().toUpperCase());
			provision.setActivityType(appointment.getDescription().toLowerCase());
			provision.setWorkZone(appointment.getAdditionalData().get(1).getValue());
			provision.setActiveStatus(Constants.PROVISION_STATUS_ACTIVE);
			provision.setStatusToa(Constants.PROVISION_STATUS_DONE);
			provision.setLastTrackingStatus(Status.IN_TOA.getStatusName());
			provision.setGenericSpeech(inToaStatus != null ? inToaStatus.getSpeechWithoutSchedule()
					: Status.IN_TOA.getSpeechWithoutSchedule());
			provision.setDescriptionStatus(
					inToaStatus != null ? inToaStatus.getDescription() : Status.IN_TOA.getDescription());
			provision.setFrontSpeech(inToaStatus != null ? inToaStatus.getFront() : Status.IN_TOA.getFrontSpeech());

			Customer customer = new Customer();
			customer.setName(appointment.getRelatedParty().get(0).getName());
			customer.setDocumentType(appointment.getRelatedParty().get(0).getLegalId().get(0).getNationalIdType());
			customer.setDocumentNumber(appointment.getRelatedParty().get(0).getLegalId().get(0).getNationalId());
			customer.setPhoneNumber(appointment.getContactMedium().get(4).getNumber());
			customer.setMail(appointment.getContactMedium().get(6).getEmail());
			customer.setDistrict(appointment.getRelatedPlace().getAddress().getCity());
			customer.setProvince(appointment.getRelatedPlace().getAddress().getStateOrProvince());
			customer.setDepartment(appointment.getRelatedPlace().getAddress().getRegion());
			customer.setAddress(appointment.getRelatedPlace().getName());
			customer.setLatitude(appointment.getRelatedPlace().getAddress().getCoordinates().getLatitude());
			customer.setLongitude(appointment.getRelatedPlace().getAddress().getCoordinates().getLongitude());
			customer.setCarrier(!customer.getPhoneNumber().isEmpty() ? getCarrier(customer.getPhoneNumber()) : false);
			provision.setCustomer(customer);

			Contacts contact = new Contacts();
			contact.setFullName(appointment.getRelatedObject().get(1).getAdditionalData().size() > 0
					? appointment.getRelatedObject().get(1).getAdditionalData().get(0).getValue()
					: "");
			contact.setPhoneNumber(appointment.getContactMedium().get(5).getNumber());
			contact.setMail(appointment.getContactMedium().get(7).getEmail());
			contact.setCarrier(!contact.getPhoneNumber().isEmpty() ? getCarrier(contact.getPhoneNumber()) : false);
			provision.getContacts().add(contact);

			HomePhone phone = new HomePhone();
			phone.setNetworkTechnology(appointment.getRelatedObject().get(2).getAdditionalData().get(0).getValue());
			phone.setTechnology(appointment.getRelatedObject().get(2).getAdditionalData().get(1).getValue());
			provision.setHomePhoneDetail(phone);

			Internet internet = new Internet();
			internet.setNetworkTechnology(appointment.getRelatedObject().get(2).getAdditionalData().get(2).getValue());
			internet.setTechnology(appointment.getRelatedObject().get(2).getAdditionalData().get(3).getValue());
			provision.setInternetDetail(internet);

			Television tv = new Television();
			tv.setNetworkTechnology(appointment.getRelatedObject().get(2).getAdditionalData().get(4).getValue());
			tv.setTechnology(appointment.getRelatedObject().get(2).getAdditionalData().get(5).getValue());
			provision.setTvDetail(tv);

			Notifications notifications = new Notifications();
			notifications.setIntoaSendNotify(false);
			provision.setNotifications(notifications);

			InToa inToa = new InToa();
			inToa.setXaNote(appointment.getNote().get(0).getText());
			inToa.setXaCreationDate(appointment.getCreationDate());
			inToa.setDate(kafkaTOARequest.getEventTime());
			inToa.setXaScheduler(appointment.getScheduler().toUpperCase());
			inToa.setLongitude(appointment.getRelatedPlace().getAddress().getCoordinates().getLongitude());
			inToa.setLatitude(appointment.getRelatedPlace().getAddress().getCoordinates().getLatitude());
			provision.setInToa(inToa);

			WoPreStart prestart = new WoPreStart();
			prestart.setTrackingUrl(null);
			prestart.setAvailableTracking(false);
			provision.setWoPreStart(prestart);

			StatusLog statusLog = new StatusLog();
			statusLog.setStatus(kafkaTOARequest.getEventType());
			statusLog.setXaidst(appointment.getId());
			provision.getLogStatus().add(statusLog);

			try {
				if (!appointment.getScheduledDate().equals("3000-01-01")) {

					pe.telefonica.provision.model.Status scheduled = getInfoStatus(Status.SCHEDULED.getStatusName(),
							statusList);

					String range = getRange(appointment);

					String dateString = appointment.getScheduledDate().substring(0, 10);
					String dateString2 = getFormatedDate(appointment);

					statusLog = new StatusLog();
					statusLog.setStatus(Status.SCHEDULED.getStatusName());
					statusLog.setXaidst(appointment.getId());
					statusLog.setScheduledDate(dateString);
					statusLog.setScheduledRange(range);
					provision.getLogStatus().add(statusLog);

					provision.setHasSchedule(true);
					provision.setLastTrackingStatus(Status.SCHEDULED.getStatusName());
					provision.setGenericSpeech(
							scheduled != null ? scheduled.getGenericSpeech() : Status.SCHEDULED.getGenericSpeech());
					provision.setDescriptionStatus(
							scheduled != null ? scheduled.getDescription() : Status.SCHEDULED.getDescription());
					provision.setFrontSpeech(
							scheduled != null ? scheduled.getFront() : Status.SCHEDULED.getFrontSpeech());

					// Llamar a servicio de agendamiento para regularizar la agenda
					trazabilidadScheduleApi
							.insertSchedule(generateScheduleRequest(provision, appointment, range, dateString2));
				}

			} catch (Exception e) {
				return false;
			}

			Optional<Provision> opt = provisionRepository.insertProvision(provision);

			return opt.isPresent();
		}

		return false;
	}

	private boolean updateProvision(Provision provision, KafkaTOARequest kafkaTOARequest) throws Exception {

		Optional<List<pe.telefonica.provision.model.Status>> statusListOptional = provisionRepository
				.getAllInfoStatus();

		List<pe.telefonica.provision.model.Status> statusList = statusListOptional.get();

		String provisionStatus = kafkaTOARequest.getEventType();

		boolean updatedProvision = false;

		switch (provisionStatus.toUpperCase()) {
		case Constants.STATUS_IN_TOA:
			pe.telefonica.provision.model.Status inToaStatus = getInfoStatus(Status.IN_TOA.getStatusName(), statusList);
			updatedProvision = updateInToa(provision, kafkaTOARequest, inToaStatus);
			break;
		case Constants.STATUS_WO_PRESTART:
			pe.telefonica.provision.model.Status preStartStatus = getInfoStatus(Status.WO_PRESTART.getStatusName(),
					statusList);
			updatedProvision = updateWoPrestart(provision, kafkaTOARequest, preStartStatus);
			break;
		case Constants.STATUS_WO_INIT:
			pe.telefonica.provision.model.Status initStatus = getInfoStatus(Status.WO_INIT.getStatusName(), statusList);
			updatedProvision = updateWoInit(provision, kafkaTOARequest, initStatus);
			break;
		case Constants.STATUS_WO_COMPLETED:
			pe.telefonica.provision.model.Status completedStatus = getInfoStatus(Status.WO_COMPLETED.getStatusName(),
					statusList);
			updatedProvision = updateWoCompleted(provision, kafkaTOARequest, completedStatus);
			break;
		case Constants.STATUS_WO_CANCEL:
			pe.telefonica.provision.model.Status cancelStatus = getInfoStatus(Status.WO_CANCEL.getStatusName(),
					statusList);
			updatedProvision = updateWoCancel(provision, kafkaTOARequest, cancelStatus);
			break;
		case Constants.STATUS_WO_RESCHEDULE:
			pe.telefonica.provision.model.Status rescheduleStatus = getInfoStatus(Status.SCHEDULED.getStatusName(),
					statusList);
			updatedProvision = updateWoReschdule(provision, kafkaTOARequest, rescheduleStatus);
			break;
		case Constants.STATUS_WO_NOTDONE:
			pe.telefonica.provision.model.Status notDoneStatus = getInfoStatus(Status.WO_NOTDONE.getStatusName(),
					statusList);
			updatedProvision = updateWoNotDone(provision, kafkaTOARequest, notDoneStatus);
			break;
		case Constants.STATUS_WO_PRE_NOTDONE:
			pe.telefonica.provision.model.Status prenotDoneStatus = getInfoStatus(Status.WO_PRENOTDONE.getStatusName(),
					statusList);
			updatedProvision = updateWoPreNotDone(provision, kafkaTOARequest, prenotDoneStatus);
			break;
		default:
			break;
		}

		return updatedProvision;
	}

	private String getFormatedDate(Appointment appointment) throws ParseException {

		SimpleDateFormat parseador2 = new SimpleDateFormat("yyyy-MM-dd"); // el que parsea
		SimpleDateFormat formateador2 = new SimpleDateFormat("dd/MM/yyyy"); // el que formatea

		Date date2 = parseador2.parse(appointment.getScheduledDate());// ("31-03-2016");

		return formateador2.format(date2);
	}

	private String getRange(Appointment appointment) {
		return (appointment.getTimeSlot().trim().equals("09-13")
				|| appointment.getTimeSlot().toString().trim().equals("9-13")) ? "AM" : "PM";
	}

	private ScheduleRequest generateScheduleRequest(Provision provision, Appointment appointment, String range,
			String appointmentDate) {

		ScheduleRequest scheduleRequest = new ScheduleRequest();
		scheduleRequest.setBucket(provision.getWorkZone());
		scheduleRequest.setWorkZone(provision.getWorkZone());
		scheduleRequest.setPilot(false);
		scheduleRequest.setChannel("TZ");
		scheduleRequest.setOrderCode(provision.getXaRequest());
		scheduleRequest.setXaOrderCode(provision.getXaRequest());
		scheduleRequest.setRequestId(provision.getIdProvision());
		scheduleRequest.setRequestType(provision.getActivityType());
		scheduleRequest.setRequestName(provision.getProductName());
		scheduleRequest.setRequestId(provision.getIdProvision());
		scheduleRequest.setSelectedDate(appointmentDate);
		scheduleRequest.setSelectedRange(range);
		scheduleRequest.setStpsiCode(appointment.getId());
		scheduleRequest.setDocumentNumber(provision.getCustomer().getDocumentNumber());
		scheduleRequest.setDocumentType(provision.getCustomer().getDocumentType());
		scheduleRequest.setScheduler(provision.getScheduler());
		scheduleRequest.setPriority(provision.getPriority());
		scheduleRequest.setCustomerType(provision.getCustomerType());
		scheduleRequest.setCustomerSubType(provision.getCustomerSubType());
		scheduleRequest.setPhoneNetworkTechnology(
				provision.getHomePhoneDetail() != null ? provision.getHomePhoneDetail().getNetworkTechnology() : "");
		scheduleRequest.setPhoneTechnology(
				provision.getHomePhoneDetail() != null ? provision.getHomePhoneDetail().getTechnology() : "");
		scheduleRequest.setBroadbandNetworkTechnology(
				provision.getInternetDetail() != null ? provision.getInternetDetail().getNetworkTechnology() : "");
		scheduleRequest.setBroadbandTechnology(
				provision.getInternetDetail() != null ? provision.getInternetDetail().getTechnology() : "");
		scheduleRequest.setTvNetworkTechnology(
				provision.getTvDetail() != null ? provision.getTvDetail().getNetworkTechnology() : "");
		scheduleRequest.setTvTechnology(provision.getTvDetail() != null ? provision.getTvDetail().getTechnology() : "");
		scheduleRequest.setCustomer(new CustomerRequest().fromCustomer(provision.getCustomer()));

		return scheduleRequest;
	}

	@Override
	public boolean updateInToa(Provision provision, KafkaTOARequest kafkaToaRequest,
			pe.telefonica.provision.model.Status inToaStatus) {

		Appointment appointment = kafkaToaRequest.getEvent().getAppointment();

		InToa inToa = new InToa();
		inToa.setXaNote(appointment.getNote().get(0).getText());
		inToa.setXaCreationDate(appointment.getCreationDate());
		inToa.setDate(kafkaToaRequest.getEventTime());
		inToa.setXaScheduler(appointment.getScheduler().toUpperCase());
		inToa.setLongitude(appointment.getRelatedPlace().getAddress().getCoordinates().getLongitude());
		inToa.setLatitude(appointment.getRelatedPlace().getAddress().getCoordinates().getLatitude());

		StatusLog statusLog = new StatusLog();
		statusLog.setStatus(Status.IN_TOA.getStatusName());
		statusLog.setXaidst(appointment.getId());
		provision.getLogStatus().add(statusLog);

		Update update = new Update();
		update.set("notifications.into_send_notify", false);
		update.set("show_location", false);
		update.set("wo_prestart.tracking_url", null);
		update.set("wo_prestart.available_tracking", false);
		update.set("in_toa", inToa);
		update.set("active_status", Constants.PROVISION_STATUS_ACTIVE);
		update.set("status_toa", Constants.PROVISION_STATUS_DONE);
		update.set("last_tracking_status", Status.IN_TOA.getStatusName());
		update.set("generic_speech", inToaStatus != null ? inToaStatus.getSpeechWithoutSchedule()
				: Status.IN_TOA.getSpeechWithoutSchedule());
		update.set("description_status",
				inToaStatus != null ? inToaStatus.getDescription() : Status.IN_TOA.getDescription());
		update.set("front_speech", inToaStatus != null ? inToaStatus.getFront() : Status.IN_TOA.getFrontSpeech());
		update.set("log_status", provision.getLogStatus());
		update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)));

		provisionRepository.updateProvision(provision, update);

		return true;
	}

	@Override
	public boolean updateWoPrestart(Provision provision, KafkaTOARequest kafkaToaRequest,
			pe.telefonica.provision.model.Status preStartStatus) {

		Appointment appointment = kafkaToaRequest.getEvent().getAppointment();

		WoPreStart woPreStart = provision.getWoPreStart() != null ? provision.getWoPreStart() : new WoPreStart();
		woPreStart.setNameResource(appointment.getRelatedParty().get(1).getName());
		woPreStart.setDate(appointment.getStatusChangeDate());
		woPreStart.setTechnicalId(appointment.getRelatedParty().get(1).getId());
		woPreStart.setFullName(appointment.getRelatedParty().get(1).getName());
		woPreStart.setDocumentNumber(appointment.getRelatedParty().get(1).getLegalId().get(0).getNationalId());
		woPreStart.setPhoneNumber(appointment.getRelatedParty().get(1).getContactMedium().get(0).getNumber());
		woPreStart.setLatitude(appointment.getRelatedPlace().getAddress().getCoordinates().getLatitude());
		woPreStart.setLongitude(appointment.getRelatedPlace().getAddress().getCoordinates().getLongitude());

		StatusLog statusLog = new StatusLog();
		statusLog.setStatus(Status.WO_PRESTART.getStatusName());
		statusLog.setXaidst(appointment.getId());
		provision.getLogStatus().add(statusLog);

		Update update = new Update();
		update.set("external_id", appointment.getRelatedParty().get(1).getId());
		update.set("active_status", Constants.PROVISION_STATUS_SCHEDULE_IN_PROGRESS);
		update.set("customer.latitude", appointment.getRelatedPlace().getAddress().getCoordinates().getLatitude());
		update.set("customer.longitude", appointment.getRelatedPlace().getAddress().getCoordinates().getLongitude());
		update.set("show_location", false);
		update.set("notifications.prestart_send_notify", false);
		update.set("last_tracking_status", Status.WO_PRESTART.getStatusName());
		update.set("generic_speech",
				preStartStatus != null ? preStartStatus.getGenericSpeech() : Status.WO_PRESTART.getGenericSpeech());
		update.set("description_status",
				preStartStatus != null ? preStartStatus.getDescription() : Status.WO_PRESTART.getDescription());
		update.set("front_speech",
				preStartStatus != null ? preStartStatus.getFront() : Status.WO_PRESTART.getFrontSpeech());
		update.set("log_status", provision.getLogStatus());

		// Job Woprestart
		LocalDateTime nowDate = LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE));

		if (nowDate.getHour() >= 07 && nowDate.getHour() <= 20) {
//			if (nowDate.getHour() >= 0 && nowDate.getHour() <= 23) {

			// SMS
			sendSMSWoPrestartHolder(provision);

			update.set("notifications.prestart_send_notify", true);
			update.set("notifications.prestart_send_date",
					LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)));

			if (Boolean.valueOf(System.getenv("TDP_SIMPLI_ENABLE"))) {

				String switchAzure = System.getenv("TDP_SWITCH_AZURE");
				String tokenExternal = switchAzure.equals("true") ? trazabilidadSecurityApi.gerateTokenAzure()
						: trazabilidadSecurityApi.generateToken();

				// validate TechAvailable
				GetTechnicianAvailableRequest getTechnicianAvailableRequest = new GetTechnicianAvailableRequest();
				getTechnicianAvailableRequest.setDni(woPreStart.getDocumentNumber());

				String isAvailableTech = trazabilidadScheduleApi.getTechAvailable(getTechnicianAvailableRequest);

				if (isAvailableTech != null) {
					sendEmailToCustomer(provision.getCustomer(), woPreStart);

					SimpliRequest simpliRequest = new SimpliRequest();
					simpliRequest.setLatitude(woPreStart.getLatitude());
					simpliRequest.setLongitude(woPreStart.getLongitude());
					simpliRequest.setVisitTitle(woPreStart.getFullName());
					simpliRequest.setVisitAddress(provision.getCustomer().getAddress());
					simpliRequest.setDriverUserName(isAvailableTech);
					simpliRequest.setToken(tokenExternal);

					int count = 0;
					int maxTries = 2;

					while (count < maxTries) {
						String urlSimpli = switchAzure.equals("true")
								? urlSimpli = simpliConnectApi.getUrlTraking(simpliRequest)
								: simpliConnectApi.getUrlTrakingOld(simpliRequest);

						if (urlSimpli != null) {
							// SEND SMS BY CONTACTS
							woPreStart.setTrackingUrl(urlSimpli);
							provision.setWoPreStart(woPreStart);
							sendSMSWoPrestartContact(provision);

							woPreStart.setAvailableTracking(true);

						} else {
							if (++count == maxTries) {
								break;
							}
						}
					}

				}
			}
		}

		update.set("wo_prestart", woPreStart);
		update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)));

		provisionRepository.updateProvision(provision, update);

		return true;
	}

	@Override
	public boolean updateWoInit(Provision provision, KafkaTOARequest kafkaToaRequest,
			pe.telefonica.provision.model.Status initStatus) {

		Appointment appointment = kafkaToaRequest.getEvent().getAppointment();

		WoInit woInit = new WoInit();
		woInit.setNameResource(appointment.getRelatedParty().get(1).getName());
		woInit.setEtaStartTime(appointment.getStartDate());
		woInit.setEtaEndTime(appointment.getEndDate());
		woInit.setXaCreationDate(appointment.getCreationDate());
		woInit.setDate(appointment.getStatusChangeDate());
		woInit.setXaNote(appointment.getNote().get(0).getText());

		StatusLog statusLog = new StatusLog();
		statusLog.setStatus(Status.WO_INIT.getStatusName());
		statusLog.setXaidst(appointment.getId());
		provision.getLogStatus().add(statusLog);

		Update update = new Update();
		update.set("wo_init", woInit);
		update.set("show_location", false);
		update.set("active_status", Constants.PROVISION_STATUS_WOINIT);
		update.set("last_tracking_status", Status.WO_INIT.getStatusName());
		update.set("generic_speech",
				initStatus != null ? initStatus.getGenericSpeech() : Status.WO_INIT.getGenericSpeech());
		update.set("description_status",
				initStatus != null ? initStatus.getDescription() : Status.WO_INIT.getDescription());
		update.set("front_speech", initStatus != null ? initStatus.getFront() : Status.WO_INIT.getFrontSpeech());
		update.set("log_status", provision.getLogStatus());
		update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)));

		provisionRepository.updateProvision(provision, update);

		return true;
	}

	@Override
	public boolean updateWoCompleted(Provision provision, KafkaTOARequest kafkaToaRequest,
			pe.telefonica.provision.model.Status completedStatus) {

		Appointment appointment = kafkaToaRequest.getEvent().getAppointment();

		WoCompleted woCompleted = new WoCompleted();
		woCompleted.setXaCreationDate(appointment.getCreationDate());
		woCompleted.setDate(appointment.getStatusChangeDate());
		woCompleted.setXaNote(appointment.getNote().get(0).getText());
		woCompleted.setEtaStartTime(appointment.getStartDate());
		woCompleted.setEtaEndTime(appointment.getEndDate());
		woCompleted.setObservation(appointment.getStatusChangeDate());

		StatusLog statusLog = new StatusLog();
		statusLog.setStatus(Status.WO_COMPLETED.getStatusName());
		statusLog.setXaidst(appointment.getId());
		provision.getLogStatus().add(statusLog);

		Update update = new Update();
		update.set("wo_completed", woCompleted);
		update.set("active_status", Constants.PROVISION_STATUS_COMPLETED);
		update.set("show_location", false);
		update.set("notifications.completed_send_notify", false);
		update.set("last_tracking_status", Status.WO_COMPLETED.getStatusName());
		update.set("generic_speech",
				completedStatus != null ? completedStatus.getGenericSpeech() : Status.WO_COMPLETED.getGenericSpeech());
		update.set("description_status",
				completedStatus != null ? completedStatus.getDescription() : Status.WO_COMPLETED.getDescription());
		update.set("front_speech",
				completedStatus != null ? completedStatus.getFront() : Status.WO_COMPLETED.getFrontSpeech());
		update.set("log_status", provision.getLogStatus());
		update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)));

		provisionRepository.updateProvision(provision, update);

		return true;
	}

	@Override
	public boolean updateWoCancel(Provision provision, KafkaTOARequest kafkaToaRequest,
			pe.telefonica.provision.model.Status cancelStatus) {

		Appointment appointment = kafkaToaRequest.getEvent().getAppointment();

		// se cancela porque se regulariza la ficticia en una real
		if (appointment.getStatusReason().toString().equals("2")) {
			return false;
		}

		WoCancel woCancel = new WoCancel();
		woCancel.setUserCancel(appointment.getRelatedParty().get(2).getId());
		woCancel.setXaCancelReason(appointment.getStatusReason());

		StatusLog statusLog = new StatusLog();
		statusLog.setStatus(Status.WO_CANCEL.getStatusName());
		statusLog.setXaidst(appointment.getId());
		provision.getLogStatus().add(statusLog);

		Update update = new Update();
		update.set("wo_cancel", woCancel);
		update.set("active_status", Constants.PROVISION_STATUS_CANCELLED);
		update.set("send_notify", false);
		update.set("xa_cancel_reason", appointment.getStatusReason());
		update.set("user_cancel", appointment.getRelatedParty().get(2).getId());
		update.set("last_tracking_status", Status.WO_CANCEL.getStatusName());
		update.set("generic_speech",
				cancelStatus != null ? cancelStatus.getGenericSpeech() : Status.WO_CANCEL.getGenericSpeech());
		update.set("description_status",
				cancelStatus != null ? cancelStatus.getDescription() : Status.WO_CANCEL.getDescription());
		update.set("front_speech", cancelStatus != null ? cancelStatus.getFront() : Status.WO_CANCEL.getFrontSpeech());
		update.set("show_location", false);
		update.set("log_status", provision.getLogStatus());
		update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)));

		// Actualiza estado en provision
		provisionRepository.updateProvision(provision, update);

		ScheduleNotDoneRequest scheduleNotDoneRequest = new ScheduleNotDoneRequest();
		scheduleNotDoneRequest.setRequestId(provision.getIdProvision());
		scheduleNotDoneRequest.setRequestType(provision.getActivityType());
		scheduleNotDoneRequest.setStPsiCode(appointment.getId());
		scheduleNotDoneRequest.setFlgFicticious(false);

		// Cancela agenda
		trazabilidadScheduleApi.cancelLocalSchedule(scheduleNotDoneRequest);

		return true;
	}

	@Override
	public boolean updateWoReschdule(Provision provision, KafkaTOARequest kafkaToaRequest,
			pe.telefonica.provision.model.Status rescheduleStatus) {

		Appointment appointment = kafkaToaRequest.getEvent().getAppointment();
		String range = getRange(appointment);
		String dateString = appointment.getScheduledDate().substring(0, 10);
		String dateString2;

		try {
			dateString2 = getFormatedDate(appointment);
		} catch (ParseException e) {
			log.error(e.getLocalizedMessage());
			return false;
		}

		List<StatusLog> listLogx = provision.getLogStatus().stream().filter(x -> "SCHEDULED".equals(x.getStatus()))
				.collect(Collectors.toList());

		if (listLogx.size() > 0
				&& (listLogx.get(listLogx.size() - 1).getScheduledDate().contentEquals(dateString.toString())
						&& listLogx.get(listLogx.size() - 1).getScheduledRange().contentEquals(range))) {
			return true;
		}

		WoReshedule woReshedule = new WoReshedule();
		woReshedule.setXaAppointmentScheduler(appointment.getScheduler().toUpperCase());
		woReshedule.setTimeSlot(range);

		StatusLog statusLog = new StatusLog();
		statusLog.setStatus(Status.SCHEDULED.getStatusName());
		statusLog.setScheduledRange(range);
		statusLog.setScheduledDate(dateString.toString());
		statusLog.setXaidst(appointment.getId());
		provision.getLogStatus().add(statusLog);

		Update update = new Update();
		update.set("wo_schedule", woReshedule);
		update.set("active_status", Constants.PROVISION_STATUS_ACTIVE);
		update.set("date", appointment.getScheduledDate());
		update.set("send_notify", false);
		update.set("time_slot", range);
		update.set("last_tracking_status", Status.SCHEDULED.getStatusName());
		update.set("generic_speech",
				rescheduleStatus != null ? rescheduleStatus.getGenericSpeech() : Status.SCHEDULED.getGenericSpeech());
		update.set("description_status",
				rescheduleStatus != null ? rescheduleStatus.getDescription() : Status.SCHEDULED.getDescription());
		update.set("front_speech",
				rescheduleStatus != null ? rescheduleStatus.getFront() : Status.SCHEDULED.getFrontSpeech());
		update.set("log_status", provision.getLogStatus());
		update.set("show_location", false);
		update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)));

		// Actualizar provision
		provisionRepository.updateProvision(provision, update);

		// Actualiza el agendamiento
		trazabilidadScheduleApi.updateSchedule(generateScheduleRequest(provision, appointment, range, dateString2));

		return true;
	}

	@Override
	public boolean updateWoNotDone(Provision provision, KafkaTOARequest kafkaToaRequest,
			pe.telefonica.provision.model.Status notDoneStatus) {

		Appointment appointment = kafkaToaRequest.getEvent().getAppointment();

		WoNotdone woNotdone = new WoNotdone();
		woNotdone.setaNotDoneTypeInstall(appointment.getAdditionalData().get(2).getValue());
		woNotdone.setaNotDoneReasonInstall(appointment.getStatusReason());
		woNotdone.setaNotDoneSubReasonInstall(appointment.getStatusReason());
		woNotdone.setaObservation(appointment.getNote().get(0).getText());
		woNotdone.setUserNotdone(appointment.getRelatedParty().get(4).getId());

		StatusLog statusLog = new StatusLog();
		statusLog.setStatus(Status.WO_NOTDONE.getStatusName());
		statusLog.setXaidst(appointment.getId());
		provision.getLogStatus().add(statusLog);

		String speech = notDoneStatus != null ? notDoneStatus.getGenericSpeech() : Status.WO_NOTDONE.getGenericSpeech();
		speech = hasCustomerInfo(provision.getCustomer())
				? speech.replace(Constants.TEXT_NAME_REPLACE, provision.getCustomer().getName().split(" ")[0])
				: speech;

		Update update = new Update();
		update.set("wo_notdone", woNotdone);
		update.set("active_status", Constants.PROVISION_STATUS_NOTDONE);
		update.set("a_observation", appointment.getNote().get(0).getText());
		update.set("user_notdone", appointment.getRelatedParty().get(4).getId());
		update.set("last_tracking_status", Status.WO_NOTDONE.getStatusName());
		update.set("generic_speech", speech);
		update.set("description_status",
				notDoneStatus != null ? notDoneStatus.getDescription() : Status.WO_NOTDONE.getDescription());
		update.set("front_speech",
				notDoneStatus != null ? notDoneStatus.getFront() : Status.WO_NOTDONE.getFrontSpeech());
		update.set("log_status", provision.getLogStatus());
		update.set("show_location", false);
		update.set("send_notify", false);

		String subReason;
		String nameReplace = (provision.getCustomer().getName() != null && !provision.getCustomer().getName().isEmpty())
				? provision.getCustomer().getName().split(" ")[0]
				: "Hola";

		if (notDoneStatus.getReturnedList() != null && notDoneStatus.getReturnedList().size() > 0) {
			Optional<ReturnedProvision> notDoneList = notDoneStatus.getReturnedList().stream()
					.filter(x -> woNotdone.getaNotDoneReasonInstall().equals(x.getCodReason())).findFirst();

			if (notDoneList.isPresent()) {
				subReason = notDoneList.get().getSubReason().replace(Constants.TEXT_NAME_REPLACE, nameReplace);
				update.set("sub_reason_not_done", subReason);
				update.set("action_not_done", notDoneList.get().getAction());
			} else {
				subReason = Constants.DEFAULT_NOTDONE_SUBREASON.replace(Constants.TEXT_NAME_REPLACE, nameReplace);
				update.set("sub_reason_not_done", subReason);
				update.set("action_not_done", Constants.DEFAULT_NOTDONE_ACTION);
			}
		} else {
			subReason = Constants.DEFAULT_NOTDONE_SUBREASON.replace(Constants.TEXT_NAME_REPLACE, nameReplace);
			update.set("sub_reason_not_done", subReason);
			update.set("action_not_done", Constants.DEFAULT_NOTDONE_ACTION);
		}

		update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)));

		// Actualiza provision
		provisionRepository.updateProvision(provision, update);

		ScheduleNotDoneRequest scheduleNotDoneRequest = new ScheduleNotDoneRequest();
		scheduleNotDoneRequest.setRequestId(provision.getIdProvision());
		scheduleNotDoneRequest.setRequestType(provision.getActivityType());
		scheduleNotDoneRequest.setStPsiCode(provision.getXaIdSt());
		scheduleNotDoneRequest.setFlgFicticious(false);

		// Cancela agenda sin ir a PSI
		trazabilidadScheduleApi.cancelLocalSchedule(scheduleNotDoneRequest);

		return true;
	}

	@Override
	public boolean updateWoPreNotDone(Provision provision, KafkaTOARequest kafkaToaRequest,
			pe.telefonica.provision.model.Status preNotDoneStatus) {

		Appointment appointment = kafkaToaRequest.getEvent().getAppointment();

		WoPreNotdone woPreNotdone = new WoPreNotdone();
		woPreNotdone.setaNotDoneTypeInstall(appointment.getAdditionalData().get(2).getValue());
		woPreNotdone.setaNotDoneReasonInstall(appointment.getStatusReason());
		woPreNotdone.setaNotDoneSubReasonInstall(appointment.getStatusReason());
		woPreNotdone.setaObservation(appointment.getNote().get(0).getText());
		woPreNotdone.setUserNotdone(appointment.getRelatedParty().get(4).getId());

		StatusLog statusLog = new StatusLog();
		statusLog.setStatus(Status.WO_PRENOTDONE.getStatusName());
		statusLog.setXaidst(appointment.getId());
		provision.getLogStatus().add(statusLog);

		String speech = preNotDoneStatus != null ? preNotDoneStatus.getGenericSpeech() : Status.WO_PRENOTDONE.getGenericSpeech();
		speech = hasCustomerInfo(provision.getCustomer())
				? speech.replace(Constants.TEXT_NAME_REPLACE, provision.getCustomer().getName().split(" ")[0])
				: speech;

		Update update = new Update();
		update.set("wo_notdone", woPreNotdone);
		update.set("active_status", Constants.PROVISION_STATUS_NOTDONE);
		update.set("a_observation", appointment.getNote().get(0).getText());
		update.set("user_prenotdone", appointment.getRelatedParty().get(4).getId());
		update.set("last_tracking_status", Status.WO_NOTDONE.getStatusName());
		update.set("generic_speech", speech);
		update.set("description_status",
				preNotDoneStatus != null ? preNotDoneStatus.getDescription() : Status.WO_NOTDONE.getDescription());
		update.set("front_speech",
				preNotDoneStatus != null ? preNotDoneStatus.getFront() : Status.WO_NOTDONE.getFrontSpeech());
		update.set("log_status", provision.getLogStatus());
		update.set("show_location", false);
		update.set("send_notify", false);

		String subReason;
		String nameReplace = (provision.getCustomer().getName() != null && !provision.getCustomer().getName().isEmpty())
				? provision.getCustomer().getName().split(" ")[0]
				: "Hola";

		if (preNotDoneStatus.getReturnedList() != null && preNotDoneStatus.getReturnedList().size() > 0) {
			Optional<ReturnedProvision> notDoneList = preNotDoneStatus.getReturnedList().stream()
					.filter(x -> woPreNotdone.getaNotDoneReasonInstall().equals(x.getCodReason())).findFirst();

			if (notDoneList.isPresent()) {
				subReason = notDoneList.get().getSubReason().replace(Constants.TEXT_NAME_REPLACE, nameReplace);
				update.set("sub_reason_not_done", subReason);
				update.set("action_not_done", notDoneList.get().getAction());
			} else {
				subReason = Constants.DEFAULT_NOTDONE_SUBREASON.replace(Constants.TEXT_NAME_REPLACE, nameReplace);
				update.set("sub_reason_not_done", subReason);
				update.set("action_not_done", Constants.DEFAULT_NOTDONE_ACTION);
			}
		} else {
			subReason = Constants.DEFAULT_NOTDONE_SUBREASON.replace(Constants.TEXT_NAME_REPLACE, nameReplace);
			update.set("sub_reason_not_done", subReason);
			update.set("action_not_done", Constants.DEFAULT_NOTDONE_ACTION);
		}

		update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)));

		update.set("notifications.prestart_send_notify", true);
		update.set("notifications.prestart_send_date", LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)));
		
		// SMS
		sendSMSWoPreNotDoneHolder(provision);
		
		// Actualiza provision
		provisionRepository.updateProvision(provision, update);
			
		return true;
	}
}