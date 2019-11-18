package pe.telefonica.provision.service.impl;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import pe.telefonica.provision.conf.ExternalApi;
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
import pe.telefonica.provision.controller.request.SMSByIdRequest.Message.MsgParameter;
import pe.telefonica.provision.controller.request.UpdateFromToaRequest;
import pe.telefonica.provision.controller.response.ProvisionHeaderResponse;
import pe.telefonica.provision.controller.response.ProvisionResponse;
import pe.telefonica.provision.controller.response.SMSByIdResponse;
import pe.telefonica.provision.external.BOApi;
import pe.telefonica.provision.external.PSIApi;
import pe.telefonica.provision.external.TrazabilidadScheduleApi;
import pe.telefonica.provision.external.TrazabilidadSecurityApi;
import pe.telefonica.provision.external.request.ScheduleUpdateFicticiousRequest;
import pe.telefonica.provision.model.Contacts;
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.model.Internet;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.Provision.StatusLog;
import pe.telefonica.provision.model.Queue;
import pe.telefonica.provision.model.Television;
import pe.telefonica.provision.model.provision.InToa;
import pe.telefonica.provision.model.provision.WoCompleted;
import pe.telefonica.provision.model.provision.WoInit;
import pe.telefonica.provision.model.provision.WoPreStart;
import pe.telefonica.provision.repository.ProvisionRepository;
import pe.telefonica.provision.service.ProvisionService;
import pe.telefonica.provision.service.request.PSIUpdateClientRequest;
import pe.telefonica.provision.util.constants.Constants;
import pe.telefonica.provision.util.constants.ConstantsLogData;
import pe.telefonica.provision.util.constants.Status;

@Service("provisionService")
@Transactional
public class ProvisionServiceImpl implements ProvisionService {

	private static final Log log = LogFactory.getLog(ProvisionServiceImpl.class);
	private ProvisionRepository provisionRepository;

	@Autowired
	private ExternalApi api;

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
		ProvisionResponse<Customer> response = new ProvisionResponse<Customer>();
		ProvisionHeaderResponse header = new ProvisionHeaderResponse();

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
		/*
		 * ProvisionArrayResponse<Provision> response = new
		 * ProvisionArrayResponse<Provision>(); ProvisionHeaderResponse header = new
		 * ProvisionHeaderResponse();
		 */

		if (provisions.get().size() == 0 && provisionRequest.getBody().getDocumentType().equals("CE")) {
			provisions = provisionRepository.findAll("CEX", provisionRequest.getBody().getDocumentNumber());
		}

		if (provisions.get().size() == 0 && provisionRequest.getBody().getDocumentType().equals("PASAPORTE")) {
			provisions = provisionRepository.findAll("PAS", provisionRequest.getBody().getDocumentNumber());
		}

