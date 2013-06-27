package com.mokinetworks.tools.heroku;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import com.mokinetworks.tools.heroku.model.HerokuAccount;

/**
 * Convenience class for accessing Heroku's API. 
 * @author dwelch2344
 *
 */
public class HerokuClient {

	private static final String HEROKU_PROTOCOL = "https";
	private static final String HEROKU_HOST = "api.heroku.com";
	private static final int HEROKU_PORT = 443;
	
	private static final String BASE = "https://api.heroku.com/";
	
	private String email, key;
	private RestTemplate rt;

	public HerokuClient(String email, String key) {
		Assert.hasText(email, "Your Heroku email address is required");
		Assert.hasText(email, "Your Heroku API key is required");
		
		this.email = email;
		this.key = key;
		rt = initializeRestTemplate();
	}

	/**
	 * Test method to toy around with Heroku's response api
	 * @param url
	 * @return
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public HashMap<String, String> queryForSimpleMap(String url){
		return rt.getForObject(BASE + url, HashMap.class);
	}
	
	public HerokuAccount getAccount(){
		return rt.getForObject(BASE + "account", HerokuAccount.class);
	}
	
	private RestTemplate initializeRestTemplate() {
		// configure our HttpClient with our credentials
		HttpHost host = new HttpHost(HEROKU_HOST, HEROKU_PORT, HEROKU_PROTOCOL);
		HttpComponentsClientHttpRequestFactoryBasicAuth requestFactory = new HttpComponentsClientHttpRequestFactoryBasicAuth(host);
		RestTemplate restTemplate = new RestTemplate(requestFactory);
		DefaultHttpClient httpClient = (DefaultHttpClient) requestFactory.getHttpClient();
		httpClient.getCredentialsProvider().setCredentials(
				new AuthScope(HEROKU_HOST, HEROKU_PORT, AuthScope.ANY_REALM),
				new UsernamePasswordCredentials(email, key));
		
		// Configure Jackson to play nice
		MappingJacksonHttpMessageConverter jackson = new MappingJacksonHttpMessageConverter();
		ObjectMapper mapper = jackson.getObjectMapper();
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		// Add our Jackson json converter
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
		messageConverters.add(jackson);
		restTemplate.setMessageConverters(messageConverters);
		return restTemplate;
	}
	
	/**
	 * Helper class to handle authentication.
	 * @author dwelch2344
	 *
	 */
	private class HttpComponentsClientHttpRequestFactoryBasicAuth extends HttpComponentsClientHttpRequestFactory {
		private HttpHost host;

		public HttpComponentsClientHttpRequestFactoryBasicAuth(HttpHost host) {
			super();
			this.host = host;
		}

		protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
			return createHttpContext();
		}

		private HttpContext createHttpContext() {
			// Create AuthCache instance
			AuthCache authCache = new BasicAuthCache();
			
			// Generate BASIC scheme object and add it to the local auth cache
			BasicScheme basicAuth = new BasicScheme();
			authCache.put(host, basicAuth);
			
			// Add AuthCache to the execution context
			BasicHttpContext localcontext = new BasicHttpContext();
			localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
			return localcontext;
		}

	}

}
