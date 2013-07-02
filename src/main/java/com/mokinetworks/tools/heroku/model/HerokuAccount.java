package com.mokinetworks.tools.heroku.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString(of={"id", "email", "lastLogin"})
public class HerokuAccount {

	private boolean allowTracking, beta, confirmed, verified;
	private String createdAt, lastLogin, updatedAt;
	private String id, email;
	
}
