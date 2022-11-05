package com.example.demo.service;

import com.example.demo.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
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

    @Override
    public void onUpdateReceived(Update update)
    {
        if((update.hasMessage() && update.getMessage().hasText()))
        {
            String message = update.getMessage().getText().toLowerCase();
            long chatId = update.getMessage().getChatId();
            if(message.equals("/start"))
            {
                startCommand(chatId, update.getMessage().getChat().getFirstName());
            }
            else
            {
                sendMessage(chatId, "Command not found");
            }

        }
    }
        private void startCommand(long chatId, String username)
        {
            String tgMessage = "Hi " + username + ", how can I help you?";
            sendMessage(chatId, tgMessage);
        }

        private void sendMessage(long chatId, String textToSend)
        {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText(textToSend);
            try{
                execute(sendMessage);
            }
            catch(TelegramApiException e){}
        }
}
