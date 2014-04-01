package com.automate.client.managers.command;

public interface Condition <ConditionVariables> {

	public boolean conditionsMet(ConditionVariables variables);
	
}
