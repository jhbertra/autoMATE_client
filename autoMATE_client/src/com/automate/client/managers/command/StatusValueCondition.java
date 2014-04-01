package com.automate.client.managers.command;

import java.util.List;

import com.automate.protocol.models.Status;
import com.automate.protocol.models.Type;

public class StatusValueCondition implements Condition<List<Status<?>>> {

	private String mStatusName;
	private Type mType;
	private Object mValue;
	private boolean mDefault;
	
	public StatusValueCondition(String statusName, Type type, Object value, boolean defaultValue) {
		this.mStatusName = statusName;
		this.mType = type;
		this.mValue = value;
		this.mDefault = defaultValue;
	}

	@Override
	public boolean conditionsMet(List<Status<?>> variables) {
		if(variables == null) return mDefault;
		for(Status<?> status : variables) {
			if(status.type == this.mType) {
				if(status.name.equals(mStatusName)) {
					return status.value.equals(mValue);
				}
			}
		}
		return mDefault;
	}
	
	public static StatusValueCondition newInstance(String statusName, Type type, String value, boolean defaultValue) {
		switch(type) {
			case BOOLEAN:
				if(value.equalsIgnoreCase("true")) return new StatusValueCondition(statusName, type, true, defaultValue);
				else if(value.equalsIgnoreCase("false")) return new StatusValueCondition(statusName, type, false, defaultValue);
				else return null;
			case INTEGER:
				try {
					return new StatusValueCondition(statusName, type, Integer.parseInt(value), defaultValue);
				} catch(NumberFormatException e) {
					return null;
				}
			case PERCENT:
				try {
					double percentValue = Double.parseDouble(((String) value).substring(0, ((String) value).indexOf("%"))) / 100.0;
					return new StatusValueCondition(statusName, type, percentValue, defaultValue);
				} catch(Exception e) {
					return null;
				}
			case REAL:
				try {
					return new StatusValueCondition(statusName, type, Double.parseDouble(value), defaultValue);
				} catch(NumberFormatException e) {
					return null;
				}
			case STRING:
				return new StatusValueCondition(value, type, value, defaultValue);
			default:
				return null;
		}
	}
}
