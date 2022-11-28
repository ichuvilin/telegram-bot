package com.ichuvilin.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface AnswerConsumer {
	public void consume(SendMessage sendMessage);
}
