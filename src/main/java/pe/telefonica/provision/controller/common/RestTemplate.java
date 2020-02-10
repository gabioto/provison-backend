package pe.telefonica.provision.controller.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;

public class RestTemplate {

	private static String KEY_JKS = "Amdocs123";

	public RestTemplate(HttpComponentsClientHttpRequestFactory clientHttpRequestFactory) {
		// TODO Auto-generated constructor stub
		
	}

	public RestTemplate() {
		// TODO Auto-generated constructor stub
	}

	public static org.springframework.web.client.RestTemplate initClientRestTemplate() {

		char[] password = KEY_JKS.toCharArray();

		SSLContext sslContext = null;
		try {
			sslContext = SSLContextBuilder.create()
					.loadKeyMaterial(keyStore("classpath:keystore.jks", password), password)
					.loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
		} catch (Exception e) {
			e.printStackTrace();
		}

		HttpClient client = HttpClients.custom().setSSLContext(sslContext).build();

		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();

		clientHttpRequestFactory.setHttpClient(client);

		return new org.springframework.web.client.RestTemplate(clientHttpRequestFactory);

	}

	private static KeyStore keyStore(String file, char[] password) throws Exception {
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		File key = ResourceUtils.getFile(file);
		try (InputStream in = new FileInputStream(key)) {
			keyStore.load(in, password);
		}
		return keyStore;
	}

}
