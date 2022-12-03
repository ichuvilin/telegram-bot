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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

	private static final Random random = new Random();

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
		} else if (USER_TASK.equals(serviceCommand)) {
			userTask(user, update);
		} else if (UserState.BASE_STATE.equals(userState)) {
			output = processServiceCommand(user, text);
		} else if (UserState.ADD_TASK.equals(userState)) {
			output = processAddTask(user, text);
		}

		var chatId = update.getMessage().getChatId();
		if (!output.equals(""))
			sendAnswer(output, chatId);
	}

	@Override
	public void processCallBackMessage(Update update) {
		var sendMessage = new SendMessage();
		var callback = update.getCallbackQuery();
		var chatId = callback.getFrom().getId();
		Long taskId = Long.valueOf(callback.getData());
		StringBuilder sb = new StringBuilder();
		var title = todoDAO.findById(taskId).get().getTitle();
		sb.append(String.format("The task \"%s\" has been removed. " +
				"View the new list of tasks with the /my_task command.",title));
		todoDAO.deleteById(taskId);
		sendMessage.setText(String.valueOf(sb));
		sendMessage.setChatId(chatId);
		sendAnswer(sendMessage);
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
		} else {
			return "Unsupported command! To see a list of available commands, type /help";
		}
	}

	private void userTask(User user, Update update) {

		var sendMessage = new SendMessage();
		StringBuilder sb = new StringBuilder();
		List<Todos> todos = todoDAO.findByUserId(user.getId());
		if (todos.size() != 0) {
			sb.append(String.format("%s's task\n", user.getFirstName()));
			InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
			List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
			List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
			List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
			List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
			for (int i = 0; i < todos.size(); i++) {
				sb.append(String.format("\t%d: %s(id: %d)\n", i + 1, todos.get(i).getTitle(), todos.get(i).getId()));
				var button = new InlineKeyboardButton();
				button.setText(String.valueOf(todos.get(i).getId()));
				button.setCallbackData(String.valueOf(todos.get(i).getId()));
				int rand = random.nextInt(3);
				if (rand == 0) {
					rowInline1.add(button);
				} else if (rand == 1) {
					rowInline2.add(button);
				} else {
					rowInline3.add(button);
				}
			}
			rowsInline.add(rowInline1);
			rowsInline.add(rowInline2);
			rowsInline.add(rowInline3);
			markupInline.setKeyboard(rowsInline);
			sendMessage.setReplyMarkup(markupInline);
		} else {
			sb.append(String.format("%s you don't have tasks, to add them enter the command /add_task", user.getFirstName()));
		}

		var chatId = update.getMessage().getChatId();
		sendMessage.setText(String.valueOf(sb));
		sendMessage.setChatId(chatId);
		sendAnswer(sendMessage);
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

	private void sendAnswer(SendMessage message) {
		producerService.producerAnswer(message);
	}


	private void saveRawData(Update update) {
		RawData rawData = RawData.builder().event(update).build();
		rawDataDAO.save(rawData);
	}
}
