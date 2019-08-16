package pe.telefonica.provision.repository.impl;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.mongodb.client.result.UpdateResult;

import pe.telefonica.provision.api.request.MailRequest;
import pe.telefonica.provision.api.request.MailRequest.MailParameter;
import pe.telefonica.provision.api.request.ProvisionRequest;
import pe.telefonica.provision.conf.Constants;
import pe.telefonica.provision.conf.ExternalApi;
import pe.telefonica.provision.conf.IBMSecurity;
import pe.telefonica.provision.conf.IBMSecurityAgendamiento;
import pe.telefonica.provision.dto.Provision;
import pe.telefonica.provision.dto.Queue;
import pe.telefonica.provision.repository.ProvisionRepository;
import pe.telefonica.provision.service.request.PSIUpdateClientRequest;
import pe.telefonica.provision.service.response.PSIUpdateClientResponse;
import pe.telefonica.provision.util.DateUtil;

@Repository
public class ProvisionRepositoryImpl implements ProvisionRepository {

	private static final Log log = LogFactory.getLog(ProvisionRepositoryImpl.class);
	private final MongoOperations mongoOperations;

	@Autowired
	private ExternalApi api;

	@Autowired
	private IBMSecurityAgendamiento securitySchedule;
	
	@Autowired
	private IBMSecurity security;

	@Autowired
	public ProvisionRepositoryImpl(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}

	@Override
	public Optional<List<Provision>> findAll(ProvisionRequest provisionRequest) {
		List<Provision> provisions = this.mongoOperations.find(
				new Query(Criteria.where("customer.document_type").is(provisionRequest.getDocumentType())
						.and("customer.document_number").is(provisionRequest.getDocumentNumber())
						.orOperator(Criteria.where("active_status").is(Constants.PROVISION_STATUS_ACTIVE),
								Criteria.where("active_status").is(Constants.PROVISION_STATUS_ADDRESS_CHANGED))),
				Provision.class);
		Optional<List<Provision>> optionalProvisions = Optional.ofNullable(provisions);
		return optionalProvisions;
	}

	@Override
	public Optional<Provision> getOrder(ProvisionRequest provisionRequest) {
		Provision provision = this.mongoOperations.findOne(
				new Query(Criteria.where("customer.document_type").is(provisionRequest.getDocumentType())
						.and("customer.document_number").is(provisionRequest.getDocumentNumber())
						.orOperator(Criteria.where("active_status").is(Constants.PROVISION_STATUS_ACTIVE),
								Criteria.where("active_status").is(Constants.PROVISION_STATUS_ADDRESS_CHANGED))),
				Provision.class);
		Optional<Provision> optionalOrder = Optional.ofNullable(provision);
		return optionalOrder;
	}

	@Override
	public Optional<String> getStatus(String provisionId) {
		Provision provision = this.mongoOperations
				.findOne(new Query(Criteria.where("_id").is(new ObjectId(provisionId))), Provision.class);
		Optional<String> optionalOrder = Optional.ofNullable(provision.getActiveStatus());
		return optionalOrder;
	}

	@Override
	public Optional<List<Provision>> insertProvisionList(List<Provision> provisionRequestList) {
		List<Provision> provisions = (List<Provision>) this.mongoOperations.insertAll(provisionRequestList);
		Optional<List<Provision>> optionalProvisions = Optional.ofNullable(provisions);
		return optionalProvisions;
	}

	@Override
	public Optional<Provision> getProvisionById(String provisionId) {
		Provision provision = null;
		try {
			provision = this.mongoOperations.findOne(
					new Query(Criteria.where("idProvision").is(new ObjectId(provisionId)).orOperator(
							Criteria.where("active_status").is(Constants.PROVISION_STATUS_ACTIVE),
							Criteria.where("active_status").is(Constants.PROVISION_STATUS_ADDRESS_CHANGED))),
					Provision.class);
		} catch (Exception e) {
			log.info(e.getMessage());
		}

		Optional<Provision> optionalSchedule = Optional.ofNullable(provision);

		return optionalSchedule;
	}

	@Override
	public boolean updateProvision(Provision provision, Update update) {
		UpdateResult result = this.mongoOperations.updateFirst(
				new Query(Criteria.where("idProvision").is(new ObjectId(provision.getIdProvision()))), update,
				Provision.class);

		return result.getMatchedCount() > 0;
	}

	@Override
	public boolean updateContactInfoPsi(Provision provision) {
		log.info(this.getClass().getName() + " - " + "updateContactInfoPsi");
		boolean contactUpdated = updatePSIClient(provision);
		return contactUpdated;
	}

	@Override
	public boolean updateCancelSchedule(Provision provision) {
		RestTemplate restTemplate = new RestTemplate();
		String urlProvisionUser = api.getScheduleUrl() + api.getUpdateSchedule();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", securitySchedule.getAuth());
		headersMap.add("X-IBM-Client-Id", securitySchedule.getClientId());
		headersMap.add("X-IBM-Client-Secret", securitySchedule.getClientSecret());

		JsonObject jObject = new JsonObject();
		jObject.addProperty("requestId", provision.getIdProvision());
		jObject.addProperty("requestType", "provision");

		HttpEntity<JsonObject> entityProvision = new HttpEntity<JsonObject>(jObject, headersMap);

		try {
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(urlProvisionUser, entityProvision,
					String.class);
			log.info("responseEntity: " + responseEntity.getBody());

			return responseEntity.getStatusCode().equals(HttpStatus.OK);
		} catch (Exception e) {
			log.info("Exception = " + e.getMessage());
			return false;
		}
	}

