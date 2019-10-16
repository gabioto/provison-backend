package pe.telefonica.provision.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
	 	@Bean
	    public RestTemplate restTemplateOld() {
	 		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
            = new HttpComponentsClientHttpRequestFactory();

	         //factory = new SimpleClientHttpRequestFactory();

	 		clientHttpRequestFactory.setConnectTimeout(1000);
	 		clientHttpRequestFactory.setReadTimeout(1000);

	        return new RestTemplate(clientHttpRequestFactory);
	    }
}
