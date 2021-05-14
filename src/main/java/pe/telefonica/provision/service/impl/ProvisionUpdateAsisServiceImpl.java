package pe.telefonica.provision.service.impl;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
import pe.telefonica.provision.external.PSIApi;
import pe.telefonica.provision.external.SimpliConnectApi;
import pe.telefonica.provision.external.TrazabilidadScheduleApi;
import pe.telefonica.provision.external.TrazabilidadSecurityApi;
import pe.telefonica.provision.external.request.schedule.GetTechnicianAvailableRequest;
import pe.telefonica.provision.external.request.simpli.SimpliRequest;
import pe.telefonica.provision.model.Contacts;
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.Provision.StatusLog;
import pe.telefonica.provision.model.ReturnedProvision;
import pe.telefonica.provision.model.params.Parameter;
import pe.telefonica.provision.model.provision.InToa;
import pe.telefonica.provision.model.provision.WoCancel;
import pe.telefonica.provision.model.provision.WoCompleted;
import pe.telefonica.provision.model.provision.WoInit;
import pe.telefonica.provision.model.provision.WoNotdone;
import pe.telefonica.provision.model.provision.WoPreStart;
import pe.telefonica.provision.model.provision.WoReshedule;
import pe.telefonica.provision.repository.ParamsRepository;
import pe.telefonica.provision.repository.ProvisionRepository;
import pe.telefonica.provision.service.ProvisionUpdateService.ProvisionUpdateAsisService;
import pe.telefonica.provision.util.constants.Constants;
import pe.telefonica.provision.util.constants.Status;

@Service
@Transactional
public class ProvisionUpdateAsisServiceImpl extends ProvisionUpdateServiceImpl implements ProvisionUpdateAsisService {

	private static final Log log = LogFactory.getLog(ProvisionUpdateAsisServiceImpl.class);

	@Autowired
	private ParamsRepository paramsRepository;

	@Autowired
	private ProvisionRepository provisionRepository;

	@Autowired
	private TrazabilidadScheduleApi trazabilidadScheduleApi;

	@Autowired
	private TrazabilidadSecurityApi trazabilidadSecurityApi;

	@Autowired
	private SimpliConnectApi simpliConnectApi;

	@Autowired
	private PSIApi restPSI;

	@Override
	public boolean provisionUpdateFromTOA(KafkaTOARequest kafkaTOARequest) throws Exception {

		boolean updated = false;

		Appointment appointment = kafkaTOARequest.getEvent().getAppointment();

		String xaRequirementNumber = appointment.getRelatedObject().get(0).getAdditionalData().get(1).getValue();

		String xaRequest = appointment.getRelatedObject().get(0).getAdditionalData().get(0).getValue();

		boolean fromSale = xaRequirementNumber.startsWith("MT") || xaRequirementNumber.startsWith("VF");

		Provision provision = !fromSale ? provisionRepository.getByOrderCodeForUpdate(xaRequest)
				: provisionRepository.getByOrderCodeForUpdateFicticious(xaRequirementNumber);

		updated = updateProvision(provision, kafkaTOARequest, fromSale, appointment, xaRequest, xaRequirementNumber);

		return updated;
	}

