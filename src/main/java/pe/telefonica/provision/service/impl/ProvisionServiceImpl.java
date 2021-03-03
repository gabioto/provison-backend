package pe.telefonica.provision.service.impl;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;

import pe.telefonica.provision.conf.ProvisionTexts;
import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.common.ApiResponse;
import pe.telefonica.provision.controller.common.ResponseHeader;
import pe.telefonica.provision.controller.request.ApiTrazaSetContactInfoUpdateRequest;
import pe.telefonica.provision.controller.request.CancelRequest;
import pe.telefonica.provision.controller.request.ContactRequest;
import pe.telefonica.provision.controller.request.GetProvisionByOrderCodeRequest;
import pe.telefonica.provision.controller.request.InsertCodeFictionalRequest;
import pe.telefonica.provision.controller.request.InsertOrderRequest;
import pe.telefonica.provision.controller.request.KafkaTOARequest;
import pe.telefonica.provision.controller.request.MailRequest.MailParameter;
import pe.telefonica.provision.controller.request.ProvisionRequest;
import pe.telefonica.provision.controller.request.SMSByIdRequest.Contact;
import pe.telefonica.provision.controller.request.SMSByIdRequest.Message.MsgParameter;
import pe.telefonica.provision.controller.request.ScheduleNotDoneRequest;
import pe.telefonica.provision.controller.request.UpdateFromToaRequest;
import pe.telefonica.provision.controller.response.ProvisionHeaderResponse;
import pe.telefonica.provision.controller.response.ProvisionResponse;
import pe.telefonica.provision.dto.ComponentsDto;
import pe.telefonica.provision.dto.ProvisionDto;
import pe.telefonica.provision.dto.ProvisionTrazaDto;
import pe.telefonica.provision.external.BOApi;
import pe.telefonica.provision.external.PSIApi;
import pe.telefonica.provision.external.SimpliConnectApi;
import pe.telefonica.provision.external.TrazabilidadScheduleApi;
import pe.telefonica.provision.external.TrazabilidadSecurityApi;
import pe.telefonica.provision.external.request.ScheduleUpdateFicticiousRequest;
import pe.telefonica.provision.external.request.schedule.GetTechnicianAvailableRequest;
import pe.telefonica.provision.external.request.simpli.SimpliRequest;
import pe.telefonica.provision.model.Contacts;
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.model.HomePhone;
import pe.telefonica.provision.model.Internet;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.Provision.StatusLog;
import pe.telefonica.provision.model.Queue;
import pe.telefonica.provision.model.ReturnedProvision;
import pe.telefonica.provision.model.StatusAtis;
import pe.telefonica.provision.model.Television;
import pe.telefonica.provision.model.UpFront;
import pe.telefonica.provision.model.provision.Configurada;
import pe.telefonica.provision.model.provision.InToa;
import pe.telefonica.provision.model.provision.Notifications;
import pe.telefonica.provision.model.provision.PendienteDeAprobacion;
import pe.telefonica.provision.model.provision.PendienteDeValidacion;
import pe.telefonica.provision.model.provision.WoCancel;
import pe.telefonica.provision.model.provision.WoCompleted;
import pe.telefonica.provision.model.provision.WoInit;
import pe.telefonica.provision.model.provision.WoNotdone;
import pe.telefonica.provision.model.provision.WoPreStart;
import pe.telefonica.provision.repository.ProvisionRepository;
import pe.telefonica.provision.service.ProvisionService;
import pe.telefonica.provision.service.request.PSIUpdateClientRequest;
import pe.telefonica.provision.util.constants.Constants;
import pe.telefonica.provision.util.constants.ProductType;
import pe.telefonica.provision.util.constants.Status;

@Service("provisionService")
@Transactional
public class ProvisionServiceImpl implements ProvisionService {

	private static final Log log = LogFactory.getLog(ProvisionServiceImpl.class);
	private ProvisionRepository provisionRepository;

	@Autowired
	private ProvisionTexts provisionTexts;

	@Autowired
	private BOApi bOApi;

	@Autowired
	private PSIApi restPSI;

	@Autowired
	private SimpliConnectApi simpliConnectApi;

	@Autowired
	private TrazabilidadSecurityApi trazabilidadSecurityApi;

	@Autowired
	private TrazabilidadScheduleApi trazabilidadScheduleApi;

	@Autowired
	public ProvisionServiceImpl(ProvisionRepository provisionRepository) {
		this.provisionRepository = provisionRepository;
	}

	@Override
	public Customer validateUser(ApiRequest<ProvisionRequest> provisionRequest) {

		Optional<Provision> provision;

		provision = provisionRepository.getOrder(provisionRequest.getBody().getDocumentType(),
				provisionRequest.getBody().getDocumentNumber());

		if (provision.isPresent() && provision.get().getCustomer() != null) {

			Provision prov = provision.get();
			prov.getCustomer().setProductName(prov.getProductName());
			return prov.getCustomer();

		} else {
			return null;

		}

	}

	@Override
	public List<ProvisionDto> getAll(ApiRequest<ProvisionRequest> provisionRequest) {

		Optional<List<ProvisionDto>> provisions;
		List<ProvisionDto> listProvisionDto = new ArrayList<ProvisionDto>();

		provisions = provisionRepository.findAll(provisionRequest.getBody().getDocumentType(),
				provisionRequest.getBody().getDocumentNumber());

		if (provisions.isPresent() && provisions.get().size() > 0) {
			listProvisionDto = provisions.get();

			return listProvisionDto;
		} else {
			return null;
		}
	}

	@Override
	public List<ProvisionTrazaDto> getAllTraza(ApiRequest<ProvisionRequest> provisionRequest) {
		Optional<List<ProvisionTrazaDto>> provisions;
		List<ProvisionTrazaDto> listProvisionDto = new ArrayList<ProvisionTrazaDto>();

		provisions = provisionRepository.findAllTraza(provisionRequest.getBody().getDocumentType(),
				provisionRequest.getBody().getDocumentNumber());

		if (provisions.isPresent() && provisions.get().size() > 0) {
			listProvisionDto = provisions.get();

			return listProvisionDto;
		} else {
			return null;
		}
	}

