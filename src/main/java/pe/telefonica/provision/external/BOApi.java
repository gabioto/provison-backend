package pe.telefonica.provision.external;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import pe.telefonica.provision.conf.ExternalApi;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.service.request.BORequest;
import pe.telefonica.provision.util.constants.Constants;

@Component
public class BOApi {
	private static final Log log = LogFactory.getLog(BOApi.class);
	
	@Autowired
	private ExternalApi api;
	
	public Boolean sendRequestToBO(Provision provision, String action) {
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
}
