package com.automate.client.managers.command;

import java.util.List; 

public class Command {

	public final String name;
	public final List<ArgumentSpecification> args;
	public final Condition condition;
	
	public Command(String name, List<ArgumentSpecification> args, Condition condition) {
		this.name = name;
		this.args = args;
		this.condition = condition;
	}
	
}
