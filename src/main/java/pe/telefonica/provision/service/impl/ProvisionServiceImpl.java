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
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.telefonica.provision.model.Toolbox;
import pe.telefonica.provision.repository.ToolboxRepository;
import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.common.ApiResponse;
import pe.telefonica.provision.controller.common.ResponseHeader;
import pe.telefonica.provision.controller.request.ApiTrazaSetContactInfoUpdateRequest;
import pe.telefonica.provision.controller.request.CancelRequest;
import pe.telefonica.provision.controller.request.ContactRequest;
import pe.telefonica.provision.controller.request.GetProvisionByOrderCodeRequest;
import pe.telefonica.provision.controller.request.InsertCodeFictionalRequest;
import pe.telefonica.provision.controller.request.InsertOrderRequest;
import pe.telefonica.provision.controller.request.MailRequest.MailParameter;
import pe.telefonica.provision.controller.request.ProvisionRequest;
import pe.telefonica.provision.controller.request.SMSByIdRequest.Contact;
import pe.telefonica.provision.controller.request.SMSByIdRequest.Message.MsgParameter;
import pe.telefonica.provision.controller.response.ProvisionHeaderResponse;
import pe.telefonica.provision.controller.response.ProvisionResponse;
import pe.telefonica.provision.dto.ComponentsDto;
import pe.telefonica.provision.dto.ProvisionCustomerDto;
import pe.telefonica.provision.dto.ProvisionDetailTrazaDto;
import pe.telefonica.provision.dto.ProvisionDto;
import pe.telefonica.provision.dto.ProvisionTrazaDto;
import pe.telefonica.provision.external.BOApi;
import pe.telefonica.provision.external.NmoApi;
import pe.telefonica.provision.external.PSIApi;