		if (provisions.isPresent() && !provisions.get().isEmpty()) {
			return provisions.get();
			/*
			 * header.setCode(HttpStatus.OK.value()).setMessage(HttpStatus.OK.name());
			 * response.setHeader(header).setData(provisions.get());
			 */
		} else {
			return null;

			/*
			 * header.setCode(HttpStatus.OK.value()).
			 * setMessage("No se encontraron provisiones"); response.setHeader(header);
			 */
		}

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
								"newST = " + newProvision.getXaIdSt(), ConstantsLogData.PROVISION_UPDATE_ST);
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
		provision.setXaIdSt("");
		provision.setDummyStPsiCode("");
		provision.setOriginCode("");

		provision.setCommercialOp(getData[12]);
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
		provision.setActiveStatus(Status.PENDIENTE.getStatusName());
		provision.setStatusToa(Constants.PROVISION_STATUS_INCOMPLETE);

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
		customer.setDocumentType(getData[13]);
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
		
		if(!request.getStatus().equalsIgnoreCase(Status.PENDIENTE.getStatusName())) {
			StatusLog statusLogCurrent = new StatusLog();
			statusLogCurrent.setStatus(request.getStatus());
			statusLogCurrent.setDescription(request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName()) ? Status.INGRESADO.getDescription() : Status.CAIDO.getDescription() );
			
			listLog.add(statusLogCurrent);
			
			provision.setLastTrackingStatus(request.getStatus());
			provision.setActiveStatus(request.getStatus().toLowerCase());
			
		}
		
		
		provision.setLogStatus(listLog);

		return provision;
	}

	private Update fillProvisionUpdate(String data) {
		String getData[] = data.split("\\|", -1);
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

		// provision.setCommercialOp(getData[12]);
		update.set("commercial_op", getData[12]);
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

		//String data = "VENTASFIJA_PARKUR|TGESTIONA_FVD|MT15149|ARMANDO AUGUSTO CALVO QUIROZ CALVO QUIROZ|07578669|987654321|AVDMIRO QUESADA, AURELIO          260   INT:1401 - URB SANTA INES-|LIMA|LIMA|SAN ISIDRO|Movistar Total 24GB TV Estándar Digital HD 60Mbps||MIGRACION|DNI||CAIDA|07/11/2019 15:56:57|05/11/2019 20:40:57||pruebas@hotmail.com||TRIO||||59|No||59|||MOVISTAR TOTAL|||NO APLICA||||||||ATIS||||||||||||||||||||||||||||||||||||||";
		// String getData[] = data.split("\\|");
		String getData[] = request.getData().split("\\|");
		Provision provisionx = provisionRepository.getProvisionBySaleCode(getData[2]);
		if (provisionx != null) {

			Update update = fillProvisionUpdate(request.getData());

			List<StatusLog> listLog = provisionx.getLogStatus();

			StatusLog statusLog = new StatusLog();
			statusLog.setStatus(request.getStatus());
			statusLog.setDescription(request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName()) ? Status.INGRESADO.getDescription() : Status.CAIDO.getDescription() );
			
			if(request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName()) && !provisionx.getDummyStPsiCode().isEmpty()) {
				ScheduleUpdateFicticiousRequest updateFicRequest = new ScheduleUpdateFicticiousRequest();
				updateFicRequest.setOrderCode(getData[11]);
				updateFicRequest.setOriginCode(provisionx.getOriginCode() );
				updateFicRequest.setSaleCode(provisionx.getSaleCode());
				updateFicRequest.setFictitiousCode(provisionx.getDummyStPsiCode());
				
				boolean updateFicticious = trazabilidadScheduleApi.updateFicticious(updateFicRequest);
				update.set("is_update_dummy_st_psi_code", updateFicticious ? true: false);
				
			}
			// status_toa
			update.set("active_status",
					request.getStatus().equalsIgnoreCase(Status.INGRESADO.getStatusName())
							? Status.INGRESADO.getStatusName().toLowerCase()
							: Constants.PROVISION_STATUS_CANCELLED);
			// update.set("status_toa",
			// request.getStatus().equalsIgnoreCase(ConstantsTracking.INGRESADO) ?
			// Constants.PROVISION_STATUS_ACTIVE: Constants.PROVISION_STATUS_CANCELLED);

			update.set("last_tracking_status", request.getStatus());
			
			listLog.add(statusLog);
			update.set("log_status", listLog);

			// provisionx.setLogStatus(listLog);

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
					ApiResponse<SMSByIdResponse> apiResponse = trazabilidadSecurityApi.sendSMS(provision.getCustomer(),
							Constants.MSG_PRO_CANCELLED_BY_CUSTOMER_KEY, msgParameters.toArray(new MsgParameter[0]),
							"");
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

				// TODO: url como parametro?
				ApiResponse<SMSByIdResponse> apiResponse = trazabilidadSecurityApi.sendSMS(provision.getCustomer(),
						Constants.MSG_PRO_CUSTOMER_UNREACHABLE_KEY, msgParameters.toArray(new MsgParameter[0]),
						"http://www.movistar.com.pe");

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
					List<MsgParameter> msgParameters = new ArrayList<>();

					ApiResponse<SMSByIdResponse> apiResponse = trazabilidadSecurityApi.sendSMS(provision.getCustomer(),
							Constants.MSG_ADDRESS_UPDATED_KEY, msgParameters.toArray(new MsgParameter[0]),
							provisionTexts.getWebUrl());

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
	public Provision orderCancellation(String provisionId) {
		boolean sentBOCancellation;
		boolean messageSent;
		boolean provisionUpdated;
		boolean scheduleUpdated;
		Optional<Provision> optional = provisionRepository.getProvisionById(provisionId);

		if (optional.isPresent()) {

			Provision provision = optional.get();
			Update update = new Update();
			update.set("active_status", Constants.PROVISION_STATUS_CANCELLED);
			provision.setActiveStatus(Constants.PROVISION_STATUS_CANCELLED);

			// sentBOCancellation = sendCancellation(provision);
			sentBOCancellation = bOApi.sendRequestToBO(provision, "4");

			if (!sentBOCancellation) {
				return null;
			}

			if (provision.getHasSchedule()) {
				// scheduleUpdated = provisionRepository.updateCancelSchedule(new
				// CancelRequest(provision.getIdProvision(), "provision"));
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
				sendCancelledMailByUser(provision, Constants.ADDRESS_CANCELLED_BY_CUSTOMER);
			} catch (Exception e) {
				log.info(ProvisionServiceImpl.class.getCanonicalName() + ": " + e.getMessage());
			}

			String name = provision.getCustomer().getName().split(" ")[0];

			List<MsgParameter> msgParameters = new ArrayList<>();
			MsgParameter paramName = new MsgParameter();
			paramName.setKey(Constants.TEXT_NAME_REPLACE);
			paramName.setValue(name);

			MsgParameter paramProduct = new MsgParameter();
			paramProduct.setKey(Constants.TEXT_PRODUCT_REPLACE);
			paramProduct.setValue(provision.getProductName());

			msgParameters.add(paramName);
			msgParameters.add(paramProduct);

			ApiResponse<SMSByIdResponse> apiResponse = trazabilidadSecurityApi.sendSMS(provision.getCustomer(),
					Constants.MSG_PRO_CANCELLED_BY_CUSTOMER_KEY, msgParameters.toArray(new MsgParameter[0]), "");

			// ApiResponse<SMSByIdResponse> apiResponse = sendSMS(provision.getCustomer(),
			// Constants.MSG_PRO_CANCELLED_BY_CUSTOMER_KEY, msgParameters.toArray(new
			// MsgParameter[0]), "");

			if (apiResponse.getHeader().getResultCode().equals(String.valueOf(HttpStatus.OK.value()))) {
				messageSent = true;
			} else {
				messageSent = false;
			}
			return messageSent ? provision : null;
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
			//provision.getCustomer().setContactName(contactFullname);
			//provision.getCustomer().setContactPhoneNumber(Integer.valueOf(contactCellphone));
			provision.getCustomer().setContactCarrier(contactCellphoneIsMovistar.toString());

			// boolean contactUpdated = provisionRepository.updateContactInfoPsi(provision);
			//boolean contactUpdated = restPSI.updatePSIClient(provision);
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

				List<MsgParameter> msgParameters = new ArrayList<>();
				// Nota: si falla el envio de SMS, no impacta al resto del flujo, por lo que no
				// se valida la respuesta
				// ApiResponse<SMSByIdResponse> apiResponse = sendSMS(provision.getCustomer(),
				// Constants.MSG_CONTACT_UPDATED_KEY, msgParameters.toArray(new
				// MsgParameter[0]), provisionTexts.getWebUrl());
				ApiResponse<SMSByIdResponse> apiResponse = trazabilidadSecurityApi.sendSMS(provision.getCustomer(),
						Constants.MSG_CONTACT_UPDATED_KEY, msgParameters.toArray(new MsgParameter[0]),
						provisionTexts.getWebUrl());

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
				}
				else {
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
				statusLog.setScheduledDate(scheduledDate);

			if (scheduledRange != null && !scheduledRange.equals(""))
				statusLog.setScheduledRange(scheduledRange);

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
					
					
					psiRequest.getBodyUpdateClient().setNombre_completo(a < quanty_contact ? listContact.get(a).getFullName() : "");
					psiRequest.getBodyUpdateClient().setTelefono1(a < quanty_contact ? listContact.get(a).getPhoneNumber().toString() : "");
					
					
				}
				if (a == 1) {
					

					psiRequest.getBodyUpdateClient().setNombre_completo2(a < quanty_contact ? listContact.get(a).getFullName() : "");
					psiRequest.getBodyUpdateClient().setTelefono2(a < quanty_contact ? listContact.get(a).getPhoneNumber().toString() : "");
				

				}
				if (a == 2) {
					psiRequest.getBodyUpdateClient().setNombre_completo3(a < quanty_contact ? listContact.get(a).getFullName() : "");
					psiRequest.getBodyUpdateClient().setTelefono3(a < quanty_contact ? listContact.get(a).getPhoneNumber().toString() : "");
				
					
				}
				if (a == 3) {
					psiRequest.getBodyUpdateClient().setNombre_completo4(a < quanty_contact ? listContact.get(a).getFullName() : "");
					psiRequest.getBodyUpdateClient().setTelefono4(a < quanty_contact ? listContact.get(a).getPhoneNumber().toString() : "");
				
				}

			}
			
			psiRequest.getBodyUpdateClient().setCorreo(request.getEmail());
			psiRequest.getBodyUpdateClient().setSolicitud(provision.getDummyStPsiCode());
			
			
			/*Customer customer = provision.getCustomer() != null ? provision.getCustomer() : new Customer();
			
			customer.setMail(request.getEmail());
			
			provision.setCustomer(customer);
			provision.setContacts(contactsList);*/

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

			//update.set("customer.contact_name", contactName1);
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
	public boolean provisionInsertCodeFictional(InsertCodeFictionalRequest request) {

		Provision provision = provisionRepository.getProvisionBySaleCode(request.getSaleCode());
		
		if (provision != null) {
			Update update = new Update();
			List<StatusLog> listLog = provision.getLogStatus();
			
			update.set("dummy_st_psi_code", request.getFictionalCode());
			
			StatusLog statusLog = new StatusLog();
			statusLog.setStatus(Status.FICTICIOUS_SCHEDULED.getStatusName());
			statusLog.setDescription(Status.FICTICIOUS_SCHEDULED.getDescription());
			
			statusLog.setScheduledDate(request.getScheduleDate());
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
			provisionAdd.setDummyStPsiCode(request.getFictionalCode());
			provisionAdd.setOriginCode(request.getOriginCode());
			provisionAdd.setActiveStatus(Status.PENDIENTE.getStatusName().toLowerCase());
			List<StatusLog> listLog = new ArrayList<>();
			
			StatusLog StatusPendiente = new StatusLog();
			StatusLog statusLogDummy = new StatusLog();
			StatusPendiente.setStatus(Status.PENDIENTE.getStatusName());
			
			statusLogDummy.setStatus(Status.FICTICIOUS_SCHEDULED.getStatusName());
			
			
			statusLogDummy.setScheduledDate(request.getScheduleDate());
			statusLogDummy.setScheduledRange(request.getScheduleRange());
			
			listLog.add(StatusPendiente);
			listLog.add(statusLogDummy);
			
			provisionAdd.setLogStatus(listLog);
			provisionAdd.setLastTrackingStatus(Status.FICTICIOUS_SCHEDULED.getStatusName());

			provisionRepository.insertProvision(provisionAdd);

		}

		return true;
	}

	@Override
	public boolean provisionUpdateFromTOA(UpdateFromToaRequest request) {

		Provision provision = provisionRepository.getByOrderCodeForUpdate(request.getOrderCode());
		List<StatusLog> listLog = provision.getLogStatus();
		String[] getData = request.getData().split("\\|", -1);

		if (provision != null) {
			if (request.getStatus().equalsIgnoreCase(Status.IN_TOA.getStatusName())) {
				
				
				Update update = new Update();
				update.set("xa_creation_date", getData[3]);
				update.set("xa_id_st", getData[4]);
				update.set("xa_requirement_number", getData[5]);
				update.set("appt_number", getData[6]);
				update.set("activity_type", getData[8]);
				update.set("work_zone", getData[16]);
				
				if(provision.getXaIdSt() != null || provision.getXaIdSt() != "") {
					
					update.set("has_schedule", false);
				}
				InToa inToa = new InToa();

				inToa.setXaNote(getData[9]);
				// update.set("xa_note", getData[9]);
				inToa.setDate(getData[15]);
				// update.set("date", getData[15]);
				inToa.setXaScheduler(getData[16]);
				// update.set("xa_scheduler", getData[16]);
				inToa.setLongitude(getData[18]);
				// update.set("longitude", getData[18]);
				inToa.setLatitude(getData[19]);
				// update.set("latitude", getData[19]);

				update.set("in_toa", inToa);
				update.set("active_status", Constants.PROVISION_STATUS_ACTIVE);
				update.set("status_toa", Constants.PROVISION_STATUS_DONE);
				
				StatusLog statusLog = new StatusLog();
				statusLog.setStatus(Status.IN_TOA.getStatusName());
				statusLog.setDescription(Status.IN_TOA.getDescription());
				
				update.set("last_tracking_status", Status.IN_TOA.getStatusName());
				listLog.add(statusLog);
				update.set("log_status", listLog);

				provisionRepository.updateProvision(provision, update);
				return true;
			}
			if (request.getStatus().equalsIgnoreCase(Status.WO_PRESTART.getStatusName())) {

				Update update = new Update();
				update.set("external_id", getData[1]);

				WoPreStart woPreStart = new WoPreStart();

				woPreStart.setNameResource(getData[3]);
				woPreStart.setDate(getData[4]);
				update.set("wo_prestart", woPreStart);

				StatusLog statusLog = new StatusLog();
				statusLog.setStatus(Status.WO_PRESTART.getStatusName());
				statusLog.setDescription(Status.WO_PRESTART.getDescription());
				
				update.set("last_tracking_status", Status.WO_PRESTART.getStatusName());
				listLog.add(statusLog);
				update.set("log_status", listLog);

				provisionRepository.updateProvision(provision, update);
				return true;

			}

			if (request.getStatus().equalsIgnoreCase(Status.WO_INIT.getStatusName())) {

				Update update = new Update();

				WoInit woInit = new WoInit();

				woInit.setNameResource(getData[2]);
				woInit.setEtaStartTime(getData[10]);
				woInit.setEtaEndTime(getData[2]);
				woInit.setXaCreationDate(getData[6]);
				woInit.setDate(getData[23]);
				woInit.setXaNote(getData[15]);
				update.set("wo_init", woInit);

				StatusLog statusLog = new StatusLog();

				statusLog.setStatus(Status.WO_INIT.getStatusName());
				statusLog.setDescription(Status.WO_INIT.getDescription());
				
				update.set("last_tracking_status", Status.WO_INIT.getStatusName());
				listLog.add(statusLog);
				update.set("log_status", listLog);

				provisionRepository.updateProvision(provision, update);
				return true;

			}

			if (request.getStatus().equalsIgnoreCase(Status.WO_COMPLETED.getStatusName())) {
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
				
				StatusLog statusLog = new StatusLog();

				statusLog.setStatus(Status.WO_COMPLETED.getStatusName());
				statusLog.setDescription(Status.WO_COMPLETED.getDescription());
				
				update.set("last_tracking_status", Status.WO_COMPLETED.getStatusName());
				listLog.add(statusLog);
				update.set("log_status", listLog);

				provisionRepository.updateProvision(provision, update);
				return true;

			}

		}

		return false;
	}

	public boolean getCarrier(String phoneNumber) {
		return restPSI.getCarrier(phoneNumber);
	}

	@Override
	public Customer getCustomerByOrderCode(String orderCode) {

		Provision provision = provisionRepository.getByOrderCodeForUpdate(orderCode);

		if (provision != null) {
			provision.getCustomer().setProductName(provision.getProductName());
			return provision.getCustomer();
		}
		return null;
	}

}
