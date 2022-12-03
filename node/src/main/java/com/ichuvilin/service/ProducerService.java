package com.ichuvilin.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

public interface ProducerService {
	void producerAnswer(SendMessage sendMessage);
}
