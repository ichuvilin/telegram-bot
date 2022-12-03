package com.ichuvilin.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface ConsumerService {
	void consumeCallbackMessage(Update update);
	void consumeTextMessage(Update update);
}
