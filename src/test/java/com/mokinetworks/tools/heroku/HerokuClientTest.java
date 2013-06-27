package com.mokinetworks.tools.heroku;

import org.junit.BeforeClass;
import org.junit.Test;

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
}
