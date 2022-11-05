package com.example.demo.service;

import com.example.demo.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class Bot extends TelegramLongPollingBot
{
    BotConfig botConfig;
    public Bot(BotConfig botConfig)
    {
        this.botConfig = botConfig;
        List<BotCommand>listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "greeting"));
        listOfCommands.add(new BotCommand("/about", "about project"));
        try{
            execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        }
        catch(TelegramApiException e){}

    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    private String condition = "start";
    List<UserCondition>users = new ArrayList<>();

    @Override
    public void onUpdateReceived(Update update)
    {
        if((update.hasMessage() && update.getMessage().hasText()))
        {
            String message = update.getMessage().getText().toLowerCase();
            String chatId = update.getMessage().getChatId().toString();
            UserCondition userCondition = saveUser(chatId);
            if(message.equals("/list"))
            {
                System.out.println(users);
            }
            if(message.equals("/start"))
            {
                startCommand(chatId, update.getMessage().getChat().getFirstName());
                userCondition.setCondition(BotCondition.SELECT_LANG);
            }
            else if(userCondition.getCondition().equals(BotCondition.SELECT_LANG))
            {
                if(message.equals("1"))
                {
                    sendMessage(chatId, "You chose english");
                    userCondition.setLanguage("english");
                    userCondition.setCondition(BotCondition.ENTER_MSG);
                    sendMessage(chatId, "Enter your name");
                }
                else if(message.equals("2"))
                {
                    sendMessage(chatId, "Вы выбрали русский язык");
                    userCondition.setLanguage("russian");
                    userCondition.setCondition(BotCondition.ENTER_MSG);
                    sendMessage(chatId, "Введите ваше имя");
                }
                else
                {
                    sendMessage(chatId, "Language not found");
                }
            }
            else if(userCondition.getCondition().equals(BotCondition.ENTER_MSG))
            {
                userCondition.setName(message);

            }
            else
            {
                sendMessage(chatId, "Command not found");
            }

        }
    }
        private void startCommand(String chatId, String username)
        {
            String tgMessage = "Hi " + username + "!\nPlease choose language: \n1. English\n2.Russian";
            sendMessage(chatId, tgMessage);
        }

        private void sendMessage(String chatId, String textToSend)
        {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(textToSend);
            try{
                execute(sendMessage);
            }
            catch(TelegramApiException e){}
        }

        private UserCondition saveUser(String chatId)
        {
            for(UserCondition user : users)
            {
                if(user.getChatId().equals(chatId))
                {
                    return user;
                }
            }
            UserCondition user = new UserCondition();
            user.setChatId(chatId);
            users.add(user);
            return user;
        }
}
