package pe.telefonica.provision.external;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pe.telefonica.provision.external.request.simpli.SimpliConnectRequest;
import pe.telefonica.provision.external.request.simpli.SimpliRequest;
import pe.telefonica.provision.conf.IBMSecuritySimpli;
import pe.telefonica.provision.conf.ExternalApi;

@Component
public class SimpliConnectApi {

	private static final Logger log = LoggerFactory.getLogger(SimpliConnectApi.class);

	@Autowired
	private ExternalApi externalApi;
	
	@Autowired
	private IBMSecuritySimpli iBMSecuritySimpli;
	
	@Autowired
	TrazabilidadSecurityApi loggerApi;
	
	public String getUrlTraking(SimpliRequest request) {
        String url = externalApi.getSimpliBaseUrlAzure() + externalApi.getSimpliGetUrlAzure();
		
		LocalDateTime startHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
		LocalDateTime endHour;

		String numberLatitude = request.getLatitude().substring(0, request.getLatitude().indexOf('.'));
		String numberLongitude = request.getLongitude().substring(0, request.getLongitude().indexOf('.'));

		String fractionaLatitude = request.getLatitude().substring(request.getLatitude().indexOf('.') + 1);
		String fractionaLongitude = request.getLongitude().substring(request.getLongitude().indexOf('.') + 1);

		String latitude = numberLatitude + "."
				+ (fractionaLatitude.length() >= 6 ? fractionaLatitude.substring(0, 6) : fractionaLatitude);
		String longitude = numberLongitude + "."
				+ (fractionaLongitude.length() >= 6 ? fractionaLongitude.substring(0, 6) : fractionaLongitude);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + request.getToken());
		headers.add("Ocp-Apim-Subscription-Key", externalApi.getApiClientKeyAzure());
		headers.add("Content-Type", "application/json");

		SimpliConnectRequest requestConnect = new SimpliConnectRequest();
		requestConnect = requestConnect.generateRequest(Double.valueOf(latitude), Double.valueOf(longitude),
				request.getVisitTitle(), request.getVisitAddress(), request.getDriverUserName());

		HttpEntity<SimpliConnectRequest> requestEntity = new HttpEntity<SimpliConnectRequest>(requestConnect, headers);

		try {
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			ResponseEntity<String> result = restTemplate.postForEntity(url, requestEntity, String.class);
			
			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("SIMPLI_APICONNECT_AZURE", "getUrlTrakingAzure", new Gson().toJson(requestEntity.getBody()),
					result.getBody().replace("\r\n", ""), url, startHour, endHour, result.getStatusCodeValue());
			
			if (result.getStatusCode().equals(HttpStatus.OK)) {
				JsonObject jsonObject = new JsonParser().parse(result.getBody().toString()).getAsJsonObject();
				String urlSimpli = jsonObject.get("BodyOut").getAsJsonObject().get("url").toString().replaceAll("\"",
						"");				
				return urlSimpli;
			}
			return null;
		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());
			String error = ex.getLocalizedMessage().substring(0, ex.getLocalizedMessage().indexOf(" "));
			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("SIMPLI_APICONNECT_AZURE", "getUrlTrakingAzure", new Gson().toJson(requestConnect),
					ex.getLocalizedMessage(), url, startHour, endHour, Integer.parseInt(error));
			return null;
		}
	}

	public String getUrlTrakingOld(SimpliRequest request) {
		String url = externalApi.getSimpliBaseUrl() + externalApi.getSimpliGetUrl();
		
		LocalDateTime startHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
		LocalDateTime endHour;

		String numberLatitude = request.getLatitude().substring(0, request.getLatitude().indexOf('.'));
		String numberLongitude = request.getLongitude().substring(0, request.getLongitude().indexOf('.'));

		String fractionaLatitude = request.getLatitude().substring(request.getLatitude().indexOf('.') + 1);
		String fractionaLongitude = request.getLongitude().substring(request.getLongitude().indexOf('.') + 1);

		String latitude = numberLatitude + "."
				+ (fractionaLatitude.length() >= 6 ? fractionaLatitude.substring(0, 6) : fractionaLatitude);
		String longitude = numberLongitude + "."
				+ (fractionaLongitude.length() >= 6 ? fractionaLongitude.substring(0, 6) : fractionaLongitude);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + request.getToken());
		headers.add("X-IBM-Client-Id", iBMSecuritySimpli.getClientId());
		headers.add("Content-Type", "application/json");
		
		SimpliConnectRequest requestConnect = new SimpliConnectRequest();
		requestConnect = requestConnect.generateRequest(Double.valueOf(latitude), Double.valueOf(longitude),
				request.getVisitTitle(), request.getVisitAddress(), request.getDriverUserName());

		HttpEntity<SimpliConnectRequest> requestEntity = new HttpEntity<SimpliConnectRequest>(requestConnect, headers);

		try {
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			ResponseEntity<String> result = restTemplate.postForEntity(url, requestEntity, String.class);

			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("SIMPLI_APICONNECT", "getUrlTraking", new Gson().toJson(requestEntity.getBody()),
					result.getBody(), url, startHour, endHour, result.getStatusCodeValue());

			if (result.getStatusCode().equals(HttpStatus.OK)) {
				JsonObject jsonObject = new JsonParser().parse(result.getBody().toString()).getAsJsonObject();
				String urlSimpli = jsonObject.get("BodyOut").getAsJsonObject().get("url").toString().replaceAll("\"",
						"");
				return urlSimpli;				
			}
			return null;
		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());
			String error = ex.getLocalizedMessage().substring(0, ex.getLocalizedMessage().indexOf(" "));			
			endHour = LocalDateTime.now(ZoneOffset.of("-05:00"));
			loggerApi.thirdLogEvent("SIMPLI_APICONNECT", "getUrlTraking", new Gson().toJson(requestEntity.getBody()),
					ex.getLocalizedMessage(), url, startHour, endHour, Integer.parseInt(error));

			return null;
		}
	}
}