	private Provision evaluateProvisionComponents(Provision provision) {
		ProductType producType = null;
		try {

			for (ProductType prod : ProductType.values()) {
				if (provision.getProductType().toUpperCase().equals(prod.getTypeName()))
					producType = prod;
			}

			if (producType != null) {
				if (producType.isTv()) {
					provision.getComponents().add(addTvComponent(provision.getTvDetail()));
				}

				if (producType.isInternet()) {
					provision.getComponents().add(addInternetComponent(provision.getInternetDetail()));
				}

				if (producType.isLine()) {
					provision.getComponents().add(addLineComponent(provision.getHomePhoneDetail()));
				}
			} else {
				if (provision.getTvDetail() != null) {
					evaluateTvFields(provision);
				}

				if (provision.getInternetDetail() != null) {
					evaluateInternetFields(provision);
				}

				if (provision.getHomePhoneDetail() != null) {
					evaluateLineFields(provision);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return provision;
	}

	private void evaluateTvFields(Provision provision) {
		Television television = provision.getTvDetail();
		ComponentsDto components = null;

		if ((television.getType() != null && !television.getType().isEmpty())
				|| (television.getTvSignal() != null && !television.getTvSignal().isEmpty())
				|| television.getTvBlocks() != null
				|| (television.getTechnology() != null && !television.getTechnology().isEmpty())
				|| television.getEquipmentsNumber() != null
				|| (television.getEquipment() != null && !television.getEquipment().isEmpty())
				|| (television.getDescription() != null && !television.getDescription().isEmpty())
				|| television.getAdditionalSmartHd() != null || television.getAdditionalHd() != null) {

			components = addTvComponent(television);
		}

		if (components != null) {
			provision.getComponents().add(components);
		}
	}

	private ComponentsDto addTvComponent(Television television) {
		ComponentsDto components = new ComponentsDto();
		components.setTitle(Constants.COMPONENTS_TITLE_TV);
		components.setName(Constants.COMPONENTS_NAME_TV);
		if (television != null) {
			components.setDescription((television.getDescription() != null && !television.getDescription().isEmpty())
					? television.getDescription()
					: Constants.COMPONENTS_DESC_TV);
		} else {
			components.setDescription(Constants.COMPONENTS_DESC_TV);
		}

		return components;
	}

	private void evaluateInternetFields(Provision provision) {
		Internet internet = provision.getInternetDetail();
		ComponentsDto components = null;

		if ((internet.getDescription() != null && !internet.getDescription().isEmpty())
				|| (internet.getEquipment() != null && !internet.getEquipment().isEmpty())
				|| (internet.getPromoSpeed() != null && !internet.getPromoSpeed().isEmpty())
				|| (internet.getSmartWifi() != null && !internet.getSmartWifi().isEmpty())
				|| (internet.getSpeed() != null && !internet.getSpeed().isEmpty())
				|| (internet.getSvaInternet() != null && !internet.getSvaInternet().isEmpty())
				|| (internet.getTechnology() != null && !internet.getTechnology().isEmpty())
				|| (internet.getTimePromoSpeed() != null && !internet.getTimePromoSpeed().isEmpty())) {

			components = addInternetComponent(internet);
		}

		if (components != null) {
			provision.getComponents().add(components);
		}
	}

	private ComponentsDto addInternetComponent(Internet internet) {
		ComponentsDto components = new ComponentsDto();
		components = new ComponentsDto();
		components.setTitle(Constants.COMPONENTS_TITLE_INTERNET);
		components.setName(Constants.COMPONENTS_NAME_INTERNET);

		if (internet != null) {
			components.setDescription((internet.getDescription() != null && !internet.getDescription().isEmpty())
					? internet.getDescription()
					: Constants.COMPONENTS_DESC_INTERNET);
		} else {
			components.setDescription(Constants.COMPONENTS_DESC_INTERNET);
		}

		return components;
	}

	private void evaluateLineFields(Provision provision) {
		HomePhone line = provision.getHomePhoneDetail();
		ComponentsDto components = null;

		if ((line.getDescription() != null && !line.getDescription().isEmpty()) || line.getEquipmenstNumber() != null
				|| (line.getEquipment() != null && !line.getEquipment().isEmpty())
				|| (line.getSvaLine() != null && !line.getSvaLine().isEmpty())
				|| (line.getType() != null && !line.getType().isEmpty())) {

			components = addLineComponent(line);
		}

		if (components != null) {
			provision.getComponents().add(components);
		}
	}

	private ComponentsDto addLineComponent(HomePhone line) {
		ComponentsDto components = new ComponentsDto();
		components.setTitle(Constants.COMPONENTS_TITLE_LINE);
		components.setName(Constants.COMPONENTS_NAME_LINE);

		if (line != null) {
			components.setDescription(
					(line.getDescription() != null && !line.getDescription().isEmpty()) ? line.getDescription()
							: Constants.COMPONENTS_DESC_LINE);
		} else {
			components.setDescription(Constants.COMPONENTS_DESC_LINE);
		}

		return components;
	}

	private Provision fillProvisionInsert(InsertOrderRequest request) {
		Optional<List<pe.telefonica.provision.model.Status>> statusListOptional = provisionRepository
				.getAllInfoStatus();
		List<pe.telefonica.provision.model.Status> statusList = statusListOptional.get();

		String[] getData = request.getData().split("\\|", -1);
		Provision provision = new Provision();
		String speech = "";

		provision.setSaleSource(getData[0]);
		provision.setBack(getData[1]);
		provision.setSaleCode(getData[2]);
		provision.setProductName(getData[10]);
		provision.setXaRequest(getData[11]);
		provision.setOriginCode(request.getDataOrigin());
		provision.setCommercialOp(getData[12].toUpperCase());
		provision.setProductCode(getData[14]);
		provision.setProductNameSource(getData[15]);
		provision.setKafkaDateSend(getData[17]);
		provision.setSaleRequestDate(getData[18]);
		provision.setSaleRegisterDate(getData[19]);
		provision.setProductSub(getData[21]);
		provision.setProductType(getData[22]);
		provision.setChannelEntered(getData[26]);
		provision.setProtectedData(getData[27]);
		provision.setRegularPrice(getData[29]);
		provision.setPromoPrice(getData[30]);
		provision.setCampaign(getData[31]);
		provision.setPaymentMethod(getData[34]);
		provision.setInstallPrice(getData[35]);
		provision.setInstallPriceMonth(getData[36]);
		provision.setProductInternalEquipment(getData[41]);
		provision.setCodePsCode(getData[28]);
		provision.setLegacies(getData[42]);
		provision.setProductSignal(getData[43]);

		Notifications notifications = new Notifications();
		notifications.setIntoaSendNotify(
				(Constants.TIPO_RUC.equalsIgnoreCase(getData[13]) && !getData[4].startsWith(Constants.RUC_NATURAL)));

		provision.setNotifications(notifications);

		List<String> productPsAdmin = new ArrayList<>();
		productPsAdmin.add(getData[44]);
		productPsAdmin.add(getData[45]);
		productPsAdmin.add(getData[46]);
		productPsAdmin.add(getData[47]);
		provision.setProductPsAdmin(productPsAdmin);

		List<String> svaCode = new ArrayList<>();
		svaCode.add(getData[48]);
		svaCode.add(getData[49]);
		svaCode.add(getData[50]);
		svaCode.add(getData[51]);
		svaCode.add(getData[52]);
		svaCode.add(getData[53]);
		svaCode.add(getData[54]);
		svaCode.add(getData[55]);
		svaCode.add(getData[56]);
		svaCode.add(getData[57]);
		provision.setSvaCode(svaCode);

		Customer customer = new Customer();
		customer.setName(getData[3]);
		customer.setDocumentType(getData[13].toUpperCase());
		customer.setDocumentNumber(getData[4]);
		customer.setPhoneNumber(getData[5]);
		customer.setMail(getData[20]);
		customer.setAddress(getData[6]);
		customer.setDistrict(getData[9]);
		customer.setProvince(getData[8]);
		customer.setDepartment(getData[7]);
		customer.setLongitude(getData[23]);
		customer.setLatitude(getData[24]);
		customer.setOriginData(request.getDataOrigin());
		provision.setCustomer(customer);

		Internet internet = new Internet();
		internet.setSpeed(getData[25]);
		internet.setTimePromoSpeed(getData[32]);
		internet.setPromoSpeed(getData[33]);
		internet.setTechnology(getData[37]);
		provision.setInternetDetail(internet);

		Television television = new Television();
		television.setTechnology(getData[38]);
		television.setTvSignal(getData[39]);
		television.setEquipment(getData[40]);
		provision.setTvDetail(television);

		UpFront upFront = new UpFront();
		String upFrontFields = getData[58];
		String[] upFrontArray = (upFrontFields != null && !upFrontFields.isEmpty()) ? upFrontFields.split(",") : null;

		if (upFrontArray != null) {
			upFront.setCip(upFrontArray[4]);
			upFront.setCurrency(upFrontArray[5]);
			upFront.setAmount(Double.valueOf(upFrontArray[6]));
			upFront.setCipUrl("");
			upFront.setStatus(upFrontArray[8]);

			try {
				LocalDateTime paymentDate;
				LocalDateTime expDate;
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
				expDate = LocalDateTime.parse(upFrontArray[7], formatter);
				upFront.setExpDate(expDate);

				if (upFrontArray.length >= 10) {
					paymentDate = LocalDateTime.parse(upFrontArray[9], formatter);
					upFront.setPaymentDate(paymentDate);
				}
			} catch (Exception e) {
				log.error(this.getClass().getName() + " - Exception: " + e.getMessage());
			}

			provision.setUpFront(upFront);
			provision.setIsUpFront(true);
		}

		Status pendingStatus = provision.getIsUpFront() ? Status.PENDIENTE_PAGO : Status.PENDIENTE;
		pe.telefonica.provision.model.Status pendiente = getInfoStatus(pendingStatus.getStatusName(), statusList);
		speech = pendiente != null ? pendiente.getSpeechWithoutSchedule() : pendingStatus.getSpeechWithoutSchedule();
		speech = hasCustomerInfo(customer)
				? speech.replace(Constants.TEXT_NAME_REPLACE, customer.getName().split(" ")[0])
				: speech;

		provision.setLastTrackingStatus(pendingStatus.getStatusName());
		provision.setActiveStatus(pendingStatus.getStatusName().toLowerCase());
		provision.setStatusToa(pendingStatus.getStatusName().toLowerCase());
		provision.setGenericSpeech(speech);
		provision.setDescriptionStatus(pendiente != null ? pendiente.getDescription() : pendingStatus.getDescription());
		provision.setFrontSpeech(pendiente != null ? pendiente.getFront() : pendingStatus.getFrontSpeech());

		List<StatusLog> listLog = new ArrayList<>();
		StatusLog statusLog = new StatusLog();
		statusLog.setStatus(pendingStatus.getStatusName());
		listLog.add(statusLog);

		if (!request.getStatus().equalsIgnoreCase(Status.PENDIENTE.getStatusName())
				&& !request.getStatus().equalsIgnoreCase(Status.PENDIENTE_PAGO.getStatusName())) {
			StatusLog statusLogCurrent = new StatusLog();
			statusLogCurrent.setStatus(request.getStatus());

			if (request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName())) {
				pe.telefonica.provision.model.Status ingresado = getInfoStatus(Status.INGRESADO.getStatusName(),
						statusList);
				speech = ingresado != null ? ingresado.getSpeechWithoutSchedule()
						: Status.INGRESADO.getSpeechWithoutSchedule();
				speech = hasCustomerInfo(customer)
						? speech.replace(Constants.TEXT_NAME_REPLACE, customer.getName().split(" ")[0])
						: speech;
				provision.setDescriptionStatus(
						ingresado != null ? ingresado.getDescription() : Status.INGRESADO.getDescription());
				provision.setGenericSpeech(speech);
				provision.setFrontSpeech(ingresado != null ? ingresado.getFront() : Status.INGRESADO.getFrontSpeech());
				provision.setActiveStatus(Status.INGRESADO.getStatusName().toLowerCase());
				provision.setStatusToa(Status.INGRESADO.getStatusName().toLowerCase());
			} else if (request.getStatus().equalsIgnoreCase(Status.CAIDA.getStatusName())) {
				pe.telefonica.provision.model.Status caida = getInfoStatus(Status.CAIDA.getStatusName(), statusList);
				provision.setDescriptionStatus(caida != null ? caida.getDescription() : Status.CAIDA.getDescription());
				provision.setGenericSpeech(caida != null ? caida.getGenericSpeech() : Status.CAIDA.getGenericSpeech());
				provision.setFrontSpeech(caida != null ? caida.getFront() : Status.CAIDA.getFrontSpeech());
				provision.setActiveStatus(Constants.PROVISION_STATUS_CAIDA);
				provision.setStatusToa(Constants.PROVISION_STATUS_CAIDA);
			} else if (request.getStatus().equalsIgnoreCase(Status.PAGADO.getStatusName())) {
				pe.telefonica.provision.model.Status paid = getInfoStatus(Status.PAGADO.getStatusName(), statusList);
				provision.setDescriptionStatus(paid != null ? paid.getDescription() : Status.PAGADO.getDescription());
				provision.setGenericSpeech(paid != null ? paid.getGenericSpeech() : Status.PAGADO.getGenericSpeech());
				provision.setFrontSpeech(paid != null ? paid.getFront() : Status.PAGADO.getFrontSpeech());
				provision.setActiveStatus(Status.PAGADO.getStatusName().toLowerCase());
				provision.setStatusToa(Status.PAGADO.getStatusName().toLowerCase());
				provision.setSendNotify(false);
			}

			listLog.add(statusLogCurrent);

			provision.setRegisterDateUpdate(LocalDateTime.now(ZoneOffset.of("-05:00")));
			provision.setLastTrackingStatus(request.getStatus());
		}

		provision.setLogStatus(listLog);

		return provision;
	}

	private boolean hasCustomerInfo(Customer customer) {
		return customer != null && customer.getName() != null && !customer.getName().isEmpty();
	}

	private Update fillProvisionUpdate(InsertOrderRequest request) {
		String getData[] = request.getData().split("\\|", -1);

		Update update = new Update();

		update.set("register_date_update", LocalDateTime.now(ZoneOffset.of("-05:00")));
		update.set("sale_source", getData[0]);
		update.set("back", getData[1]);
		update.set("product_name", getData[10]);
		update.set("xa_request", getData[11]);
		update.set("commercial_op", getData[12].toUpperCase());
		update.set("product_code", getData[14]);
		update.set("product_name_source", getData[15]);
		update.set("kafka_date_send", getData[17]);
		update.set("sale_request_date", getData[18]);
		update.set("sale_register_date", getData[19]);
		update.set("product_sub", getData[21]);
		update.set("product_type", getData[22]);
		update.set("channel_entered", getData[26]);
		update.set("protected_data", getData[27]);
		update.set("regular_price", getData[29]);
		update.set("promo_price", getData[30]);
		update.set("campaign", getData[31]);
		update.set("payment_method", getData[34]);
		update.set("install_price", getData[35]);
		update.set("install_price_month", getData[36]);
		update.set("product_internal_equipment", getData[41]);
		update.set("legacies", getData[42]);
		update.set("product_signal", getData[43]);

		List<String> productPsAdmin = new ArrayList<>();
		productPsAdmin.add(getData[44]);
		productPsAdmin.add(getData[45]);
		productPsAdmin.add(getData[46]);
		productPsAdmin.add(getData[47]);

		update.set("product_ps_admin", productPsAdmin);

		List<String> svaCode = new ArrayList<>();
		svaCode.add(getData[48]);
		svaCode.add(getData[49]);
		svaCode.add(getData[50]);
		svaCode.add(getData[51]);
		svaCode.add(getData[52]);
		svaCode.add(getData[53]);
		svaCode.add(getData[54]);
		svaCode.add(getData[55]);
		svaCode.add(getData[56]);
		svaCode.add(getData[57]);
		update.set("sva_code", svaCode);

		update.set("customer.name", getData[3]);
		update.set("customer.document_type", getData[13]);
		update.set("customer.document_number", getData[4]);
		boolean isUpdate = true;
		if (getData[16].toString().equalsIgnoreCase(Status.CAIDA.getStatusName())
				|| getData[16].toString().equalsIgnoreCase(Status.PAGADO.getStatusName())) {
			isUpdate = false;
		}
		if (isUpdate) {

			update.set("customer.phone_number", getData[5]);

		}

		update.set("customer.mail", getData[20]);
		update.set("customer.carrier", false);
		update.set("customer.address", getData[6]);
		update.set("customer.district", getData[9]);
		update.set("customer.province", getData[8]);
		update.set("customer.department", getData[7]);
		update.set("customer.longitude", getData[23]);
		update.set("customer.latitude", getData[24]);
		update.set("internet_detail.speed", getData[25]);
		update.set("internet_detail.time_promo_speed", getData[32]);
		update.set("internet_detail.promo_speed", getData[33]);
		update.set("internet_detail.technology", getData[37]);
		update.set("television_detail.technology", getData[38]);
		update.set("television_detail.tv_signal", getData[39]);
		update.set("television_detail.equipment", getData[40]);

		UpFront upFront = new UpFront();
		String upFrontFields = getData[58];
		String[] upFrontArray = (upFrontFields != null && !upFrontFields.isEmpty()) ? upFrontFields.split(",") : null;

		if (upFrontArray != null) {
			upFront.setCip(upFrontArray[4]);
			upFront.setCurrency(upFrontArray[5]);
			upFront.setAmount(Double.valueOf(upFrontArray[6]));
			upFront.setCipUrl("");
			upFront.setStatus(upFrontArray[8]);

			try {
				LocalDateTime paymentDate;
				LocalDateTime expDate;
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
				expDate = LocalDateTime.parse(upFrontArray[7], formatter);
				upFront.setExpDate(expDate);

				if (upFrontArray.length >= 10) {
					paymentDate = LocalDateTime.parse(upFrontArray[9], formatter);
					upFront.setPaymentDate(paymentDate);
				}
			} catch (Exception e) {
				log.error(this.getClass().getName() + " - Exception: " + e.getMessage());
			}

			update.set("up_front", upFront);
			update.set("is_up_front", true);
		}

		return update;
	}

	@Override
	public boolean insertProvision(InsertOrderRequest request) {

		String speech = "";
		String getData[];
		Provision provisionx = null;
		// InsertOrderRequest request = formatProvision(message);

//		if (request == null) {
//			return false;
//		}

		if (request.getDataOrigin().equalsIgnoreCase("ORDENES")) {
			return false;
		}
		getData = request.getData().split("\\|");
		provisionx = request.getDataOrigin().equalsIgnoreCase("ATIS")
				? provisionRepository.getProvisionByXaRequest(getData[1])
				: provisionRepository.getProvisionBySaleCode(getData[2]);

		Optional<List<pe.telefonica.provision.model.Status>> statusListOptional = provisionRepository
				.getAllInfoStatus();
		List<pe.telefonica.provision.model.Status> statusList = statusListOptional.get();

		if (provisionx != null) {

			List<StatusLog> listLog = provisionx.getLogStatus();

			if (request.getStatus().equalsIgnoreCase(Status.PENDIENTE.getStatusName())
					|| request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName())
					|| request.getStatus().equalsIgnoreCase(Status.CAIDA.getStatusName())
					|| request.getStatus().equalsIgnoreCase(Status.PENDIENTE_PAGO.getStatusName())
					|| request.getStatus().equalsIgnoreCase(Status.PAGADO.getStatusName())) {
				List<StatusLog> listIngresado = listLog.stream()
						.filter(items -> Status.INGRESADO.getStatusName().equals(items.getStatus()))
						.collect(Collectors.toList());
				List<StatusLog> listCaida = listLog.stream()
						.filter(items -> Status.CAIDA.getStatusName().equals(items.getStatus()))
						.collect(Collectors.toList());

				if (listIngresado.size() > 0) {
					return false;
				}

				if (listCaida.size() > 0) {
					return false;
				}

				Update update = fillProvisionUpdate(request);

				boolean hasFictitious = validateFictitiousSchedule(listLog);

				StatusLog statusLog = new StatusLog();
				statusLog.setStatus(request.getStatus());

				String status = "";

				if (request.getStatus().equalsIgnoreCase(Status.PENDIENTE.getStatusName())) {
					pe.telefonica.provision.model.Status pendiente = getInfoStatus(Status.PENDIENTE.getStatusName(),
							statusList);
					if (pendiente != null) {
						speech = hasFictitious ? pendiente.getGenericSpeech() : pendiente.getSpeechWithoutSchedule();
					} else {
						speech = hasFictitious ? Status.PENDIENTE.getGenericSpeech()
								: Status.PENDIENTE.getSpeechWithoutSchedule();
					}

					speech = hasCustomerInfo(provisionx.getCustomer()) ? speech.replace(Constants.TEXT_NAME_REPLACE,
							provisionx.getCustomer().getName().split(" ")[0]) : speech;
					provisionx.setGenericSpeech(speech);
					provisionx.setDescriptionStatus(
							pendiente != null ? pendiente.getDescription() : Status.PENDIENTE.getDescription());
					provisionx.setFrontSpeech(
							pendiente != null ? pendiente.getFront() : Status.PENDIENTE.getFrontSpeech());
					status = Status.PENDIENTE.getStatusName().toLowerCase();
				} else if (request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName())) {
					pe.telefonica.provision.model.Status ingresado = getInfoStatus(Status.INGRESADO.getStatusName(),
							statusList);
					if (ingresado != null) {
						speech = hasFictitious ? ingresado.getGenericSpeech() : ingresado.getSpeechWithoutSchedule();
					} else {
						speech = hasFictitious ? Status.INGRESADO.getGenericSpeech()
								: Status.INGRESADO.getSpeechWithoutSchedule();
					}
					speech = hasCustomerInfo(provisionx.getCustomer()) ? speech.replace(Constants.TEXT_NAME_REPLACE,
							provisionx.getCustomer().getName().split(" ")[0]) : speech;
					provisionx.setGenericSpeech(speech);
					provisionx.setDescriptionStatus(
							ingresado != null ? ingresado.getDescription() : Status.INGRESADO.getDescription());
					provisionx.setFrontSpeech(
							ingresado != null ? ingresado.getFront() : Status.INGRESADO.getFrontSpeech());
					status = Status.INGRESADO.getStatusName().toLowerCase();
				} else if (request.getStatus().equalsIgnoreCase(Status.CAIDA.getStatusName())) {
					pe.telefonica.provision.model.Status caida = getInfoStatus(Status.CAIDA.getStatusName(),
							statusList);
					provisionx.setDescriptionStatus(
							caida != null ? caida.getDescription() : Status.CAIDA.getDescription());
					provisionx.setGenericSpeech(
							caida != null ? caida.getGenericSpeech() : Status.CAIDA.getGenericSpeech());
					provisionx.setFrontSpeech(caida != null ? caida.getFront() : Status.CAIDA.getFrontSpeech());
					status = Constants.PROVISION_STATUS_CAIDA;
				} else if (request.getStatus().equalsIgnoreCase(Status.PENDIENTE_PAGO.getStatusName())) {
					pe.telefonica.provision.model.Status pendingPayment = getInfoStatus(
							Status.PENDIENTE_PAGO.getStatusName(), statusList);
					provisionx.setDescriptionStatus(pendingPayment != null ? pendingPayment.getDescription()
							: Status.PENDIENTE_PAGO.getDescription());
					provisionx.setGenericSpeech(pendingPayment != null ? pendingPayment.getGenericSpeech()
							: Status.PENDIENTE_PAGO.getGenericSpeech());
					provisionx.setFrontSpeech(pendingPayment != null ? pendingPayment.getFront()
							: Status.PENDIENTE_PAGO.getFrontSpeech());
					status = Status.PENDIENTE_PAGO.getStatusName().toLowerCase();
				} else if (request.getStatus().equalsIgnoreCase(Status.PAGADO.getStatusName())) {
					pe.telefonica.provision.model.Status paid = getInfoStatus(Status.PAGADO.getStatusName(),
							statusList);
					provisionx.setDescriptionStatus(
							paid != null ? paid.getDescription() : Status.PAGADO.getDescription());
					provisionx.setGenericSpeech(
							paid != null ? paid.getGenericSpeech() : Status.PAGADO.getGenericSpeech());
					provisionx.setFrontSpeech(paid != null ? paid.getFront() : Status.PAGADO.getFrontSpeech());
					status = Status.PAGADO.getStatusName().toLowerCase();
					update.set("send_notify", false);
				}

				if (provisionx.getDummyStPsiCode() != null && provisionx.getIsUpdatedummyStPsiCode() != true) {

					if (request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName())
							&& !provisionx.getDummyStPsiCode().isEmpty()) {

						ScheduleUpdateFicticiousRequest updateFicRequest = new ScheduleUpdateFicticiousRequest();
						updateFicRequest.setOrderCode(getData[11]);
						updateFicRequest.setOriginCode(provisionx.getOriginCode());
						updateFicRequest.setSaleCode(provisionx.getSaleCode());
						updateFicRequest.setFictitiousCode(provisionx.getDummyXaRequest());
						updateFicRequest.setRequestName(getData[10]);
						updateFicRequest.setRequestId(provisionx.getIdProvision());

						// Actualiza agenda
						if (!provisionx.getLastTrackingStatus().equals(Status.WO_CANCEL.getStatusName())) {
							boolean updateFicticious = trazabilidadScheduleApi.updateFicticious(updateFicRequest);
							update.set("is_update_dummy_st_psi_code", updateFicticious ? true : false);
						}
					}
				}

				if (status.equalsIgnoreCase(Constants.PROVISION_STATUS_CAIDA)
						&& provisionx.getDummyStPsiCode() != null) {

					trazabilidadScheduleApi.updateCancelSchedule(new CancelRequest(provisionx.getIdProvision(),
							Constants.ACTIVITY_TYPE_PROVISION.toLowerCase(), provisionx.getDummyStPsiCode(), true));
				}

				update.set("active_status", status);
				update.set("status_toa", status);
				update.set("send_notify",
						(Constants.TIPO_RUC.equals(provisionx.getCustomer().getDocumentType().toLowerCase())
								&& !provisionx.getCustomer().getDocumentNumber().startsWith(Constants.RUC_NATURAL)));
				update.set("show_location", false);
				update.set("last_tracking_status", request.getStatus());
				update.set("description_status", provisionx.getDescriptionStatus());
				update.set("generic_speech", provisionx.getGenericSpeech());
				update.set("front_speech", provisionx.getFrontSpeech());
				update.set("front_speech", provisionx.getFrontSpeech());

				listLog.add(statusLog);
				update.set("log_status", listLog);
				update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of("-05:00")));
				provisionx = evaluateProvisionComponents(provisionx);