	private boolean updateProvision(Provision provision, KafkaTOARequest kafkaTOARequest, boolean fromSale,
			Appointment appointment, String getXaRequest, String getXaRequirementNumber) throws Exception {

		String speech = "";

		String provisionStatus = kafkaTOARequest.getEventType();

		String getXaIdSt = kafkaTOARequest.getEvent().getAppointment().getRelatedObject().get(0).getAdditionalData()
				.get(5).getValue();

		Optional<List<pe.telefonica.provision.model.Status>> statusListOptional = provisionRepository
				.getAllInfoStatus();

		List<pe.telefonica.provision.model.Status> statusList = statusListOptional.get();

		if (provision != null) {
			List<StatusLog> listLog = provision.getLogStatus();

			// valida Bucket x Producto
			boolean boolBucket = validateBucketProduct(appointment, provision, provisionStatus);

			if (boolBucket) {
				return false;
			}

			pe.telefonica.provision.model.Status dummyInToa = getInfoStatus(Status.DUMMY_IN_TOA.getStatusName(),
					statusList);

			speech = dummyInToa != null ? dummyInToa.getGenericSpeech() : Status.DUMMY_IN_TOA.getGenericSpeech();
			speech = hasCustomerInfo(provision.getCustomer())
					? speech.replace(Constants.TEXT_NAME_REPLACE, provision.getCustomer().getName().split(" ")[0])
					: speech;

			if (provisionStatus.equalsIgnoreCase(Status.IN_TOA.getStatusName())) {
				Update update = new Update();
				if (provision.getCommercialOp().equals(Constants.OP_COMMERCIAL_MIGRACION)) {
					Parameter objParams = paramsRepository.getMessage(Constants.MESSAGE_RETURN);
					if (objParams != null) {
						update.set("text_return", objParams.getValue());
					}
				}

				if (fromSale) {
					// IN_TOA fictitious
					// NO SMS
					StatusLog statusLog = new StatusLog();
					statusLog.setStatus(Status.DUMMY_IN_TOA.getStatusName());
					listLog.add(statusLog);

					update.set("log_status", listLog);
					update.set("last_tracking_status", Status.DUMMY_IN_TOA.getStatusName());
					update.set("generic_speech", speech);
					update.set("description_status",
							dummyInToa != null ? dummyInToa.getDescription() : Status.DUMMY_IN_TOA.getDescription());
					update.set("front_speech",
							dummyInToa != null ? dummyInToa.getFront() : Status.DUMMY_IN_TOA.getFrontSpeech());
					update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of("-05:00")));

					provisionRepository.updateProvision(provision, update);
					return true;

				} else if (getXaRequest.toString().equals("0") && !fromSale) {
					// IN_TOA Monoproducto
					pe.telefonica.provision.model.Status inToa = getInfoStatus(Status.IN_TOA.getStatusName(),
							statusList);

					String speechInToa = inToa != null ? inToa.getGenericSpeech() : Status.IN_TOA.getGenericSpeech();
					speechInToa = hasCustomerInfo(provision.getCustomer()) ? speechInToa.replace(
							Constants.TEXT_NAME_REPLACE, provision.getCustomer().getName().split(" ")[0]) : speechInToa;

					// SI SMS
					StatusLog statusLog = new StatusLog();
					statusLog.setStatus(Status.IN_TOA.getStatusName());

					update.set("xa_request", getXaRequirementNumber);
					update.set("xa_id_st", getXaIdSt);
					update.set("xa_requirement_number", getXaRequirementNumber);
					update.set("appt_number", appointment.getId());
					update.set("activity_type", appointment.getDescription().toLowerCase());
					update.set("work_zone", appointment.getAdditionalData().get(2).getValue());
					update.set("notifications.intoa_send_notify", false);
					listLog.add(statusLog);
					update.set("log_status", listLog);
					update.set("last_tracking_status", Status.IN_TOA.getStatusName());
					update.set("generic_speech", speechInToa);
					update.set("description_status",
							inToa != null ? inToa.getDescription() : Status.IN_TOA.getDescription());
					update.set("front_speech", inToa != null ? inToa.getFront() : Status.IN_TOA.getFrontSpeech());

					update.set("active_status", Constants.PROVISION_STATUS_ACTIVE);
					update.set("status_toa", Constants.PROVISION_STATUS_DONE);

					update.set("show_location", false);
					update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of("-05:00")));
					provisionRepository.updateProvision(provision, update);
					return true;
				} else {
					pe.telefonica.provision.model.Status inToaStatus = getInfoStatus(Status.IN_TOA.getStatusName(),
							statusList);

					update.set("xa_id_st", getXaIdSt);
					update.set("xa_requirement_number", getXaRequirementNumber);
					update.set("appt_number", appointment.getId());
					update.set("activity_type", appointment.getDescription().toLowerCase());
					update.set("work_zone", appointment.getAdditionalData().get(1).getValue());
					update.set("notifications.into_send_notify", false);
					update.set("show_location", false);
					if (provision.getXaIdSt() != null) {
						update.set("has_schedule", false);
					}

					update.set("wo_prestart.tracking_url", null);
					update.set("wo_prestart.available_tracking", false);

					InToa inToa = new InToa();

					inToa.setXaNote(appointment.getNote().get(0).getText());
					inToa.setXaCreationDate(appointment.getCreationDate());
					inToa.setDate(kafkaTOARequest.getEventTime());
					inToa.setXaScheduler(appointment.getScheduler());
					inToa.setLongitude(appointment.getRelatedPlace().getAddress().getCoordinates().getLongitude());
					inToa.setLatitude(appointment.getRelatedPlace().getAddress().getCoordinates().getLatitude());

					update.set("in_toa", inToa);
					update.set("active_status", Constants.PROVISION_STATUS_ACTIVE);
					update.set("status_toa", Constants.PROVISION_STATUS_DONE);

					StatusLog statusLog = new StatusLog();
					statusLog.setStatus(Status.IN_TOA.getStatusName());
					statusLog.setXaidst(getXaIdSt);

					update.set("last_tracking_status", Status.IN_TOA.getStatusName());
					update.set("generic_speech", inToaStatus != null ? inToaStatus.getSpeechWithoutSchedule()
							: Status.IN_TOA.getSpeechWithoutSchedule());
					update.set("description_status",
							inToaStatus != null ? inToaStatus.getDescription() : Status.IN_TOA.getDescription());
					update.set("front_speech",
							inToaStatus != null ? inToaStatus.getFront() : Status.IN_TOA.getFrontSpeech());
					listLog.add(statusLog);

					// Regularizar Agenda Ficticia
					if (provision.getXaIdSt() == null && provision.getDummyStPsiCode() != null) {
						List<StatusLog> listLogx = listLog.stream()
								.filter(x -> Status.FICTICIOUS_SCHEDULED.getStatusName().equals(x.getStatus()))
								.collect(Collectors.toList());

						List<StatusLog> listLogCancelled = listLog.stream()
								.filter(x -> Status.WO_CANCEL.getStatusName().equals(x.getStatus()))
								.collect(Collectors.toList());

						if (listLogx.size() > 0 && listLogCancelled.size() == 0
								&& isAValidSchedule(listLogx.get(0).getScheduledDate())) {

							pe.telefonica.provision.model.Status scheduled = getInfoStatus(
									Status.SCHEDULED.getStatusName(), statusList);

							StatusLog statusSchedule = new StatusLog();
							statusSchedule.setStatus(Status.SCHEDULED.getStatusName());
							statusSchedule.setXaidst(getXaIdSt);
							statusSchedule.setScheduledDate(listLogx.get(0).getScheduledDate());
							statusSchedule.setScheduledRange(listLogx.get(0).getScheduledRange());
							listLog.add(statusSchedule);

							update.set("last_tracking_status", Status.SCHEDULED.getStatusName());
							update.set("generic_speech", scheduled != null ? scheduled.getGenericSpeech()
									: Status.SCHEDULED.getGenericSpeech());
							update.set("description_status",
									scheduled != null ? scheduled.getDescription() : Status.SCHEDULED.getDescription());
							update.set("front_speech",
									scheduled != null ? scheduled.getFront() : Status.SCHEDULED.getFrontSpeech());

							// update psiCode by schedule
							trazabilidadScheduleApi.updatePSICodeReal(provision.getIdProvision(),
									provision.getXaRequest(), getXaIdSt, appointment.getDescription().toLowerCase(),
									provision.getCustomer());

						}
					}

					update.set("log_status", listLog);

					// carrier titular
					boolean carrierTitular = getCarrier(provision.getCustomer().getPhoneNumber());
					provision.getCustomer().setCarrier(carrierTitular);
					update.set("customer.carrier", carrierTitular);

					// Add carrier phone contact
					List<Contacts> contacts = provision.getContacts();

					if (contacts != null) {

						for (Contacts list : contacts) {
							// boolean phoneCarrier = ;
							list.setCarrier(getCarrier(list.getPhoneNumber()));

						}
						update.set("contacts", contacts);
					}

					// send sms invitation
					provision.setContacts(contacts);
					update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of("-05:00")));
					provisionRepository.updateProvision(provision, update);

					return true;
				}
			}

			if (provisionStatus.equalsIgnoreCase(Status.WO_PRESTART.getStatusName())
					&& !provision.getXaIdSt().isEmpty()) {

				pe.telefonica.provision.model.Status preStartStatus = getInfoStatus(Status.WO_PRESTART.getStatusName(),
						statusList);

				Update update = new Update();
				update.set("external_id", appointment.getRelatedParty().get(1).getId());
				// update.set("xa_request", getData[2]);
				update.set("active_status", Constants.PROVISION_STATUS_SCHEDULE_IN_PROGRESS);

				WoPreStart woPreStart = provision.getWoPreStart() != null ? provision.getWoPreStart()
						: new WoPreStart();

				// String[] technicianInfo = getData[3].split("-");

				woPreStart.setNameResource(appointment.getRelatedParty().get(1).getName());
				woPreStart.setDate(appointment.getStatusChangeDate());
				woPreStart.setTechnicalId(appointment.getRelatedParty().get(1).getId());
				woPreStart.setFullName(appointment.getRelatedParty().get(1).getName());
				woPreStart.setDocumentNumber(appointment.getRelatedParty().get(1).getLegalId().get(0).getNationalId());
				woPreStart.setPhoneNumber(appointment.getRelatedParty().get(1).getContactMedium().get(0).getNumber());
				woPreStart.setLatitude(appointment.getRelatedPlace().getAddress().getCoordinates().getLatitude());
				woPreStart.setLongitude(appointment.getRelatedPlace().getAddress().getCoordinates().getLongitude());

				update.set("activity_type", appointment.getDescription().toLowerCase());
				update.set("xa_id_st", getXaIdSt);
				update.set("show_location", false);
				update.set("notifications.prestart_send_notify", false);

				StatusLog statusLog = new StatusLog();
				statusLog.setStatus(Status.WO_PRESTART.getStatusName());
				statusLog.setXaidst(provision.getXaIdSt());

				update.set("customer.latitude",
						appointment.getRelatedPlace().getAddress().getCoordinates().getLatitude());
				update.set("customer.longitude",
						appointment.getRelatedPlace().getAddress().getCoordinates().getLongitude());
				update.set("last_tracking_status", Status.WO_PRESTART.getStatusName());
				update.set("generic_speech", preStartStatus != null ? preStartStatus.getGenericSpeech()
						: Status.WO_PRESTART.getGenericSpeech());
				update.set("description_status",
						preStartStatus != null ? preStartStatus.getDescription() : Status.WO_PRESTART.getDescription());
				update.set("front_speech",
						preStartStatus != null ? preStartStatus.getFront() : Status.WO_PRESTART.getFrontSpeech());
				listLog.add(statusLog);
				update.set("log_status", listLog);

				// Job Woprestart
				// woPreStart.setAvailableTracking(false);
				LocalDateTime nowDate = LocalDateTime.now(ZoneOffset.of("-05:00"));
				if (nowDate.getHour() >= 07 && nowDate.getHour() <= 20) {
//				if (nowDate.getHour() >= 0 && nowDate.getHour() <= 23) {

					// SMS
					sendSMSWoPrestartHolder(provision);
					update.set("notifications.prestart_send_notify", true);
					update.set("notifications.prestart_send_date", LocalDateTime.now(ZoneOffset.of("-05:00")));

					if (Boolean.valueOf(System.getenv("TDP_SIMPLI_ENABLE"))) {

						String switchAzure = System.getenv("TDP_SWITCH_AZURE");
						String tokenExternal = "";
						if (switchAzure.equals("true")) {
							tokenExternal = trazabilidadSecurityApi.gerateTokenAzure();
						} else {
							tokenExternal = trazabilidadSecurityApi.generateToken();
						}
						// validate TechAvailable
						GetTechnicianAvailableRequest getTechnicianAvailableRequest = new GetTechnicianAvailableRequest();
						getTechnicianAvailableRequest.setDni(woPreStart.getDocumentNumber());

						String isAvailableTech = trazabilidadScheduleApi
								.getTechAvailable(getTechnicianAvailableRequest);
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
							boolean needSend = true;
							while (needSend) {
								String urlSimpli = "";

								if (switchAzure.equals("true")) {
									urlSimpli = simpliConnectApi.getUrlTraking(simpliRequest);
								} else {
									urlSimpli = simpliConnectApi.getUrlTrakingOld(simpliRequest);
								}

								if (urlSimpli != null) {
									// SEND SMS BY CONTACTS
									woPreStart.setTrackingUrl(urlSimpli);
									provision.setWoPreStart(woPreStart);
									sendSMSWoPrestartContact(provision);

									woPreStart.setAvailableTracking(true);
									needSend = false;
								} else {
									if (++count == maxTries) {
										break;
									}
								}
							}

							needSend = false;
						}
					}
				}

				update.set("wo_prestart", woPreStart);
				update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of("-05:00")));
				provisionRepository.updateProvision(provision, update);
				return true;

			}

			if (provisionStatus.equalsIgnoreCase(Status.WO_INIT.getStatusName()) && !provision.getXaIdSt().isEmpty()) {

				pe.telefonica.provision.model.Status initStatus = getInfoStatus(Status.WO_INIT.getStatusName(),
						statusList);

				Update update = new Update();
				WoInit woInit = new WoInit();

				woInit.setNameResource(appointment.getRelatedParty().get(1).getName());
				woInit.setEtaStartTime(appointment.getStartDate());
				woInit.setEtaEndTime(appointment.getEndDate());
				woInit.setXaCreationDate(appointment.getCreationDate());
				woInit.setDate(appointment.getStatusChangeDate());
				woInit.setXaNote(appointment.getNote().get(0).getText());

				update.set("wo_init", woInit);
				update.set("show_location", false);
				update.set("xa_id_st", getXaIdSt);
				update.set("xa_requirement_number", getXaRequirementNumber);
				update.set("appt_number", appointment.getId());
				update.set("activity_type", appointment.getDescription().toLowerCase());
				update.set("active_status", Constants.PROVISION_STATUS_WOINIT);

				// update.set("xa_request", getData[5]);
				StatusLog statusLog = new StatusLog();
				statusLog.setStatus(Status.WO_INIT.getStatusName());
				statusLog.setXaidst(provision.getXaIdSt());

				update.set("last_tracking_status", Status.WO_INIT.getStatusName());
				update.set("generic_speech",
						initStatus != null ? initStatus.getGenericSpeech() : Status.WO_INIT.getGenericSpeech());
				update.set("description_status",
						initStatus != null ? initStatus.getDescription() : Status.WO_INIT.getDescription());
				update.set("front_speech",
						initStatus != null ? initStatus.getFront() : Status.WO_INIT.getFrontSpeech());
				listLog.add(statusLog);
				update.set("log_status", listLog);
				update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of("-05:00")));
				provisionRepository.updateProvision(provision, update);
				return true;

			}

			if (provisionStatus.equalsIgnoreCase(Status.WO_COMPLETED.getStatusName())
					&& !provision.getXaIdSt().isEmpty()) {

				pe.telefonica.provision.model.Status completedStatus = getInfoStatus(
						Status.WO_COMPLETED.getStatusName(), statusList);

				Update update = new Update();
				WoCompleted woCompleted = new WoCompleted();

				woCompleted.setXaCreationDate(appointment.getCreationDate());
				woCompleted.setDate(appointment.getStatusChangeDate());
				woCompleted.setXaNote(appointment.getNote().get(0).getText());
				woCompleted.setEtaStartTime(appointment.getStartDate());
				woCompleted.setEtaEndTime(appointment.getEndDate());

				woCompleted.setObservation(appointment.getStatusChangeDate());

				update.set("wo_completed", woCompleted);

				update.set("active_status", Constants.PROVISION_STATUS_COMPLETED);

				update.set("show_location", false);
				update.set("xa_id_st", getXaIdSt);
				update.set("xa_requirement_number", getXaRequirementNumber);
				update.set("appt_number", appointment.getId());
				update.set("activity_type", appointment.getDescription().toLowerCase());
				update.set("notifications.completed_send_notify", false);

				StatusLog statusLog = new StatusLog();
				statusLog.setStatus(Status.WO_COMPLETED.getStatusName());
				statusLog.setXaidst(provision.getXaIdSt());

				update.set("last_tracking_status", Status.WO_COMPLETED.getStatusName());
				update.set("generic_speech", completedStatus != null ? completedStatus.getGenericSpeech()
						: Status.WO_COMPLETED.getGenericSpeech());
				update.set("description_status", completedStatus != null ? completedStatus.getDescription()
						: Status.WO_COMPLETED.getDescription());
				update.set("front_speech",
						completedStatus != null ? completedStatus.getFront() : Status.WO_COMPLETED.getFrontSpeech());
				listLog.add(statusLog);
				update.set("log_status", listLog);
				update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of("-05:00")));

				provisionRepository.updateProvision(provision, update);
				return true;
			}

			if (provisionStatus.equalsIgnoreCase(Status.WO_CANCEL.getStatusName())) {
				// && "0".equals(getData[16].toString())) {
				String xaIdSt = "";

				// se cancela por que se regulariza la ficticia en una real
				if (appointment.getStatusReason().toString().equals("2")) {
					return false;
				}

				pe.telefonica.provision.model.Status cancelStatus = getInfoStatus(Status.WO_CANCEL.getStatusName(),
						statusList);

				if (provision.getXaIdSt() != null && !provision.getXaIdSt().isEmpty()) {
					xaIdSt = provision.getXaIdSt();
				} else {
					if (provision.getDummyStPsiCode() != null && !provision.getDummyStPsiCode().isEmpty()) {
						xaIdSt = provision.getDummyStPsiCode();
					} else {
						return false;
					}
				}

				Update update = new Update();

				WoCancel woCancel = new WoCancel();
				woCancel.setUserCancel(appointment.getRelatedParty().get(2).getId());
				woCancel.setXaCancelReason(appointment.getStatusReason());
				update.set("wo_cancel", woCancel);
				update.set("active_status", Constants.PROVISION_STATUS_CANCELLED);

				StatusLog statusLog = new StatusLog();
				statusLog.setStatus(Status.WO_CANCEL.getStatusName());
				statusLog.setXaidst(xaIdSt);

				update.set("send_notify", false);
				update.set("xa_cancel_reason", appointment.getStatusReason());
				update.set("user_cancel", appointment.getRelatedParty().get(2).getId());
				update.set("last_tracking_status", Status.WO_CANCEL.getStatusName());
				update.set("generic_speech",
						cancelStatus != null ? cancelStatus.getGenericSpeech() : Status.WO_CANCEL.getGenericSpeech());
				update.set("description_status",
						cancelStatus != null ? cancelStatus.getDescription() : Status.WO_CANCEL.getDescription());
				update.set("front_speech",
						cancelStatus != null ? cancelStatus.getFront() : Status.WO_CANCEL.getFrontSpeech());
				update.set("xa_id_st", getXaIdSt);
				update.set("xa_requirement_number", getXaRequirementNumber);
				update.set("appt_number", appointment.getId());

				update.set("show_location", false);

				listLog.add(statusLog);
				update.set("log_status", listLog);
				update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of("-05:00")));

				// Actualiza estado en provision
				provisionRepository.updateProvision(provision, update);

				ScheduleNotDoneRequest scheduleNotDoneRequest = new ScheduleNotDoneRequest();
				scheduleNotDoneRequest.setRequestId(provision.getIdProvision());
				scheduleNotDoneRequest.setRequestType(provision.getActivityType());
				scheduleNotDoneRequest.setStPsiCode(xaIdSt);

				if (getXaIdSt.equals(getXaRequirementNumber.toString())
						&& getXaRequirementNumber.toString().equals(appointment.getId().toString())) {
					scheduleNotDoneRequest.setFlgFicticious(true);
					scheduleNotDoneRequest.setRequestType(Constants.ACTIVITY_TYPE_PROVISION.toLowerCase());
				} else {
					scheduleNotDoneRequest.setFlgFicticious(false);
				}

				// Cancela agenda
				trazabilidadScheduleApi.cancelLocalSchedule(scheduleNotDoneRequest);

				return true;
			}

			if (provisionStatus.equalsIgnoreCase(Status.WO_RESCHEDULE.getStatusName())
					&& !provision.getXaIdSt().isEmpty()) {
				pe.telefonica.provision.model.Status rescheduleStatus = getInfoStatus(Status.SCHEDULED.getStatusName(),
						statusList);

				String identificadorSt = getXaIdSt.toString();

				Update update = new Update();
				WoReshedule woReshedule = new WoReshedule();
				String range = "AM";

				if (appointment.getTimeSlot().trim().equals("09-13")
						|| appointment.getTimeSlot().toString().trim().equals("9-13")) {
					range = "AM";
				} else {
					range = "PM";
				}
				String rangeFinal = range;

				String dateString = appointment.getScheduledDate().substring(0, 10);

				if ((identificadorSt == null || identificadorSt.isEmpty())
						&& (rangeFinal == null || rangeFinal.isEmpty())
						&& (dateString == null || dateString.isEmpty())) {
					return false;
				}

				List<StatusLog> listLogx = listLog.stream()
						.filter(x -> "SCHEDULED".equals(x.getStatus()) && identificadorSt.equals(x.getXaidst()))
						.collect(Collectors.toList());

				if (listLogx.size() > 0) {
					if (listLogx.get(listLogx.size() - 1).getScheduledDate().contentEquals(dateString.toString())
							&& listLogx.get(listLogx.size() - 1).getScheduledRange().contentEquals(rangeFinal)) {
						return true;
					}

				}

				woReshedule.setXaAppointmentScheduler(appointment.getScheduler());
				woReshedule.setTimeSlot(range);
				update.set("wo_schedule", woReshedule);
				update.set("active_status", Constants.PROVISION_STATUS_ACTIVE);

				update.set("xa_id_st", getXaIdSt);
				update.set("xa_requirement_number", getXaRequirementNumber);
				update.set("appt_number", appointment.getId());
				update.set("activity_type", appointment.getDescription().toLowerCase());

				StatusLog statusLog = new StatusLog();
				statusLog.setStatus(Status.SCHEDULED.getStatusName());
				statusLog.setScheduledRange(rangeFinal);
				statusLog.setScheduledDate(dateString.toString());
				statusLog.setXaidst(provision.getXaIdSt());

				update.set("date", appointment.getScheduledDate());
				update.set("send_notify", false);
				update.set("time_slot", range);
				update.set("last_tracking_status", Status.SCHEDULED.getStatusName());
				update.set("generic_speech", rescheduleStatus != null ? rescheduleStatus.getGenericSpeech()
						: Status.SCHEDULED.getGenericSpeech());
				update.set("description_status", rescheduleStatus != null ? rescheduleStatus.getDescription()
						: Status.SCHEDULED.getDescription());
				update.set("front_speech",
						rescheduleStatus != null ? rescheduleStatus.getFront() : Status.SCHEDULED.getFrontSpeech());
				listLog.add(statusLog);
				update.set("log_status", listLog);

				update.set("show_location", false);
				update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of("-05:00")));

				// Actualizar provision
				provisionRepository.updateProvision(provision, update);

				// el que parsea
				SimpleDateFormat parseador2 = new SimpleDateFormat("yyyy-MM-dd");
				// el que formatea
				SimpleDateFormat formateador2 = new SimpleDateFormat("dd/MM/yyyy");

				Date date2 = parseador2.parse(appointment.getScheduledDate());// ("31-03-2016");
				String dateString2 = formateador2.format(date2);

				Customer customer = new Customer();
				customer.setDocumentNumber(provision.getCustomer().getDocumentNumber());
				customer.setDocumentType(provision.getCustomer().getDocumentType());
				ScheduleRequest scheduleRequest = new ScheduleRequest();
				scheduleRequest.setBucket(provision.getWorkZone());
				scheduleRequest.setPilot(false);
				// scheduleRequest.setOrderCode(provision.getXaRequest());
				scheduleRequest.setXaOrderCode(provision.getXaRequest());
				scheduleRequest.setRequestId(provision.getIdProvision());
				scheduleRequest.setRequestType(provision.getActivityType());
				scheduleRequest.setSelectedDate(dateString2);
				scheduleRequest.setSelectedRange(range);
				scheduleRequest.setStpsiCode(getXaIdSt);
				scheduleRequest.setCustomer(new CustomerRequest().fromCustomer(customer));

				scheduleRequest.setDocumentNumber(provision.getCustomer().getDocumentNumber());
				scheduleRequest.setDocumentType(provision.getCustomer().getDocumentType());
				scheduleRequest.setOrderCode(provision.getXaRequest());
				scheduleRequest.setBucket(provision.getWorkZone());

				// Actualiza el agendamiento
				trazabilidadScheduleApi.updateSchedule(scheduleRequest);

				return true;
			}

			if (provisionStatus.equalsIgnoreCase(Status.WO_NOTDONE.getStatusName())
					&& !provision.getXaIdSt().isEmpty()) {
				pe.telefonica.provision.model.Status notDoneStatus = getInfoStatus(Status.WO_NOTDONE.getStatusName(),
						statusList);

				Update update = new Update();
				WoNotdone woNotdone = new WoNotdone();

				woNotdone.setaNotDoneTypeInstall(appointment.getAdditionalData().get(2).getValue());
				woNotdone.setaNotDoneReasonInstall(appointment.getStatusReason());
				woNotdone.setaNotDoneSubReasonInstall(appointment.getStatusReason());
				woNotdone.setaObservation(appointment.getNote().get(0).getText());
				woNotdone.setUserNotdone(appointment.getRelatedParty().get(4).getId());

				update.set("wo_notdone", woNotdone);
				update.set("xa_id_st", getXaIdSt);
				update.set("xa_requirement_number", getXaRequirementNumber);
				update.set("appt_number", appointment.getId());
				update.set("activity_type", appointment.getDescription().toLowerCase());

				update.set("active_status", Constants.PROVISION_STATUS_NOTDONE);

				StatusLog statusLog = new StatusLog();
				statusLog.setStatus(Status.WO_NOTDONE.getStatusName());
				statusLog.setXaidst(provision.getXaIdSt());
				listLog.add(statusLog);

				speech = notDoneStatus != null ? notDoneStatus.getGenericSpeech()
						: Status.WO_NOTDONE.getGenericSpeech();
				speech = hasCustomerInfo(provision.getCustomer())
						? speech.replace(Constants.TEXT_NAME_REPLACE, provision.getCustomer().getName().split(" ")[0])
						: speech;

				update.set("a_observation", appointment.getNote().get(0).getText());
				update.set("user_notdone", appointment.getRelatedParty().get(4).getId());
				update.set("last_tracking_status", Status.WO_NOTDONE.getStatusName());
				update.set("generic_speech", speech);
				update.set("description_status",
						notDoneStatus != null ? notDoneStatus.getDescription() : Status.WO_NOTDONE.getDescription());
				update.set("front_speech",
						notDoneStatus != null ? notDoneStatus.getFront() : Status.WO_NOTDONE.getFrontSpeech());
				update.set("log_status", listLog);
				update.set("show_location", false);
				update.set("send_notify", false);

				String subReason;
				String nameReplace = (provision.getCustomer().getName() != null
						&& !provision.getCustomer().getName().isEmpty())
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
						subReason = Constants.DEFAULT_NOTDONE_SUBREASON.replace(Constants.TEXT_NAME_REPLACE,
								nameReplace);
						update.set("sub_reason_not_done", subReason);
						update.set("action_not_done", Constants.DEFAULT_NOTDONE_ACTION);
					}
				} else {
					subReason = Constants.DEFAULT_NOTDONE_SUBREASON.replace(Constants.TEXT_NAME_REPLACE, nameReplace);
					update.set("sub_reason_not_done", subReason);
					update.set("action_not_done", Constants.DEFAULT_NOTDONE_ACTION);
				}
				update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of("-05:00")));

				// Actualiza provision
				provisionRepository.updateProvision(provision, update);
				ScheduleNotDoneRequest scheduleNotDoneRequest = new ScheduleNotDoneRequest();
				// Solo cancelar agenda sin ir a PSI
				scheduleNotDoneRequest.setRequestId(provision.getIdProvision());
				scheduleNotDoneRequest.setRequestType(provision.getActivityType());
				scheduleNotDoneRequest.setStPsiCode(provision.getXaIdSt());
				scheduleNotDoneRequest.setFlgFicticious(false);

				// Cancela agenda sin ir a PSI
				trazabilidadScheduleApi.cancelLocalSchedule(scheduleNotDoneRequest);

				return true;
			}
		}
		return false;
	}

	private boolean validateBucketProduct(Appointment appointment, Provision provision, String status)
			throws Exception {
		boolean errorBucket = false; // validar IN_TOA
		// Valida DNI
		if (Constants.TIPO_RUC.equals(provision.getCustomer().getDocumentType().toLowerCase())
				&& !provision.getCustomer().getDocumentNumber().startsWith(Constants.RUC_NATURAL)) {
			errorBucket = true;

			return errorBucket;
		}
		if (Constants.STATUS_IN_TOA.equalsIgnoreCase(status == null ? "" : status)) {

			errorBucket = !getBucketByProduct(provision.getOriginCode(), provision.getCommercialOp(),
					appointment.getAdditionalData().get(1).getValue());
		}

		return errorBucket;
	}

	private boolean isAValidSchedule(String scheduleDate) {
		LocalDate lScheduleDate = LocalDate.parse(scheduleDate);
		LocalDate today = LocalDate.now(ZoneOffset.of("-05:00"));

		return lScheduleDate.compareTo(today) > 0;
	}

	private boolean getBucketByProduct(String channel, String product, String bucket) throws Exception {
		try {
			boolean responseBucket = restPSI.getBucketByProduct(bucket, product, channel);

			return responseBucket;

		} catch (Exception e) {
			log.error(this.getClass().getName() + " - Exception: " + e.getMessage());

			throw e;
		}
	}

	@Override
	public boolean updateInToa(Provision provision, KafkaTOARequest kafkaToaRequest,
			pe.telefonica.provision.model.Status status) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateWoPrestart(Provision provision, KafkaTOARequest kafkaToaRequest,
			pe.telefonica.provision.model.Status status) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateWoInit(Provision provision, KafkaTOARequest kafkaToaRequest,
			pe.telefonica.provision.model.Status status) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateWoCompleted(Provision provision, KafkaTOARequest kafkaToaRequest,
			pe.telefonica.provision.model.Status status) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateWoCancel(Provision provision, KafkaTOARequest kafkaToaRequest,
			pe.telefonica.provision.model.Status status) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateWoReschdule(Provision provision, KafkaTOARequest kafkaToaRequest,
			pe.telefonica.provision.model.Status status) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateWoNotDone(Provision provision, KafkaTOARequest kafkaToaRequest,
			pe.telefonica.provision.model.Status status) {
		// TODO Auto-generated method stub
		return false;
	}

}