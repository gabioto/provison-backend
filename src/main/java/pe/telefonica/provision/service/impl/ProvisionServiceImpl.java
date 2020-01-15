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
import pe.telefonica.provision.controller.request.ApiTrazaSetContactInfoUpdateRequest;
import pe.telefonica.provision.controller.request.CancelRequest;
import pe.telefonica.provision.controller.request.ContactRequest;
import pe.telefonica.provision.controller.request.GetProvisionByOrderCodeRequest;
import pe.telefonica.provision.controller.request.InsertCodeFictionalRequest;
import pe.telefonica.provision.controller.request.InsertOrderRequest;
import pe.telefonica.provision.controller.request.MailRequest.MailParameter;
import pe.telefonica.provision.controller.request.ProvisionRequest;
import pe.telefonica.provision.controller.request.SMSByIdRequest;
import pe.telefonica.provision.controller.request.SMSByIdRequest.Contact;
import pe.telefonica.provision.controller.request.SMSByIdRequest.Message.MsgParameter;
import pe.telefonica.provision.controller.request.ScheduleNotDoneRequest;
import pe.telefonica.provision.controller.request.ScheduleRequest;
import pe.telefonica.provision.controller.request.UpdateFromToaRequest;
import pe.telefonica.provision.controller.response.ProvisionHeaderResponse;
import pe.telefonica.provision.controller.response.ProvisionResponse;
import pe.telefonica.provision.controller.response.SMSByIdResponse;
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
import pe.telefonica.provision.util.constants.ConstantsMessageKey;
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
		Optional<Provision> provision = provisionRepository.getOrder(provisionRequest.getBody().getDocumentType(),
				provisionRequest.getBody().getDocumentNumber());

		if (!provision.isPresent() && provisionRequest.getBody().getDocumentType().equals("CE")) {
			provision = provisionRepository.getOrder("CEX", provisionRequest.getBody().getDocumentNumber());
		}

		if (!provision.isPresent() && provisionRequest.getBody().getDocumentType().equalsIgnoreCase("PASAPORTE")) {
			provision = provisionRepository.getOrder("PAS", provisionRequest.getBody().getDocumentNumber());
		}

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

		Optional<List<Provision>> provisions = provisionRepository.findAll(provisionRequest.getBody().getDocumentType(),
				provisionRequest.getBody().getDocumentNumber());
		List<Provision> provisionList = null;

		if (provisions.get().size() == 0 && provisionRequest.getBody().getDocumentType().equals("CE")) {
			provisions = provisionRepository.findAll("CEX", provisionRequest.getBody().getDocumentNumber());
		}

		if (provisions.get().size() == 0 && provisionRequest.getBody().getDocumentType().equals("PASAPORTE")) {
			provisions = provisionRepository.findAll("PAS", provisionRequest.getBody().getDocumentNumber());
		}

		if (provisions.isPresent() && provisions.get().size() != 0) {
			provisionList = provisions.get();

			for (Provision provision : provisionList) {
				if (provision.getTvDetail() != null) {
					ComponentsDto tv = evaluateTvFields(provision);

					if (tv != null) {
						provision.getComponents().add(tv);
					}
				}

				if (provision.getInternetDetail() != null) {
					ComponentsDto internet = evaluateInternetFields(provision);

					if (internet != null) {
						provision.getComponents().add(internet);
					}
				}

				if (provision.getHomePhoneDetail() != null) {
					ComponentsDto line = evaluateLineFields(provision);

					if (line != null) {
						provision.getComponents().add(line);
					}
				}
			}

			return provisionList;
		} else {
			return null;
		}
	}

	private ComponentsDto evaluateTvFields(Provision provision) {
		Television television = provision.getTvDetail();
		ComponentsDto components = null;

		if (television.getType() != null || television.getTvSignal() != null || television.getTvBlocks() != null
				|| television.getTechnology() != null || television.getEquipmentsNumber() != null
				|| television.getEquipment() != null || television.getDescription() != null
				|| television.getAdditionalSmartHd() != null || television.getAdditionalHd() != null) {

			components = new ComponentsDto();
			components.setTitle(Constants.COMPONENTS_TITLE_TV);
			components.setName(Constants.COMPONENTS_NAME_TV);
			components.setDescription((television.getDescription() != null && !television.getDescription().isEmpty())
					? television.getDescription()
					: Constants.COMPONENTS_DESC_TV);
		}

		return components;
	}

	private ComponentsDto evaluateInternetFields(Provision provision) {
		Internet internet = provision.getInternetDetail();
		ComponentsDto components = null;

		if (internet.getDescription() != null || internet.getEquipment() != null || internet.getPromoSpeed() != null
				|| internet.getSmartWifi() != null || internet.getSpeed() != null || internet.getSvaInternet() != null
				|| internet.getTechnology() != null || internet.getTimePromoSpeed() != null) {

			components = new ComponentsDto();
			components.setTitle(Constants.COMPONENTS_TITLE_INTERNET);
			components.setName(Constants.COMPONENTS_NAME_INTERNET);
			components.setDescription((internet.getDescription() != null && !internet.getDescription().isEmpty())
					? internet.getDescription()
					: Constants.COMPONENTS_DESC_INTERNET);
		}

		return components;
	}

	private ComponentsDto evaluateLineFields(Provision provision) {
		HomePhone line = provision.getHomePhoneDetail();
		ComponentsDto components = null;

		if (line.getDescription() != null || line.getEquipmenstNumber() != null || line.getEquipment() != null
				|| line.getSvaLine() != null || line.getType() != null) {

			components = new ComponentsDto();
			components.setTitle(Constants.COMPONENTS_TITLE_LINE);
			components.setName(Constants.COMPONENTS_NAME_LINE);
			components.setDescription(
					(line.getDescription() != null && !line.getDescription().isEmpty()) ? line.getDescription()
							: Constants.COMPONENTS_DESC_LINE);
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

		// String separador = Pattern.quote("|");
		// tring[] getData = request.getData().split(separador);

		String[] getData = request.getData().split("\\|", -1);

		Provision provision = new Provision();

		System.out.println(getData[3]);

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

		provision.setLastTrackingStatus(Status.PENDIENTE.getStatusName());

		List<StatusLog> listLog = new ArrayList<>();

		StatusLog statusLog = new StatusLog();
		statusLog.setStatus(Status.PENDIENTE.getStatusName());
		statusLog.setDescription(Status.PENDIENTE.getDescription());

		listLog.add(statusLog);

		if (!request.getStatus().equalsIgnoreCase(Status.PENDIENTE.getStatusName())) {
			StatusLog statusLogCurrent = new StatusLog();
			statusLogCurrent.setStatus(request.getStatus());
			statusLogCurrent.setDescription(request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName())
					? Status.INGRESADO.getDescription()
					: Status.CAIDO.getDescription());

			listLog.add(statusLogCurrent);

			provision.setLastTrackingStatus(request.getStatus());

			provision.setActiveStatus(request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName())
					? Status.INGRESADO.getStatusName().toLowerCase()
					: Constants.PROVISION_STATUS_CANCELLED);

			provision.setStatusToa(request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName())
					? Status.INGRESADO.getStatusName().toLowerCase()
					: Constants.PROVISION_STATUS_CANCELLED);
		}

		provision.setLogStatus(listLog);

		return provision;
	}

	private Update fillProvisionUpdate(InsertOrderRequest request) {
		String getData[] = request.getData().split("\\|", -1);
		System.out.println(getData[3]);

		// Provision provision = new Provision();

		Update update = new Update();

		// provision.setSaleSource(getData[0]);
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

		// String data = "VENTASFIJA_PARKUR|TGESTIONA_FVD|MT15149|ARMANDO AUGUSTO CALVO
		// QUIROZ CALVO QUIROZ|07578669|987654321|AVDMIRO QUESADA, AURELIO 260 INT:1401
		// - URB SANTA INES-|LIMA|LIMA|SAN ISIDRO|Movistar Total 24GB TV Estándar
		// Digital HD 60Mbps||MIGRACION|DNI||CAIDA|07/11/2019 15:56:57|05/11/2019
		// 20:40:57||pruebas@hotmail.com||TRIO||||59|No||59|||MOVISTAR TOTAL|||NO
		// APLICA||||||||ATIS||||||||||||||||||||||||||||||||||||||";
		// String getData[] = data.split("\\|");
		String getData[] = request.getData().split("\\|");
		Provision provisionx = provisionRepository.getProvisionBySaleCode(getData[2]);

		if (provisionx != null) {

			Update update = fillProvisionUpdate(request);

			List<StatusLog> listLog = provisionx.getLogStatus();

			StatusLog statusLog = new StatusLog();
			statusLog.setStatus(request.getStatus());

			statusLog.setDescription(request.getStatus().equalsIgnoreCase(Status.PENDIENTE.getStatusName())
					? Status.PENDIENTE.getDescription()
					: request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName())
							? Status.INGRESADO.getDescription()
							: Status.CAIDO.getDescription());

			if (provisionx.getDummyStPsiCode() != null) {
				if (request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName())
						&& !provisionx.getDummyStPsiCode().isEmpty()) {
					ScheduleUpdateFicticiousRequest updateFicRequest = new ScheduleUpdateFicticiousRequest();
					updateFicRequest.setOrderCode(getData[11]);
					updateFicRequest.setOriginCode(provisionx.getOriginCode());
					updateFicRequest.setSaleCode(provisionx.getSaleCode());
					updateFicRequest.setFictitiousCode(provisionx.getDummyXaRequest());
					updateFicRequest.setRequestName(provisionx.getProductName());
					updateFicRequest.setRequestId(provisionx.getIdProvision());

					// Actualiza agenda
					boolean updateFicticious = trazabilidadScheduleApi.updateFicticious(updateFicRequest);
					update.set("is_update_dummy_st_psi_code", updateFicticious ? true : false);

				}
			}

			// status_toa
			String status = request.getStatus().equalsIgnoreCase(Status.PENDIENTE.getStatusName())
					? Status.PENDIENTE.getStatusName().toLowerCase()
					: request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName())
							? Status.INGRESADO.getStatusName().toLowerCase()
							: Constants.PROVISION_STATUS_CANCELLED;

			update.set("active_status", status);
			update.set("status_toa", status);
			update.set("send_notify", false);
			update.set("show_location", false);

			update.set("last_tracking_status", request.getStatus());

			listLog.add(statusLog);
			update.set("log_status", listLog);

			// provisionx.setLogStatus(listLog);
			// Actualiza provision
			provisionRepository.updateProvision(provisionx, update);

		} else {

			provisionRepository.insertProvision(fillProvisionInsert(request));

		}
		return true;
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
		Optional<Provision> optional = provisionRepository.getProvisionById(provisionId);

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
					contacts.add(contactCustomer);

