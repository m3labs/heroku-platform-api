package com.mokinetworks.tools.heroku;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mokinetworks.tools.heroku.model.HerokuApp;

public class HerokuClientTest{

	private static final String API_KEY = System.getenv("HEROKU_KEY");
	private static HerokuClient hc;
	
	@BeforeClass
	public static void setup(){
		hc = new HerokuClient("dave.welch@mokinetworks.com", API_KEY);
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testSimpleMap(){
		System.out.println( hc.queryForSimpleMap("account") );
	}
	
	@Test
	public void testAccountInfo(){
		System.out.println( hc.getAccount() );
	}
	
	@Test
	public void testListApps(){
		List<HerokuApp> apps = hc.getApps();
		for(HerokuApp app : apps){
			System.out.println( app );
		}
	}
	
	@Test
	public void testDeleteApp(){
		HerokuApp app = hc.createApp("moki-dave101");
		System.out.println("[CREATED] " + app);
		
		hc.deleteApp(app.getName());
		System.out.println("[DELETED] " + app);
	}
	
}
