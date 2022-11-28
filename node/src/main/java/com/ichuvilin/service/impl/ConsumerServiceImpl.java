package com.ichuvilin.service.impl;

import com.ichuvilin.service.ConsumerService;
import com.ichuvilin.service.MainService;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.ichuvilin.model.RabbitQueue.TEXT_MESSAGE_UPDATE;

@Service
@Log4j
public class ConsumerServiceImpl implements ConsumerService {

	private final MainService mainService;

	public ConsumerServiceImpl(MainService mainService) {
		this.mainService = mainService;
	}


	@Override
	@RabbitListener(queues = TEXT_MESSAGE_UPDATE)
	public void consumeTextMessage(Update update) {
		log.debug("NODE: Text message is received");
		mainService.processTextMessage(update);
	}
}
