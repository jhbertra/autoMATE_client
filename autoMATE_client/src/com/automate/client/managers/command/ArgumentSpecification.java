package com.automate.client.managers.command;

import java.util.HashMap;

import com.automate.protocol.models.Type;

public class ArgumentSpecification {

	public final String mName;
	public final Type mType;
	public final ArgumentRange<?> range;
	
	public ArgumentSpecification(String mName, Type mType, ArgumentRange range) {
		this.mName = mName;
		this.mType = mType;
		this.range = range;
	}
	
}
