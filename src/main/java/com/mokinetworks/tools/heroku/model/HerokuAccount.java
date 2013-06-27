package com.mokinetworks.tools.heroku.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Getter @Setter
@ToString(of={"id", "email", "lastLogin"})
//@JsonIgnoreProperties(ignoreUnknown = true)
public class HerokuAccount {

	private boolean allowTracking, beta, confirmed, verified;
	private String createdAt, lastLogin, updatedAt;
	private String id, email;
	
}
