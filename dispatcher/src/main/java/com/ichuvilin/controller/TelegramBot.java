package com.ichuvilin.controller;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Log4j
public class TelegramBot extends TelegramLongPollingBot {

	@Value("${bot.name}")
	private String botName;
	@Value("${bot.token}")
	private String botToken;

	@Override
	public String getBotUsername() {
		return botName;
	}

	@Override
	public String getBotToken() {
		return botToken;
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage()) {
			log.debug("New Message: " + update.getMessage().getText());
			var chatId = update.getMessage().getChatId();
			var sendMessage = new SendMessage();
			sendMessage.setText("Dispatcher");
			sendMessage.setChatId(chatId);
			try {
				execute(sendMessage);
			} catch (TelegramApiException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
