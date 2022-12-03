package com.ichuvilin.service.impl;

import com.ichuvilin.controller.UpdateController;
import com.ichuvilin.service.AnswerConsumer;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import static com.ichuvilin.model.RabbitQueue.ANSWER_MESSAGE;

@Service
@Log4j
public class AnswerConsumerImpl implements AnswerConsumer {

	private final UpdateController updateController;

	public AnswerConsumerImpl(UpdateController updateController) {
		this.updateController = updateController;
	}

	@Override
	@RabbitListener(queues = ANSWER_MESSAGE)
	public void consume(SendMessage sendMessage) {
		log.debug("Message on answer queue");
		updateController.setView(sendMessage);
	}
}
