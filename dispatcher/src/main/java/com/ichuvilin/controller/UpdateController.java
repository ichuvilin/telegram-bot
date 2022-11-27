package com.ichuvilin.controller;

import com.ichuvilin.utils.MessageUtils;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Log4j
public class UpdateController {
	private TelegramBot telegramBot;
	private final MessageUtils messageUtils;

	public UpdateController(MessageUtils messageUtils) {
		this.messageUtils = messageUtils;
	}

	public void registerBot(TelegramBot tgBot) {
		this.telegramBot = tgBot;
	}


	public void processUpdate(Update update) {
		if (update == null) {
			log.error("Received update is null");
			return;
		}
		if (update.hasMessage()) {
			distributeMessageByType(update);
		}
	}

	private void distributeMessageByType(Update update) {
		var message = update.getMessage();
		if (message.hasText()) {
			processTextMessage(update);
		} else {
			setUnsupportedMessageTypeView(update);
		}
	}

	private void setUnsupportedMessageTypeView(Update update) {
		var sendMessage = messageUtils.generateSendMessageWithText(update, "Unsupported message type");
		log.error("Unsupported message type");
		setView(sendMessage);
	}


	private void processTextMessage(Update update) {
		var message = update.getMessage();
		var sendMessage = new SendMessage();
		sendMessage.setText("Dispatcher");
		sendMessage.setChatId(message.getChatId());
		telegramBot.sendAnswerMessage(sendMessage);
	}

	private void setView(SendMessage sendMessage) {
		telegramBot.sendAnswerMessage(sendMessage);
	}

}