	private Boolean updatePSIClient(Provision provision) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		String requestUrl = api.getPsiUrl() + api.getPsiUpdateClient();
		log.info("updatePSIClient - URL: " + requestUrl);

		PSIUpdateClientRequest request = new PSIUpdateClientRequest();
		request.getHeaderIn().setCountry("PE");
		request.getHeaderIn().setLang("es");
		request.getHeaderIn().setEntity("TDP");
		request.getHeaderIn().setSystem("COL");
		request.getHeaderIn().setSubsystem("TRA");
		request.getHeaderIn().setOriginator("PE:TDP:COL:TRA");
		request.getHeaderIn().setSender("OracleServiceBus");
		request.getHeaderIn().setUserId("USERTRA");
		request.getHeaderIn().setWsId("SistemTRA");
		request.getHeaderIn().setWsIp("192.168.100.1");
		request.getHeaderIn().setOperation("updateClient");
		request.getHeaderIn().setDestination("PE:TDP:COL:TRA");
		request.getHeaderIn().setExecId("550e8400-e29b-41d4-a716-446655440000");
		request.getHeaderIn().setTimestamp(DateUtil.getNowPsi(Constants.TIMESTAMP_FORMAT_PSI));
		request.getHeaderIn().setMsgType("REQUEST");

		request.getBodyUpdateClient().getUser().setNow(DateUtil.getNowPsi(Constants.TIMESTAMP_FORMAT_USER));
		request.getBodyUpdateClient().getUser().setLogin("appmovistar");
		request.getBodyUpdateClient().getUser().setCompany("telefonica-pe");
		request.getBodyUpdateClient().getUser().setAuth_string(generateAuthString());
		request.getBodyUpdateClient().setSolicitud(provision.getXaIdSt());
		request.getBodyUpdateClient().setNombre_completo(provision.getCustomer().getContactName());
		request.getBodyUpdateClient().setCorreo(provision.getCustomer().getMail());
		request.getBodyUpdateClient().setTelefono1(String.valueOf(provision.getCustomer().getContactPhoneNumber()));

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization",
				"Bearer AAIkNjcxMjg5ZWItM2EyMC00ZTE4LWIzNTMtMjMxZGU5MmJiMDQ3A64HPWrsJq7VB9o6hCBJwBgamLHvKGNE2r4v8I5VRuINqlNU6wAO36ZhWzFwwD2-Gti_ca3vMAwaycR6P6UkNZFq685zKjWAk9RBoO1_5q4");
		headersMap.add("X-IBM-Client-Id", "671289eb-3a20-4e18-b353-231de92bb047");

		HttpEntity<PSIUpdateClientRequest> entity = new HttpEntity<PSIUpdateClientRequest>(request, headersMap);

		try {
			ResponseEntity<PSIUpdateClientResponse> responseEntity = restTemplate.postForEntity(requestUrl, entity,
					PSIUpdateClientResponse.class);

			log.info("setSchedulePSI - Body: " + responseEntity.getBody());

			return responseEntity.getStatusCode().equals(HttpStatus.OK);
		} catch (Exception e) {
			log.info("Exception = " + e.getMessage());
			return false;
		}
	}

	private String stringToMD5(String string) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(string.getBytes(Charset.forName("UTF8")));
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
			log.error(e.getMessage());
		}
		return null;
	}

	private String generateAuthString() {
		String passMD5 = stringToMD5("aPpM0v1S7@R");
		String authString = stringToMD5(DateUtil.getNowPsi(Constants.DATE_FORMAT_PSI_AUTH) + passMD5);
		return authString;
	}

	@Override
	public Optional<Queue> isQueueAvailable() {
		Queue queue = null;
		try {
			queue = this.mongoOperations.findOne(new Query(Criteria.where("idContingencia").is("1")), Queue.class);
		} catch (Exception e) {
			log.info(e.getMessage());
		}

		Optional<Queue> optionalQueue = Optional.ofNullable(queue);

		return optionalQueue;
	}

	@Override
	public boolean sendCancelledMail(Provision provision, String name, String idTemplate) {
		RestTemplate restTemplate = new RestTemplate();
		String urlSendMail = api.getSecurityUrl() + api.getSendMail();
		Calendar cancelationDate = Calendar.getInstance();
		cancelationDate.setTime(new Date());
		String month = cancelationDate.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
		month = month.substring(0, 1).toUpperCase() + month.substring(1);
		int day = cancelationDate.get(Calendar.DAY_OF_MONTH);
		int year = cancelationDate.get(Calendar.YEAR);
		
		ArrayList<MailParameter> mailParameters = new ArrayList<>();
		
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
		mailParameter4.setParamValue(day + " de " + month + " de " + year);
		mailParameters.add(mailParameter4);
		
		MailParameter mailParameter5 = new MailParameter();
		mailParameter5.setParamKey("STOREURL");
		mailParameter5.setParamValue(api.getSecurityUrl());
		mailParameters.add(mailParameter5);

		
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", security.getAuth());
		headersMap.add("X-IBM-Client-Id", security.getClientId());
		headersMap.add("X-IBM-Client-Secret", security.getClientSecret());

		MailRequest mailRequest = new MailRequest();
		mailRequest.setMailParameters(mailParameters.toArray(new MailParameter[mailParameters.size()]));
		mailRequest.setMailTemplateId(idTemplate);

		HttpEntity<MailRequest> entityMail = new HttpEntity<MailRequest>(mailRequest, headersMap);

		try {
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(urlSendMail, entityMail,
					String.class);
			log.info("responseEntity: " + responseEntity.getBody());

			return responseEntity.getStatusCode().equals(HttpStatus.OK);
		} catch (Exception e) {
			log.info("Exception = " + e.getMessage());
			return false;
		}
	}
}
