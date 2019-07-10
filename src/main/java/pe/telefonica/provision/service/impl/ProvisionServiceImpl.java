package pe.telefonica.provision.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

import pe.telefonica.provision.api.request.ProvisionRequest;
import pe.telefonica.provision.api.response.ProvisionArrayResponse;
import pe.telefonica.provision.api.response.ProvisionHeaderResponse;
import pe.telefonica.provision.api.response.ProvisionResponse;
import pe.telefonica.provision.conf.Constants;
import pe.telefonica.provision.conf.ExternalApi;
import pe.telefonica.provision.conf.ProvisionTexts;
import pe.telefonica.provision.dto.Customer;
import pe.telefonica.provision.dto.Provision;
import pe.telefonica.provision.repository.ProvisionRepository;
import pe.telefonica.provision.service.ProvisionService;
import pe.telefonica.provision.service.request.BORequest;
import pe.telefonica.provision.service.request.SMSRequest;

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
	public ProvisionServiceImpl(ProvisionRepository provisionRepository) {
		this.provisionRepository = provisionRepository;
	}

	@Override
	public ProvisionResponse<Customer> validateUser(ProvisionRequest provisionRequest) {
		Optional<Provision> fault = provisionRepository.getOrder(provisionRequest);
		ProvisionResponse<Customer> response = new ProvisionResponse<Customer>();
		ProvisionHeaderResponse header = new ProvisionHeaderResponse();

		if (fault.isPresent() && fault.get().getCustomer() != null) {
			header.setCode(HttpStatus.OK.value()).setMessage(HttpStatus.OK.name());
			response.setHeader(header).setData(fault.get().getCustomer());
		} else {
			header.setCode(HttpStatus.OK.value()).setMessage("No se encontraron datos del cliente");
			response.setHeader(header);
		}

		return response;
	}

	@Override
	public ProvisionArrayResponse<Provision> getAll(ProvisionRequest provisionRequest) {
		Optional<List<Provision>> provisions = provisionRepository.findAll(provisionRequest);
		ProvisionArrayResponse<Provision> response = new ProvisionArrayResponse<Provision>();
		ProvisionHeaderResponse header = new ProvisionHeaderResponse();
		if (!provisions.get().isEmpty()) {
			header.setCode(HttpStatus.OK.value()).setMessage(HttpStatus.OK.name());
			response.setHeader(header).setData(provisions.get());
		} else {
			header.setCode(HttpStatus.OK.value()).setMessage("No se encontraron provisiones");
			response.setHeader(header);
		}
		return response;
	}

	@Override
	public ProvisionArrayResponse<Provision> insertProvisionList(List<Provision> provisionList) {
		Optional<List<Provision>> provisions = provisionRepository.insertProvisionList(provisionList);
		ProvisionArrayResponse<Provision> response = new ProvisionArrayResponse<Provision>();
		ProvisionHeaderResponse header = new ProvisionHeaderResponse();
		if (provisions.get().size() == provisionList.size()) {
			header.setCode(HttpStatus.OK.value()).setMessage(HttpStatus.OK.name());
		} else {
			header.setCode(HttpStatus.OK.value()).setMessage("No se encontraron provisiones");
		}
		response.setHeader(header);
		return response;
	}

	@Override
	public Boolean setProvisionIsValidated(String provisionId) {
		Optional<Provision> optional = provisionRepository.getProvisionById(provisionId);

		if (optional.isPresent()) {
			Provision provision = optional.get();
			Update update = new Update();
			update.set("validated_address", "true"); // TODO: CAMBIAR A BOOLEAN!!!

			boolean updated = provisionRepository.updateProvision(provision, update);

			if (updated) {
				return true;
			} else {
				return false;
			}

		} else {
			return false;
		}
	}

	@Override
	public Boolean requestAddressUpdate(String provisionId) {
		Optional<Provision> optional = provisionRepository.getProvisionById(provisionId);

		if (optional.isPresent()) {
			Provision provision = optional.get();
			Update update = new Update();
			update.set("active_status", Constants.PROVISION_STATUS_ADDRESS_CHANGED);

			boolean updated = provisionRepository.updateProvision(provision, update);

			if (updated) {
				// TODO: capturar respuesta del BO?
				sendAddressChangeRequest(provision);
				return true;
			} else {
				return false;
			}

		} else {
			return false;
		}
	}

	@Override
	public Boolean receiveAddressUpdateBO(String action, String provisionId, String newDepartment, String newProvince,
			String newDistrict, String newAddress, String newReference, boolean isSMSRequired) {
		Optional<Provision> optional = provisionRepository.getProvisionById(provisionId);

		if (optional.isPresent()) {
			Provision provision = optional.get();

			if (action.equals(Constants.ADDRESS_CANCELLED_BY_CUSTOMER)
					|| (action.equals(Constants.ADDRESS_CANCELLED_BY_CHANGE))) {

				Update update = new Update();
				update.set("active_status", Constants.PROVISION_STATUS_CANCELLED);

				boolean updated = provisionRepository.updateProvision(provision, update);

				/*
				 * if (updated) { if (action.equals(Constants.ADDRESS_CANCELLED_BY_CUSTOMER)) {
				 * // TODO: capturar respuesta del envio de sms?
				 * sendSMS(provision.getCustomer(), provisionTexts.getCancelled(), ""); } return
				 * true; } else { return false; }
				 */

				if (isSMSRequired) {
					sendSMS(provision.getCustomer(), provisionTexts.getUnreachable(), "");
				}

				return updated;
			} else if (action.equals(Constants.ADDRESS_UNREACHABLE)) {
				sendSMS(provision.getCustomer(), provisionTexts.getUnreachable(), "");
				return true;
			} else {
				Update update = new Update();
				update.set("active_status", Constants.PROVISION_STATUS_ACTIVE);
				update.set("customer.department", newDepartment);
				update.set("customer.province", newDepartment);
				update.set("customer.district", newDepartment);
				update.set("customer.address", newDepartment);
				update.set("customer.reference", newDepartment);

				boolean updated = provisionRepository.updateProvision(provision, update);

				if (updated) {
					sendSMS(provision.getCustomer(), provisionTexts.getAddressUpdated(), "");
					return true;
				} else {
					return false;
				}
			}
		} else {
			return false;
		}

	}

	@Override
	public Boolean orderCancellation(String provisionId) {
		Optional<Provision> optional = provisionRepository.getProvisionById(provisionId);

		if (optional.isPresent()) {
			Provision provision = optional.get();
			Update update = new Update();
			update.set("active_status", Constants.PROVISION_STATUS_CANCELLED);

			boolean updated = provisionRepository.updateProvision(provision, update);

			if (updated) {
				// TODO: capturar respuesta del envio de sms?
				sendSMS(provision.getCustomer(), provisionTexts.getCancelled(), "");
				// TODO: capturar respuesta del BO?
				sendCancellation(provision);
				return true;
			} else {
				return false;
			}

		} else {
			return false;
		}
	}

	private Boolean sendSMS(Customer customer, String message, String webURL) {
		RestTemplate restTemplate = new RestTemplate();

		String sendSMSUrl = api.getSecurityUrl() + api.getSendSMS();
		SMSRequest smsRequest = new SMSRequest();
		smsRequest.setCustomerPhone(String.valueOf(customer.getPhoneNumber()));
		Boolean customerPhoneIsMovistar = false;
		if (customer.getCarrier().equals("true")) {
			customerPhoneIsMovistar = true;
		}
		smsRequest.setCustomerPhoneIsMovistar(customerPhoneIsMovistar);
		smsRequest.setContactPhone(String.valueOf(customer.getContactPhoneNumber()));
		Boolean contactrPhoneIsMovistar = false;
		// TODO: Aplicar Validacion
		contactrPhoneIsMovistar = true;
		/*
		 * if (customer.getContactCarrier().equals("true")) { contactrPhoneIsMovistar =
		 * true; }
		 */
		smsRequest.setContactPhoneIsMovistar(contactrPhoneIsMovistar);
		smsRequest.setMessage(message);
		smsRequest.setWebURL("");

		ResponseEntity<String> responseEntity = restTemplate.postForEntity(sendSMSUrl, smsRequest, String.class);
		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Boolean setContactInfoUpdate(String provisionId, String contactFullname, String contactCellphone,
			Boolean contactCellphoneIsMovistar) {
		Optional<Provision> optional = provisionRepository.getProvisionById(provisionId);

		if (optional.isPresent()) {
			Provision provision = optional.get();

			Update update = new Update();
			update.set("customer.contact_name", contactFullname);
			update.set("customer.contact_phone_number", Integer.valueOf(contactCellphone));
			update.set("customer.contact_carrier", contactCellphoneIsMovistar.toString());
			provision.getCustomer().setContactName(contactFullname);
			provision.getCustomer().setContactPhoneNumber(Integer.valueOf(contactCellphone));
			provision.getCustomer().setContactCarrier(contactCellphoneIsMovistar.toString());

			boolean updated = provisionRepository.updateProvision(provision, update);

			if (updated) {
				
				boolean contactUpdated = provisionRepository.updateContactInfoPsi(provision);
				return contactUpdated;
			} else {
				return false;
			}

		} else {
			return false;
		}
		
		/*ProvisionArrayResponse<Provision> response = new ProvisionArrayResponse<Provision>();
		ProvisionHeaderResponse header = new ProvisionHeaderResponse();
		if (provisions.get().size() == provisionList.size()) {
			header.setCode(HttpStatus.OK.value()).setMessage(HttpStatus.OK.name());
		} else {
			header.setCode(HttpStatus.OK.value()).setMessage("No se encontraron provisiones");
		}
		response.setHeader(header);
		return response;*/
	}

	private Boolean sendAddressChangeRequest(Provision provision) {
		return sendRequestToBO(provision, "3");
	}

	private Boolean sendCancellation(Provision provision) {
		return sendRequestToBO(provision, "4");
	}

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
}
