package com.ichuvilin.service.impl;

import com.ichuvilin.dao.RawDataDAO;
import com.ichuvilin.entity.RawData;
import com.ichuvilin.service.MainService;
import com.ichuvilin.service.ProducerService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Log4j
public class MainServiceImpl implements MainService {

	private final RawDataDAO rawDataDAO;
	private final ProducerService producerService;

	public MainServiceImpl(RawDataDAO rawDataDAO, ProducerService producerService) {
		this.rawDataDAO = rawDataDAO;
		this.producerService = producerService;
	}

	@Override
	public void processTextMessage(Update update) {
		saveRawData(update);

		var message = update.getMessage();
		var chatId = message.getChatId();

		sendAnswer("NODE", chatId);
	}


	private void sendAnswer(String text, Long chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText(text);
		producerService.producerAnswer(message);
	}


	private void saveRawData(Update update) {
		RawData rawData = RawData.builder().event(update).build();
		rawDataDAO.save(rawData);
	}
}
