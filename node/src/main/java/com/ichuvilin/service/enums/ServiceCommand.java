package com.ichuvilin.service.enums;

public enum ServiceCommand {
	HELP("/help"),
	START("/start"),
	ADD_TASK("/add_task"),
	CANCEL("/cancel"),
	USER_TASK("/my_task");

	private final String value;

	ServiceCommand(String s) {
		this.value = s;
	}

	@Override
	public String toString() {
		return value;
	}

	public static ServiceCommand fromValue(String v) {
		for (ServiceCommand c : ServiceCommand.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		return null;
	}
}