				Boolean isUpdate = provisionRepository.updateProvision(provisionx, update);
				return isUpdate ? true : false;

			} else {

				if (request.getStatus().equalsIgnoreCase(Status.PETICION_PENDIENTE.getStatusName())) {
					return false;
				}
				Update update = new Update();

				update.set("commercial_op_atis", getData[11]);
				update.set("cod_cliente_atis", getData[31]);
				update.set("cod_cuenta_atis", getData[32]);
				update.set("technology", getData[29]);
				update.set("cancelado_motivo_atis", getData[26]);
				update.set("cancelado_submotivo_atis", getData[27]);

				StatusLog statusLog = new StatusLog();

				if (request.getStatus().equalsIgnoreCase(Status.FINALIZADO.getStatusName())) {
					pe.telefonica.provision.model.Status finalizado = getInfoStatus(Status.FINALIZADO.getStatusName(),
							statusList);

					update.set("description_status",
							finalizado != null ? finalizado.getDescription() : Status.FINALIZADO.getDescription());
					update.set("generic_speech",
							finalizado != null ? finalizado.getGenericSpeech() : Status.FINALIZADO.getGenericSpeech());
					update.set("front_speech",
							finalizado != null ? finalizado.getFront() : Status.FINALIZADO.getFrontSpeech());

					update.set("last_tracking_status", Status.FINALIZADO.getStatusName());
					update.set("active_status", Status.FINALIZADO.getStatusName().toLowerCase());
					update.set("status_toa", Status.FINALIZADO.getStatusName().toLowerCase());

					statusLog.setStatus(Status.FINALIZADO.getStatusName());
					listLog.add(statusLog);
					update.set("log_status", listLog);

				} else if (request.getStatus().equalsIgnoreCase(Status.TERMINADA.getStatusName())) {

					pe.telefonica.provision.model.Status finalizado = getInfoStatus(Status.TERMINADA.getStatusName(),
							statusList);

					update.set("description_status",
							finalizado != null ? finalizado.getDescription() : Status.TERMINADA.getDescription());
					update.set("generic_speech",
							finalizado != null ? finalizado.getGenericSpeech() : Status.TERMINADA.getGenericSpeech());
					update.set("front_speech",
							finalizado != null ? finalizado.getFront() : Status.TERMINADA.getFrontSpeech());

					update.set("last_tracking_status", Status.TERMINADA.getStatusName());
					update.set("active_status", Status.TERMINADA.getStatusName().toLowerCase());
					update.set("status_toa", Status.TERMINADA.getStatusName().toLowerCase());

					statusLog.setStatus(Status.TERMINADA.getStatusName());
					listLog.add(statusLog);
					update.set("log_status", listLog);

				} else if (request.getStatus().equalsIgnoreCase(Status.CANCELADA_ATIS.getStatusName())) {

					pe.telefonica.provision.model.Status finalizado = getInfoStatus(
							Status.CANCELADA_ATIS.getStatusName(), statusList);

					LocalDateTime paymentReturn = LocalDateTime.now(ZoneOffset.of("-05:00"));

					paymentReturn = paymentReturn.plusDays(15);
					UpFront upFront = provisionx.getUpFront();
					if (upFront != null) {
						upFront.setPaymentReturn(paymentReturn);

						update.set("up_front", upFront);
					}

					update.set("description_status",
							finalizado != null ? finalizado.getDescription() : Status.CANCELADA_ATIS.getDescription());
					update.set("generic_speech", finalizado != null ? finalizado.getGenericSpeech()
							: Status.CANCELADA_ATIS.getGenericSpeech());
					update.set("front_speech",
							finalizado != null ? finalizado.getFront() : Status.CANCELADA_ATIS.getFrontSpeech());

					update.set("last_tracking_status", Status.CANCELADA_ATIS.getStatusName());
					update.set("active_status", Status.CANCELADA_ATIS.getStatusName().toLowerCase());
					update.set("status_toa", Status.CANCELADA_ATIS.getStatusName().toLowerCase());

					statusLog.setStatus(Status.CANCELADA_ATIS.getStatusName());
					listLog.add(statusLog);
					update.set("log_status", listLog);

				} else if (request.getStatus().equalsIgnoreCase(Status.PENDIENTE_DE_VALIDACION.getStatusName())) {
					PendienteDeValidacion pendienteDeValidacion = new PendienteDeValidacion();
					pendienteDeValidacion.setCodeStatusRequest(getData[3]);
					pendienteDeValidacion.setChangeDate(getData[5]);
					pendienteDeValidacion.setRequestDate(getData[2]);
					update.set("pendiente_de_validacion", pendienteDeValidacion);

				} else if (request.getStatus().equalsIgnoreCase(Status.CONFIGURADA.getStatusName())) {
					Configurada configurada = new Configurada();
					configurada.setCodeStatusRequest(getData[3]);
					configurada.setChangeDate(getData[5]);
					configurada.setRequestDate(getData[2]);
					update.set("configurada", configurada);
				} else if (request.getStatus().equalsIgnoreCase(Status.PENDIENTE_DE_APROBACION.getStatusName())) {
					PendienteDeAprobacion pendienteDeAprobacion = new PendienteDeAprobacion();
					pendienteDeAprobacion.setCodeStatusRequest(getData[3]);
					pendienteDeAprobacion.setChangeDate(getData[5]);
					pendienteDeAprobacion.setRequestDate(getData[2]);
					update.set("pendiente_de_aprovacion", pendienteDeAprobacion);
				}
				update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of("-05:00")));

				Boolean isUpdate = provisionRepository.updateProvision(provisionx, update);
				return isUpdate ? true : false;

			}

		} else {

			Provision provision = fillProvisionInsert(request);
			provision = evaluateProvisionComponents(provision);
			provisionRepository.insertProvision(provision);
			return true;
		}
	}

	private InsertOrderRequest formatProvision(String message) {
		String separador = Pattern.quote(Constants.BARRA_VERTICAL);
		String[] parts = message.split(separador);

		if ((parts[0] == null ? "" : parts[0]).equalsIgnoreCase(Constants.STATUS_FIJA_PARKOUR)
				|| (parts[0] == null ? "" : parts[0]).equalsIgnoreCase(Constants.STATUS_ATIS)) {

			String estado = parts[16] == null ? "" : parts[16];
			String dataOrigin = parts[0] == null ? "" : parts[0];

			if (dataOrigin.equalsIgnoreCase("ATIS")) {
				dataOrigin = "ATIS";
				estado = parts[3];
			} else {
				dataOrigin = parts[2].substring(0, 2);
				dataOrigin = dataOrigin.startsWith("MT") ? "MT" : "VF";
			}

			try {
				if (estado != null) {
					estado = estado.trim();
					if (estado.trim().equalsIgnoreCase(Constants.STATUS_PENDIENTE)
							|| estado.trim().equalsIgnoreCase(Constants.STATUS_INGRESADO)
							|| estado.trim().equalsIgnoreCase(Constants.STATUS_CAIDA)
							|| estado.trim().equalsIgnoreCase(Constants.STATUS_PENDIENTE_PAGO)
							|| estado.trim().equalsIgnoreCase(Constants.STATUS_PAGADO)) {

						return new InsertOrderRequest(message, estado, dataOrigin);

					} else if (estado.equalsIgnoreCase(StatusAtis.FINALIZADO.getStatusNameAtis())
							|| estado.equalsIgnoreCase(StatusAtis.TERMINADA.getStatusNameAtis())
							|| estado.equalsIgnoreCase(StatusAtis.CANCELADA_ATIS.getStatusNameAtis())
							|| estado.equalsIgnoreCase(StatusAtis.PENDIENTE_DE_VALIDACION.getStatusNameAtis())
							|| estado.equalsIgnoreCase(StatusAtis.CONFIGURADA.getStatusNameAtis())
							|| estado.equalsIgnoreCase(StatusAtis.PENDIENTE_DE_APROBACION.getStatusNameAtis())) {

						for (StatusAtis item : StatusAtis.values()) {

							if (item.getStatusNameAtis().equalsIgnoreCase(estado)) {
								estado = item.getStatusNameTraza();
							}
						}

						return new InsertOrderRequest(message, estado, dataOrigin);
					}
				}
			} catch (Exception e) {
				log.error("Exception => " + e.getMessage());
			}
		} else {
			log.error("Message => " + message);
		}

		return null;
	}

	@Override
	public Provision setProvisionIsValidated(String provisionId) {
		Optional<Provision> optional = provisionRepository.getProvisionById(provisionId);

		if (optional.isPresent()) {
			Provision provision = optional.get();
			Update update = new Update();
			update.set("validated_address", "true");
			provision.setValidatedAddress("true");

			boolean updated = provisionRepository.updateProvision(provision, update);

			return updated ? provision : null;
		} else {
			return null;
		}
	}

	@Override
	public Provision requestAddressUpdate(String provisionId) {
		Optional<Provision> optional = provisionRepository.getProvisionById(provisionId);

		if (optional.isPresent()) {
			Provision provision = optional.get();
			Update update = new Update();
			update.set("active_status", Constants.PROVISION_STATUS_ADDRESS_CHANGED);
			update.set("validated_address", "true");
			provision.setActiveStatus(Constants.PROVISION_STATUS_ADDRESS_CHANGED);

			boolean updated = provisionRepository.updateProvision(provision, update);

			if (updated) {
				boolean sent = bOApi.sendRequestToBO(provision, "3");
				// boolean sent = sendAddressChangeRequest(provision);
				return sent ? provision : null;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public Boolean receiveAddressUpdateBO(String action, String provisionId, String newDepartment, String newProvince,
			String newDistrict, String newAddress, String newReference, boolean isSMSRequired) {

		Optional<Provision> optional = provisionRepository.getProvisionByIdAndActiveStatus(provisionId,
				Constants.PROVISION_STATUS_ADDRESS_CHANGED);

		if (optional.isPresent()) {
			Provision provision = optional.get();
			String name = provision.getCustomer().getName().split(" ")[0];

			if (action.equals(Constants.ADDRESS_CANCELLED_BY_CUSTOMER)
					|| (action.equals(Constants.ADDRESS_CANCELLED_BY_CHANGE))) {

				Update update = new Update();
				update.set("active_status", Constants.PROVISION_STATUS_CANCELLED);

				boolean updated = provisionRepository.updateProvision(provision, update);

				if (isSMSRequired) {
					List<MsgParameter> msgParameters = new ArrayList<>();
					MsgParameter paramName = new MsgParameter();
					paramName.setKey(Constants.TEXT_NAME_REPLACE);
					paramName.setValue(name);

					MsgParameter paramProduct = new MsgParameter();
					paramProduct.setKey(Constants.TEXT_PRODUCT_REPLACE);
					paramProduct.setValue(provision.getProductName());

					msgParameters.add(paramName);
					msgParameters.add(paramProduct);

					List<Contact> contacts = new ArrayList<>();

					Contact contactCustomer = new Contact();
					contactCustomer.setPhoneNumber(provision.getCustomer().getPhoneNumber());
					contactCustomer.setIsMovistar(provision.getCustomer().getCarrier());
					contactCustomer.setFullName(provision.getCustomer().getName());
					contactCustomer.setHolder(true);
					contacts.add(contactCustomer);

					trazabilidadSecurityApi.sendSMS(contacts, Constants.MSG_PRO_CANCELLED_BY_BO_KEY,
							msgParameters.toArray(new MsgParameter[0]), "", "");

					try {

						sendCancelledMail(provision, name, "192828");

					} catch (Exception e) {
						log.error(this.getClass().getName() + " - Exception: " + e.getMessage());
					}

				}

				return updated;
			} else if (action.equals(Constants.ADDRESS_UNREACHABLE)) {
				Update update = new Update();
				update.set("active_status", Constants.PROVISION_STATUS_CANCELLED);
				boolean updated = provisionRepository.updateProvision(provision, update);

				List<MsgParameter> msgParameters = new ArrayList<>();
				MsgParameter paramProduct = new MsgParameter();
				paramProduct.setKey(Constants.TEXT_PRODUCT_REPLACE);
				paramProduct.setValue(provision.getProductName());

				msgParameters.add(paramProduct);

				List<Contact> contacts = new ArrayList<>();

				Contact contactCustomer = new Contact();
				contactCustomer.setPhoneNumber(provision.getCustomer().getPhoneNumber());
				contactCustomer.setIsMovistar(provision.getCustomer().getCarrier());
				contactCustomer.setHolder(true);
				contactCustomer.setFullName(provision.getCustomer().getName());
				contacts.add(contactCustomer);

				trazabilidadSecurityApi.sendSMS(contacts, Constants.MSG_PRO_CUSTOMER_UNREACHABLE_KEY,
						msgParameters.toArray(new MsgParameter[0]), "", "");

				try {

					sendCancelledMail(provision, name, "192909");
				} catch (Exception e) {
					log.error(this.getClass().getName() + " - Exception: " + e.getMessage());
				}

				return updated;
			} else {
				Update update = new Update();
				update.set("active_status", Constants.PROVISION_STATUS_ACTIVE);
				update.set("customer.department", newDepartment);
				update.set("customer.province", newProvince);
				update.set("customer.district", newDistrict);
				update.set("customer.address", newAddress);
				update.set("customer.reference", newReference);

				boolean updated = provisionRepository.updateProvision(provision, update);

				if (updated) {

					List<Contact> contacts = new ArrayList<>();

					Contact contactCustomer = new Contact();
					contactCustomer.setPhoneNumber(provision.getCustomer().getPhoneNumber());
					contactCustomer.setIsMovistar(provision.getCustomer().getCarrier());
					contacts.add(contactCustomer);

					return true;
				} else {
					return false;
				}
			}
		} else {
			return false;
		}

	}

	private boolean sendCancelledMail(Provision provision, String name, String codeTemplate) {
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_EMAILING, new Locale("es", "ES"));
		sdf.setTimeZone(TimeZone.getTimeZone("GMT-5:00"));
		String scheduleDateStr = sdf.format(Calendar.getInstance().getTime());
		ArrayList<MailParameter> mailParameters = new ArrayList<>();

		if (provision.getCustomer().getMail() == null || provision.getCustomer().getMail().isEmpty()) {
			return false;
		}

		MailParameter mailParameter1 = new MailParameter();
		mailParameter1.setParamKey("SHORTNAME");
		mailParameter1.setParamValue(name);
		mailParameters.add(mailParameter1);

		MailParameter mailParameter2 = new MailParameter();
		mailParameter2.setParamKey("EMAIL");
		mailParameter2.setParamValue(provision.getCustomer().getMail());
		mailParameters.add(mailParameter2);

		MailParameter mailParameter3 = new MailParameter();
		mailParameter3.setParamKey("PROVISIONNAME");
		mailParameter3.setParamValue(provision.getProductName());
		mailParameters.add(mailParameter3);

		MailParameter mailParameter4 = new MailParameter();
		mailParameter4.setParamKey("CANCELATIONDATE");
		mailParameter4.setParamValue(scheduleDateStr);
		mailParameters.add(mailParameter4);

		MailParameter mailParameter5 = new MailParameter();
		mailParameter5.setParamKey("STOREURL");
		mailParameter5.setParamValue("http://www.movistar.com.pe");
		mailParameters.add(mailParameter5);

		boolean isSendMail = trazabilidadSecurityApi.sendMail(codeTemplate,
				mailParameters.toArray(new MailParameter[mailParameters.size()]));
		return isSendMail;
	}

	@Override
	public Provision orderCancellation(String provisionId, String cause, String detail) {
		boolean sentBOCancellation;
		boolean provisionUpdated;
		boolean scheduleUpdated;
		Optional<Provision> optional = provisionRepository.getProvisionById(provisionId);

		if (optional.isPresent()) {

			Provision provision = optional.get();
			pe.telefonica.provision.model.Status cancel = getInfoStatus(Status.CANCEL.getStatusName(), null);

			StatusLog statusLog = new StatusLog();
			statusLog.setStatus(Status.CANCEL.getStatusName());

			provision.getLogStatus().add(statusLog);
			provision.setActiveStatus(Constants.PROVISION_STATUS_CANCELLED);
			provision.setLastTrackingStatus(Status.CANCEL.getStatusName());
			provision.setGenericSpeech(cancel != null ? cancel.getGenericSpeech() : Status.CANCEL.getGenericSpeech());
			provision.setDescriptionStatus(cancel != null ? cancel.getDescription() : Status.CANCEL.getDescription());
			provision.setFrontSpeech(cancel != null ? cancel.getFront() : Status.CANCEL.getFrontSpeech());
			provision.setCancellationCause(cause);
			provision.setCancellationDetail(detail);

			Update update = new Update();
			update.set("active_status", Constants.PROVISION_STATUS_CANCELLED);
			update.set("cancellation_cause", cause);
			update.set("cancellation_detail", detail);
			update.set("log_status", provision.getLogStatus());
			update.set("last_tracking_status", Status.CANCEL.getStatusName());
			update.set("description_status", cancel != null ? cancel.getDescription() : Status.CANCEL.getDescription());
			update.set("generic_speech", cancel != null ? cancel.getGenericSpeech() : Status.CANCEL.getGenericSpeech());
			update.set("front_speech", cancel != null ? cancel.getFront() : Status.CANCEL.getFrontSpeech());

			sentBOCancellation = bOApi.sendRequestToBO(provision, "4");

			if (!sentBOCancellation) {
				return null;
			}

			if (provision.getHasSchedule()) {
				scheduleUpdated = trazabilidadScheduleApi.updateCancelSchedule(
						new CancelRequest(provision.getIdProvision(), "provision", provision.getXaIdSt(), false));
				if (!scheduleUpdated) {
					return null;
				}
			}

			provisionUpdated = provisionRepository.updateProvision(provision, update);

			if (!provisionUpdated) {
				return null;
			}

			try {
				sendCancelBySMS(provision);

			} catch (Exception e) {
				log.error(this.getClass().getName() + " - Exception: " + e.getMessage());
			}

			return provision;
		} else {
			return null;
		}
	}

	private void sendCancelBySMS(Provision provision) {
		List<MsgParameter> msgParameters = new ArrayList<>();
		MsgParameter paramName = new MsgParameter();
		paramName.setKey(Constants.TEXT_NAME_REPLACE);
		paramName.setValue("");

		msgParameters.add(paramName);

		List<Contact> contacts = new ArrayList<>();
		Contact contactCustomer = new Contact();
		contactCustomer.setPhoneNumber(provision.getCustomer().getPhoneNumber());
		contactCustomer.setIsMovistar(provision.getCustomer().getCarrier());
		contactCustomer.setHolder(true);
		contactCustomer.setFullName(provision.getCustomer().getName());
		contacts.add(contactCustomer);

		trazabilidadSecurityApi.sendSMS(contacts, Constants.MSG_PRO_CANCELLED_BY_CUSTOMER_KEY,
				msgParameters.toArray(new MsgParameter[0]), "", "");
	}

	@Override
	public ProvisionResponse<String> getStatus(String provisionId) {
		Optional<Provision> optional = provisionRepository.getStatus(provisionId);
		ProvisionResponse<String> response = new ProvisionResponse<String>();
		ProvisionHeaderResponse header = new ProvisionHeaderResponse();

		if (optional.isPresent()) {
			header.setCode(HttpStatus.OK.value()).setMessage(HttpStatus.OK.name());
			response.setHeader(header).setData(optional.get().getActiveStatus());
		} else {
			header.setCode(HttpStatus.OK.value()).setMessage("No se encontraron provisiones");
			response.setHeader(header);
		}

		return response;
	}

	@Override
	public ProvisionResponse<Boolean> validateQueue() {
		Optional<Queue> optional = provisionRepository.isQueueAvailable();
		ProvisionResponse<Boolean> response = new ProvisionResponse<Boolean>();
		ProvisionHeaderResponse header = new ProvisionHeaderResponse();

		if (optional.isPresent()) {
			Queue queue = optional.get();
			header.setCode(HttpStatus.OK.value()).setMessage(HttpStatus.OK.name());
			response.setHeader(header).setData(queue.getActive());
		} else {
			header.setCode(HttpStatus.NO_CONTENT.value()).setMessage("No se encontraron datos");
			response.setHeader(header);
		}
		return response;
	}

	@Override
	public ProvisionResponse<Boolean> updateOrderSchedule(String provisionId, LocalDate scheduledDate,
			String scheduledRange, Integer scheduledType) {
		Optional<Provision> optional = provisionRepository.getProvisionById(provisionId);
		Optional<List<pe.telefonica.provision.model.Status>> statusListOptional = provisionRepository
				.getAllInfoStatus();
		List<pe.telefonica.provision.model.Status> statusList = statusListOptional.get();
		ProvisionResponse<Boolean> response = new ProvisionResponse<Boolean>();
		ProvisionHeaderResponse header = new ProvisionHeaderResponse();
		try {
			if (optional.isPresent()) {
				Provision provision = optional.get();
				String nomEstado = "";
				String description = "";
				String speech = "";
				String frontSpeech = "";

				if (scheduledType == 2) {
					pe.telefonica.provision.model.Status fictitious = getInfoStatus(
							Status.FICTICIOUS_SCHEDULED.getStatusName(), statusList);
					nomEstado = Status.FICTICIOUS_SCHEDULED.getStatusName();
					description = fictitious != null ? fictitious.getDescription()
							: Status.FICTICIOUS_SCHEDULED.getDescription();
					speech = fictitious != null ? fictitious.getGenericSpeech()
							: Status.FICTICIOUS_SCHEDULED.getGenericSpeech();
					frontSpeech = fictitious != null ? fictitious.getFront()
							: Status.FICTICIOUS_SCHEDULED.getFrontSpeech();
				} else {
					pe.telefonica.provision.model.Status scheduled = getInfoStatus(Status.SCHEDULED.getStatusName(),
							statusList);
					nomEstado = Status.SCHEDULED.getStatusName();
					description = scheduled != null ? scheduled.getDescription() : Status.SCHEDULED.getDescription();
					speech = scheduled != null ? scheduled.getGenericSpeech() : Status.SCHEDULED.getGenericSpeech();
					frontSpeech = scheduled != null ? scheduled.getFront() : Status.SCHEDULED.getFrontSpeech();
				}

				speech = hasCustomerInfo(provision.getCustomer())
						? speech.replace(Constants.TEXT_NAME_REPLACE, provision.getCustomer().getName().split(" ")[0])
						: speech;

				boolean updated = updateTrackingStatus(provision.getXaRequest(), provision.getXaIdSt(), nomEstado, true,
						scheduledDate, scheduledRange, scheduledType, description, speech, frontSpeech);

				if (updated) {
					header.setCode(HttpStatus.OK.value()).setMessage(HttpStatus.OK.name());
					response.setHeader(header).setData(true);
				} else {
					header.setCode(HttpStatus.BAD_REQUEST.value()).setMessage("No se pudo actualizar");
					response.setHeader(header).setData(false);
				}
			} else {
				header.setCode(HttpStatus.NO_CONTENT.value()).setMessage("No se encontraron provisiones");
				response.setHeader(header).setData(false);
			}
		} catch (Exception exception) {
			throw exception;
		}

		return response;
	}

	@Override
	public List<Provision> getAllInTimeRange(LocalDateTime startDate, LocalDateTime endDate) {
		Optional<List<Provision>> optional = provisionRepository.getAllInTimeRange(startDate, endDate);

		if (optional.isPresent()) {
			return optional.get();
		}

		return null;
	}

	@Override
	public Boolean updateTrackingStatus(String xaRequest, String xaIdSt, String status, boolean comesFromSchedule,
			LocalDate scheduledDate, String scheduledRange, Integer scheduleType, String description, String speech,
			String frontSpeech) {
		boolean updated = false;
		Optional<Provision> optionalProvision = provisionRepository.getProvisionByXaRequestAndSt(xaRequest, xaIdSt);

		if (optionalProvision.isPresent()) {
			Provision provision = optionalProvision.get();
			List<StatusLog> logStatus = provision.getLogStatus() == null ? new ArrayList<>() : provision.getLogStatus();

			StatusLog statusLog = new StatusLog();
			statusLog.setStatus(status);

			if (scheduledDate != null)
				statusLog.setScheduledDate(scheduledDate.toString());

			if (scheduledRange != null && !scheduledRange.equals(""))
				statusLog.setScheduledRange(scheduledRange);

			if (xaIdSt != null)
				statusLog.setXaidst(xaIdSt);

			logStatus.add(statusLog);

			provision.setLastTrackingStatus(status);
			provision.setGenericSpeech(speech);
			provision.setDescriptionStatus(description);
			provision.setFrontSpeech(frontSpeech);

			updated = provisionRepository.updateTrackingStatus(optionalProvision.get(), logStatus, description, speech,
					frontSpeech, comesFromSchedule);
		}

		return updated;
	}

	@Override
	public Provision getProvisionByOrderCode(ApiRequest<GetProvisionByOrderCodeRequest> request) {
		return provisionRepository.getProvisionByOrderCode(request);
	}

	@Override
	public Provision setContactInfoUpdate(ApiTrazaSetContactInfoUpdateRequest request) throws Exception {
		Provision provision = provisionRepository.getProvisionByXaIdSt(request.getPsiCode());

		PSIUpdateClientRequest psiRequest = new PSIUpdateClientRequest();
		int count = 0;
		int maxTries = 2;

		while (true) {
			try {
				if (provision != null) {
					List<ContactRequest> listContact = request.getContacts();
					List<Contacts> contactsList = new ArrayList<>();

					if (request.isHolderWillReceive()) {
						ContactRequest contactRequest = new ContactRequest();
						contactRequest.setFullName(provision.getCustomer().getName());
						contactRequest.setPhoneNumber((provision.getCustomer().getPhoneNumber() != null
								&& !provision.getCustomer().getPhoneNumber().isEmpty())
										? Integer.valueOf(provision.getCustomer().getPhoneNumber())
										: 0);
						request.getContacts().clear();
						request.getContacts().add(contactRequest);
					}

					for (int a = 0; a < request.getContacts().size(); a++) {
						Contacts contacts = new Contacts();
						contacts.setFullName(listContact.get(a).getFullName());
						contacts.setPhoneNumber(listContact.get(a).getPhoneNumber().toString());
						boolean isMovistar = false;

						if (!listContact.get(a).getPhoneNumber().toString().equals("")) {
							String switchOnPremise = System.getenv("TDP_SWITCH_ON_PREMISE");
							if (switchOnPremise.equals("true")) {
								isMovistar = restPSI.getCarrier(listContact.get(a).getPhoneNumber().toString());
							} else {
								isMovistar = restPSI.getCarrierOld(listContact.get(a).getPhoneNumber().toString());
							}
						}
						contacts.setCarrier(isMovistar);
						contactsList.add(contacts);

						if (a == 0) {
							psiRequest.getBodyUpdateClient().setNombre_completo(listContact.get(a).getFullName());
							psiRequest.getBodyUpdateClient()
									.setTelefono1(listContact.get(a).getPhoneNumber().toString());
						}

						if (a == 1) {
							psiRequest.getBodyUpdateClient().setNombre_completo2(listContact.get(a).getFullName());
							psiRequest.getBodyUpdateClient()
									.setTelefono2(listContact.get(a).getPhoneNumber().toString());
						}

						if (a == 2) {
							psiRequest.getBodyUpdateClient().setNombre_completo3(listContact.get(a).getFullName());
							psiRequest.getBodyUpdateClient()
									.setTelefono3(listContact.get(a).getPhoneNumber().toString());
						}

						if (a == 3) {
							psiRequest.getBodyUpdateClient().setNombre_completo4(listContact.get(a).getFullName());
							psiRequest.getBodyUpdateClient()
									.setTelefono4(listContact.get(a).getPhoneNumber().toString());
						}

					}

					psiRequest.getBodyUpdateClient().setSolicitud(provision.getXaIdSt());
					psiRequest.getBodyUpdateClient().setCorreo(
							provision.getCustomer().getMail() != null ? provision.getCustomer().getMail() : "");

					boolean updatedPsi = restPSI.updatePSIClient(psiRequest);

					if (updatedPsi) {
						Update update = new Update();
						update.set("contacts", request.isHolderWillReceive() ? null : contactsList);
						provisionRepository.updateProvision(provision, update);

						if (provision.getContacts() != null) {
							provision.getContacts().clear();
						}

						provision.setContacts(request.isHolderWillReceive() ? null : contactsList);
					} else {
						throw new Exception();
					}

					return provision;

				} else {
					return null;
				}
			} catch (Exception e) {
				if (++count == maxTries) {
					throw e;
				}
			}
		}
	}

	@Override
	public Boolean apiContactInfoUpdate(ApiTrazaSetContactInfoUpdateRequest request) {

		Provision provision = provisionRepository.getProvisionByDummyStPsiCode(request.getPsiCode());

		PSIUpdateClientRequest psiRequest = new PSIUpdateClientRequest();
		if (provision != null) {

			// Provision provision = optional.get();
			List<ContactRequest> listContact = request.getContacts();
			List<Contacts> contactsList = new ArrayList<>();

			for (int a = 0; a < 4; a++) {

				int quanty_contact = request.getContacts().size();

				if (a < quanty_contact) {

					Contacts contacts = new Contacts();
					contacts.setFullName(listContact.get(a).getFullName());
					contacts.setPhoneNumber(listContact.get(a).getPhoneNumber().toString());

					contactsList.add(contacts);
				}

				if (a == 0) {

					psiRequest.getBodyUpdateClient()
							.setNombre_completo(a < quanty_contact ? listContact.get(a).getFullName() : "");
					psiRequest.getBodyUpdateClient()
							.setTelefono1(a < quanty_contact ? listContact.get(a).getPhoneNumber().toString() : "");

				}
				if (a == 1) {

					psiRequest.getBodyUpdateClient()
							.setNombre_completo2(a < quanty_contact ? listContact.get(a).getFullName() : "");
					psiRequest.getBodyUpdateClient()
							.setTelefono2(a < quanty_contact ? listContact.get(a).getPhoneNumber().toString() : "");

				}
				if (a == 2) {
					psiRequest.getBodyUpdateClient()
							.setNombre_completo3(a < quanty_contact ? listContact.get(a).getFullName() : "");
					psiRequest.getBodyUpdateClient()
							.setTelefono3(a < quanty_contact ? listContact.get(a).getPhoneNumber().toString() : "");

				}
				if (a == 3) {
					psiRequest.getBodyUpdateClient()
							.setNombre_completo4(a < quanty_contact ? listContact.get(a).getFullName() : "");
					psiRequest.getBodyUpdateClient()
							.setTelefono4(a < quanty_contact ? listContact.get(a).getPhoneNumber().toString() : "");

				}
			}

			psiRequest.getBodyUpdateClient().setCorreo(request.getEmail());
			psiRequest.getBodyUpdateClient().setSolicitud(provision.getDummyStPsiCode());

			restPSI.updatePSIClient(psiRequest);

			Update update = new Update();

			update.set("customer.mail", request.getEmail());

			update.set("contacts", contactsList);

			provisionRepository.updateProvision(provision, update);

			return true;

		} else {

			return false;
		}
	}

	@Override
	public boolean provisionInsertCodeFictitious(InsertCodeFictionalRequest request) {

		Provision provision = provisionRepository.getProvisionBySaleCode(request.getSaleCode());
		String speech;

		if (provision != null) {
			Update update = new Update();
			List<StatusLog> listLog = provision.getLogStatus();

			pe.telefonica.provision.model.Status fictitious = getInfoStatus(Status.FICTICIOUS_SCHEDULED.getStatusName(),
					null);
			speech = fictitious != null ? fictitious.getGenericSpeech()
					: Status.FICTICIOUS_SCHEDULED.getGenericSpeech();
			speech = hasCustomerInfo(provision.getCustomer())
					? speech.replace(Constants.TEXT_NAME_REPLACE, provision.getCustomer().getName().split(" ")[0])
					: speech;

			update.set("dummy_st_psi_code", request.getDummyStPsiCode());
			update.set("dummy_xa_request", request.getDummyXaRequest());
			update.set("is_update_dummy_st_psi_code", request.getIsUpdatedummyStPsiCode());
			update.set("has_schedule", true);

			StatusLog statusLog = new StatusLog();
			statusLog.setStatus(Status.FICTICIOUS_SCHEDULED.getStatusName());
			statusLog.setScheduledDate(request.getScheduleDate().toString());
			statusLog.setScheduledRange(request.getScheduleRange());

			update.set("work_zone", request.getBucket());
			update.set("origin_code", request.getOriginCode());
			update.set("last_tracking_status", Status.FICTICIOUS_SCHEDULED.getStatusName());
			update.set("generic_speech", speech);
			update.set("description_status",
					fictitious != null ? fictitious.getDescription() : Status.FICTICIOUS_SCHEDULED.getDescription());
			update.set("front_speech",
					fictitious != null ? fictitious.getFront() : Status.FICTICIOUS_SCHEDULED.getFrontSpeech());
			listLog.add(statusLog);
			update.set("log_status", listLog);

			/**/
			// Validar si tiene INGRESADO y actualizar agenda
			if (provision.getLastTrackingStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName())) {

				ScheduleUpdateFicticiousRequest updateFicRequest = new ScheduleUpdateFicticiousRequest();
				updateFicRequest.setOrderCode(provision.getXaRequest());
				updateFicRequest.setOriginCode(request.getOriginCode());
				updateFicRequest.setSaleCode(provision.getSaleCode());
				updateFicRequest.setFictitiousCode(request.getDummyXaRequest());
				updateFicRequest.setRequestName(provision.getProductName());
				updateFicRequest.setRequestId(provision.getIdProvision());
				update.set("is_update_dummy_st_psi_code", true);
				trazabilidadScheduleApi.updateFicticious(updateFicRequest);
			}
			/**/

			provisionRepository.updateProvision(provision, update);

			/**/
			// Validar si tiene INGRESADO y actualizar agenda
			if (provision.getLastTrackingStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName())) {
				ScheduleUpdateFicticiousRequest updateFicRequest = new ScheduleUpdateFicticiousRequest();
				updateFicRequest.setOrderCode(provision.getXaRequest());
				updateFicRequest.setOriginCode(request.getOriginCode());
				updateFicRequest.setSaleCode(provision.getSaleCode());
				updateFicRequest.setFictitiousCode(request.getDummyXaRequest());
				updateFicRequest.setRequestName(provision.getProductName());
				updateFicRequest.setRequestId(provision.getIdProvision());
				trazabilidadScheduleApi.updateFicticious(updateFicRequest);
			}
			/**/

		} else {

			Provision provisionAdd = new Provision();

			provisionAdd.setSaleCode(request.getSaleCode());
			provisionAdd.setDummyXaRequest(request.getDummyXaRequest());
			provisionAdd.setDummyStPsiCode(request.getDummyStPsiCode());
			provisionAdd.setHasSchedule(true);
			provisionAdd.setOriginCode(request.getOriginCode());
			provisionAdd.setProductName("Pedido Movistar");
			provisionAdd.setCommercialOp(request.getCommercialOp());

			Customer customer = new Customer();

			customer.setDocumentType(request.getCustomerDocumentType());
			customer.setDocumentNumber(request.getCustomerDocumentNumber());
			customer.setName(request.getCustomerName());
			customer.setLatitude(request.getCustomerLatitude());
			customer.setLongitude(request.getCustomerLongitude());

			provisionAdd.setCustomer(customer);

			List<StatusLog> listLog = new ArrayList<>();
			StatusLog statusPendiente = new StatusLog();
			StatusLog statusLogDummy = new StatusLog();

			if (request.getIsUpFront()) {
				provisionAdd.setActiveStatus(Status.PENDIENTE_PAGO.getStatusName().toLowerCase());
				provisionAdd.setStatusToa(Status.PENDIENTE_PAGO.getStatusName().toLowerCase());
				statusPendiente.setStatus(Status.PENDIENTE_PAGO.getStatusName());
				provisionAdd.setIsUpFront(true);
			} else {
				provisionAdd.setActiveStatus(Status.PENDIENTE.getStatusName().toLowerCase());
				provisionAdd.setStatusToa(Status.PENDIENTE.getStatusName().toLowerCase());
				statusPendiente.setStatus(Status.PENDIENTE.getStatusName());
			}

			statusLogDummy.setStatus(Status.FICTICIOUS_SCHEDULED.getStatusName());
			statusLogDummy.setScheduledDate(request.getScheduleDate().toString());
			statusLogDummy.setScheduledRange(request.getScheduleRange());

			listLog.add(statusPendiente);
			listLog.add(statusLogDummy);

			pe.telefonica.provision.model.Status fictitious = getInfoStatus(Status.FICTICIOUS_SCHEDULED.getStatusName(),
					null);
			speech = fictitious != null ? fictitious.getGenericSpeech()
					: Status.FICTICIOUS_SCHEDULED.getGenericSpeech();
			speech = hasCustomerInfo(customer)
					? speech.replace(Constants.TEXT_NAME_REPLACE, customer.getName().split(" ")[0])
					: speech;

			provisionAdd.setLogStatus(listLog);
			provisionAdd.setLastTrackingStatus(Status.FICTICIOUS_SCHEDULED.getStatusName());
			provisionAdd.setGenericSpeech(speech);
			provisionAdd.setDescriptionStatus(
					fictitious != null ? fictitious.getDescription() : Status.FICTICIOUS_SCHEDULED.getDescription());
			provisionAdd.setFrontSpeech(
					fictitious != null ? fictitious.getFront() : Status.FICTICIOUS_SCHEDULED.getFrontSpeech());
			provisionAdd.setComponents(new ArrayList<>());

			Notifications notifications = new Notifications();
			notifications.setIntoaSendNotify(false);
			provisionAdd.setNotifications(notifications);

			provisionRepository.insertProvision(provisionAdd);

		}

		return true;
	}

	private boolean validateBuckectProduct(KafkaTOARequest kafkaTOARequest, Provision provision, String status)
			throws Exception {
		boolean errorBucket = false; // validar IN_TOA
		// Valida DNI
		if (Constants.TIPO_RUC.equals(provision.getCustomer().getDocumentType().toLowerCase())
				&& !provision.getCustomer().getDocumentNumber().startsWith(Constants.RUC_NATURAL)) {
			errorBucket = true;

			return errorBucket;
		}
		if (Constants.STATUS_IN_TOA.equalsIgnoreCase(status == null ? "" : status)) { // validate
																						// bucket
																						// and
			errorBucket = !getBucketByProduct(provision.getOriginCode(), provision.getCommercialOp(),
					kafkaTOARequest.getEvent().getAppointment().getAdditionalData().get(1).getValue());
		}

		return errorBucket;
	}

	private void sendSMSWoPrestartContact(Provision provision) {
		if (!Boolean.valueOf(System.getenv("TDP_MESSAGE_PROVISION_ENABLE"))) {
			return;
		}
		List<Contacts> conct = provision.getContacts();

		for (Contacts item : conct) {
			String text = item.getFullName();

			String nameCapitalize = text.substring(0, 1).toUpperCase() + text.substring(1);

			List<MsgParameter> msgParameters = new ArrayList<>();
			MsgParameter paramName = new MsgParameter();
			paramName.setKey(Constants.TEXT_NAME_REPLACE);
			paramName.setValue(nameCapitalize);

			msgParameters.add(paramName);
			// msgParameters.add(paramProduct);

			List<Contact> contacts = new ArrayList<>();

			Contact contactCustomer = new Contact();
			contactCustomer.setPhoneNumber(item.getPhoneNumber());
			contactCustomer.setIsMovistar(item.getCarrier());
			contactCustomer.setFullName(item.getFullName());
			contactCustomer.setHolder(false);
			contacts.add(contactCustomer);

			String urlTraza = provision.getWoPreStart().getTrackingUrl();

			trazabilidadSecurityApi.sendSMS(contacts, Constants.MSG_PRO_SCHEDULE_TECHNICIAN_KEY,
					msgParameters.toArray(new MsgParameter[0]), "", urlTraza);
		}

	}

	private void sendSMSWoPrestartHolder(Provision provision) {
		if (!Boolean.valueOf(System.getenv("TDP_MESSAGE_PROVISION_ENABLE"))) {
			return;
		}
		String text = provision.getCustomer().getName();

		String nameCapitalize = text.substring(0, 1).toUpperCase() + text.substring(1);

		List<MsgParameter> msgParameters = new ArrayList<>();
		MsgParameter paramName = new MsgParameter();
		paramName.setKey(Constants.TEXT_NAME_REPLACE);
		paramName.setValue(nameCapitalize);

		msgParameters.add(paramName);
		// msgParameters.add(paramProduct);

		List<Contact> contacts = new ArrayList<>();

		Contact contactCustomer = new Contact();
		contactCustomer.setPhoneNumber(provision.getCustomer().getPhoneNumber());
		contactCustomer.setIsMovistar(provision.getCustomer().getCarrier());
		contactCustomer.setFullName(provision.getCustomer().getName());
		contactCustomer.setHolder(true);
		contacts.add(contactCustomer);

		String urlTraza = provisionTexts.getWebUrl();
		trazabilidadSecurityApi.sendSMS(contacts, Constants.MSG_FAULT_WOPRESTART,
				msgParameters.toArray(new MsgParameter[0]), urlTraza, "");

	}

	@Override
	public boolean provisionUpdateFromTOA(UpdateFromToaRequest request) throws Exception {

		KafkaTOARequest kafkaTOARequest = new Gson().fromJson(request.getData(), KafkaTOARequest.class);

		String getXaRequirementNumber = kafkaTOARequest.getEvent().getAppointment().getRelatedObject().get(0)
				.getAdditionalData().get(1).getValue();
		String getXaRequest = kafkaTOARequest.getEvent().getAppointment().getRelatedObject().get(0).getAdditionalData()
				.get(0).getValue();
		boolean fromSale = getXaRequirementNumber.startsWith("MT") || getXaRequirementNumber.startsWith("VF");

		boolean bool = false;

		Provision provision = new Provision();

		if (!fromSale) {
			provision = provisionRepository.getByOrderCodeForUpdate(getXaRequest);
		} else {
			// Llamar al método de busqueda ficticio
			provision = provisionRepository.getByOrderCodeForUpdateFicticious(getXaRequirementNumber);
		}

		bool = updateProvision(provision, kafkaTOARequest, request, fromSale);

		return bool;
	}

	private boolean updateProvision(Provision provision, KafkaTOARequest kafkaTOARequest, UpdateFromToaRequest request,
			boolean fromSale) throws Exception {

		String getXaRequest = kafkaTOARequest.getEvent().getAppointment().getRelatedObject().get(0).getAdditionalData()
				.get(0).getValue();
		String getXaRequirementNumber = kafkaTOARequest.getEvent().getAppointment().getRelatedObject().get(0)
				.getAdditionalData().get(1).getValue();
		String getXaIdSt = kafkaTOARequest.getEvent().getAppointment().getRelatedObject().get(0).getAdditionalData()
				.get(5).getValue();

		KafkaTOARequest.Event.Appointment appointment = kafkaTOARequest.getEvent().getAppointment();
		String speech = "";
		Optional<List<pe.telefonica.provision.model.Status>> statusListOptional = provisionRepository
				.getAllInfoStatus();
		List<pe.telefonica.provision.model.Status> statusList = statusListOptional.get();

		if (provision != null) {
			List<StatusLog> listLog = provision.getLogStatus();

			// valida Bucket x Producto
			boolean boolBucket = validateBuckectProduct(kafkaTOARequest, provision, request.getStatus());

			if (boolBucket) {
				return false;
			}

			pe.telefonica.provision.model.Status dummyInToa = getInfoStatus(Status.DUMMY_IN_TOA.getStatusName(),
					statusList);

			speech = dummyInToa != null ? dummyInToa.getGenericSpeech() : Status.DUMMY_IN_TOA.getGenericSpeech();
			speech = hasCustomerInfo(provision.getCustomer())
					? speech.replace(Constants.TEXT_NAME_REPLACE, provision.getCustomer().getName().split(" ")[0])
					: speech;

			if (request.getStatus().equalsIgnoreCase(Status.IN_TOA.getStatusName())) {
				if (fromSale) {
					// IN_TOA fictitious
					Update update = new Update();
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

					Update update = new Update();
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

					Update update = new Update();

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
					inToa.setLongitude(appointment.getRelatedPlace().getCoordinates().getLongitude());
					inToa.setLatitude(appointment.getRelatedPlace().getCoordinates().getLatitude());

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

			if (request.getStatus().equalsIgnoreCase(Status.WO_PRESTART.getStatusName())
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
				woPreStart.setLatitude(appointment.getRelatedPlace().getCoordinates().getLatitude());
				woPreStart.setLongitude(appointment.getRelatedPlace().getCoordinates().getLongitude());

				update.set("activity_type", appointment.getDescription().toLowerCase());
				update.set("xa_id_st", getXaIdSt);
				update.set("show_location", false);
				update.set("notifications.prestart_send_notify", false);

				StatusLog statusLog = new StatusLog();
				statusLog.setStatus(Status.WO_PRESTART.getStatusName());
				statusLog.setXaidst(provision.getXaIdSt());

				update.set("customer.latitude", appointment.getRelatedPlace().getCoordinates().getLatitude());
				update.set("customer.longitude", appointment.getRelatedPlace().getCoordinates().getLongitude());
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
				if (nowDate.getHour() >= 07 && nowDate.getHour() <= 19) {
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
							tokenExternal = trazabilidadSecurityApi.gerateToken();
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

			if (request.getStatus().equalsIgnoreCase(Status.WO_INIT.getStatusName())
					&& !provision.getXaIdSt().isEmpty()) {

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

			if (request.getStatus().equalsIgnoreCase(Status.WO_COMPLETED.getStatusName())
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

			if (request.getStatus().equalsIgnoreCase(Status.WO_CANCEL.getStatusName())) {
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

			/*
			 * if
			 * (request.getStatus().equalsIgnoreCase(Status.WO_RESCHEDULE.getStatusName())
			 * && !provision.getXaIdSt().isEmpty()) { pe.telefonica.provision.model.Status
			 * rescheduleStatus = getInfoStatus(Status.SCHEDULED.getStatusName(),
			 * statusList);
			 * 
			 * String identificadorSt = getXaIdSt.toString();
			 * 
			 * Update update = new Update(); WoReshedule woReshedule = new WoReshedule();
			 * String range = "AM";
			 * 
			 * if (appointment.getTimeSlot().trim().equals("09-13") ||
			 * appointment.getTimeSlot().toString().trim().equals("9-13")) { range = "AM"; }
			 * else { range = "PM"; } String rangeFinal = range;
			 * 
			 * String dateString = appointment.getScheduledDate().substring(0, 10);
			 * 
			 * if ((identificadorSt == null || identificadorSt.isEmpty()) && (rangeFinal ==
			 * null || rangeFinal.isEmpty()) && (dateString == null ||
			 * dateString.isEmpty())) { return false; }
			 * 
			 * List<StatusLog> listLogx = listLog.stream() .filter(x ->
			 * "SCHEDULED".equals(x.getStatus()) && identificadorSt.equals(x.getXaidst()))
			 * .collect(Collectors.toList());
			 * 
			 * if (listLogx.size() > 0) { if (listLogx.get(listLogx.size() -
			 * 1).getScheduledDate().contentEquals(dateString.toString()) &&
			 * listLogx.get(listLogx.size() -
			 * 1).getScheduledRange().contentEquals(rangeFinal)) { return true; }
			 * 
			 * }
			 * 
			 * woReshedule.setXaAppointmentScheduler(appointment.getScheduler());
			 * woReshedule.setTimeSlot(range); update.set("wo_schedule", woReshedule);
			 * update.set("active_status", Constants.PROVISION_STATUS_ACTIVE);
			 * 
			 * update.set("xa_id_st", getXaIdSt); update.set("xa_requirement_number",
			 * getXaRequirementNumber); update.set("appt_number", appointment.getId());
			 * update.set("activity_type", appointment.getDescription().toLowerCase());
			 * 
			 * StatusLog statusLog = new StatusLog();
			 * statusLog.setStatus(Status.SCHEDULED.getStatusName());
			 * statusLog.setScheduledRange(rangeFinal);
			 * statusLog.setScheduledDate(dateString.toString());
			 * statusLog.setXaidst(provision.getXaIdSt());
			 * 
			 * update.set("date", appointment.getScheduledDate()); update.set("send_notify",
			 * false); update.set("time_slot", range); update.set("last_tracking_status",
			 * Status.SCHEDULED.getStatusName()); update.set("generic_speech",
			 * rescheduleStatus != null ? rescheduleStatus.getGenericSpeech() :
			 * Status.SCHEDULED.getGenericSpeech()); update.set("description_status",
			 * rescheduleStatus != null ? rescheduleStatus.getDescription() :
			 * Status.SCHEDULED.getDescription()); update.set("front_speech",
			 * rescheduleStatus != null ? rescheduleStatus.getFront() :
			 * Status.SCHEDULED.getFrontSpeech()); listLog.add(statusLog);
			 * update.set("log_status", listLog);
			 * 
			 * update.set("show_location", false); update.set("statusChangeDate",
			 * LocalDateTime.now(ZoneOffset.of("-05:00")));
			 * 
			 * // Actualizar provision provisionRepository.updateProvision(provision,
			 * update);
			 * 
			 * // el que parsea SimpleDateFormat parseador2 = new
			 * SimpleDateFormat("yyyy-MM-dd"); // el que formatea SimpleDateFormat
			 * formateador2 = new SimpleDateFormat("dd/MM/yyyy");
			 * 
			 * Date date2 = parseador2.parse(appointment.getScheduledDate());//
			 * ("31-03-2016"); String dateString2 = formateador2.format(date2);
			 * 
			 * Customer customer = new Customer();
			 * customer.setDocumentNumber(provision.getCustomer().getDocumentNumber());
			 * customer.setDocumentType(provision.getCustomer().getDocumentType());
			 * ScheduleRequest scheduleRequest = new ScheduleRequest();
			 * scheduleRequest.setBucket(provision.getWorkZone());
			 * scheduleRequest.setPilot(false); //
			 * scheduleRequest.setOrderCode(provision.getXaRequest());
			 * scheduleRequest.setXaOrderCode(provision.getXaRequest());
			 * scheduleRequest.setRequestId(provision.getIdProvision());
			 * scheduleRequest.setRequestType(provision.getActivityType());
			 * scheduleRequest.setSelectedDate(dateString2);
			 * scheduleRequest.setSelectedRange(range);
			 * scheduleRequest.setStpsiCode(getXaIdSt);
			 * scheduleRequest.setCustomer(customer);
			 * 
			 * scheduleRequest.setDocumentNumber(provision.getCustomer().getDocumentNumber()
			 * );
			 * scheduleRequest.setDocumentType(provision.getCustomer().getDocumentType());
			 * scheduleRequest.setOrderCode(provision.getXaRequest());
			 * scheduleRequest.setBucket(provision.getWorkZone());
			 * 
			 * // Actualiza el agendamiento
			 * trazabilidadScheduleApi.updateSchedule(scheduleRequest);
			 * 
			 * return true; }
			 */

			if (request.getStatus().equalsIgnoreCase(Status.WO_NOTDONE.getStatusName())
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

	private boolean getCarrier(String phoneNumber) {

		boolean isMovistar = false;
		if (!phoneNumber.trim().equals("")) {
			String switchOnPremise = System.getenv("TDP_SWITCH_ON_PREMISE");
			if (switchOnPremise.equals("true")) {
				isMovistar = restPSI.getCarrier(phoneNumber);
			} else {
				isMovistar = restPSI.getCarrierOld(phoneNumber);
			}
		}
		return isMovistar;
	}

	@Override
	public Provision getProvisionBySaleCode(String saleCode) {

		Provision provision = provisionRepository.getProvisionBySaleCode(saleCode);

		if (provision != null) {

			return provision;
		}
		return null;
	}

	@Override
	public boolean getBucketByProduct(String channel, String product, String bucket) throws Exception {
		try {
			boolean responseBucket = restPSI.getBucketByProduct(bucket, product, channel);

			return responseBucket;

		} catch (Exception e) {
			log.error(this.getClass().getName() + " - Exception: " + e.getMessage());

			throw e;
		}
	}

	@Override
	public ApiResponse<List<Contacts>> getContactList(String provisionId) {
		Optional<Provision> optional = provisionRepository.getStatus(provisionId);
		ApiResponse<List<Contacts>> response = new ApiResponse<List<Contacts>>();
		ResponseHeader header = new ResponseHeader();

		if (optional.isPresent()) {
			Provision provision = optional.get();

			if (provision.getContacts() != null && provision.getContacts().size() > 0) {
				if (!provision.getCustomer().getPhoneNumber().equals(provision.getContacts().get(0).getPhoneNumber())) {
					for (Contacts cont : provision.getContacts()) {
						cont.setHolder(false);
					}

					Contacts contacts = new Contacts();
					contacts.setCarrier(provision.getCustomer().getCarrier());
					contacts.setFullName(provision.getCustomer().getName());
					contacts.setPhoneNumber(provision.getCustomer().getPhoneNumber());
					contacts.setHolder(true);
					provision.getContacts().add(contacts);
				}
			} else {
				List<Contacts> lContacts = new ArrayList<Contacts>();
				Contacts contacts = new Contacts();
				contacts.setCarrier(provision.getCustomer().getCarrier());
				contacts.setFullName(provision.getCustomer().getName());
				contacts.setPhoneNumber(provision.getCustomer().getPhoneNumber());
				contacts.setHolder(true);
				lContacts.add(contacts);
				provision.setContacts(lContacts);
			}

			header.setResultCode(HttpStatus.OK.name());
			response.setHeader(header);
			response.setBody(provision.getContacts());
		} else {
			header.setResultCode(HttpStatus.OK.name());
			response.setHeader(header);
		}

		return response;
	}

	@Override
	public List<Provision> getOrderToNotify() {
		Optional<List<Provision>> optional = provisionRepository.getOrderToNotify();
		if (optional.isPresent()) {
			// Insertar lógica para wo_cancel
			List<Provision> listita = new ArrayList<Provision>();
			listita = optional.get();
			// Actualiza Flag y Date de envio Notify en BD
			provisionRepository.updateFlagDateNotify(optional.get());

			for (int i = 0; i < listita.size(); i++) {
				List<StatusLog> list = listita.get(i).getLogStatus();

				// remove provision by status cancelled
				List<StatusLog> listCacelled = list.stream()
						.filter(x -> Status.CANCEL.getStatusName().equals(x.getStatus())).collect(Collectors.toList());

				if (listCacelled.size() > 0) {
					listita.remove(i);
				}

				// remove provision by status caido
				List<StatusLog> listCaido = list.stream().filter(x -> Status.CAIDA.getStatusName().equals(x.getStatus())
						&& Status.WO_CANCEL.getStatusName().equals(x.getStatus())).collect(Collectors.toList());

				if (listCaido.size() > 0) {
					listita.remove(i);
				}

			}
			return listita;
		}

		return null;
	}

	@Override
	public boolean updateShowLocation(Provision provision) {
		return provisionRepository.updateShowLocation(provision);
	}

	private boolean validateFictitiousSchedule(List<StatusLog> listStatus) {

		for (StatusLog statusLog : listStatus) {
			if (statusLog.getStatus().equalsIgnoreCase(Status.FICTICIOUS_SCHEDULED.getStatusName())) {
				return true;
			}
		}

		return false;
	}

	private pe.telefonica.provision.model.Status getInfoStatus(String statusName,
			List<pe.telefonica.provision.model.Status> statusList) {
		pe.telefonica.provision.model.Status localStatus = null;

		if (statusList == null) {
			Optional<pe.telefonica.provision.model.Status> repoStatus = provisionRepository.getInfoStatus(statusName);
			localStatus = repoStatus.get();
		} else {
			for (pe.telefonica.provision.model.Status status : statusList) {
				if (status.getStatusName().equalsIgnoreCase(statusName)) {
					localStatus = status;
				}
			}
		}

		return localStatus;
	}

	@Override
	public List<Provision> getUpFrontProvisions() {
		List<Provision> provisions = new ArrayList<>();
		Optional<List<Provision>> optProvisions = provisionRepository.getUpFrontProvisionsOnDay();

		if (optProvisions.isPresent()) {
			provisions = optProvisions.get();

			provisionRepository.updateUpFrontProvisionRead(provisions);

			for (int i = 0; i < provisions.size(); i++) {
				List<StatusLog> listPaid = provisions.get(i).getLogStatus().stream()
						.filter(x -> Status.PAGADO.getStatusName().equals(x.getStatus())).collect(Collectors.toList());

				if (listPaid.size() > 0) {
					provisions.remove(i);
				}
			}
		}

		return provisions;
	}

	private void sendEmailToCustomer(Customer objCustomer, WoPreStart objWoPreStart) {
		ArrayList<MailParameter> mailParameters = new ArrayList<MailParameter>();
		String customerFullName = objCustomer.getName();

		MailParameter mailParameter1 = new MailParameter();
		mailParameter1.setParamKey("SHORTNAME");
		if (customerFullName.trim().length() > 0) {
			String[] customerFullNameArrStr = customerFullName.split(" ");
			mailParameter1.setParamValue(customerFullNameArrStr[0]);
		} else {
			mailParameter1.setParamValue("");
		}
		mailParameters.add(mailParameter1);

		MailParameter mailParameter2 = new MailParameter();
		mailParameter2.setParamKey("EMAIL");
		mailParameter2.setParamValue(objCustomer.getMail());
		mailParameters.add(mailParameter2);

		MailParameter mailParameter3 = new MailParameter();
		mailParameter3.setParamKey("TECNICNAME");
		mailParameter3.setParamValue(objWoPreStart.getFullName());
		mailParameters.add(mailParameter3);

		MailParameter mailParameter4 = new MailParameter();
		mailParameter4.setParamKey("TECNICID");
		mailParameter4.setParamValue(objWoPreStart.getDocumentNumber());
		mailParameters.add(mailParameter4);

		mailParameter4 = new MailParameter();
		mailParameter4.setParamKey("TECNICDOCTYPE");
		mailParameter4.setParamValue(objWoPreStart.getDocumentNumber().length() == 8 ? "DNI" : "N° DOCUMENTO");
		mailParameters.add(mailParameter4);

		MailParameter mailParameter5 = new MailParameter();
		mailParameter5.setParamKey("SCHEDULEORDER");
		mailParameter5.setParamValue(provisionTexts.getWebUrl());
		mailParameters.add(mailParameter5);

		trazabilidadSecurityApi.sendMail("192826", mailParameters.toArray(new MailParameter[mailParameters.size()]));
	}

	private boolean isAValidSchedule(String scheduleDate) {
		LocalDate lScheduleDate = LocalDate.parse(scheduleDate);
		LocalDate today = LocalDate.now(ZoneOffset.of("-05:00"));

		return lScheduleDate.compareTo(today) > 0;
	}

}