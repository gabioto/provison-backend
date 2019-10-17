package pe.telefonica.provision.service.impl;

import java.text.SimpleDateFormat;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.common.ApiResponse;
import pe.telefonica.provision.controller.common.ResponseHeader;
import pe.telefonica.provision.controller.request.CancelRequest;
import pe.telefonica.provision.controller.request.MailRequest;
import pe.telefonica.provision.controller.request.MailRequest.MailParameter;
import pe.telefonica.provision.controller.request.ProvisionRequest;
import pe.telefonica.provision.controller.request.SMSByIdRequest;
import pe.telefonica.provision.controller.request.SMSByIdRequest.Contact;
import pe.telefonica.provision.controller.request.SMSByIdRequest.Message;
import pe.telefonica.provision.controller.request.SMSByIdRequest.Message.MsgParameter;
import pe.telefonica.provision.controller.request.SetContactInfoUpdateRequest;
import pe.telefonica.provision.controller.response.ProvisionArrayResponse;
import pe.telefonica.provision.controller.response.ProvisionHeaderResponse;
import pe.telefonica.provision.controller.response.ProvisionResponse;
import pe.telefonica.provision.controller.response.SMSByIdResponse;
import pe.telefonica.provision.conf.ExternalApi;
import pe.telefonica.provision.conf.IBMSecuritySeguridad;
import pe.telefonica.provision.conf.ProvisionTexts;
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.Queue;
import pe.telefonica.provision.repository.ProvisionRepository;
import pe.telefonica.provision.service.ProvisionService;
import pe.telefonica.provision.service.request.BORequest;
import pe.telefonica.provision.service.request.SMSRequest;
import pe.telefonica.provision.external.PSIApi;
import pe.telefonica.provision.external.BOApi;
import pe.telefonica.provision.external.TrazabilidadSecurityApi;
import pe.telefonica.provision.external.TrazabilidadScheduleApi;
import pe.telefonica.provision.util.exception.FunctionalErrorException;
import pe.telefonica.provision.util.constants.Constants;
import pe.telefonica.provision.util.exception.DataNotFoundException;
import pe.telefonica.provision.util.exception.ServerNotFoundException;

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
	private IBMSecuritySeguridad ibmSecuritySeguridad;
	
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
		Optional<Provision> provision = provisionRepository.getOrder(provisionRequest.getBody().getDocumentType(), provisionRequest.getBody().getDocumentNumber());
		ProvisionResponse<Customer> response = new ProvisionResponse<Customer>();
		ProvisionHeaderResponse header = new ProvisionHeaderResponse();
		
		if (!provision.isPresent() && provisionRequest.getBody().getDocumentType().equals("CE")) {
			provision = provisionRepository.getOrder("CEX", provisionRequest.getBody().getDocumentNumber());
		}
		
		if (!provision.isPresent() && provisionRequest.getBody().getDocumentType().equals("PASAPORTE")) {
			provision = provisionRepository.getOrder( "PAS", provisionRequest.getBody().getDocumentNumber());
		}

		if (provision.isPresent() && provision.get().getCustomer() != null) {
			
			Provision prov = provision.get();
			return prov.getCustomer();
			
			/*prov.getCustomer().setProductName(prov.getProductName());
			header.setCode(HttpStatus.OK.value()).setMessage(HttpStatus.OK.name());
			response.setHeader(header).setData(prov.getCustomer());*/
			
		} else {
			return null;
			
			/*header.setCode(HttpStatus.OK.value()).setMessage("No se encontraron datos del cliente");
			response.setHeader(header);*/
		}

		//return response;
	}

	@Override
	public List<Provision> getAll(ApiRequest<ProvisionRequest> provisionRequest) {
		
		
		
		Optional<List<Provision>> provisions = provisionRepository.findAll(provisionRequest.getBody().getDocumentType(), provisionRequest.getBody().getDocumentNumber());
		/*ProvisionArrayResponse<Provision> response = new ProvisionArrayResponse<Provision>();
		ProvisionHeaderResponse header = new ProvisionHeaderResponse();*/
		
		if (provisions.get().size() == 0 && provisionRequest.getBody().getDocumentType().equals("CE")) {
			provisions = provisionRepository.findAll("CEX", provisionRequest.getBody().getDocumentType());
		}
		
		if (provisions.get().size() == 0 && provisionRequest.getBody().getDocumentType().equals("PASAPORTE")) {
			provisions = provisionRepository.findAll("PAS", provisionRequest.getBody().getDocumentType());
		}
		
		if (provisions.isPresent() && !provisions.get().isEmpty()) {
			return provisions.get();
			/*header.setCode(HttpStatus.OK.value()).setMessage(HttpStatus.OK.name());
			response.setHeader(header).setData(provisions.get());*/
		} else {
			return null;
			
			/*header.setCode(HttpStatus.OK.value()).setMessage("No se encontraron provisiones");
			response.setHeader(header);*/
		}
		
	}

	@Override
	public List<Provision> insertProvisionList(List<Provision> provisionList) {
		
		Optional<List<Provision>> provisions = provisionRepository.insertProvisionList(provisionList);
		
		if (provisions.get().size() == provisionList.size()) {
			
			return provisions.get();
			
			//header.setCode(HttpStatus.OK.value()).setMessage(HttpStatus.OK.name());
		} else {
			return null;
			//header.setCode(HttpStatus.OK.value()).setMessage("No se encontraron provisiones");
		}
		//response.setHeader(header);
		//return response;
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
				//boolean sent = sendAddressChangeRequest(provision);
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
					ApiResponse<SMSByIdResponse> apiResponse = trazabilidadSecurityApi.sendSMS(provision.getCustomer(), Constants.MSG_PRO_CANCELLED_BY_CUSTOMER_KEY, msgParameters.toArray(new MsgParameter[0]), "");
					//ApiResponse<SMSByIdResponse> apiResponse = sendSMS(provision.getCustomer(), Constants.MSG_PRO_CANCELLED_BY_CUSTOMER_KEY, msgParameters.toArray(new MsgParameter[0]), "");

					try {
						//provisionRepository.sendCancelledMail(provision, name, "179829", Constants.ADDRESS_CANCELLED_BY_CUSTOMER);
						
						sendCancelledMail(provision, name, "179829" );
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
				
				//TODO: url como parametro?
				ApiResponse<SMSByIdResponse> apiResponse = trazabilidadSecurityApi.sendSMS(provision.getCustomer(), Constants.MSG_PRO_CUSTOMER_UNREACHABLE_KEY, msgParameters.toArray(new MsgParameter[0]), "http://www.movistar.com.pe");
				
				//ApiResponse<SMSByIdResponse> apiResponse = sendSMS(provision.getCustomer(), Constants.MSG_PRO_CUSTOMER_UNREACHABLE_KEY, msgParameters.toArray(new MsgParameter[0]), "http://www.movistar.com.pe");

				try {
					//provisionRepository.sendCancelledMail(provision, name, "179824", Constants.ADDRESS_UNREACHABLE);
					sendCancelledMail(provision, name, "179824" );
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
					
					ApiResponse<SMSByIdResponse> apiResponse = trazabilidadSecurityApi.sendSMS(provision.getCustomer(), Constants.MSG_ADDRESS_UPDATED_KEY, msgParameters.toArray(new MsgParameter[0]), provisionTexts.getWebUrl());
					
					//ApiResponse<SMSByIdResponse> apiResponse = sendSMS(provision.getCustomer(), Constants.MSG_ADDRESS_UPDATED_KEY, msgParameters.toArray(new MsgParameter[0]), provisionTexts.getWebUrl());
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
		
		boolean isSendMail = trazabilidadSecurityApi.sendMail(codeTemplate, mailParameters.toArray(new MailParameter[mailParameters.size()]));
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

			//sentBOCancellation = sendCancellation(provision);
			sentBOCancellation = bOApi.sendRequestToBO(provision, "4");

			if (!sentBOCancellation) {
				return null;
			}

			if (provision.getHasSchedule()) {
				//scheduleUpdated = provisionRepository.updateCancelSchedule(new CancelRequest(provision.getIdProvision(), "provision"));
				scheduleUpdated = trazabilidadScheduleApi.updateCancelSchedule(new CancelRequest(provision.getIdProvision(), "provision"));
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
			
			ApiResponse<SMSByIdResponse> apiResponse = trazabilidadSecurityApi.sendSMS(provision.getCustomer(), Constants.MSG_PRO_CANCELLED_BY_CUSTOMER_KEY, msgParameters.toArray(new MsgParameter[0]), "");
			
			//ApiResponse<SMSByIdResponse> apiResponse = sendSMS(provision.getCustomer(), Constants.MSG_PRO_CANCELLED_BY_CUSTOMER_KEY, msgParameters.toArray(new MsgParameter[0]), "");
			
			if(apiResponse.getHeader().getResultCode().equals(String.valueOf(HttpStatus.OK.value()))) {
				messageSent = true;
			}else {
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
		
		return trazabilidadSecurityApi.sendMail("179833", mailParameters.toArray(new MailParameter[0]));
		
		//return sendMail("179833", mailParameters.toArray(new MailParameter[0]));
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

		//return sendMail("179829", mailParameters.toArray(new MailParameter[0]));
		return trazabilidadSecurityApi.sendMail("179829", mailParameters.toArray(new MailParameter[0]));
	}

	/*private Boolean sendMail(String templateId, MailParameter[] mailParameters) {
		RestTemplate restTemplate = new RestTemplate();

		String sendMailUrl = api.getSecurityUrl() + api.getSendMail();
		MailRequest mailRequest = new MailRequest();
		mailRequest.setMailTemplateId(templateId);
		mailRequest.setMailParameters(mailParameters);

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", ibmSecuritySeguridad.getAuth());
		headersMap.add("X-IBM-Client-Id", ibmSecuritySeguridad.getClientId());
		headersMap.add("X-IBM-Client-Secret", ibmSecuritySeguridad.getClientSecret());

		HttpEntity<MailRequest> entity = new HttpEntity<MailRequest>(mailRequest, headersMap);

		ResponseEntity<String> responseEntity = restTemplate.postForEntity(sendMailUrl, entity, String.class);
		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			return true;
		} else {
			return false;
		}
	}*/

	/*private ApiResponse<SMSByIdResponse> sendSMS(Customer customer, String msgKey, MsgParameter[] msgParameters, String webURL) {
		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("X-IBM-Client-Id", ibmSecuritySeguridad.getClientId());
		headersMap.add("X-IBM-Client-Secret", ibmSecuritySeguridad.getClientSecret());
		headersMap.add("Authorization", ibmSecuritySeguridad.getAuth());
		headersMap.add("Content-Type", "application/json");
		
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		String url = api.getSecurityUrl() + api.getSendSMSById();
		
		SMSByIdRequest smsByIdRequest = new SMSByIdRequest();
		
		Message message = new Message();
		message.setMsgKey(msgKey);
		message.setMsgParameters(msgParameters);
		message.setWebURL(webURL);
		
		List<Contact> contacts = new ArrayList<>();
		
		Contact contactCustomer = new Contact();
		contactCustomer.setPhoneNumber(customer.getPhoneNumber().toString()); //TODO: Cambiar integer a string
		contactCustomer.setIsMovistar(Boolean.valueOf(customer.getCarrier()));
		
		Contact contactContact = new Contact();
		contactContact.setPhoneNumber(customer.getContactPhoneNumber().toString()); //TODO: Cambiar integer a string
		contactContact.setIsMovistar(Boolean.valueOf(customer.getContactCarrier()));
		
		contacts.add(contactCustomer);
		contacts.add(contactContact);
		
		smsByIdRequest.setContacts(contacts.toArray(new Contact[0]));
		smsByIdRequest.setMessage(message);
		

		ApiRequest<SMSByIdRequest> apiRequest = new ApiRequest<SMSByIdRequest>(Constants.APP_NAME_PROVISION, Constants.USER_PROVISION, Constants.OPER_SEND_SMS_BY_ID, smsByIdRequest);
		HttpEntity<ApiRequest<SMSByIdRequest>> entity = new HttpEntity<ApiRequest<SMSByIdRequest>>(apiRequest, headersMap);
		
		ParameterizedTypeReference<ApiResponse<SMSByIdResponse>>  parameterizedTypeReference = new ParameterizedTypeReference<ApiResponse<SMSByIdResponse>>(){};

		ResponseEntity<ApiResponse<SMSByIdResponse>> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, parameterizedTypeReference);
		
		return responseEntity.getBody();
	}*/

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
				

				//boolean contactUpdated = provisionRepository.updateContactInfoPsi(provision);
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
					//Nota: si falla el envio de SMS, no impacta al resto del flujo, por lo que no se valida la respuesta
					//ApiResponse<SMSByIdResponse> apiResponse = sendSMS(provision.getCustomer(), Constants.MSG_CONTACT_UPDATED_KEY, msgParameters.toArray(new MsgParameter[0]), provisionTexts.getWebUrl());
					ApiResponse<SMSByIdResponse> apiResponse = trazabilidadSecurityApi.sendSMS(provision.getCustomer(), Constants.MSG_CONTACT_UPDATED_KEY, msgParameters.toArray(new MsgParameter[0]), provisionTexts.getWebUrl());
					
					return provision;
				} else {
					return null;
				}
			} else {
				return null;
			}
		
		
		
	}
	
	
	/*private Boolean sendAddressChangeRequest(Provision provision) {
		return sendRequestToBO(provision, "3");
	}*/

	/*private Boolean sendCancellation(Provision provision) {
		return sendRequestToBO(provision, "4");
	}*/

	private Boolean sendRequestToBO(Provision provision, String action) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		String sendRequestBO = api.getBoUrl() + api.getSendRequestToBO();

		log.info("sendRequestToBO - BO - URL: " + sendRequestBO);

		String formattedDate = "";
		Date scheduledDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_BO);
		try {
			dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_BO);
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-5:00"));
			formattedDate = dateFormat.format(scheduledDate);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		BORequest boRequest = new BORequest();
		boRequest.setCodUser("47381888");
		boRequest.setDescription(provision.getProductName());
		boRequest.setNrodocumentotitular(provision.getCustomer().getDocumentNumber());
		boRequest.setNombretitular(provision.getCustomer().getName());
		boRequest.setTelefonotitular(String.valueOf(provision.getCustomer().getPhoneNumber()));
		boRequest.setTelefonocontacto(String.valueOf(provision.getCustomer().getContactPhoneNumber()));
		boRequest.setNombrecontacto(provision.getCustomer().getContactName());
		boRequest.setCorreotitular(provision.getCustomer().getMail());
		boRequest.setDireccion(provision.getCustomer().getAddress());
		boRequest.setFechaagenda(formattedDate);
		boRequest.setFranja("");
		boRequest.setCodorigin(1); // 1 provision, 2 averia
		boRequest.setCodaction(action); // 1 agenda, 2 datos contacto, 3 datos direccion, 4 cancelar
		boRequest.setCodigoTraza(provision.getIdProvision());
		boRequest.setCodigostpsi(provision.getXaIdSt());
		boRequest.setCodigopedido(provision.getXaRequest());
		boRequest.setCarrier(Boolean.valueOf(provision.getCustomer().getCarrier()));

		// TODO: poner en parametros o properties
		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Access-Token", "iAJiahANAjahIOaoPAIUnIAUZPzOPIW");
		headersMap.add("Content-Type", "application/json");

		HttpEntity<BORequest> entityBO = new HttpEntity<BORequest>(boRequest, headersMap);

		try {
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(sendRequestBO, entityBO, String.class);

			log.info("sendRequestToBO - BO - Response: " + responseEntity.getBody());

			if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			log.info("Exception = " + e.getMessage());
			return false;
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
	public ProvisionResponse<Boolean> updateOrderSchedule(String provisionId) {
		Optional<Provision> optional = provisionRepository.getProvisionById(provisionId);
		ProvisionResponse<Boolean> response = new ProvisionResponse<Boolean>();
		ProvisionHeaderResponse header = new ProvisionHeaderResponse();

		if (optional.isPresent()) {
			Provision provision = optional.get();
			Update update = new Update();
			update.set("has_schedule", true);
			boolean updated = provisionRepository.updateProvision(provision, update);

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

		return response;
	}

	@Override
	public List<Provision> getAllInTimeRange(LocalDateTime startDate, LocalDateTime endDate) {
		Optional<List<Provision>> optional = provisionRepository.getAllInTimeRange(startDate, endDate.plusDays(1));
		
		if(optional.isPresent()) {
			return optional.get();
		}

		return null;
	}

	
}
