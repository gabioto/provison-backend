package pe.telefonica.provision.external;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import pe.telefonica.provision.conf.SSLClientFactory;
import pe.telefonica.provision.conf.SSLClientFactory.HttpClientType;

public class ConfigRestTemplate {
	private RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
	
	
	
    private ClientHttpRequestFactory getClientHttpRequestFactory() {
    	SSLClientFactory.getClientHttpRequestFactory(HttpClientType.OkHttpClient);
    	
        int timeout = 20000;
        int readtimeout = 20000;
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
                = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(timeout);
        clientHttpRequestFactory.setReadTimeout(readtimeout);
       
        return clientHttpRequestFactory;
    }

    /**
     * @return the restTemplate
     */
    public RestTemplate getRestTemplate() {
    	
    	restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        return restTemplate;
    }

    /**
     * @param restTemplate the restTemplate to set
     */
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

}
