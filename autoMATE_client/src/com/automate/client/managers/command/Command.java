package com.automate.client.managers.command;

import java.util.List;

import com.automate.protocol.models.CommandArgument;

public class Command {

	public final String name;
	public final List<CommandArgument<?>> args;

	public Command(String name, List<CommandArgument<?>> args) {
		this.name = name;
		this.args = args;
	}
	
}
