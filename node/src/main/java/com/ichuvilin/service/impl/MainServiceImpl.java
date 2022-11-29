package com.ichuvilin.service.impl;

import com.ichuvilin.dao.RawDataDAO;
import com.ichuvilin.dao.TodoDAO;
import com.ichuvilin.dao.UserDAO;
import com.ichuvilin.entity.RawData;
import com.ichuvilin.entity.Todos;
import com.ichuvilin.entity.User;
import com.ichuvilin.entity.enums.UserState;
import com.ichuvilin.service.MainService;
import com.ichuvilin.service.ProducerService;
import com.ichuvilin.service.enums.ServiceCommand;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static com.ichuvilin.service.enums.ServiceCommand.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {

	private final RawDataDAO rawDataDAO;
	private final ProducerService producerService;
	private final UserDAO userDAO;
	private final TodoDAO todoDAO;

	public MainServiceImpl(RawDataDAO rawDataDAO, ProducerService producerService, UserDAO userDAO, TodoDAO todoDAO) {
		this.rawDataDAO = rawDataDAO;
		this.producerService = producerService;
		this.userDAO = userDAO;
		this.todoDAO = todoDAO;
	}

	@Override
	public void processTextMessage(Update update) {
		saveRawData(update);

		var user = findOrSaveUser(update);
		var userState = user.getState();
		var text = update.getMessage().getText();
		var output = "";

		var serviceCommand = ServiceCommand.fromValue(text);
		if (CANCEL.equals(serviceCommand)) {
			output = cancelProcess(user);
		} else if (UserState.BASE_STATE.equals(userState)) {
			output = processServiceCommand(user, text);
		} else if (UserState.ADD_TASK.equals(userState)) {
			output = processAddTask(user, text);
		}

		var chatId = update.getMessage().getChatId();
		sendAnswer(output, chatId);
	}

	private String processAddTask(User user, String text) {
		Todos todo = Todos.builder().title(text).user(user).build();
		todoDAO.save(todo);
		return "Task has been added. If you want to add another task just write it or exit the add mode with the /cancel command";
	}

	private String cancelProcess(User user) {
		user.setState(UserState.BASE_STATE);
		userDAO.save(user);
		return "Command cancelled!";
	}

	private String processServiceCommand(User user, String cmd) {
		var serviceCommand = ServiceCommand.fromValue(cmd);
		if (START.equals(serviceCommand)) {
			return String.format("Hi %s. To see a list of available commands, type /help", user.getFirstName());
		} else if (HELP.equals(serviceCommand)) {
			return help();
		} else if (ADD_TASK.equals(serviceCommand)) {
			user.setState(UserState.ADD_TASK);
			userDAO.save(user);
			return "Enter the name of the task";
		} else if (USER_TASK.equals(serviceCommand)) {
			return userTask(user);
		} else {
			return "Unsupported command! To see a list of available commands, type /help";
		}
	}

	private String userTask(User user) {

		List<Todos> todos = todoDAO.findByUserId(user.getId());
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%s's task\n", user.getFirstName()));

		for (int i = 0; i < todos.size(); i++) {
			sb.append(String.format("\t%d: %s\n", i + 1, todos.get(i).getTitle()));
		}

		return String.valueOf(sb);
	}

	private String help() {
		return """
				Command list:
				\t/help - this is message
				\t/cancel - cancel command
				\t/add_task - create new task
				\t/my_task - check your task
				""";
	}

	private User findOrSaveUser(Update update) {
		var telegramUser = update.getMessage().getFrom();
		User persistentUser = userDAO.findByTelegramUserId(telegramUser.getId());
		if (persistentUser == null) {
			User transientUser = User.builder()
					.telegramUserId(telegramUser.getId())
					.username(telegramUser.getUserName())
					.firstName(telegramUser.getFirstName())
					.lastName(telegramUser.getLastName())
					.state(UserState.BASE_STATE)
					.build();
			return userDAO.save(transientUser);
		}
		return persistentUser;
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
