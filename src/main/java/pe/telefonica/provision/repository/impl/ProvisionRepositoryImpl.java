package pe.telefonica.provision.repository.impl;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.mongodb.client.result.UpdateResult;

import pe.telefonica.provision.api.common.ApiRequest;
import pe.telefonica.provision.api.common.ApiResponse;
import pe.telefonica.provision.api.request.CancelRequest;
import pe.telefonica.provision.api.request.MailRequest;
import pe.telefonica.provision.api.request.MailRequest.MailParameter;
import pe.telefonica.provision.api.request.ProvisionRequest;
import pe.telefonica.provision.conf.Constants;
import pe.telefonica.provision.conf.ExternalApi;
import pe.telefonica.provision.conf.IBMSecuritySeguridad;
import pe.telefonica.provision.conf.IBMSecurityAgendamiento;
import pe.telefonica.provision.conf.SSLClientFactory;
import pe.telefonica.provision.conf.SSLClientFactory.HttpClientType;
import pe.telefonica.provision.dto.OAuthToken;
import pe.telefonica.provision.dto.Provision;
import pe.telefonica.provision.dto.Queue;
import pe.telefonica.provision.exception.ServerNotFoundException;
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
	private IBMSecuritySeguridad security;

	@Autowired
	public ProvisionRepositoryImpl(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}

	@Override
	public Optional<List<Provision>> findAll(ProvisionRequest provisionRequest, String documentType) {
		List<Provision> provisions = this.mongoOperations.find(
				new Query(Criteria.where("customer.document_type").is(documentType).and("customer.document_number")
						.is(provisionRequest.getDocumentNumber())
						.orOperator(Criteria.where("active_status").is(Constants.PROVISION_STATUS_ACTIVE),
								Criteria.where("active_status").is(Constants.PROVISION_STATUS_ADDRESS_CHANGED))),
				Provision.class);
		Optional<List<Provision>> optionalProvisions = Optional.ofNullable(provisions);
		return optionalProvisions;
	}

	@Override
	public Optional<Provision> getOrder(ProvisionRequest provisionRequest, String documentType) {
		Provision provision = this.mongoOperations.findOne(
				new Query(Criteria.where("customer.document_type").is(documentType).and("customer.document_number")
						.is(provisionRequest.getDocumentNumber())
						.orOperator(Criteria.where("active_status").is(Constants.PROVISION_STATUS_ACTIVE),
								Criteria.where("active_status").is(Constants.PROVISION_STATUS_ADDRESS_CHANGED))),
				Provision.class);
		Optional<Provision> optionalOrder = Optional.ofNullable(provision);
		return optionalOrder;
	}

	@Override
	public Optional<Provision> getStatus(String provisionId) {
		Provision provision = this.mongoOperations
				.findOne(new Query(Criteria.where("_id").is(new ObjectId(provisionId))), Provision.class);
		Optional<Provision> optionalOrder = Optional.ofNullable(provision);
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
	public boolean updateCancelSchedule(CancelRequest cancelRequest) {
		log.info("updateCancelSchedule");
		RestTemplate restTemplate = new RestTemplate();
		String urlProvisionUser = api.getScheduleUrl() + api.getUpdateSchedule();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", securitySchedule.getAuth());
		headersMap.add("X-IBM-Client-Id", securitySchedule.getClientId());
		headersMap.add("X-IBM-Client-Secret", securitySchedule.getClientSecret());

		HttpEntity<CancelRequest> entityProvision = new HttpEntity<CancelRequest>(cancelRequest, headersMap);

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
		String oAuthToken;
		RestTemplate restTemplate = new RestTemplate(
				SSLClientFactory.getClientHttpRequestFactory(HttpClientType.OkHttpClient));
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		String requestUrl = api.getPsiUrl() + api.getPsiUpdateClient();
		log.info("updatePSIClient - URL: " + requestUrl);

		PSIUpdateClientRequest request = new PSIUpdateClientRequest();
		request.getHeaderIn().setCountry("PE");
		request.getHeaderIn().setLang("es");
		request.getHeaderIn().setEntity("TDP");

		request.getHeaderIn().setSystem("SIVADAC");
		request.getHeaderIn().setSubsystem("SIVADAC");
		request.getHeaderIn().setOriginator("PE:TDP:SIVADAC:SIVADAC");
		request.getHeaderIn().setSender("OracleServiceBus");
		request.getHeaderIn().setUserId("USERSIVADAC");
		request.getHeaderIn().setWsId("SistemSivadac");
		request.getHeaderIn().setWsIp("10.10.10.10");
		request.getHeaderIn().setOperation("updateClient");
		request.getHeaderIn().setDestination("PE:SIVADAC:SIVADAC:SIVADAC");
		request.getHeaderIn().setExecId("550e8400-e29b-41d4-a716-446655440000");
		request.getHeaderIn().setTimestamp(DateUtil.getNowPsi(Constants.TIMESTAMP_FORMAT_PSI));
		request.getHeaderIn().setMsgType("REQUEST");

		/*
		 * request.getHeaderIn().setSystem("COL");
		 * request.getHeaderIn().setSubsystem("TRA");
		 * request.getHeaderIn().setOriginator("PE:TDP:COL:TRA");
		 * request.getHeaderIn().setSender("OracleServiceBus");
		 * request.getHeaderIn().setUserId("USERTRA");
		 * request.getHeaderIn().setWsId("SistemTRA");
		 * request.getHeaderIn().setWsIp("192.168.100.1");
		 * request.getHeaderIn().setOperation("updateClient");
		 * request.getHeaderIn().setDestination("PE:TDP:COL:TRA");
		 * request.getHeaderIn().setExecId("550e8400-e29b-41d4-a716-446655440000");
		 * request.getHeaderIn().setTimestamp(DateUtil.getNowPsi(Constants.
		 * TIMESTAMP_FORMAT_PSI)); request.getHeaderIn().setMsgType("REQUEST");
		 */

		request.getBodyUpdateClient().getUser().setNow(DateUtil.getNowPsi(Constants.TIMESTAMP_FORMAT_USER));
		request.getBodyUpdateClient().getUser().setLogin("appmovistar");
		request.getBodyUpdateClient().getUser().setCompany("telefonica-pe");
		request.getBodyUpdateClient().getUser().setAuth_string(generateAuthString());
		request.getBodyUpdateClient().setSolicitud(provision.getXaIdSt());
		request.getBodyUpdateClient().setNombre_completo(provision.getCustomer().getContactName());
		request.getBodyUpdateClient().setCorreo(provision.getCustomer().getMail());
		request.getBodyUpdateClient().setTelefono1(String.valueOf(provision.getCustomer().getContactPhoneNumber()));

		log.info("updatePSIClient - request: " + request.toString());

		oAuthToken = getAuthToken(provision.getCustomer().getName());
		log.info("updatePSIClient - oAuthToken: " + oAuthToken);

		if (oAuthToken.isEmpty()) {
			return false;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + oAuthToken);
		headers.set("X-IBM-Client-Id", api.getOauth2Client());

		log.info("updatePSIClient - headers: " + headers.toString());

		HttpEntity<PSIUpdateClientRequest> entity = new HttpEntity<PSIUpdateClientRequest>(request, headers);

		try {
			ResponseEntity<PSIUpdateClientResponse> responseEntity = restTemplate.postForEntity(requestUrl, entity,
					PSIUpdateClientResponse.class);

			log.info("updatePSIClient - responseEntity.Body: " + responseEntity.getBody().toString());

			return responseEntity.getStatusCode().equals(HttpStatus.OK);
		} catch (HttpClientErrorException ex) {
			log.info("HttpClientErrorException = " + ex.getMessage());
			log.info("getResponseBodyAsString = " + ex.getResponseBodyAsString());
			throw new ServerNotFoundException(ex.getResponseBodyAsString());
			// return false;
		} catch (Exception ex) {
			log.info("Exception = " + ex.getMessage());
			throw new ServerNotFoundException(ex.getMessage());
			// return false;
		}
	}

	private String getAuthToken(String customerName) {
		String psiTokenGenerated = "";
		Optional<OAuthToken> optionalAuthToken = getTokenFromCollection();

		if (optionalAuthToken.isPresent()) {
			OAuthToken oAuthToken = optionalAuthToken.get();
			Date now = new Date();
			long timeDiff = now.getTime() - (Long.parseLong(oAuthToken.getConsentedOn()) * 1000);

			if (timeDiff >= ((Integer.parseInt(oAuthToken.getExpiresIn()) - 5) * 1000)) {
				psiTokenGenerated = getTokenFromPSI(customerName, false);
			} else {
				psiTokenGenerated = oAuthToken.getAccessToken();
			}

		} else {
			psiTokenGenerated = getTokenFromPSI(customerName, true);
		}

		return psiTokenGenerated;
	}

	private String getTokenFromPSI(String customerName, boolean toInsert) {
		RestTemplate restTemplate = new RestTemplate();
		boolean updated = true;
		String urlToken = api.getSecurityUrl() + api.getOauthToken();

		MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<String, String>();
		headersMap.add("Content-Type", "application/json");
		headersMap.add("Authorization", security.getAuth());
		headersMap.add("X-IBM-Client-Id", security.getClientId());
		headersMap.add("X-IBM-Client-Secret", security.getClientSecret());

		ApiRequest<Object> request = new ApiRequest<Object>(Constants.APP_NAME_PROVISION, customerName,
				Constants.OPER_GET_OAUTH_TOKEN, null);

		HttpEntity<ApiRequest<Object>> entityToken = new HttpEntity<ApiRequest<Object>>(request, headersMap);

		try {
			ParameterizedTypeReference<ApiResponse<OAuthToken>> parameterizedTypeReference = new ParameterizedTypeReference<ApiResponse<OAuthToken>>() {
			};
			ResponseEntity<ApiResponse<OAuthToken>> responseEntity = restTemplate.exchange(urlToken, HttpMethod.POST,
					entityToken, parameterizedTypeReference);

			if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
				if (toInsert) {
					insertToken(responseEntity.getBody().getBody());
				} else {
					updated = updateTokenInCollection(responseEntity.getBody());
				}
			} else {
				return "";
			}

			log.info("responseEntity: " + responseEntity.getBody());

			return updated ? ((OAuthToken) responseEntity.getBody().getBody()).getAccessToken() : "";
		} catch (Exception e) {
			log.info("Exception = " + e.getMessage());
			return "";
		}
	}

	private Optional<OAuthToken> getTokenFromCollection() {
		OAuthToken oAuthToken = null;
		try {
			oAuthToken = this.mongoOperations
					.findOne(new Query(Criteria.where("token_key").is("PARAM_KEY_OAUTH_TOKEN")), OAuthToken.class);
		} catch (Exception e) {
			log.info(e.getMessage());
		}

		Optional<OAuthToken> optionalPsiToken = Optional.ofNullable(oAuthToken);

		return optionalPsiToken;
	}

	private boolean updateTokenInCollection(ApiResponse<OAuthToken> apiResponse) {

		if (apiResponse.getBody() != null) {
			OAuthToken oAuthToken = apiResponse.getBody();
			Update update = new Update();
			update.set("token_type", oAuthToken.getTokenType());
			update.set("access_token", oAuthToken.getAccessToken());
			update.set("expires_in", oAuthToken.getExpiresIn());
			update.set("consented_on", oAuthToken.getConsentedOn());
			update.set("scope", oAuthToken.getScope());
			update.set("refresh_token", oAuthToken.getRefreshToken());
			update.set("refresh_token_expires_in", oAuthToken.getRefreshTokenExpiresIn());

			UpdateResult result = this.mongoOperations.updateFirst(
					new Query(Criteria.where("token_key").is("PARAM_KEY_OAUTH_TOKEN")), update, OAuthToken.class);

			return result.getMatchedCount() > 0;
		} else {
			return false;
		}
	}

	private boolean insertToken(OAuthToken oAuthToken) {
		this.mongoOperations.insert(oAuthToken);
		return true;
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
	public boolean sendCancelledMail(Provision provision, String name, String idTemplate, String cancellationReason) {
		RestTemplate restTemplate = new RestTemplate();
		String urlSendMail = api.getSecurityUrl() + api.getSendMail();
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
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(urlSendMail, entityMail, String.class);
			log.info("responseEntity: " + responseEntity.getBody());

			return responseEntity.getStatusCode().equals(HttpStatus.OK);
		} catch (Exception e) {
			log.info("Exception = " + e.getMessage());
			return false;
		}
	}
}
