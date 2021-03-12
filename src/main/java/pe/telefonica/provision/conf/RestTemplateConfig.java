package pe.telefonica.provision.conf;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


public class RestTemplateConfig {
	
		
	 	
	    public RestTemplate restTemplate() {
	 		
	 		/*HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
            = new HttpComponentsClientHttpRequestFactory();*/
	 		
	 		SimpleClientHttpRequestFactory test = new SimpleClientHttpRequestFactory();
	       

	 		test.setConnectTimeout(20000);
	 		test.setReadTimeout(20000);

	        return new RestTemplate(test);
	        
	    }
}
