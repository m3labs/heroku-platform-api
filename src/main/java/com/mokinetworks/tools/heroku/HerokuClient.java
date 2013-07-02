package com.mokinetworks.tools.heroku;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

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
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import com.mokinetworks.tools.heroku.model.HerokuAccount;
import com.mokinetworks.tools.heroku.model.HerokuApp;

/**
 * Convenience class for accessing Heroku's API.
 *  
 * @author dwelch2344
 *
 */
@Slf4j
public class HerokuClient {

	private static final String HEROKU_PROTOCOL = "https";
	private static final String HEROKU_HOST = "api.heroku.com";
	private static final int HEROKU_PORT = 443;
	
	private static final String BASE = "https://api.heroku.com/";
	
	private final String email, key;
	private final RestTemplate rt;

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
	
	private <T> T get(String url, Class<T> klazz){
		String endpoint = BASE + url;
		log.info("Requesting {} from {}", klazz, endpoint);
		return rt.getForObject(endpoint, klazz);
	}
	
	private <T> List<T> getList(String url, Class<T> klazz){
		@SuppressWarnings("unchecked")
		T[] arr = (T[]) get(url, Array.newInstance(klazz, 0).getClass()); 
		return Arrays.asList(arr);
	}
	
	public HerokuAccount getAccount(){
		return get("account", HerokuAccount.class);
	}
	
	public List<HerokuApp> getApps(){
		return getList("apps", HerokuApp.class);
	}

	public HerokuApp createApp(final String name){
		Map<String, String> data = new HashMap<String, String>();
	    data.put("name", name);
	    
		return rt.postForObject(BASE + "apps", data, HerokuApp.class);
	}
	
	public HerokuApp getAppInfo(String id){
		return get("apps/" + id, HerokuApp.class);
	}
	
	public void deleteApp(String id){
		rt.delete(BASE + "apps/" + id);
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
		
		// Configure Jackson to play nice with our JSON
		MappingJacksonHttpMessageConverter jackson = new MappingJacksonHttpMessageConverter();
		jackson.getObjectMapper()
			.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES)
			.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		// Add our Jackson json converter
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
		messageConverters.add(jackson);
		restTemplate.setMessageConverters(messageConverters);
		
		// Add out header interceptor (otherwise POST parameters will be ignored)
		restTemplate.setInterceptors( Arrays.asList(HEROKU_HEADER_INTERCEPTOR) );
		return restTemplate;
	}
	
	/**
	 * Forces the headers to match what Heroku's platform API expects
	 */
	private static final ClientHttpRequestInterceptor HEROKU_HEADER_INTERCEPTOR = new ClientHttpRequestInterceptor(){
		public ClientHttpResponse intercept(HttpRequest request,
				byte[] body, ClientHttpRequestExecution execution)
				throws IOException {
			request.getHeaders().set("Accept", "application/vnd.heroku+json; version=3");
			request.getHeaders().set("Content-Type", "application/json");
			return execution.execute(request, body);
		}
	};
	
	/**
	 * Helper class to handle basic authentication.
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