//					ApiResponse<SMSByIdResponse> apiResponse = trazabilidadSecurityApi.sendSMS(contacts,
//							Constants.MSG_PRO_CANCELLED_BY_CUSTOMER_KEY, msgParameters.toArray(new MsgParameter[0]),
//							"");
					// ApiResponse<SMSByIdResponse> apiResponse = sendSMS(provision.getCustomer(),
					// Constants.MSG_PRO_CANCELLED_BY_CUSTOMER_KEY, msgParameters.toArray(new
					// MsgParameter[0]), "");

					try {
						// provisionRepository.sendCancelledMail(provision, name, "179829",
						// Constants.ADDRESS_CANCELLED_BY_CUSTOMER);

						sendCancelledMail(provision, name, "179829");
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
						msgParameters.toArray(new MsgParameter[0]), provisionTexts.getWebUrl());

				// ApiResponse<SMSByIdResponse> apiResponse = sendSMS(provision.getCustomer(),
				// Constants.MSG_PRO_CUSTOMER_UNREACHABLE_KEY, msgParameters.toArray(new
				// MsgParameter[0]), "http://www.movistar.com.pe");

				try {
					// provisionRepository.sendCancelledMail(provision, name, "179824",
					// Constants.ADDRESS_UNREACHABLE);
					sendCancelledMail(provision, name, "179824");
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
		boolean messageSent;
		boolean provisionUpdated;
		boolean scheduleUpdated;
		Optional<Provision> optional = provisionRepository.getProvisionById(provisionId);

		if (optional.isPresent()) {

			Provision provision = optional.get();
			Update update = new Update();
			update.set("active_status", Constants.PROVISION_STATUS_CANCELLED);
			update.set("cancellation_cause", cause);
			update.set("cancellation_detail", detail);

			provision.setActiveStatus(Constants.PROVISION_STATUS_CANCELLED);
			provision.setCancellationCause(cause);
			provision.setCancellationDetail(detail);

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
				sendCancelledMailByUser(provision, Constants.ADDRESS_CANCELLED_BY_CUSTOMER);
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

		MailParameter mailParameter3 = new MailParameter();
		mailParameter3.setParamKey("CONTACTFULLNAME");
		mailParameter3.setParamValue("contact name");
		mailParameters.add(mailParameter3);

		MailParameter mailParameter4 = new MailParameter();
		mailParameter4.setParamKey("CONTACTID");
		mailParameter4.setParamValue("123456789");
		mailParameters.add(mailParameter4);

		MailParameter mailParameter5 = new MailParameter();
		mailParameter5.setParamKey("FOLLOWORDER");
		mailParameter5.setParamValue(provisionTexts.getWebUrl());
		mailParameters.add(mailParameter5);

		return trazabilidadSecurityApi.sendMail("186162", mailParameters.toArray(new MailParameter[0]));

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

		// return sendMail("179829", mailParameters.toArray(new MailParameter[0]));
		return trazabilidadSecurityApi.sendMail("179829", mailParameters.toArray(new MailParameter[0]));
	}

	@Override
	public Provision setContactInfoUpdate(String provisionId, String contactFullname, String contactCellphone,
			Boolean contactCellphoneIsMovistar) {

		Optional<Provision> optional = provisionRepository.getProvisionById(provisionId);
		boolean updated = false;

		if (optional.isPresent()) {
			Provision provision = optional.get();
			// provision.getCustomer().setContactName(contactFullname);
			// provision.getCustomer().setContactPhoneNumber(Integer.valueOf(contactCellphone));
			provision.getCustomer().setContactCarrier(contactCellphoneIsMovistar.toString());

			// boolean contactUpdated = provisionRepository.updateContactInfoPsi(provision);
			// boolean contactUpdated = restPSI.updatePSIClient(provision);
			boolean contactUpdated = true;
			if (contactUpdated) {
				Update update = new Update();
				update.set("customer.contact_name", contactFullname);
				update.set("customer.contact_phone_number", Integer.valueOf(contactCellphone));
				update.set("customer.contact_carrier", contactCellphoneIsMovistar.toString());
				updated = provisionRepository.updateProvision(provision, update);
			}

			if (updated) {
				try {
					sendContactInfoChangedMail(provision);
				} catch (Exception e) {
					log.info(ProvisionServiceImpl.class.getCanonicalName() + ": " + e.getMessage());
					return provision;
				}

//				List<MsgParameter> msgParameters = new ArrayList<>();
				// Nota: si falla el envio de SMS, no impacta al resto del flujo, por lo que no
				// se valida la respuesta
				// ApiResponse<SMSByIdResponse> apiResponse = sendSMS(provision.getCustomer(),
				// Constants.MSG_CONTACT_UPDATED_KEY, msgParameters.toArray(new
				// MsgParameter[0]), provisionTexts.getWebUrl());
				List<Contact> contacts = new ArrayList<>();

				Contact contactCustomer = new Contact();
				contactCustomer.setPhoneNumber(provision.getCustomer().getPhoneNumber());
				contactCustomer.setIsMovistar(provision.getCustomer().getCarrier());
				contacts.add(contactCustomer);

//				ApiResponse<SMSByIdResponse> apiResponse = trazabilidadSecurityApi.sendSMS(contacts,
//						Constants.MSG_CONTACT_UPDATED_KEY, msgParameters.toArray(new MsgParameter[0]),
//						provisionTexts.getWebUrl());

				return provision;
			} else {
				return null;
			}
		} else {
			return null;
		}
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

				if (scheduledType == 2) {
					nomEstado = Status.FICTICIOUS_SCHEDULED.getStatusName();
					description = Status.FICTICIOUS_SCHEDULED.getDescription();
				} else {
					nomEstado = Status.SCHEDULED.getStatusName();
					description = Status.SCHEDULED.getDescription();
				}

				boolean updated = updateTrackingStatus(provision.getXaRequest(), provision.getXaIdSt(), nomEstado, true,
						scheduledDate, scheduledRange, scheduledType, description);

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
			LocalDate scheduledDate, String scheduledRange, Integer scheduleType, String description) {
		boolean updated = false;
		Optional<Provision> optionalProvision = provisionRepository.getProvisionByXaRequestAndSt(xaRequest, xaIdSt);
		log.info(ProvisionServiceImpl.class.getCanonicalName() + " - updateTrackingStatus: xaRequest = " + xaRequest
				+ ", xaIdSt =" + xaIdSt + ", status = " + status);

		if (optionalProvision.isPresent()) {
			Provision provision = optionalProvision.get();
			List<StatusLog> logStatus = provision.getLogStatus() == null ? new ArrayList<>() : provision.getLogStatus();

			StatusLog statusLog = new StatusLog();
			statusLog.setStatus(status);
			statusLog.setDescription(description);

			if (scheduledDate != null)
				statusLog.setScheduledDate(scheduledDate.toString());

			if (scheduledRange != null && !scheduledRange.equals(""))
				statusLog.setScheduledRange(scheduledRange);

			if (xaIdSt != null)
				statusLog.setXaidst(xaIdSt);

			logStatus.add(statusLog);

			provision.setLastTrackingStatus(status);

			updated = provisionRepository.updateTrackingStatus(optionalProvision.get(), logStatus, comesFromSchedule);
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

					provision.getContacts().clear();
					provision.setContacts(request.isHolderWillReceive() ? null : contactsList);

					sendInfoUpdateSMS(provision);

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
		ProvisionResponse<List<Contacts>> contactsResponse = getContactList(provision.getIdProvision());
		List<Contact> contacts = SMSByIdRequest.mapContacts(contactsResponse.getData());

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

		if (provision != null) {
			Update update = new Update();
			List<StatusLog> listLog = provision.getLogStatus();

			update.set("dummy_st_psi_code", request.getDummyStPsiCode());
			update.set("dummy_xa_request", request.getDummyXaRequest());
			update.set("has_schedule", true);

			StatusLog statusLog = new StatusLog();
			statusLog.setStatus(Status.FICTICIOUS_SCHEDULED.getStatusName());
			statusLog.setDescription(Status.FICTICIOUS_SCHEDULED.getDescription());

			statusLog.setScheduledDate(request.getScheduleDate().toString());
			statusLog.setScheduledRange(request.getScheduleRange());

			update.set("work_zone", request.getBucket());
			update.set("origin_code", request.getOriginCode());
			update.set("last_tracking_status", Status.FICTICIOUS_SCHEDULED.getStatusName());
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

			List<StatusLog> listLog = new ArrayList<>();

			StatusLog statusPendiente = new StatusLog();
			StatusLog statusLogDummy = new StatusLog();
			statusPendiente.setStatus(Status.PENDIENTE.getStatusName());
			statusPendiente.setDescription(Status.PENDIENTE.getDescription());

			statusLogDummy.setStatus(Status.FICTICIOUS_SCHEDULED.getStatusName());
			statusLogDummy.setDescription(Status.FICTICIOUS_SCHEDULED.getDescription());

			statusLogDummy.setScheduledDate(request.getScheduleDate().toString());
			statusLogDummy.setScheduledRange(request.getScheduleRange());

			listLog.add(statusPendiente);
			listLog.add(statusLogDummy);

			provisionAdd.setLogStatus(listLog);
			provisionAdd.setLastTrackingStatus(Status.FICTICIOUS_SCHEDULED.getStatusName());

			provisionRepository.insertProvision(provisionAdd);

		}

		return true;
	}

	/*
	 * private boolean validateBuckectProduct(String[] getData, Provision provision)
	 * throws Exception { boolean errorBucket = false; // validar IN_TOA if
	 * (Constants.STATUS_IN_TOA.equalsIgnoreCase(getData[0] == null ? "" :
	 * getData[0])) { // validate bucket and name product errorBucket =
	 * getBucketByProduct(provision.getOriginCode(), provision.getCommercialOp(),
	 * getData[17]); if (errorBucket) { // valida DNI if
	 * (Constants.TIPO_RUC.equals(provision.getCustomer().getDocumentType().
	 * toLowerCase()) &&
	 * !provision.getCustomer().getDocumentNumber().startsWith(Constants.RUC_NATURAL
	 * )) { errorBucket = false; log.info("No es persona natural. Documento: " +
	 * provision.getCustomer().getDocumentType() + " NumDoc: " +
	 * provision.getCustomer().getDocumentNumber()); } else {
	 * log.info("Es persona natural. Documento: " +
	 * provision.getCustomer().getDocumentType() + " NumDoc: " +
	 * provision.getCustomer().getDocumentNumber()); } } } return true; }
	 */

	@Override
	public boolean provisionUpdateFromTOA(UpdateFromToaRequest request, String xaRequest, String xaRequirementNumber)
			throws Exception {
		boolean bool = false;
		String[] getData = request.getData().split("\\|", -1);
		Provision provision = new Provision();
		// validar si es vf o mt
		if (!xaRequirementNumber.startsWith("MT") && !xaRequirementNumber.startsWith("VF")) {
			provision = provisionRepository.getByOrderCodeForUpdate(xaRequest);
		} else {
			// Llamar al método de busqueda ficticio
			provision = provisionRepository.getByOrderCodeForUpdateFicticious(xaRequirementNumber);
		}

		bool = updateProvision(provision, getData, request);
		return bool;
	}

	private boolean updateProvision(Provision provision, String[] getData, UpdateFromToaRequest request)
			throws Exception {

		if (provision != null) {

			List<StatusLog> listLog = provision.getLogStatus();
			/*
			 * // valida Bucket x Producto boolean boolBucket =
			 * validateBuckectProduct(getData, provision);
			 * 
			 * if (!boolBucket) { return false; }
			 */

			if (request.getStatus().equalsIgnoreCase(Status.IN_TOA.getStatusName())) {

				String origin = getData[6].toString().substring(0, 2);
				if (getData[2].toString().equals("0")
						&& (origin.equalsIgnoreCase("VF") || origin.equalsIgnoreCase("MT"))) {
					// IN_TO fictitious
					Update update = new Update();
					// NO SMS
					StatusLog statusLog = new StatusLog();
					statusLog.setStatus(Status.DUMMY_IN_TOA.getStatusName());
					statusLog.setDescription(Status.DUMMY_IN_TOA.getDescription());
					listLog.add(statusLog);

					update.set("log_status", listLog);
					update.set("xa_request", getData[2]);
					update.set("xa_id_st", getData[4]);
					update.set("xa_requirement_number", getData[5]);
					update.set("appt_number", getData[6]);
					update.set("activity_type", getData[8].toLowerCase());
					update.set("work_zone", getData[16]);
					update.set("last_tracking_status", Status.IN_TOA.getStatusName());
					update.set("active_status", Constants.PROVISION_STATUS_ACTIVE);
					update.set("status_toa", Constants.PROVISION_STATUS_DONE);
					
					update.set("show_location", false);

					provisionRepository.updateProvision(provision, update);
					return true;

				} else if (getData[2].toString().equals("0")
						&& (!origin.equalsIgnoreCase("VF") && !origin.equalsIgnoreCase("MT"))) {

					// IN_TOA Monoproducto
					Update update = new Update();
					// SI SMS

					StatusLog statusLog = new StatusLog();
					statusLog.setStatus(Status.DUMMY_IN_TOA.getStatusName());
					statusLog.setDescription(Status.DUMMY_IN_TOA.getDescription());
					update.set("xa_request", getData[5]);
					update.set("xa_id_st", getData[4]);
					update.set("xa_requirement_number", getData[5]);
					update.set("appt_number", getData[6]);
					update.set("activity_type", getData[8].toLowerCase());
					update.set("work_zone", getData[16]);
					update.set("send_notify", false);
					listLog.add(statusLog);
					update.set("log_status", listLog);
					update.set("last_tracking_status", Status.IN_TOA.getStatusName());

					update.set("active_status", Constants.PROVISION_STATUS_ACTIVE);
					update.set("status_toa", Constants.PROVISION_STATUS_DONE);
					
					update.set("show_location", false);

					provisionRepository.updateProvision(provision, update);
					return true;
				} else {
					Update update = new Update();
					// update.set("xa_creation_date", getData[3]);
					// SI SMS
					update.set("xa_id_st", getData[4]);
					update.set("xa_requirement_number", getData[5]);
					update.set("appt_number", getData[6]);
					update.set("activity_type", getData[8].toLowerCase());
					update.set("work_zone", getData[16]);
					update.set("send_notify", false);
					if (provision.getXaIdSt() != null) {
						update.set("has_schedule", false);
					}

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
					statusLog.setDescription(Status.IN_TOA.getDescription());
					statusLog.setXaidst(getData[4]);

					update.set("last_tracking_status", Status.IN_TOA.getStatusName());
					
					update.set("show_location", false);
					
					listLog.add(statusLog);

					// Regularizar Agenda Ficticia

					if (provision.getXaIdSt() == null) {
						if (!provision.getDummyStPsiCode().isEmpty()) {
							List<StatusLog> listLogx = listLog.stream()
									.filter(x -> "FICTICIOUS_SCHEDULED".equals(x.getStatus()))
									.collect(Collectors.toList());
							if (listLogx.size() > 0) {
								StatusLog statusSchedule = new StatusLog();
								statusSchedule.setStatus(Status.SCHEDULED.getStatusName());
								statusSchedule.setDescription(Status.SCHEDULED.getDescription());
								statusSchedule.setXaidst(getData[4]);
								statusSchedule.setScheduledDate(listLogx.get(0).getScheduledDate());
								statusSchedule.setScheduledRange(listLogx.get(0).getScheduledRange());
								listLog.add(statusSchedule);
								update.set("last_tracking_status", Status.SCHEDULED.getStatusName());

							}
						}

					}

					update.set("log_status", listLog);

					LocalDateTime dateSendedSMS = LocalDateTime.now(ZoneOffset.of("-05:00"));
					provision.setDateSendedSMS(dateSendedSMS);

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
					if (provision.getDummyStPsiCode() != null) {
						if (!provision.getDummyStPsiCode().isEmpty()) {
							provision.setHasSendedSMS(sendedSMSInvitationHasSchedule(provision) ? true : false);

						} else {
							provision.setHasSendedSMS(sendedSMSInvitationNotSchedule(provision) ? true : false);
						}
					}

					// update psiCode by schedule

					trazabilidadScheduleApi.updatePSICodeReal(provision.getIdProvision(), provision.getXaRequest(),
							getData[4], getData[8].toLowerCase());

					provisionRepository.updateProvision(provision, update);

					return true;
				}
			}

			if (request.getStatus().equalsIgnoreCase(Status.WO_PRESTART.getStatusName())
					&& !provision.getXaIdSt().isEmpty()) {

				Update update = new Update();
				update.set("external_id", getData[1]);
				update.set("active_status", Constants.PROVISION_STATUS_SCHEDULE_IN_PROGRESS);

				WoPreStart woPreStart = new WoPreStart();

				woPreStart.setNameResource(getData[3]);
				woPreStart.setDate(getData[4]);
				update.set("wo_prestart", woPreStart);
				
				update.set("show_location", false);

				StatusLog statusLog = new StatusLog();
				statusLog.setStatus(Status.WO_PRESTART.getStatusName());
				statusLog.setDescription(Status.WO_PRESTART.getDescription());
				statusLog.setXaidst(provision.getXaIdSt());

				update.set("last_tracking_status", Status.WO_PRESTART.getStatusName());
				listLog.add(statusLog);
				update.set("log_status", listLog);

				provisionRepository.updateProvision(provision, update);
				return true;

			}

			if (request.getStatus().equalsIgnoreCase(Status.WO_INIT.getStatusName())
					&& !provision.getXaIdSt().isEmpty()) {

				Update update = new Update();

				WoInit woInit = new WoInit();

				woInit.setNameResource(getData[2]);
				woInit.setEtaStartTime(getData[10]);
				woInit.setEtaEndTime(getData[2]);
				woInit.setXaCreationDate(getData[6]);
				woInit.setDate(getData[23]);
				woInit.setXaNote(getData[15]);
				update.set("wo_init", woInit);
				update.set("show_location", false);

				StatusLog statusLog = new StatusLog();

				statusLog.setStatus(Status.WO_INIT.getStatusName());
				statusLog.setDescription(Status.WO_INIT.getDescription());
				statusLog.setXaidst(provision.getXaIdSt());

				update.set("last_tracking_status", Status.WO_INIT.getStatusName());
				listLog.add(statusLog);
				update.set("log_status", listLog);

				provisionRepository.updateProvision(provision, update);
				return true;

			}

			if (request.getStatus().equalsIgnoreCase(Status.WO_COMPLETED.getStatusName())
					&& !provision.getXaIdSt().isEmpty()) {
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

				StatusLog statusLog = new StatusLog();

				statusLog.setStatus(Status.WO_COMPLETED.getStatusName());
				statusLog.setDescription(Status.WO_COMPLETED.getDescription());
				statusLog.setXaidst(provision.getXaIdSt());

				update.set("last_tracking_status", Status.WO_COMPLETED.getStatusName());
				listLog.add(statusLog);
				update.set("log_status", listLog);

				provisionRepository.updateProvision(provision, update);
				return true;

			}

			if (request.getStatus().equalsIgnoreCase(Status.WO_CANCEL.getStatusName())
					&& !provision.getXaIdSt().isEmpty()) {
				Update update = new Update();

				WoCancel woCancel = new WoCancel();
				woCancel.setUserCancel(getData[15]);
				woCancel.setXaCancelReason(getData[16]);
				update.set("wo_cancel", woCancel);
				// update.set("active_status", Constants.PROVISION_STATUS_CANCELLED);

				StatusLog statusLog = new StatusLog();
				statusLog.setStatus(Status.WO_CANCEL.getStatusName());
				statusLog.setDescription(Status.WO_CANCEL.getDescription());
				statusLog.setXaidst(provision.getXaIdSt());

				update.set("send_notify", false);
				update.set("xa_cancel_reason", getData[16]);
				update.set("user_cancel", getData[15]);
				update.set("last_tracking_status", Status.WO_CANCEL.getStatusName());
				
				update.set("show_location", false);
				
				listLog.add(statusLog);
				update.set("log_status", listLog);

				// Actualiza estado en provision
				provisionRepository.updateProvision(provision, update);

				// Cancela agenda
				trazabilidadScheduleApi.updateCancelSchedule(new CancelRequest(provision.getIdProvision(),
						provision.getActivityType().toLowerCase(), provision.getXaIdSt()));

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
				// el que parsea
				SimpleDateFormat parseador = new SimpleDateFormat("dd-MM-yy");
				// el que formatea
				SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd");

				Date date = parseador.parse(getData[16]);// ("31-03-2016");
				System.out.println("Fecha de reschedule => " + formateador.format(date));
				String dateString = formateador.format(date);

				if ((identificadorSt == null || identificadorSt.isEmpty())
						&& (rangeFinal == null || rangeFinal.isEmpty())
						&& (dateString == null || dateString.isEmpty())) {
					return false;
				}

				List<StatusLog> listLogx = listLog.stream()
						.filter(x -> "SCHEDULED".equals(x.getStatus()) && identificadorSt.equals(x.getXaidst())
								&& rangeFinal.equals(x.getScheduledRange()) && dateString.equals(x.getScheduledDate()))
						.collect(Collectors.toList());

				if (listLogx.size() > 0) {
					return true;
				}

				woReshedule.setXaAppointmentScheduler(getData[23]);
				woReshedule.setTimeSlot(range);
				update.set("wo_schedule", woReshedule);
				update.set("active_status", Constants.PROVISION_STATUS_ACTIVE);

				StatusLog statusLog = new StatusLog();

				statusLog.setStatus(Status.SCHEDULED.getStatusName());
				statusLog.setDescription(Status.SCHEDULED.getDescription());
				statusLog.setXaidst(provision.getXaIdSt());

				update.set("date", getData[16]);
				update.set("send_notify", false);
				update.set("time_slot", range);
				update.set("last_tracking_status", Status.SCHEDULED.getStatusName());
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

				update.set("active_status", Constants.PROVISION_STATUS_NOTDONE);

				StatusLog statusLog = new StatusLog();

				statusLog.setStatus(Status.WO_NOTDONE.getStatusName());
				statusLog.setDescription(Status.WO_NOTDONE.getDescription());
				statusLog.setXaidst(provision.getXaIdSt());

				update.set("a_observation", getData[27]);
				update.set("user_notdone", getData[28]);
				update.set("last_tracking_status", Status.WO_NOTDONE.getStatusName());
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
				scheduleNotDoneRequest.setStPsiCode(getData[9]);

				// Cancela agenda sin ir a PSI
				trazabilidadScheduleApi.cancelSchedule(scheduleNotDoneRequest);

				return true;
			}
		}
		return false;
	}

	private boolean sendedSMSInvitationNotSchedule(Provision provision) {

		List<MsgParameter> msgParameters = new ArrayList<>();

		List<Contact> contacts = new ArrayList<>();

		Contact contactCustomer = new Contact();
		contactCustomer.setPhoneNumber(provision.getCustomer().getPhoneNumber());
		contactCustomer.setIsMovistar(provision.getCustomer().getCarrier());
		contacts.add(contactCustomer);

		ApiResponse<SMSByIdResponse> apiResponse = trazabilidadSecurityApi.sendSMS(contacts,
				ConstantsMessageKey.MSG_NOT_SCHEDUEL_TEST_KEY, msgParameters.toArray(new MsgParameter[0]),
				provisionTexts.getWebUrl());

		if (apiResponse != null) {
			return true;
		}
		return false;

	}

	private boolean sendedSMSInvitationHasSchedule(Provision provision) {

		List<MsgParameter> msgParameters = new ArrayList<>();

		List<Contact> contacts = new ArrayList<>();

		Contact contactCustomer = new Contact();
		contactCustomer.setPhoneNumber(provision.getCustomer().getPhoneNumber());
		contactCustomer.setIsMovistar(provision.getCustomer().getCarrier());
		contacts.add(contactCustomer);

		ApiResponse<SMSByIdResponse> apiResponse = trazabilidadSecurityApi.sendSMS(contacts,
				ConstantsMessageKey.MSG_HAS_SCHEDUEL_TEST_KEY, msgParameters.toArray(new MsgParameter[0]),
				provisionTexts.getWebUrl());
		if (apiResponse != null) {
			return true;
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
		Boolean errorValidate = false;

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
									errorValidate = true;
									break;
								}
							}
							if (errorValidate) {
								break;
							}
						}
						if (errorValidate) {
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
	public ProvisionResponse<List<Contacts>> getContactList(String provisionId) {
		Optional<Provision> optional = provisionRepository.getStatus(provisionId);
		ProvisionResponse<List<Contacts>> response = new ProvisionResponse<List<Contacts>>();
		ProvisionHeaderResponse header = new ProvisionHeaderResponse();

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

			header.setCode(HttpStatus.OK.value()).setMessage(HttpStatus.OK.name());
			response.setHeader(header).setData(provision.getContacts());
		} else {
			header.setCode(HttpStatus.OK.value()).setMessage("No se encontraron provisiones");
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
			// Actualiza Flag de envio Notify en BD
			provisionRepository.updateFlagNotify(optional.get());
			for (int i = 0; i < listita.size(); i++) {
				List<StatusLog> list = listita.get(i).getLogStatus();
				if (Constants.STATUS_WO_CANCEL.equalsIgnoreCase(listita.get(i).getLastTrackingStatus())
						&& (!Status.FICTICIOUS_SCHEDULED.getStatusName().equalsIgnoreCase(list.get(list.size()-2).getStatus()) && !Status.SCHEDULED.getStatusName().equalsIgnoreCase(list.get(list.size()-2).getStatus()))) {
					listita.remove(i);
					i--;
				}
			}
			return listita;
		}
		return null;
	}

	@Override
	public boolean updateShowLocation(Provision provision){
		return provisionRepository.updateShowLocation(provision);
	}
}