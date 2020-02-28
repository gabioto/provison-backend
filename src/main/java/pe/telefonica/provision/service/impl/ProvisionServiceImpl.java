package pe.telefonica.provision.service.impl;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import pe.telefonica.provision.controller.request.MailRequest.MailParameter;
import pe.telefonica.provision.controller.request.ProvisionRequest;
import pe.telefonica.provision.controller.request.SMSByIdRequest.Contact;
import pe.telefonica.provision.controller.request.SMSByIdRequest.Message.MsgParameter;
import pe.telefonica.provision.controller.request.ScheduleNotDoneRequest;
import pe.telefonica.provision.controller.request.ScheduleRequest;
import pe.telefonica.provision.controller.request.UpdateFromToaRequest;
import pe.telefonica.provision.controller.response.ProvisionHeaderResponse;
import pe.telefonica.provision.controller.response.ProvisionResponse;
import pe.telefonica.provision.dto.ComponentsDto;
import pe.telefonica.provision.external.BOApi;
import pe.telefonica.provision.external.PSIApi;
import pe.telefonica.provision.external.TrazabilidadScheduleApi;
import pe.telefonica.provision.external.TrazabilidadSecurityApi;
import pe.telefonica.provision.external.request.ScheduleUpdateFicticiousRequest;
import pe.telefonica.provision.external.response.BucketBodyResponse.OrigenBean;
import pe.telefonica.provision.external.response.ResponseBucket;
import pe.telefonica.provision.model.Contacts;
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.model.HomePhone;
import pe.telefonica.provision.model.Internet;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.Provision.StatusLog;
import pe.telefonica.provision.model.Queue;
import pe.telefonica.provision.model.Television;
import pe.telefonica.provision.model.provision.InToa;
import pe.telefonica.provision.model.provision.WoCancel;
import pe.telefonica.provision.model.provision.WoCompleted;
import pe.telefonica.provision.model.provision.WoInit;
import pe.telefonica.provision.model.provision.WoNotdone;
import pe.telefonica.provision.model.provision.WoPreStart;
import pe.telefonica.provision.model.provision.WoReshedule;
import pe.telefonica.provision.repository.ProvisionRepository;
import pe.telefonica.provision.service.ProvisionService;
import pe.telefonica.provision.service.request.PSIUpdateClientRequest;
import pe.telefonica.provision.util.constants.Constants;
import pe.telefonica.provision.util.constants.ConstantsLogData;
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

		/*
		 * if (provisionRequest.getHeader().getAppName().equals(
		 * "APP_WEB_FRONT_TRAZABILIDAD")) { provision =
		 * provisionRepository.getOrderTraza(provisionRequest.getBody().getDocumentType(
		 * ), provisionRequest.getBody().getDocumentNumber()); } else { provision =
		 * provisionRepository.getOrder(provisionRequest.getBody().getDocumentType(),
		 * provisionRequest.getBody().getDocumentNumber()); }
		 * 
		 * if (!provision.isPresent() &&
		 * provisionRequest.getBody().getDocumentType().equals("CE")) { provision =
		 * provisionRepository.getOrder("CEX",
		 * provisionRequest.getBody().getDocumentNumber()); }
		 * 
		 * if (!provision.isPresent() &&
		 * provisionRequest.getBody().getDocumentType().equalsIgnoreCase("PASAPORTE")) {
		 * provision = provisionRepository.getOrder("PAS",
		 * provisionRequest.getBody().getDocumentNumber()); }
		 */

		provision = provisionRepository.getOrder(provisionRequest.getBody().getDocumentType(),
				provisionRequest.getBody().getDocumentNumber());

		if (provision.isPresent() && provision.get().getCustomer() != null) {

			Provision prov = provision.get();
			prov.getCustomer().setProductName(prov.getProductName());
			return prov.getCustomer();

			/*
			 * prov.getCustomer().setProductName(prov.getProductName());
			 * header.setCode(HttpStatus.OK.value()).setMessage(HttpStatus.OK.name());
			 * response.setHeader(header).setData(prov.getCustomer());
			 */

		} else {
			return null;

			/*
			 * header.setCode(HttpStatus.OK.value()).
			 * setMessage("No se encontraron datos del cliente");
			 * response.setHeader(header);
			 */
		}

		// return response;
	}

	@Override
	public List<Provision> getAll(ApiRequest<ProvisionRequest> provisionRequest) {

		Optional<List<Provision>> provisions;
		// List<Provision> provisionList;

		/*
		 * if (provisionRequest.getHeader().getAppName().equals(
		 * "APP_WEB_FRONT_TRAZABILIDAD")) { provisions =
		 * provisionRepository.findAllTraza(provisionRequest.getBody().getDocumentType()
		 * , provisionRequest.getBody().getDocumentNumber()); } else { provisions =
		 * provisionRepository.findAll(provisionRequest.getBody().getDocumentType(),
		 * provisionRequest.getBody().getDocumentNumber()); }
		 * 
		 * if (provisions.get().size() == 0 &&
		 * provisionRequest.getBody().getDocumentType().equals("CE")) { provisions =
		 * provisionRepository.findAll("CEX",
		 * provisionRequest.getBody().getDocumentNumber()); }
		 * 
		 * if (provisions.get().size() == 0 &&
		 * provisionRequest.getBody().getDocumentType().equals("PASAPORTE")) {
		 * provisions = provisionRepository.findAll("PAS",
		 * provisionRequest.getBody().getDocumentNumber()); }
		 */

		provisions = provisionRepository.findAll(provisionRequest.getBody().getDocumentType(),
				provisionRequest.getBody().getDocumentNumber());

		if (provisions.isPresent() && provisions.get().size() > 0) {
			/*
			 * provisionList = provisions.get();
			 * 
			 * for (Provision provision : provisionList) {
			 * evaluateProvisionComponents(provision); } return provisionList;
			 */
			return provisions.get();
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

		} catch (

		Exception e) {
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

	@Override
	public List<Provision> insertProvisionList(List<Provision> provisionList) {
		// EAPR: agregada validacion de registros previos (por cod de peticion) para
		// actualizarlos en caso de nuevos ST
		if (provisionList.isEmpty()) {
			return null;
		}

		log.info("insertProvisionList: " + provisionList);

		List<Provision> resultList = new ArrayList<Provision>();

		for (Provision newProvision : provisionList) {
			Optional<Provision> optional = provisionRepository.getProvisionByXaRequest(newProvision.getXaRequest());

			if (!optional.isPresent()) {
				provisionRepository.insertProvision(newProvision);
				resultList.add(newProvision);
			} else {
				Provision oldProvision = optional.get();
				if (newProvision.getXaRequest().equals(oldProvision.getXaRequest())) {
					String oldSt = oldProvision.getXaIdSt();
					oldProvision.setXaIdSt(newProvision.getXaIdSt());

					if (provisionRepository.resetProvision(oldProvision)) {
						// Enviar al log el cambio
						trazabilidadSecurityApi.saveLogData("", "", "", "", "UPDATE", "oldST = " + oldSt,
								"newST = " + newProvision.getXaIdSt(), ConstantsLogData.PROVISION_UPDATE_ST, "", "",
								"");
					}
				}
				resultList.add(oldProvision);
			}
		}

		return resultList;
	}

	private Provision fillProvisionInsert(InsertOrderRequest request) {

		String[] getData = request.getData().split("\\|", -1);
		Provision provision = new Provision();
		String speech = "";
		System.out.println("INSERT NEW PROVISION");
		System.out.println(getData[4]);

		provision.setSaleSource(getData[0]);
		provision.setBack(getData[1]);
		provision.setSaleCode(getData[2]);
		provision.setProductName(getData[10]);

		provision.setXaRequest(getData[11]);
		// provision.setXaIdSt("");
		// provision.setDummyStPsiCode("");
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
		provision.setActiveStatus(Status.PENDIENTE.getStatusName().toLowerCase());
		provision.setStatusToa(Status.PENDIENTE.getStatusName().toLowerCase());

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

		System.out.println(getData[23]);

		System.out.println(getData[24]);

		customer.setLongitude(getData[23]);
		customer.setLatitude(getData[24]);
		customer.setOriginData(request.getDataOrigin());

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

		provision.setCustomer(customer);

		speech = hasCustomerInfo(customer)
				? Status.PENDIENTE.getSpeechWithoutSchedule().replace(Constants.TEXT_NAME_REPLACE,
						customer.getName().split(" ")[0])
				: Status.PENDIENTE.getSpeechWithoutSchedule();

		provision.setLastTrackingStatus(Status.PENDIENTE.getStatusName());
		provision.setGenericSpeech(speech);
		provision.setDescriptionStatus(Status.PENDIENTE.getDescription());

		List<StatusLog> listLog = new ArrayList<>();
		StatusLog statusLog = new StatusLog();
		statusLog.setStatus(Status.PENDIENTE.getStatusName());

		listLog.add(statusLog);

		if (!request.getStatus().equalsIgnoreCase(Status.PENDIENTE.getStatusName())) {
			StatusLog statusLogCurrent = new StatusLog();
			statusLogCurrent.setStatus(request.getStatus());

			if (request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName())) {
				speech = hasCustomerInfo(customer) ? Status.INGRESADO.getSpeechWithoutSchedule()
						.replace(Constants.TEXT_NAME_REPLACE, customer.getName().split(" ")[0])
						: Status.INGRESADO.getSpeechWithoutSchedule();
				provision.setDescriptionStatus(Status.INGRESADO.getDescription());
				provision.setGenericSpeech(speech);
			} else {
				provision.setDescriptionStatus(Status.CAIDA.getDescription());
				provision.setGenericSpeech(Status.CAIDA.getGenericSpeech());
			}

			listLog.add(statusLogCurrent);

			provision.setRegisterDateUpdate(LocalDateTime.now(ZoneOffset.of("-05:00")));
			provision.setLastTrackingStatus(request.getStatus());

			provision.setActiveStatus(request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName())
					? Status.INGRESADO.getStatusName().toLowerCase()
					: Constants.PROVISION_STATUS_CAIDA);

			provision.setStatusToa(request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName())
					? Status.INGRESADO.getStatusName().toLowerCase()
					: Constants.PROVISION_STATUS_CAIDA);
		}

		provision.setLogStatus(listLog);

		return provision;
	}

	private boolean hasCustomerInfo(Customer customer) {
		return customer != null && customer.getName() != null && !customer.getName().isEmpty();
	}

	private Update fillProvisionUpdate(InsertOrderRequest request) {
		String getData[] = request.getData().split("\\|", -1);
		System.out.println(getData[3]);

		// Provision provision = new Provision();

		Update update = new Update();

		update.set("register_date_update", LocalDateTime.now(ZoneOffset.of("-05:00")));

		update.set("sale_source", getData[0]);
		// provision.setBack(getData[1]);
		update.set("back", getData[1]);
		// provision.setSaleCode(getData[2]);

		// provision.setProductName(getData[10]);
		update.set("product_name", getData[10]);

		// provision.setXaRequest(getData[11]);
		update.set("xa_request", getData[11]);
		update.set("origin_code", request.getDataOrigin());
		// provision.setCommercialOp(getData[12]);
		update.set("commercial_op", getData[12].toUpperCase());
		// provision.setProductCode(getData[14]);
		update.set("product_code", getData[14]);
		// provision.setProductNameSource(getData[15]);
		update.set("product_name_source", getData[15]);
		// provision.setKafkaDateSend(getData[17]);
		update.set("kafka_date_send", getData[17]);
		// provision.setSaleRequestDate(getData[18]);
		update.set("sale_request_date", getData[18]);
		// provision.setSaleRegisterDate(getData[19]);
		update.set("sale_register_date", getData[19]);
		// provision.setProductSub(getData[21]);
		update.set("product_sub", getData[21]);
		// provision.setProductType(getData[22]);
		update.set("product_type", getData[22]);
		// provision.setChannelEntered(getData[26]);
		update.set("channel_entered", getData[26]);
		// provision.setProtectedData(getData[27]);
		update.set("protected_data", getData[27]);
		// provision.setRegularPrice(getData[29]);
		update.set("regular_price", getData[29]);
		// provision.setPromoPrice(getData[30]);
		update.set("promo_price", getData[30]);
		// provision.setCampaign(getData[31]);
		update.set("campaign", getData[31]);
		// provision.setPaymentMethod(getData[34]);
		update.set("payment_method", getData[34]);
		// provision.setInstallPrice(getData[35]);
		update.set("install_price", getData[35]);
		// provision.setInstallPriceMonth(getData[36]);
		update.set("install_price_month", getData[36]);
		// provision.setProductInternalEquipment(getData[41]);
		update.set("product_internal_equipment", getData[41]);
		// provision.setLegacies(getData[42]);
		update.set("legacies", getData[42]);
		// provision.setProductSignal(getData[43]);
		update.set("product_signal", getData[43]);

		// provision.setActiveStatus("pendiente");

		List<String> productPsAdmin = new ArrayList<>();
		productPsAdmin.add(getData[44]);
		productPsAdmin.add(getData[45]);
		productPsAdmin.add(getData[46]);
		productPsAdmin.add(getData[47]);

		// provision.setProductPsAdmin(productPsAdmin);
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

		// provision.setSvaCode(svaCode);
		update.set("sva_code", svaCode);

		// Customer customer = new Customer();
		// customer.setName(getData[3]);
		update.set("customer.name", getData[3]);
		// customer.setDocumentType(getData[13]);
		update.set("customer.document_type", getData[13]);
		// customer.setDocumentNumber(getData[4]);
		update.set("customer.document_number", getData[4]);
		// customer.setPhoneNumber(getData[5]);
		update.set("customer.phone_number", getData[5]);
		// customer.setMail(getData[20]);
		update.set("customer.mail", getData[20]);
		update.set("customer.carrier", false);
		// customer.setAddress(getData[6]);
		update.set("customer.address", getData[6]);
		// customer.setDistrict(getData[9]);
		update.set("customer.district", getData[9]);
		// customer.setProvince(getData[8]);
		update.set("customer.province", getData[8]);
		// customer.setDepartment(getData[7]);
		update.set("customer.department", getData[7]);
		// customer.setLongitude(Double.parseDouble(getData[23]));
		update.set("customer.longitude", getData[23]);
		// customer.setLatitude(Double.parseDouble(getData[24]));
		update.set("customer.latitude", getData[24]);

		// Internet internet = new Internet();
		// internet.setSpeed(getData[25]);
		update.set("internet_detail.speed", getData[25]);
		// internet.setTimePromoSpeed(getData[32]);
		update.set("internet_detail.time_promo_speed", getData[32]);
		// internet.setPromoSpeed(getData[33]);
		update.set("internet_detail.promo_speed", getData[33]);
		// internet.setTechnology(getData[37]);
		update.set("internet_detail.technology", getData[37]);

		// provision.setInternetDetail(internet);

		// Television television = new Television();
		// television.setTechnology(getData[38]);
		update.set("television_detail.technology", getData[38]);
		// television.setTvSignal(getData[39]);
		update.set("television_detail.tv_signal", getData[39]);
		// television.setEquipment(getData[40]);
		update.set("television_detail.equipment", getData[40]);

		// provision.setTvDetail(television);

		// provision.setCustomer(customer);
		return update;
		// return null;
	}

	@Override
	public boolean insertProvision(InsertOrderRequest request) {

		String getData[] = request.getData().split("\\|");
		Provision provisionx = provisionRepository.getProvisionBySaleCode(getData[2]);
		String speech = "";

		if (provisionx != null) {
			System.out.println("SALE CODE ==>" + getData[2] );
			System.out.println("STATUS ==> " + request.getStatus());
			
			List<StatusLog> listLog = provisionx.getLogStatus();
			
			/*List<StatusLog> listIngresado = listLog.stream()
					.filter(items -> Status.INGRESADO.getStatusName().equals(items.getStatus()))
					.collect(Collectors.toList());
			List<StatusLog> listCaida = listLog.stream()
					.filter(items -> Status.CAIDA.getStatusName().equals(items.getStatus()))
					.collect(Collectors.toList());
			if (listIngresado.size() > 0) {
				System.out.println("INGRESADO REPETIDO");
				return false;
			}

			if (listCaida.size() > 0) {
				System.out.println("CAIDA REPETIDO ==>");
				return false;
			}*/

			Update update = fillProvisionUpdate(request);

			boolean hasFictitious = validateFictitiousSchedule(listLog);

			StatusLog statusLog = new StatusLog();
			statusLog.setStatus(request.getStatus());

			if (request.getStatus().equalsIgnoreCase(Status.PENDIENTE.getStatusName())) {
				provisionx.setDescriptionStatus(Status.PENDIENTE.getDescription());
				speech = hasFictitious ? Status.PENDIENTE.getGenericSpeech()
						: Status.PENDIENTE.getSpeechWithoutSchedule();
				speech = hasCustomerInfo(provisionx.getCustomer())
						? speech.replace(Constants.TEXT_NAME_REPLACE, provisionx.getCustomer().getName().split(" ")[0])
						: speech;
				provisionx.setGenericSpeech(speech);
			} else if (request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName())) {
				provisionx.setDescriptionStatus(Status.INGRESADO.getDescription());
				speech = hasFictitious ? Status.INGRESADO.getGenericSpeech()
						: Status.INGRESADO.getSpeechWithoutSchedule();
				speech = hasCustomerInfo(provisionx.getCustomer())
						? speech.replace(Constants.TEXT_NAME_REPLACE, provisionx.getCustomer().getName().split(" ")[0])
						: speech;
				provisionx.setGenericSpeech(speech);
			} else {
				provisionx.setDescriptionStatus(Status.CAIDA.getDescription());
				provisionx.setGenericSpeech(Status.CAIDA.getGenericSpeech());
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
					System.out.println("UPDATE SCHEDULE FICTITIOUS ==>");
					// Actualiza agenda
					if (!provisionx.getLastTrackingStatus().equals(Status.WO_CANCEL.getStatusName())) {

						boolean updateFicticious = trazabilidadScheduleApi.updateFicticious(updateFicRequest);
						update.set("is_update_dummy_st_psi_code", updateFicticious ? true : false);
					}

				}

			}

			// status_toa
			String status = request.getStatus().equalsIgnoreCase(Status.PENDIENTE.getStatusName())
					? Status.PENDIENTE.getStatusName().toLowerCase()
					: request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName())
							? Status.INGRESADO.getStatusName().toLowerCase()
							: Constants.PROVISION_STATUS_CAIDA;

			if (status.equalsIgnoreCase(Constants.PROVISION_STATUS_CAIDA) && provisionx.getDummyStPsiCode() != null) {

				ScheduleNotDoneRequest scheduleNotDoneRequest = new ScheduleNotDoneRequest();
				scheduleNotDoneRequest.setRequestId(provisionx.getIdProvision());
				scheduleNotDoneRequest.setRequestType("provision");
				scheduleNotDoneRequest.setStPsiCode(provisionx.getDummyStPsiCode());
				scheduleNotDoneRequest.setFlgFicticious(true);

				// Cancela agenda
				trazabilidadScheduleApi.cancelLocalSchedule(scheduleNotDoneRequest);
			}

			update.set("active_status", status);
			update.set("status_toa", status);
			update.set("send_notify", false);
			update.set("show_location", false);

			update.set("last_tracking_status", request.getStatus());
			update.set("description_status", provisionx.getDescriptionStatus());
			update.set("generic_speech", provisionx.getGenericSpeech());

			listLog.add(statusLog);
			update.set("log_status", listLog);

			// provisionx.setLogStatus(listLog);
			// Actualiza provision

			// Provision provision = fillProvisionInsert(request);
			provisionx = evaluateProvisionComponents(provisionx);

			Boolean isUpdate = provisionRepository.updateProvision(provisionx, update);
			return isUpdate ? true : false;

		} else {

			Provision provision = fillProvisionInsert(request);
			provision = evaluateProvisionComponents(provision);
			provisionRepository.insertProvision(provision);
			return true;
		}
		// return true;
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
							msgParameters.toArray(new MsgParameter[0]), "");
					// ApiResponse<SMSByIdResponse> apiResponse = sendSMS(provision.getCustomer(),
					// Constants.MSG_PRO_CANCELLED_BY_CUSTOMER_KEY, msgParameters.toArray(new
					// MsgParameter[0]), "");

					try {
						// provisionRepository.sendCancelledMail(provision, name, "179829",
						// Constants.ADDRESS_CANCELLED_BY_CUSTOMER);
						sendCancelledMail(provision, name, "192828");

					} catch (Exception e) {
						log.info(ProvisionServiceImpl.class.getCanonicalName() + ": " + e.getMessage());
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
						msgParameters.toArray(new MsgParameter[0]), "");

				// ApiResponse<SMSByIdResponse> apiResponse = sendSMS(provision.getCustomer(),
				// Constants.MSG_PRO_CUSTOMER_UNREACHABLE_KEY, msgParameters.toArray(new
				// MsgParameter[0]), "http://www.movistar.com.pe");

				try {
					// provisionRepository.sendCancelledMail(provision, name, "179824",
					// Constants.ADDRESS_UNREACHABLE);
					sendCancelledMail(provision, name, "192909");
				} catch (Exception e) {
					log.info(ProvisionServiceImpl.class.getCanonicalName() + ": " + e.getMessage());
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
//					List<MsgParameter> msgParameters = new ArrayList<>();
					List<Contact> contacts = new ArrayList<>();

					Contact contactCustomer = new Contact();
					contactCustomer.setPhoneNumber(provision.getCustomer().getPhoneNumber());
					contactCustomer.setIsMovistar(provision.getCustomer().getCarrier());
					contacts.add(contactCustomer);

//					ApiResponse<SMSByIdResponse> apiResponse = trazabilidadSecurityApi.sendSMS(contacts,
//							Constants.MSG_ADDRESS_UPDATED_KEY, msgParameters.toArray(new MsgParameter[0]),
//							provisionTexts.getWebUrl());

					// ApiResponse<SMSByIdResponse> apiResponse = sendSMS(provision.getCustomer(),
					// Constants.MSG_ADDRESS_UPDATED_KEY, msgParameters.toArray(new
					// MsgParameter[0]), provisionTexts.getWebUrl());
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

			StatusLog statusLog = new StatusLog();
			statusLog.setStatus(Status.CANCEL.getStatusName());

			provision.getLogStatus().add(statusLog);
			provision.setActiveStatus(Constants.PROVISION_STATUS_CANCELLED);
			provision.setLastTrackingStatus(Status.CANCEL.getStatusName());

			provision.setGenericSpeech(Status.CANCEL.getGenericSpeech());
			provision.setDescriptionStatus(Status.CANCEL.getDescription());

			provision.setCancellationCause(cause);
			provision.setCancellationDetail(detail);

			Update update = new Update();
			update.set("active_status", Constants.PROVISION_STATUS_CANCELLED);
			update.set("cancellation_cause", cause);
			update.set("cancellation_detail", detail);
			update.set("log_status", provision.getLogStatus());
			update.set("last_tracking_status", Status.CANCEL.getStatusName());

			update.set("description_status", Status.CANCEL.getDescription());
			update.set("generic_speech", Status.CANCEL.getGenericSpeech());

			sentBOCancellation = bOApi.sendRequestToBO(provision, "4");

			if (!sentBOCancellation) {
				return null;
			}

			if (provision.getHasSchedule()) {
				scheduleUpdated = trazabilidadScheduleApi.updateCancelSchedule(
						new CancelRequest(provision.getIdProvision(), "provision", provision.getXaIdSt()));
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
				// sendCancelledMailByUser(provision, Constants.ADDRESS_CANCELLED_BY_CUSTOMER);
			} catch (Exception e) {
				log.info(ProvisionServiceImpl.class.getCanonicalName() + ": " + e.getMessage());
			}

			/*
			 * String name = provision.getCustomer().getName().split(" ")[0];
			 * 
			 * List<MsgParameter> msgParameters = new ArrayList<>(); MsgParameter paramName
			 * = new MsgParameter(); paramName.setKey(Constants.TEXT_NAME_REPLACE);
			 * paramName.setValue(name);
			 * 
			 * MsgParameter paramProduct = new MsgParameter();
			 * paramProduct.setKey(Constants.TEXT_PRODUCT_REPLACE);
			 * paramProduct.setValue(provision.getProductName());
			 * 
			 * msgParameters.add(paramName); msgParameters.add(paramProduct);
			 * 
			 * List<Contact> contacts = new ArrayList<>();
			 * 
			 * Contact contactCustomer = new Contact();
			 * contactCustomer.setPhoneNumber(provision.getCustomer().getPhoneNumber());
			 * contactCustomer.setIsMovistar(provision.getCustomer().getCarrier());
			 * contacts.add(contactCustomer);
			 * 
			 * ApiResponse<SMSByIdResponse> apiResponse =
			 * trazabilidadSecurityApi.sendSMS(contacts,
			 * Constants.MSG_PRO_CANCELLED_BY_CUSTOMER_KEY, msgParameters.toArray(new
			 * MsgParameter[0]), "");
			 * 
			 * if
			 * (apiResponse.getHeader().getResultCode().equals(String.valueOf(HttpStatus.OK.
			 * value()))) { messageSent = true; } else { messageSent = false; } return
			 * messageSent ? provision : null;
			 */

			return provision;
		} else {
			return null;
		}
	}

	private Boolean sendContactInfoChangedMail(Provision provision) {
		ArrayList<MailParameter> mailParameters = new ArrayList<>();
		String customerFullName = provision.getCustomer().getName();

		if (provision.getCustomer().getMail() == null || provision.getCustomer().getMail().isEmpty()) {
			return false;
		}

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
		mailParameter2.setParamValue(provision.getCustomer().getMail());
		mailParameters.add(mailParameter2);

		for (int a = 0; a < provision.getContacts().size(); a++) {
			if (a == 0) {
				mailParameter2 = new MailParameter();
				mailParameter2.setParamKey("NOMBRE");
				mailParameter2.setParamValue(provision.getContacts().get(a).getFullName());
				mailParameters.add(mailParameter2);

				mailParameter2 = new MailParameter();
				mailParameter2.setParamKey("TELEFONO");
				mailParameter2.setParamValue(provision.getContacts().get(a).getPhoneNumber());
				mailParameters.add(mailParameter2);
			}

			if (a == 1) {
				mailParameter2 = new MailParameter();
				mailParameter2.setParamKey("NOMBRE_2");
				mailParameter2.setParamValue(provision.getContacts().get(a).getFullName());
				mailParameters.add(mailParameter2);

				mailParameter2 = new MailParameter();
				mailParameter2.setParamKey("TELEFONO_2");
				mailParameter2.setParamValue(provision.getContacts().get(a).getPhoneNumber());
				mailParameters.add(mailParameter2);
			}

			if (a == 2) {
				mailParameter2 = new MailParameter();
				mailParameter2.setParamKey("NOMBRE_3");
				mailParameter2.setParamValue(provision.getContacts().get(a).getFullName());
				mailParameters.add(mailParameter2);

				mailParameter2 = new MailParameter();
				mailParameter2.setParamKey("TELEFONO_3");
				mailParameter2.setParamValue(provision.getContacts().get(a).getPhoneNumber());
				mailParameters.add(mailParameter2);
			}

			if (a == 3) {
				mailParameter2 = new MailParameter();
				mailParameter2.setParamKey("NOMBRE_4");
				mailParameter2.setParamValue(provision.getContacts().get(a).getFullName());
				mailParameters.add(mailParameter2);

				mailParameter2 = new MailParameter();
				mailParameter2.setParamKey("TELEFONO_4");
				mailParameter2.setParamValue(provision.getContacts().get(a).getPhoneNumber());
				mailParameters.add(mailParameter2);
			}

		}

		MailParameter mailParameter5 = new MailParameter();
		mailParameter5.setParamKey("FOLLOWORDER");
		mailParameter5.setParamValue(provisionTexts.getWebUrl());
		mailParameters.add(mailParameter5);

		return trazabilidadSecurityApi.sendMail("193087", mailParameters.toArray(new MailParameter[0]));

		// return sendMail("179833", mailParameters.toArray(new MailParameter[0]));
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
				msgParameters.toArray(new MsgParameter[0]), "");
	}

	private Boolean sendCancelledMailByUser(Provision provision, String cancellationReason) {
		ArrayList<MailParameter> mailParameters = new ArrayList<>();
		String customerFullName = provision.getCustomer().getName();

		if (provision.getCustomer().getMail() == null || provision.getCustomer().getMail().isEmpty()) {
			return false;
		}

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
		mailParameter2.setParamValue(provision.getCustomer().getMail());
		mailParameters.add(mailParameter2);

		Calendar cal = Calendar.getInstance();

		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_EMAILING, new Locale("es", "ES"));
		sdf.setTimeZone(TimeZone.getTimeZone("GMT-5:00"));

		String scheduleDateStr = sdf.format(cal.getTime());

		MailParameter mailParameter4 = new MailParameter();
		mailParameter4.setParamKey("CANCELATIONDATE");
		mailParameter4.setParamValue(scheduleDateStr);
		mailParameters.add(mailParameter4);

		MailParameter mailParameter5 = new MailParameter();
		mailParameter5.setParamKey("STOREURL");
		mailParameter5.setParamValue("http://www.movistar.com.pe");
		mailParameters.add(mailParameter5);

		MailParameter mailParameter6 = new MailParameter();
		mailParameter6.setParamKey("PROVISIONNAME");
		mailParameter6.setParamValue(provision.getProductName());
		mailParameters.add(mailParameter6);

		return trazabilidadSecurityApi.sendMail("192828", mailParameters.toArray(new MailParameter[0]));
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
		ProvisionResponse<Boolean> response = new ProvisionResponse<Boolean>();
		ProvisionHeaderResponse header = new ProvisionHeaderResponse();
		try {
			if (optional.isPresent()) {
				Provision provision = optional.get();
				String nomEstado = "";
				String description = "";
				String speech = "";

				if (scheduledType == 2) {
					nomEstado = Status.FICTICIOUS_SCHEDULED.getStatusName();
					description = Status.FICTICIOUS_SCHEDULED.getDescription();
					speech = Status.FICTICIOUS_SCHEDULED.getGenericSpeech();
				} else {
					nomEstado = Status.SCHEDULED.getStatusName();
					description = Status.SCHEDULED.getDescription();
					speech = Status.SCHEDULED.getGenericSpeech();
				}

				speech = hasCustomerInfo(provision.getCustomer())
						? speech.replace(Constants.TEXT_NAME_REPLACE, provision.getCustomer().getName().split(" ")[0])
						: speech;

				boolean updated = updateTrackingStatus(provision.getXaRequest(), provision.getXaIdSt(), nomEstado, true,
						scheduledDate, scheduledRange, scheduledType, description, speech);

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
		Optional<List<Provision>> optional = provisionRepository.getAllInTimeRange(startDate, endDate.plusDays(1));

		if (optional.isPresent()) {
			return optional.get();
		}

		return null;
	}

	@Override
	public Boolean updateTrackingStatus(String xaRequest, String xaIdSt, String status, boolean comesFromSchedule,
			LocalDate scheduledDate, String scheduledRange, Integer scheduleType, String description, String speech) {
		boolean updated = false;
		Optional<Provision> optionalProvision = provisionRepository.getProvisionByXaRequestAndSt(xaRequest, xaIdSt);
		log.info(ProvisionServiceImpl.class.getCanonicalName() + " - updateTrackingStatus: xaRequest = " + xaRequest
				+ ", xaIdSt =" + xaIdSt + ", status = " + status);

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

			updated = provisionRepository.updateTrackingStatus(optionalProvision.get(), logStatus, description, speech,
					comesFromSchedule);
			log.info(ProvisionServiceImpl.class.getCanonicalName() + " - updateTrackingStatus: updated = " + updated);
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
					boolean isMovistar = restPSI.getCarrier(listContact.get(a).getPhoneNumber().toString());
					contacts.setCarrier(isMovistar);
					contactsList.add(contacts);

					if (a == 0) {
						psiRequest.getBodyUpdateClient().setNombre_completo(listContact.get(a).getFullName());
						psiRequest.getBodyUpdateClient().setTelefono1(listContact.get(a).getPhoneNumber().toString());
					}

					if (a == 1) {
						psiRequest.getBodyUpdateClient().setNombre_completo2(listContact.get(a).getFullName());
						psiRequest.getBodyUpdateClient().setTelefono2(listContact.get(a).getPhoneNumber().toString());
					}

					if (a == 2) {
						psiRequest.getBodyUpdateClient().setNombre_completo3(listContact.get(a).getFullName());
						psiRequest.getBodyUpdateClient().setTelefono3(listContact.get(a).getPhoneNumber().toString());
					}

					if (a == 3) {
						psiRequest.getBodyUpdateClient().setNombre_completo4(listContact.get(a).getFullName());
						psiRequest.getBodyUpdateClient().setTelefono4(listContact.get(a).getPhoneNumber().toString());
					}

				}

				psiRequest.getBodyUpdateClient().setSolicitud(provision.getXaIdSt());
				psiRequest.getBodyUpdateClient()
						.setCorreo(provision.getCustomer().getMail() != null ? provision.getCustomer().getMail() : "");

				System.out.println(provision.getCustomer().getMail());

				boolean updatedPsi = restPSI.updatePSIClient(psiRequest);

				if (updatedPsi) {
					Update update = new Update();
					update.set("contacts", request.isHolderWillReceive() ? null : contactsList);
					provisionRepository.updateProvision(provision, update);

					if (provision.getContacts() != null) {
						provision.getContacts().clear();
					}

					provision.setContacts(request.isHolderWillReceive() ? null : contactsList);

					// if (!request.isHolderWillReceive()) {
					// sendInfoUpdateSMS(provision);
					// sendContactInfoChangedMail(provision);
					// }
				} else {
					throw new Exception();
				}

				return provision;

			} else {
				return null;
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private void sendInfoUpdateSMS(Provision provision) {
		// ApiResponse<List<Contacts>> contactsResponse =
		// getContactList(provision.getIdProvision());
		List<Contact> contacts = new ArrayList<>();
		Contact holder = new Contact();
		holder.setIsMovistar(provision.getCustomer().getCarrier());
		holder.setFullName(provision.getCustomer().getName());
		holder.setHolder(true);
		holder.setPhoneNumber(provision.getCustomer().getPhoneNumber());
		contacts.add(holder);
		// contacts.addAll(SMSByIdRequest.mapContacts(provision.getContacts()));

		trazabilidadSecurityApi.sendSMS(contacts, Constants.MSG_CONTACT_UPDATED_KEY, null, provisionTexts.getWebUrl());
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

			/*
			 * Customer customer = provision.getCustomer() != null ? provision.getCustomer()
			 * : new Customer();
			 * 
			 * customer.setMail(request.getEmail());
			 * 
			 * provision.setCustomer(customer); provision.setContacts(contactsList);
			 */

			/*
			 * provision.getCustomer().setContactName(contactFullname);
			 * provision.getCustomer().setContactPhoneNumber(Integer.valueOf(
			 * contactCellphone));
			 * provision.getCustomer().setContactCarrier(contactCellphoneIsMovistar.toString
			 * ());
			 */

			// boolean contactUpdated = provisionRepository.updateContactInfoPsi(provision);

			restPSI.updatePSIClient(psiRequest);

			Update update = new Update();

			// update.set("customer.contact_name", contactName1);
			// update.set("customer.contact_name1", contactName2);
			// update.set("customer.contact_name2", contactName3);
			// update.set("customer.contact_name3", contactName4);

			update.set("customer.mail", request.getEmail());

			// update.set("customer.contact_phone_number", Integer.valueOf(contactPhone1));
			// update.set("customer.contact_phone_number1", Integer.valueOf(contactPhone2));
			// update.set("customer.contact_phone_number2", Integer.valueOf(contactPhone3));
			// update.set("customer.contact_phone_number3", Integer.valueOf(contactPhone4));

			update.set("contacts", contactsList);
			// update.set("customer.contact_carrier",
			// contactCellphoneIsMovistar.toString());

			provisionRepository.updateProvision(provision, update);

			return true;

		} else {

			return false;
		}
	}

	@Override
	public boolean provisionInsertCodeFictitious(InsertCodeFictionalRequest request) {

		Provision provision = provisionRepository.getProvisionBySaleCode(request.getSaleCode());
		String speech = "";

		if (provision != null) {
			Update update = new Update();
			List<StatusLog> listLog = provision.getLogStatus();

			speech = hasCustomerInfo(provision.getCustomer())
					? Status.FICTICIOUS_SCHEDULED.getGenericSpeech().replace(Constants.TEXT_NAME_REPLACE,
							provision.getCustomer().getName().split(" ")[0])
					: Status.FICTICIOUS_SCHEDULED.getGenericSpeech();

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
			update.set("description_status", Status.FICTICIOUS_SCHEDULED.getDescription());
			listLog.add(statusLog);
			update.set("log_status", listLog);

			provisionRepository.updateProvision(provision, update);

		} else {

			Provision provisionAdd = new Provision();

			provisionAdd.setSaleCode(request.getSaleCode());
			provisionAdd.setDummyXaRequest(request.getDummyXaRequest());
			provisionAdd.setDummyStPsiCode(request.getDummyStPsiCode());
			provisionAdd.setHasSchedule(true);
			provisionAdd.setOriginCode(request.getOriginCode());

			provisionAdd.setActiveStatus(Status.PENDIENTE.getStatusName().toLowerCase());
			provisionAdd.setStatusToa(Status.PENDIENTE.getStatusName().toLowerCase());

			provisionAdd.setProductName("Pedido Movistar");
			provisionAdd.setCommercialOp(request.getCommercialOp());
			provisionAdd.setActiveStatus(Status.PENDIENTE.getStatusName().toLowerCase());
			provisionAdd.setStatusToa(Status.PENDIENTE.getStatusName().toLowerCase());

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
			statusPendiente.setStatus(Status.PENDIENTE.getStatusName());

			statusLogDummy.setStatus(Status.FICTICIOUS_SCHEDULED.getStatusName());
			statusLogDummy.setScheduledDate(request.getScheduleDate().toString());
			statusLogDummy.setScheduledRange(request.getScheduleRange());

			listLog.add(statusPendiente);
			listLog.add(statusLogDummy);

			speech = hasCustomerInfo(customer)
					? Status.FICTICIOUS_SCHEDULED.getGenericSpeech().replace(Constants.TEXT_NAME_REPLACE,
							customer.getName().split(" ")[0])
					: Status.FICTICIOUS_SCHEDULED.getGenericSpeech();

			provisionAdd.setLogStatus(listLog);
			provisionAdd.setLastTrackingStatus(Status.FICTICIOUS_SCHEDULED.getStatusName());
			provisionAdd.setGenericSpeech(speech);
			provisionAdd.setDescriptionStatus(Status.FICTICIOUS_SCHEDULED.getDescription());
			provisionAdd.setStatusToa(Status.PENDIENTE.getStatusName().toLowerCase());

			provisionAdd.setComponents(new ArrayList<>());

			provisionRepository.insertProvision(provisionAdd);

		}

		return true;
	}

	private boolean validateBuckectProduct(String[] getData, Provision provision) throws Exception {
		boolean errorBucket = false; // validar IN_TOA
		// Valida DNI
		log.info("validateBuckectProduct");
		if (Constants.TIPO_RUC.equals(provision.getCustomer().getDocumentType().toLowerCase())
				&& !provision.getCustomer().getDocumentNumber().startsWith(Constants.RUC_NATURAL)) {
			errorBucket = true;
			log.info("No es persona natural. Documento: " + provision.getCustomer().getDocumentType() + " NumDoc: "
					+ provision.getCustomer().getDocumentNumber());
			return errorBucket;
		} else {
			log.info("Es persona natural. Documento: " + provision.getCustomer().getDocumentType() + " NumDoc: "
					+ provision.getCustomer().getDocumentNumber());
		}
		if (Constants.STATUS_IN_TOA.equalsIgnoreCase(getData[0] == null ? "" : getData[0])) { // validate bucket and
			errorBucket = getBucketByProduct(provision.getOriginCode(), provision.getCommercialOp(), getData[17]);
		}

		return errorBucket;
	}

	@Override
	public boolean provisionUpdateFromTOA(UpdateFromToaRequest request, String xaRequest, String xaRequirementNumber)
			throws Exception {
		boolean bool = false;
		log.info("ProvisionServiceImpl.provisionUpdateFromTOA()");
		String[] getData = request.getData().split("\\|", -1);
		Provision provision = new Provision();
		// validar si es vf o mt
		if (!xaRequirementNumber.startsWith("MT") && !xaRequirementNumber.startsWith("VF")) {
			provision = provisionRepository.getByOrderCodeForUpdate(xaRequest);
		} else {
			// Llamar al método de busqueda ficticio
			provision = provisionRepository.getByOrderCodeForUpdateFicticious(xaRequirementNumber);
		}

		log.info("Antes de update provision");
		bool = updateProvision(provision, getData, request);
		log.info("Depues de update provision");
		return bool;
	}

	private boolean updateProvision(Provision provision, String[] getData, UpdateFromToaRequest request)
			throws Exception {

		String speech = "";
		log.info("ProvisionServiceImpl.updateProvision()");
		if (provision != null) {
			log.info("Provision != null");
			List<StatusLog> listLog = provision.getLogStatus();
			log.info("Provision statuslog");
			// valida Bucket x Producto
			boolean boolBucket = validateBuckectProduct(getData, provision);

			if (boolBucket) {
				return false;
			}

			log.info("Provision boolBucket");
			speech = hasCustomerInfo(provision.getCustomer()) ? Status.DUMMY_IN_TOA.getGenericSpeech()
					.replace(Constants.TEXT_NAME_REPLACE, provision.getCustomer().getName().split(" ")[0])
					: Status.DUMMY_IN_TOA.getGenericSpeech();

			if (request.getStatus().equalsIgnoreCase(Status.IN_TOA.getStatusName())) {

				String origin = getData[6].toString().substring(0, 2);
				if (getData[2].toString().equals("0")
						&& (origin.equalsIgnoreCase("VF") || origin.equalsIgnoreCase("MT"))) {

					log.info("IF 1");
					// IN_TOA fictitious
					Update update = new Update();
					// NO SMS
					StatusLog statusLog = new StatusLog();
					statusLog.setStatus(Status.DUMMY_IN_TOA.getStatusName());
					listLog.add(statusLog);

					update.set("log_status", listLog);
					update.set("last_tracking_status", Status.DUMMY_IN_TOA.getStatusName());
					update.set("generic_speech", speech);
					update.set("description_status", Status.DUMMY_IN_TOA.getDescription());

					provisionRepository.updateProvision(provision, update);
					return true;

				} else if (getData[2].toString().equals("0")
						&& (!origin.equalsIgnoreCase("VF") && !origin.equalsIgnoreCase("MT"))) {

					log.info("IF 2");
					// IN_TOA Monoproducto
					Update update = new Update();
					// SI SMS

					StatusLog statusLog = new StatusLog();
					statusLog.setStatus(Status.DUMMY_IN_TOA.getStatusName());

					update.set("xa_request", getData[2]);
					update.set("xa_id_st", getData[4]);
					update.set("xa_requirement_number", getData[5]);
					update.set("appt_number", getData[6]);
					update.set("activity_type", getData[8].toLowerCase());
					update.set("work_zone", getData[17]);
					update.set("send_notify", false);
					listLog.add(statusLog);
					update.set("log_status", listLog);
					update.set("last_tracking_status", Status.DUMMY_IN_TOA.getStatusName());
					update.set("generic_speech", speech);
					update.set("description_status", Status.DUMMY_IN_TOA.getDescription());
					update.set("active_status", Constants.PROVISION_STATUS_ACTIVE);
					update.set("status_toa", Constants.PROVISION_STATUS_DONE);

					update.set("show_location", false);

					provisionRepository.updateProvision(provision, update);
					return true;
				} else {
					log.info("IF 3");
					Update update = new Update();
					// update.set("xa_creation_date", getData[3]);
					// SI SMS
					// update.set("xa_request", getData[2]);
					update.set("xa_id_st", getData[4]);
					update.set("xa_requirement_number", getData[5]);
					update.set("appt_number", getData[6]);
					update.set("activity_type", getData[8].toLowerCase());
					update.set("work_zone", getData[17]);
					update.set("send_notify", false);
					update.set("show_location", false);
					if (provision.getXaIdSt() != null) {
						update.set("has_schedule", false);
					}

					log.info("JEAN 1");
					InToa inToa = new InToa();

					inToa.setXaNote(getData[9]);
					inToa.setXaCreationDate(getData[3]);
					inToa.setDate(getData[15]);
					inToa.setXaScheduler(getData[16]);
					inToa.setLongitude(getData[18]);
					inToa.setLatitude(getData[19]);

					update.set("in_toa", inToa);
					update.set("active_status", Constants.PROVISION_STATUS_ACTIVE);
					update.set("status_toa", Constants.PROVISION_STATUS_DONE);

					StatusLog statusLog = new StatusLog();
					statusLog.setStatus(Status.IN_TOA.getStatusName());
					statusLog.setXaidst(getData[4]);

					update.set("last_tracking_status", Status.IN_TOA.getStatusName());
					update.set("generic_speech", Status.IN_TOA.getSpeechWithoutSchedule());
					update.set("description_status", Status.IN_TOA.getDescription());
					listLog.add(statusLog);

					log.info("JEAN 2");
					// Regularizar Agenda Ficticia

					if (provision.getXaIdSt() == null) {
						if (provision.getDummyStPsiCode() != null) {
							List<StatusLog> listLogx = listLog.stream()
									.filter(x -> Status.FICTICIOUS_SCHEDULED.getStatusName().equals(x.getStatus()))
									.collect(Collectors.toList());

							List<StatusLog> listLogCancelled = listLog.stream()
									.filter(x -> Status.WO_CANCEL.getStatusName().equals(x.getStatus()))
									.collect(Collectors.toList());

							if (listLogx.size() > 0 && listLogCancelled.size() == 0) {
								StatusLog statusSchedule = new StatusLog();
								statusSchedule.setStatus(Status.SCHEDULED.getStatusName());
								statusSchedule.setXaidst(getData[4]);
								statusSchedule.setScheduledDate(listLogx.get(0).getScheduledDate());
								statusSchedule.setScheduledRange(listLogx.get(0).getScheduledRange());
								listLog.add(statusSchedule);

								update.set("last_tracking_status", Status.SCHEDULED.getStatusName());
								update.set("generic_speech", Status.SCHEDULED.getGenericSpeech());
								update.set("description_status", Status.SCHEDULED.getDescription());

								log.info("UPDATE PSICODEREAL");
								// update psiCode by schedule
								trazabilidadScheduleApi.updatePSICodeReal(provision.getIdProvision(),
										provision.getXaRequest(), getData[4], getData[8].toLowerCase(),
										provision.getCustomer());

							}
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

					log.info("UPDATE PROVISION");
					provisionRepository.updateProvision(provision, update);

					return true;
				}
			}

			if (request.getStatus().equalsIgnoreCase(Status.WO_PRESTART.getStatusName())
					&& !provision.getXaIdSt().isEmpty()) {

				/*List<StatusLog> listLogx = listLog.stream()
						.filter(x -> Status.WO_PRESTART.getStatusName().equals(x.getStatus())
								&& getData[6].equals(x.getXaidst()))
						.collect(Collectors.toList());

				boolean alreadyExist = listLogx.size() > 0;

				if (!alreadyExist) {*/
					Update update = new Update();
					update.set("external_id", getData[1]);
					// update.set("xa_request", getData[2]);
					update.set("active_status", Constants.PROVISION_STATUS_SCHEDULE_IN_PROGRESS);

					WoPreStart woPreStart = new WoPreStart();

					woPreStart.setNameResource(getData[3]);
					woPreStart.setDate(getData[4]);
					update.set("wo_prestart", woPreStart);
					update.set("activity_type", getData[5].toLowerCase());
					update.set("xa_id_st", getData[6]);
					update.set("show_location", false);

					StatusLog statusLog = new StatusLog();
					statusLog.setStatus(Status.WO_PRESTART.getStatusName());
					statusLog.setXaidst(provision.getXaIdSt());

					update.set("customer.latitude", getData[14]);
					update.set("customer.longitude", getData[13]);
					update.set("last_tracking_status", Status.WO_PRESTART.getStatusName());
					update.set("generic_speech", Status.WO_PRESTART.getGenericSpeech());
					update.set("description_status", Status.WO_PRESTART.getDescription());
					listLog.add(statusLog);
					update.set("log_status", listLog);

					provisionRepository.updateProvision(provision, update);
					return true;
				/*} else {
					return false;
				}*/
			}

			if (request.getStatus().equalsIgnoreCase(Status.WO_INIT.getStatusName())
					&& !provision.getXaIdSt().isEmpty()) {

				/*
				 * List<StatusLog> listLogx = listLog.stream().filter( x ->
				 * Status.WO_INIT.getStatusName().equals(x.getStatus()) &&
				 * getData[7].equals(x.getXaidst())) .collect(Collectors.toList());
				 * 
				 * boolean alreadyExist = listLogx.size() > 0;
				 * 
				 * if (!alreadyExist) {
				 */
					Update update = new Update();
					WoInit woInit = new WoInit();

					woInit.setNameResource(getData[2]);
					woInit.setEtaStartTime(getData[3]);
					woInit.setEtaEndTime(getData[10]);
					woInit.setXaCreationDate(getData[6]);
					woInit.setDate(getData[23]);
					woInit.setXaNote(getData[15]);
					update.set("wo_init", woInit);
					update.set("show_location", false);
					update.set("xa_id_st", getData[7]);
					update.set("xa_requirement_number", getData[8]);
					update.set("appt_number", getData[9]);
					update.set("activity_type", getData[14].toLowerCase());
					update.set("active_status", Constants.PROVISION_STATUS_WOINIT);

					// update.set("xa_request", getData[5]);
					StatusLog statusLog = new StatusLog();
					statusLog.setStatus(Status.WO_INIT.getStatusName());
					statusLog.setXaidst(provision.getXaIdSt());

					update.set("last_tracking_status", Status.WO_INIT.getStatusName());
					update.set("generic_speech", Status.WO_INIT.getGenericSpeech());
					update.set("description_status", Status.WO_INIT.getDescription());
					listLog.add(statusLog);
					update.set("log_status", listLog);

					provisionRepository.updateProvision(provision, update);
					return true;
				/*} else {
					return false;
				}*/
			}

			if (request.getStatus().equalsIgnoreCase(Status.WO_COMPLETED.getStatusName())
					&& !provision.getXaIdSt().isEmpty()) {

				/*List<StatusLog> listLogx = listLog.stream()
						.filter(x -> Status.WO_COMPLETED.getStatusName().equals(x.getStatus())
								&& getData[8].equals(x.getXaidst()))
						.collect(Collectors.toList());

				boolean alreadyExist = listLogx.size() > 0;

				if (!alreadyExist) {*/
					Update update = new Update();
					WoCompleted woCompleted = new WoCompleted();

					woCompleted.setXaCreationDate(getData[7]);
					woCompleted.setDate(getData[4]);
					woCompleted.setXaNote(getData[14]);
					woCompleted.setEtaStartTime(getData[2]);
					woCompleted.setEtaEndTime(getData[3]);

					woCompleted.setObservation(getData[22]);
					woCompleted.setReceivePersonName(getData[23]);
					woCompleted.setReceivePersonId(getData[24]);
					woCompleted.setRelationship(getData[25]);
					update.set("wo_completed", woCompleted);

					update.set("active_status", Constants.PROVISION_STATUS_COMPLETED);

					update.set("show_location", false);
					update.set("xa_id_st", getData[8]);
					update.set("xa_requirement_number", getData[9]);
					update.set("appt_number", getData[10]);
					update.set("activity_type", getData[13].toLowerCase());

					StatusLog statusLog = new StatusLog();
					statusLog.setStatus(Status.WO_COMPLETED.getStatusName());
					statusLog.setXaidst(provision.getXaIdSt());

					update.set("last_tracking_status", Status.WO_COMPLETED.getStatusName());
					update.set("generic_speech", Status.WO_COMPLETED.getGenericSpeech());
					update.set("description_status", Status.WO_COMPLETED.getDescription());
					listLog.add(statusLog);
					update.set("log_status", listLog);

					provisionRepository.updateProvision(provision, update);
					return true;
				/*} else {
					return false;
				}*/
			}

			if (request.getStatus().equalsIgnoreCase(Status.WO_CANCEL.getStatusName())
					&& !provision.getXaIdSt().isEmpty()) {
				// && "0".equals(getData[16].toString())) {

				Update update = new Update();

				WoCancel woCancel = new WoCancel();
				woCancel.setUserCancel(getData[15]);
				woCancel.setXaCancelReason(getData[16]);
				update.set("wo_cancel", woCancel);
				update.set("active_status", Constants.PROVISION_STATUS_CANCELLED);

				StatusLog statusLog = new StatusLog();
				statusLog.setStatus(Status.WO_CANCEL.getStatusName());
				statusLog.setXaidst(provision.getXaIdSt());

				update.set("send_notify", false);
				update.set("xa_cancel_reason", getData[16]);
				update.set("user_cancel", getData[15]);
				update.set("last_tracking_status", Status.WO_CANCEL.getStatusName());
				update.set("generic_speech", Status.WO_CANCEL.getGenericSpeech());
				update.set("description_status", Status.WO_CANCEL.getDescription());
				update.set("xa_id_st", getData[4]);
				update.set("xa_requirement_number", getData[5]);
				update.set("appt_number", getData[6]);

				update.set("show_location", false);

				listLog.add(statusLog);
				update.set("log_status", listLog);

				// Actualiza estado en provision
				provisionRepository.updateProvision(provision, update);

				ScheduleNotDoneRequest scheduleNotDoneRequest = new ScheduleNotDoneRequest();
				scheduleNotDoneRequest.setRequestId(provision.getIdProvision());
				scheduleNotDoneRequest.setRequestType(provision.getActivityType());
				scheduleNotDoneRequest.setStPsiCode(getData[4]);

				if (getData[4].toString().equals(getData[5].toString())
						&& getData[5].toString().equals(getData[6].toString())) {
					scheduleNotDoneRequest.setFlgFicticious(true);
					scheduleNotDoneRequest.setRequestType(Constants.ACTIVITY_TYPE_PROVISION.toLowerCase());
				} else {
					scheduleNotDoneRequest.setFlgFicticious(false);
				}

				// Cancela agenda
				trazabilidadScheduleApi.cancelLocalSchedule(scheduleNotDoneRequest);
//				trazabilidadScheduleApi.updateCancelSchedule(new CancelRequest(provision.getIdProvision(),
//						provision.getActivityType().toLowerCase(), provision.getXaIdSt()));

				return true;
			}

			if (request.getStatus().equalsIgnoreCase(Status.WO_RESCHEDULE.getStatusName())
					&& !provision.getXaIdSt().isEmpty()) {

				String identificadorSt = getData[4].toString();

				Update update = new Update();
				WoReshedule woReshedule = new WoReshedule();
				String range = "AM";

				if (getData[17].toString().trim().equals("09-13") || getData[17].toString().trim().equals("9-13")) {
					range = "AM";
				} else {
					range = "PM";
				}
				String rangeFinal = range;

				String dateString = getData[16];// formateador.format(date);

				if ((identificadorSt == null || identificadorSt.isEmpty())
						&& (rangeFinal == null || rangeFinal.isEmpty())
						&& (dateString == null || dateString.isEmpty())) {
					return false;
				}

				/*
				 * List<StatusLog> listLogx = listLog.stream() .filter(x ->
				 * "SCHEDULED".equals(x.getStatus()) && identificadorSt.equals(x.getXaidst()) &&
				 * rangeFinal.equals(x.getScheduledRange()) &&
				 * dateString.equals(x.getScheduledDate())) .collect(Collectors.toList());
				 */

				List<StatusLog> listLogx = listLog.stream()
						.filter(x -> "SCHEDULED".equals(x.getStatus()) && identificadorSt.equals(x.getXaidst()))
						.collect(Collectors.toList());

				if (listLogx.size() > 0) {
					if (listLogx.get(listLogx.size() - 1).getScheduledDate().contentEquals(dateString)
							&& listLogx.get(listLogx.size() - 1).getScheduledRange().contentEquals(rangeFinal)) {
						return true;
					}

				}

//				if (listLogx.size() > 0) {
//					return true;
//				}

				woReshedule.setXaAppointmentScheduler(getData[23]);
				woReshedule.setTimeSlot(range);
				update.set("wo_schedule", woReshedule);
				update.set("active_status", Constants.PROVISION_STATUS_ACTIVE);

				update.set("xa_id_st", getData[4]);
				update.set("xa_requirement_number", getData[5]);
				update.set("appt_number", getData[6]);
				update.set("activity_type", getData[8].toLowerCase());

				StatusLog statusLog = new StatusLog();
				statusLog.setStatus(Status.SCHEDULED.getStatusName());
				statusLog.setScheduledRange(rangeFinal);
				statusLog.setScheduledDate(dateString);
				statusLog.setXaidst(provision.getXaIdSt());

				update.set("date", getData[16]);
				update.set("send_notify", false);
				update.set("time_slot", range);
				update.set("last_tracking_status", Status.SCHEDULED.getStatusName());
				update.set("generic_speech", Status.SCHEDULED.getGenericSpeech());
				update.set("description_status", Status.SCHEDULED.getDescription());
				listLog.add(statusLog);
				update.set("log_status", listLog);

				update.set("show_location", false);

				// Actualizar provision
				provisionRepository.updateProvision(provision, update);

				// el que parsea
				SimpleDateFormat parseador2 = new SimpleDateFormat("yyyy-MM-dd");
				// el que formatea
				SimpleDateFormat formateador2 = new SimpleDateFormat("dd/MM/yyyy");

				Date date2 = parseador2.parse(getData[16]);// ("31-03-2016");
				System.out.println("Fecha de reschedule => " + formateador2.format(date2));
				String dateString2 = formateador2.format(date2);

				Customer customer = new Customer();
				customer.setDocumentNumber(provision.getCustomer().getDocumentNumber());
				customer.setDocumentType(provision.getCustomer().getDocumentType());
				ScheduleRequest scheduleRequest = new ScheduleRequest();
				scheduleRequest.setBucket(provision.getWorkZone());
				// scheduleRequest.setDocumentNumber(provision.getCustomer().getDocumentNumber());
				// scheduleRequest.setDocumentType(provision.getCustomer().getDocumentType());
				scheduleRequest.setPilot(false);
				// scheduleRequest.setOrderCode(provision.getXaRequest());
				scheduleRequest.setXaOrderCode(provision.getXaRequest());
				scheduleRequest.setRequestId(provision.getIdProvision());
				scheduleRequest.setRequestType(provision.getActivityType());
				scheduleRequest.setSelectedDate(dateString2);
				scheduleRequest.setSelectedRange(range);
				scheduleRequest.setStpsiCode(getData[4]);
				scheduleRequest.setCustomer(customer);

				scheduleRequest.setDocumentNumber(provision.getCustomer().getDocumentNumber());
				scheduleRequest.setDocumentType(provision.getCustomer().getDocumentType());
				scheduleRequest.setOrderCode(provision.getXaRequest());
				scheduleRequest.setBucket(provision.getWorkZone());

				// Actualiza el agendamiento
				trazabilidadScheduleApi.updateSchedule(scheduleRequest);

				return true;
			}

			if (request.getStatus().equalsIgnoreCase(Status.WO_NOTDONE.getStatusName())
					&& !provision.getXaIdSt().isEmpty()) {
				Update update = new Update();
				WoNotdone woNotdone = new WoNotdone();

				woNotdone.setaNotDoneTypeInstall(getData[21]);
				woNotdone.setaNotDoneReasonInstall(getData[22]);
				woNotdone.setaNotDoneSubReasonInstall(getData[23]);
				woNotdone.setaNotDoneTypeRepair(getData[24]);
				woNotdone.setaNotDoneArea(getData[25]);
				woNotdone.setaNotDoneReasonRepair(getData[26]);
				woNotdone.setaObservation(getData[27]);
				woNotdone.setUserNotdone(getData[28]);

				update.set("wo_notdone", woNotdone);
				update.set("xa_id_st", getData[9]);
				update.set("xa_requirement_number", getData[10]);
				update.set("appt_number", getData[11]);
				update.set("activity_type", getData[14].toLowerCase());

				update.set("active_status", Constants.PROVISION_STATUS_NOTDONE);

				StatusLog statusLog = new StatusLog();
				statusLog.setStatus(Status.WO_NOTDONE.getStatusName());
				statusLog.setXaidst(provision.getXaIdSt());

				speech = hasCustomerInfo(provision.getCustomer()) ? Status.WO_NOTDONE.getGenericSpeech()
						.replace(Constants.TEXT_NAME_REPLACE, provision.getCustomer().getName().split(" ")[0])
						: Status.WO_NOTDONE.getGenericSpeech();

				update.set("a_observation", getData[27]);
				update.set("user_notdone", getData[28]);
				update.set("last_tracking_status", Status.WO_NOTDONE.getStatusName());
				update.set("generic_speech", speech);
				update.set("description_status", Status.WO_NOTDONE.getDescription());
				listLog.add(statusLog);
				update.set("log_status", listLog);

				update.set("show_location", false);
				update.set("send_notify", false);

				// Actualiza provision
				provisionRepository.updateProvision(provision, update);
				ScheduleNotDoneRequest scheduleNotDoneRequest = new ScheduleNotDoneRequest();
				// Solo cancelar agenda sin ir a PSI
				// Llamar al método de augusto.
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
		return restPSI.getCarrier(phoneNumber);
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
		Boolean errorValidate = true;

		log.info("ScheduleServiceImpl.getBucketByProduct()");

		try {
			ResponseBucket responseBucket = restPSI.getBucketByProduct();

			if (responseBucket != null) {
				// HACER MATCH BUCKET POR PRODUCTO - GENESIS

				for (Map.Entry<String, List<OrigenBean>> entry : responseBucket.getBody().getContent().entrySet()) {
					if (channel.trim().equalsIgnoreCase(entry.getKey())) {
						for (int i = 0; i < entry.getValue().size(); i++) {
							for (int j = 0; j < entry.getValue().get(i).getBuckets().size(); j++) {
								if (entry.getValue().get(i).getBuckets().get(j).trim().equalsIgnoreCase(bucket.trim())
										&& entry.getValue().get(i).getProduct().trim().equalsIgnoreCase(product)) {
									System.out.println("bucket => " + entry.getValue().get(i).getBuckets().get(j)
											+ ", product => " + entry.getValue().get(i).getProduct());
									// errorValidate = true;
									errorValidate = false;
									break;
								}
							}
							if (!errorValidate) {
								break;
							}
						}
						if (!errorValidate) {
							break;
						}
					}
				}

				return errorValidate;
			} else {
				throw new Exception();
			}

		} catch (Exception e) {
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

				/*
				 * if (Constants.STATUS_WO_CANCEL.equalsIgnoreCase(listita.get(i).
				 * getLastTrackingStatus()) &&
				 * (!Status.DUMMY_IN_TOA.getStatusName().equalsIgnoreCase(list.get(list.size() -
				 * 2).getStatus()) && !Status.SCHEDULED.getStatusName()
				 * .equalsIgnoreCase(list.get(list.size() - 2).getStatus()))) {
				 * listita.remove(i); i--; }
				 */
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
}