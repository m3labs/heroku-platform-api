package com.mokinetworks.tools.heroku.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString(of={"id", "name", "webUrl"})
public class HerokuApp {
	
	private String id, name, gitUrl, webUrl;
	
}
