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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
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
                chooseLanguage(chatId);
            }
            else if(userCondition.getCondition().equals(BotCondition.SELECT_LANG))
            {
                if(message.equals("english"))
                {
                    sendMessage(chatId, "You chose english");
                    userCondition.setLanguage("english");
                }
                else if(message.equals("russian"))
                {
                    sendMessage(chatId, "Вы выбрали русский язык");
                    userCondition.setLanguage("russian");
                }
                else
                {
                    sendMessage(chatId, "Language not found");
                }
                userCondition.setCondition(BotCondition.CHOOSE_OPERATION);
                chooseOperation(chatId, userCondition);
            }
            else if(userCondition.getCondition().equals(BotCondition.ENTER_MSG))
            {
                userCondition.setName(message);
            }

            else if(userCondition.getCondition().equals(BotCondition.CHOOSE_OPERATION))
            {
                if(message.equals("выбор языка") || message.equals("select language"))
                {
                    userCondition.setCondition(BotCondition.SELECT_LANG);
                    chooseLanguage(chatId);
                }
            }
            else
            {
                sendMessage(chatId, "Command not found");
            }

        }
    }
        private void startCommand(String chatId, String username)
        {
            String tgMessage = "Hi " + username + "!";
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

        //Хранение состояния
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

        private void markup(String chatId, List<KeyboardRow> keyboard, String text)
        {
            SendMessage sendMessage = new SendMessage();
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

            sendMessage.setText(text);
            keyboardMarkup.setKeyboard(keyboard);
            sendMessage.setReplyMarkup(keyboardMarkup);
            sendMessage.setChatId(chatId);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        private void chooseLanguage(String chatId)
        {
            String text = "Choose your language: ";
            List<KeyboardRow> keyboard = new ArrayList<>();
            KeyboardRow row = new KeyboardRow();
            row.add("English");
            keyboard.add(row);
            row = new KeyboardRow();
            row.add("Russian");
            keyboard.add(row);
            markup(chatId, keyboard, text);
        }

        private void chooseOperation(String chatId, UserCondition userCondition)
        {
            List<KeyboardRow> keyboard = new ArrayList<>();
            KeyboardRow row = new KeyboardRow();
            String text;
            if(userCondition.getLanguage().equals("english"))
            {
                text = "How can I help you?";
                row.add("Catalogue");
                keyboard.add(row);
                row = new KeyboardRow();
                row.add("Shopping cart");
                row.add("Purchase history");
                keyboard.add(row);
                row = new KeyboardRow();
                row.add("Select language");
                keyboard.add(row);
                markup(chatId, keyboard, text);
            }
            else if(userCondition.getLanguage().equals("russian"))
            {
                text = "Как могу помочь?";
                row.add("Каталог");
                keyboard.add(row);
                row = new KeyboardRow();
                row.add("Корзина");
                row.add("Исторория покупок");
                keyboard.add(row);
                row = new KeyboardRow();
                row.add("Выбор языка");
                keyboard.add(row);
                markup(chatId, keyboard, text);
            }
        }
}