import pe.telefonica.provision.external.ScheduleApi;
import pe.telefonica.provision.external.TrazabilidadScheduleApi;
import pe.telefonica.provision.external.TrazabilidadSecurityApi;
import pe.telefonica.provision.external.request.ScheduleUpdateFicticiousRequest;
import pe.telefonica.provision.model.Contacts;
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.model.HomePhone;
import pe.telefonica.provision.model.Internet;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.Provision.StatusLog;
import pe.telefonica.provision.model.Queue;
import pe.telefonica.provision.model.Television;
import pe.telefonica.provision.model.UpFront;
import pe.telefonica.provision.model.provision.Configurada;
import pe.telefonica.provision.model.provision.Notifications;
import pe.telefonica.provision.model.provision.PendienteDeAprobacion;
import pe.telefonica.provision.model.provision.PendienteDeValidacion;
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

	@Autowired
	private BOApi bOApi;

	@Autowired
	private PSIApi restPSI;
	
	@Autowired
	private NmoApi nmoPSI;

	@Autowired
	private TrazabilidadSecurityApi trazabilidadSecurityApi;

	@Autowired
	private TrazabilidadScheduleApi trazabilidadScheduleApi;
	
	@Autowired
	private ScheduleApi scheduleApi;
	
	@Autowired
	private ProvisionRepository provisionRepository;

	@Autowired
	private ToolboxRepository toolboxRepository;
	
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

		String getData[] = request.getData().split("\\|");

		Provision provisionx = null;

		Boolean indicador = Boolean.FALSE;
		if (request.getDataOrigin().equalsIgnoreCase("ATIS")) {
			provisionx = provisionRepository.getProvisionByXaRequest(getData[1]);
		} else {
			provisionx = provisionRepository.getProvisionBySaleCode(getData[2]);
			if (provisionx == null && !getData[11].equals("")) {
				provisionx = provisionRepository.getByOrderCodeForUpdate(getData[11]);
				indicador = Boolean.TRUE;
			}
		}

		if (request.getDataOrigin().equalsIgnoreCase("ORDENES")) {
			return false;
		}

		Optional<List<pe.telefonica.provision.model.Status>> statusListOptional = provisionRepository
				.getAllInfoStatus();
		List<pe.telefonica.provision.model.Status> statusList = statusListOptional.get();
		String speech = "";

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

				if (!indicador) {
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
				} else {
					status = provisionx.getActiveStatus();
					request.setStatus(provisionx.getLastTrackingStatus());
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
							Constants.ACTIVITY_TYPE_PROVISION.toLowerCase(), provisionx.getDummyStPsiCode(), true,
							"PSI", "", "", "", ""));
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
	public ProvisionDetailTrazaDto requestAddressUpdate(String provisionId) {
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
				return sent ? new ProvisionDetailTrazaDto().fromProvision(provision) : null;
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
	public ProvisionDetailTrazaDto orderCancellation(String provisionId, String cause, String detail,
			String scheduler) {

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

			if (scheduler.equalsIgnoreCase("PSI")) {

				sentBOCancellation = bOApi.sendRequestToBO(provision, "4");

				if (!sentBOCancellation) {
					return null;
				}
			}

			if (provision.getHasSchedule()) {
				scheduleUpdated = trazabilidadScheduleApi.updateCancelSchedule(
						new CancelRequest(provision.getIdProvision(), "provision", provision.getXaIdSt(), false,
								scheduler, "CANCELACIÓN SOLICITADA POR USUARIO", "CC001", "TRAZA", "UserTraza"));
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

			return new ProvisionDetailTrazaDto().fromProvision(provision);
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
	public List<ProvisionCustomerDto> getAllResendNotification(LocalDateTime startDate, LocalDateTime endDate) {
		Optional<List<ProvisionCustomerDto>> optional = provisionRepository.getAllResendNotification(startDate,
				endDate);

		// Agrego registro al objecto resend_intoa
		provisionRepository.updateResendNotification(optional.get());

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
	public ProvisionDetailTrazaDto setContactInfoUpdate(ApiTrazaSetContactInfoUpdateRequest request) throws Exception {
		Provision provision = provisionRepository.getProvisionByXaIdSt(request.getPsiCode());

		PSIUpdateClientRequest psiRequest = new PSIUpdateClientRequest();
		int count = 0;
		int maxTries = 2;

		while (count < maxTries) {
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

					return new ProvisionDetailTrazaDto().fromProvision(provision);

				} else {
					return null;
				}
			} catch (Exception e) {
				if (++count == maxTries) {
					throw e;
				}
			}
		}

		throw new Exception("Maxima cantidad de intentos permitidos");
	}
	
	@Override
	public ProvisionDetailTrazaDto setContactInfoUpdateWeb(ApiTrazaSetContactInfoUpdateRequest request) throws Exception {
		Provision provision = provisionRepository.getProvisionByXaIdSt(request.getPsiCode());

		PSIUpdateClientRequest psiRequest = new PSIUpdateClientRequest();
		int count = 0;
		int maxTries = 2;

		while (count < maxTries) {
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
					
					String switchAgendamiento = System.getenv("TDP_SWITCH_AGENDAMIENTO");
					boolean updatedPsi = false;
					if (request.getScheduler().toUpperCase().equals("PSI")) {						
						if (switchAgendamiento.equals("false")) {
							updatedPsi = restPSI.updatePSIClient(psiRequest);
						} else {
							updatedPsi = scheduleApi.modifyWorkOrderPSI(psiRequest);
						}					
					} else {						
						updatedPsi = scheduleApi.modifyWorkOrder(psiRequest);					
					}
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

					return new ProvisionDetailTrazaDto().fromProvision(provision);

				} else {
					return null;
				}
			} catch (Exception e) {
				if (++count == maxTries) {
					throw e;
				}
			}
		}
		
		throw new Exception("Maxima cantidad de intentos permitidos");
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

	@Override
	public ProvisionDetailTrazaDto getProvisionDetailById(ProvisionRequest request) {
		Provision provision = provisionRepository.getProvisionDetailById(request.getIdProvision());
		if (provision.getLastTrackingStatus().equals(Constants.STATUS_WO_PRESTART)) {			
			if (provision.getWoPreStart() != null) {
				String xaRequest = "";
				if (provision.getXaRequest() != null) {
					xaRequest = provision.getXaRequest();				
				} else {
					xaRequest = provision.getXaIdSt();
				}				
				Optional<Toolbox> optional = toolboxRepository.getLog(
						provision.getCustomer().getDocumentType(),
						provision.getCustomer().getDocumentNumber(),
						xaRequest,
						provision.getWoPreStart().getTrackingUrl());
				if (!optional.isPresent()) {
					Toolbox objToolbox = new Toolbox();
					objToolbox.setXaRequest(xaRequest);
					objToolbox.setDocumentType(provision.getCustomer().getDocumentType());
					objToolbox.setDocumentNumber(provision.getCustomer().getDocumentNumber());
					objToolbox.setPhoneNumber(provision.getCustomer().getPhoneNumber());
					objToolbox.setCarrier(provision.getCustomer().getCarrier());
					objToolbox.setUrl("");
					objToolbox.setChart(Boolean.FALSE);
					if (provision.getWoPreStart().getTrackingUrl() != null) {
						if (!provision.getWoPreStart().getTrackingUrl().equals("")) {
							objToolbox.setChart(Boolean.TRUE);
							objToolbox.setUrl(provision.getWoPreStart().getTrackingUrl());
						}
					}
					toolboxRepository.insertLog(objToolbox);
				} else if (provision.getWoPreStart().getTrackingUrl() != null) {
					if (!provision.getWoPreStart().getTrackingUrl().equals(optional.get().getUrl())) {				
						Toolbox objToolbox = new Toolbox();
						objToolbox.setXaRequest(xaRequest);
						objToolbox.setDocumentType(provision.getCustomer().getDocumentType());
						objToolbox.setDocumentNumber(provision.getCustomer().getDocumentNumber());
						objToolbox.setPhoneNumber(provision.getCustomer().getPhoneNumber());
						objToolbox.setCarrier(provision.getCustomer().getCarrier());
						objToolbox.setUrl("");
						objToolbox.setChart(Boolean.FALSE);
						if (provision.getWoPreStart().getTrackingUrl() != null) {
							if (!provision.getWoPreStart().getTrackingUrl().equals("")) {
								objToolbox.setChart(Boolean.TRUE);
								objToolbox.setUrl(provision.getWoPreStart().getTrackingUrl());
							}
						}
						toolboxRepository.insertLog(objToolbox);
					}
				}
			}
		}
		return new ProvisionDetailTrazaDto().fromProvision(provision);
	}

	@Override
	public ProvisionDetailTrazaDto updateActivity(String idProvision,  String activityId, String indicador) {
		ProvisionDetailTrazaDto provision = new ProvisionDetailTrazaDto();
		try {
			if (indicador.equals("0")) {
				String tokenExternal = trazabilidadSecurityApi.gerateTokenAzure();
				boolean resultado = nmoPSI.updateActivity(activityId, tokenExternal);
				if (resultado) {
					// Llamar al segundo servicio
					resultado = nmoPSI.serviceRequest(activityId, tokenExternal);
					if (resultado) {
						// Actualizar la provision con el nuevo estado
						provision = updateProvisionActivity(idProvision, Status.WO_PRENOTDONE_TRAZA, Constants.PROVISION_STATUS_PRENOTDONE_TRAZA); 
					}
				}
			} else if (indicador.equals("1")) {
				provision = updateProvisionActivity(idProvision, Status.WO_NOTDONE_TRAZA, Constants.PROVISION_STATUS_NOTDONE_TRAZA);
			}
		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());
		}
		return provision;
	}	
	
	private ProvisionDetailTrazaDto updateProvisionActivity(String idProvision, Status status, String statusProvision) {
		Optional<List<pe.telefonica.provision.model.Status>> statusListOptional = provisionRepository
				.getAllInfoStatus();

		Provision provision = provisionRepository.getProvisionDetailById(idProvision);
		
		List<pe.telefonica.provision.model.Status> statusList = statusListOptional.get();
		pe.telefonica.provision.model.Status notDoneStatus = getInfoStatus(status.getStatusName(),
				statusList);

		List<StatusLog> listLog = provision.getLogStatus();

		Update update = new Update();
		update.set("active_status", statusProvision);
		StatusLog statusLog = new StatusLog();
		statusLog.setStatus(status.getStatusName());
		statusLog.setXaidst(provision.getXaIdSt());
		listLog.add(statusLog);
		
		String speech = notDoneStatus != null ? notDoneStatus.getGenericSpeech() : status.getGenericSpeech();
		
		speech = hasCustomerInfo(provision.getCustomer()) ? speech.replace(Constants.TEXT_NAME_REPLACE, provision.getCustomer().getName().split(" ")[0]) : speech;
		update.set("last_tracking_status", status.getStatusName());
		update.set("generic_speech", speech);
		update.set("description_status", notDoneStatus != null ? notDoneStatus.getDescription() : status.getDescription());
		update.set("front_speech", notDoneStatus != null ? notDoneStatus.getFront() : status.getFrontSpeech());
		update.set("log_status", listLog);
		update.set("statusChangeDate", LocalDateTime.now(ZoneOffset.of("-05:00")));		
		update.set("sub_reason_not_done", speech);		
		
		provisionRepository.updateProvision(provision, update);
		
		provision = provisionRepository.getProvisionDetailById(idProvision);
		return new ProvisionDetailTrazaDto().fromProvision(provision);
	}
}