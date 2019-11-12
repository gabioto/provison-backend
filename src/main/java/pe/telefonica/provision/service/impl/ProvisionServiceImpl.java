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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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
import pe.telefonica.provision.model.Contacts;
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.Provision.StatusLog;
import pe.telefonica.provision.model.Queue;
import pe.telefonica.provision.repository.ProvisionRepository;
import pe.telefonica.provision.service.ProvisionService;
import pe.telefonica.provision.service.request.BORequest;
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
			provisions = provisionRepository.findAll("CEX", provisionRequest.getBody().getDocumentType());
		}

		if (provisions.get().size() == 0 && provisionRequest.getBody().getDocumentType().equals("PASAPORTE")) {
			provisions = provisionRepository.findAll("PAS", provisionRequest.getBody().getDocumentType());
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
	
	@Override
	public boolean insertProvision(InsertOrderRequest request) {
		
		String data = "VENTASFIJA_PARKUR|TGESTIONA_FVD|MT15149|ARMANDO AUGUSTO CALVO QUIROZ CALVO QUIROZ|07578669|987654321|AVDMIRO QUESADA, AURELIO          260   INT:1401 - URB SANTA INES-|LIMA|LIMA|SAN ISIDRO|Movistar Total 24GB TV Estándar Digital HD 60Mbps||MIGRACION|DNI||CAIDA|07/11/2019 15:56:57|05/11/2019 20:40:57||pruebas@hotmail.com||TRIO||||59|No||59|||MOVISTAR TOTAL|||NO APLICA||||||||ATIS||||||||||||||||||||||||||||||||||||||";
		
		String getData[] = data.split("\\|");
		
		System.out.println(getData);
		Provision provision = new Provision();
		
		System.out.println(getData[3]);
		
		provision.setSaleCode(getData[2]);
		
		
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
		
			
		
		provision.setCustomer(customer);

		
		
		provisionRepository.insertProvision(provision);
		//Provision provision = provisionRepository.getProvisionBySaleCode("as");
		
	
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
		mailParameter3.setParamValue(provision.getCustomer().getContactName());
		mailParameters.add(mailParameter3);

		MailParameter mailParameter4 = new MailParameter();
		mailParameter4.setParamKey("CONTACTID");
		mailParameter4.setParamValue(provision.getCustomer().getContactPhoneNumber().toString());
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
			provision.getCustomer().setContactName(contactFullname);
			provision.getCustomer().setContactPhoneNumber(Integer.valueOf(contactCellphone));
			provision.getCustomer().setContactCarrier(contactCellphoneIsMovistar.toString());

			// boolean contactUpdated = provisionRepository.updateContactInfoPsi(provision);
			boolean contactUpdated = restPSI.updatePSIClient(provision);

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
	public ProvisionResponse<Boolean> updateOrderSchedule(String provisionId,
			                                              LocalDate scheduledDate,
            											  String scheduledRange,
            											  Integer scheduledType) {
		Optional<Provision> optional = provisionRepository.getProvisionById(provisionId);
		ProvisionResponse<Boolean> response = new ProvisionResponse<Boolean>();
		ProvisionHeaderResponse header = new ProvisionHeaderResponse();
		try {
			if (optional.isPresent()) {
				Provision provision = optional.get();
				String nomEstado = "";
				
				if(scheduledType == 2)
					nomEstado = Status.FICTICIOUS_SCHEDULED.getStatusName();
				else
					nomEstado = Status.SCHEDULED.getStatusName();
				
				
				boolean updated = updateTrackingStatus(provision.getXaRequest(), provision.getXaIdSt(),
						nomEstado, true, scheduledDate, scheduledRange, scheduledType);

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
	public Boolean updateTrackingStatus(String xaRequest, 
			                            String xaIdSt, 
			                            String status, 
			                            boolean comesFromSchedule,
			                            LocalDate scheduledDate,
			                            String scheduledRange,
			                            Integer scheduleType) {
		boolean updated = false;
		Optional<Provision> optionalProvision = provisionRepository.getProvisionByXaRequestAndSt(xaRequest, xaIdSt);
		log.info(ProvisionServiceImpl.class.getCanonicalName() + " - updateTrackingStatus: xaRequest = " + xaRequest
				+ ", xaIdSt =" + xaIdSt + ", status = " + status);

		if (optionalProvision.isPresent()) {
			Provision provision = optionalProvision.get();
			List<StatusLog> logStatus = provision.getLogStatus() == null ? new ArrayList<>() : provision.getLogStatus();

			StatusLog statusLog = new StatusLog();
			statusLog.setStatus(status);
			
			if(scheduledDate != null)
				statusLog.setScheduledDate(scheduledDate);
			
			if(scheduledRange != null && !scheduledRange.equals(""))
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

		Provision provision = provisionRepository.getProvisionByXaIdSt(request.getPsiCode());

		if (provision != null) {

			// Provision provision = optional.get();
			List<ContactRequest> listContact = request.getContacts();
			List<Contacts> contactsList = new ArrayList<>();
			
			String contactName1 = "";
			String contactName2 = "";
			String contactName3 = "";
			String contactName4 = "";
			
			Integer contactPhone1 = 0;
			Integer contactPhone2 = 0;
			Integer contactPhone3 = 0;
			Integer contactPhone4 = 0;
			
			for (int a = 0; a < 4; a++) {
				
				int quanty_contact = request.getContacts().size();
				
				if(a < quanty_contact) {
					
					Contacts contacts = new Contacts();
					contacts.setFullName(listContact.get(a).getFullName());
					contacts.setPhoneNumber(listContact.get(a).getPhoneNumber().toString());
					
					contactsList.add(contacts);
				}
				
				
				

				if (a == 0) {
					contactName1 = a < quanty_contact ? listContact.get(a).getFullName(): null;
					contactPhone1 = a < quanty_contact ? listContact.get(a).getPhoneNumber(): 0;
							
					provision.getCustomer().setContactName(a < quanty_contact ? listContact.get(a).getFullName(): null);
					provision.getCustomer().setContactPhoneNumber(a < quanty_contact ? listContact.get(a).getPhoneNumber(): 0);
				}
				if (a == 1) {
					
					contactName2 = a < quanty_contact ? listContact.get(a).getFullName(): null;
					contactPhone2 = a < quanty_contact ? listContact.get(a).getPhoneNumber(): 0;
					
					provision.getCustomer().setContactName1(a < quanty_contact ? listContact.get(a).getFullName(): null);
					provision.getCustomer().setContactPhoneNumber1(a < quanty_contact ? listContact.get(a).getPhoneNumber(): 0);
				}
				if (a == 2) {
					
					contactName3 = a < quanty_contact ? listContact.get(a).getFullName(): null;
					contactPhone3 = a < quanty_contact ? listContact.get(a).getPhoneNumber(): 0;
					
					provision.getCustomer().setContactName2(a < quanty_contact ? listContact.get(a).getFullName(): null);
					provision.getCustomer().setContactPhoneNumber2(a < quanty_contact ? listContact.get(a).getPhoneNumber(): 0);
				}
				if (a == 3) {
					
					contactName4 = a < quanty_contact ? listContact.get(a).getFullName(): null;
					contactPhone4 = a < quanty_contact ? listContact.get(a).getPhoneNumber(): 0;
					
					provision.getCustomer().setContactName3(a < quanty_contact ? listContact.get(a).getFullName(): null);
					provision.getCustomer().setContactPhoneNumber3(a < quanty_contact ? listContact.get(a).getPhoneNumber(): 0);
				}

			}
			provision.getCustomer().setMail(request.getEmail());
			
			
			provision.setContacts(contactsList);
		
			
			/*provision.getCustomer().setContactName(contactFullname);
			provision.getCustomer().setContactPhoneNumber(Integer.valueOf(contactCellphone));
			provision.getCustomer().setContactCarrier(contactCellphoneIsMovistar.toString());*/

			// boolean contactUpdated = provisionRepository.updateContactInfoPsi(provision);
			
				restPSI.updatePSIClient(provision);

			
				Update update = new Update();
				
				update.set("customer.contact_name", contactName1);
				update.set("customer.contact_name1", contactName2);
				update.set("customer.contact_name2", contactName3);
				update.set("customer.contact_name3", contactName4);
				
				update.set("customer.mail", request.getEmail());
				
				update.set("customer.contact_phone_number", Integer.valueOf(contactPhone1));
				update.set("customer.contact_phone_number1", Integer.valueOf(contactPhone2));
				update.set("customer.contact_phone_number2", Integer.valueOf(contactPhone3));
				update.set("customer.contact_phone_number3", Integer.valueOf(contactPhone4));
				
				update.set("contacts", contactsList);
				//update.set("customer.contact_carrier", contactCellphoneIsMovistar.toString());
				
				provisionRepository.updateProvision(provision, update);
				
				return true;
			
		} else {

			return false;
		}

	}

	@Override
	public boolean provisionInsertCodeFictional(InsertCodeFictionalRequest request) {
		
		Provision provision = provisionRepository.getProvisionBySaleCode(request.getSaleCode());
		if(provision != null) {
			Update update = new Update();
			update.set("sale_code", request.getSaleCode());
			
			provisionRepository.updateProvision(provision, update);
			
		} else {
			
			Provision provisionAdd = new Provision();
			
			provisionAdd.setSaleCode(request.getSaleCode());
			provisionAdd.setXaIdStFict(request.getFictionalCode());
			
			provisionRepository.insertProvision(provisionAdd);
			
			
		}
		
		return true;
	}

	@Override
	public boolean provisionUpdateFromTOA(UpdateFromToaRequest request) {
		
		Provision provision = provisionRepository.getByOrderCodeForUpdate(request.getOrderCode());
		if(provision != null) {
			
			Update update = new Update();
			update.set("xaRequest", request.getOrderCode());
			update.set("product_name", "update test from toa");
			
			provisionRepository.updateProvision(provision, update);
			return true;
		} 
			
		return false;
	}

	public boolean getCarrier(String phoneNumber) {
		return restPSI.getCarrier(phoneNumber);
	}

	@Override
	public Customer getCustomerByOrderCode(String orderCode) {
		
		Provision provision = provisionRepository.getByOrderCodeForUpdate(orderCode);
		
		if(provision != null) {
			return provision.getCustomer();
		}
		return null;
	}

}
