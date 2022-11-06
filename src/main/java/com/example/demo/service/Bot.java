package com.example.demo.service;

import com.example.demo.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
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

    private String condition = BotCondition.START;
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
                    userCondition.setCondition(BotCondition.CHOOSE_OPERATION);
                    chooseOperation(chatId, userCondition);
                }
                else if(message.equals("russian"))
                {
                    sendMessage(chatId, "Вы выбрали русский язык");
                    userCondition.setLanguage("russian");
                    userCondition.setCondition(BotCondition.CHOOSE_OPERATION);
                    chooseOperation(chatId, userCondition);
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

            else if(userCondition.getCondition().equals(BotCondition.CHOOSE_OPERATION))
            {
                if(message.equals("выбор языка") || message.equals("select language"))
                {
                    userCondition.setCondition(BotCondition.SELECT_LANG);
                    chooseLanguage(chatId);
                }
                else if(message.equals("каталог") || message.equals("catalogue"))
                {
                    userCondition.setCondition(BotCondition.SELECT_CATALOGUE);
                    chooseCatalogue(chatId, userCondition);
                }
            }

            else if(userCondition.getCondition().equals(BotCondition.SELECT_CATALOGUE))
            {
                if(message.equals("назад в главное меню") || message.equals("back to main menu"))
                {
                    userCondition.setCondition(BotCondition.CHOOSE_OPERATION);
                    chooseOperation(chatId, userCondition);
                }
                else if(message.equals("игровые ноутбуки") || message.equals("gaming laptops"))
                {
                    userCondition.setCondition(BotCondition.GAMING_LAPS);
                    chooseGamingLaps(chatId, userCondition);
                }
                else if(message.equals("офисные ноутбуки") || message.equals("office laptops"))
                {
                    userCondition.setCondition(BotCondition.OFFICE_LAPS);
                    chooseOfficeLaps(chatId, userCondition);
                }
            }

            else if(userCondition.getCondition().equals(BotCondition.GAMING_LAPS))
            {
                if(message.equals("назад в категорию ноутбуков") || message.equals("back to laptop categories"))
                {
                    userCondition.setCondition(BotCondition.SELECT_CATALOGUE);
                    chooseCatalogue(chatId, userCondition);
                }
                laptopInfo(chatId, userCondition, message);
            }

            else if(userCondition.getCondition().equals(BotCondition.OFFICE_LAPS))
            {
                if(message.equals("назад в категорию ноутбуков") || message.equals("back to laptop categories"))
                {
                    userCondition.setCondition(BotCondition.SELECT_CATALOGUE);
                    chooseCatalogue(chatId, userCondition);
                }
                laptopInfo(chatId, userCondition, message);
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

        //Хранение состояния, сохранение пользователей бота
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
                text = "Main menu";
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
                text = "Главное меню";
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
    private void chooseCatalogue(String chatId, UserCondition userCondition)
    {
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        String text;
        if(userCondition.getLanguage().equals("english"))
        {
            text = "Laptop categories";
            row.add("Gaming laptops");
            row.add("Office laptops");
            keyboard.add(row);
            row = new KeyboardRow();
            row.add("Back to main menu");
            keyboard.add(row);
            markup(chatId, keyboard, text);
        }
        else if(userCondition.getLanguage().equals("russian"))
        {
            text = "Категории ноутбуков";
            row.add("Игровые ноутбуки");
            row.add("Офисные ноутбуки");
            keyboard.add(row);
            row = new KeyboardRow();
            row.add("Назад в главное меню");
            keyboard.add(row);
            markup(chatId, keyboard, text);
        }
    }
    private void chooseGamingLaps(String chatId, UserCondition userCondition)
    {
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        String text;
        if(userCondition.getLanguage().equals("english"))
        {
            text = "Gaming laptops";
            row.add("Acer aspire 7");
            row.add("Acer nitro 5");
            row.add("Asus TUF gaming");
            keyboard.add(row);
            row = new KeyboardRow();
            row.add("Asus ROG strix");
            row.add("MSI alpha 15");
            row.add("Dell XPS 13");
            keyboard.add(row);
            row = new KeyboardRow();
            row.add("Back to laptop categories");
            keyboard.add(row);
            markup(chatId, keyboard, text);
        }
        else if(userCondition.getLanguage().equals("russian"))
        {
            text = "Игровые ноутбуки";
            row.add("Acer aspire 7");
            row.add("Acer nitro 5");
            row.add("Asus TUF gaming");
            keyboard.add(row);
            row = new KeyboardRow();
            row.add("Asus ROG strix");
            row.add("MSI alpha 15");
            row.add("Dell XPS 13");
            keyboard.add(row);
            row = new KeyboardRow();
            row.add("Назад в категорию ноутбуков");
            keyboard.add(row);
            markup(chatId, keyboard, text);
        }
    }
    private void chooseOfficeLaps(String chatId, UserCondition userCondition)
    {
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        String text;
        if(userCondition.getLanguage().equals("english"))
        {
            text = "Office laptops";
            row.add("Lenovo ideapad 3");
            row.add("Acer extensa");
            row.add("Asus vivobook");
            keyboard.add(row);
            row = new KeyboardRow();
            row.add("HP 15dw");
            row.add("Dell vostro");
            row.add("Huawei matebook");
            keyboard.add(row);
            row = new KeyboardRow();
            row.add("Back to laptop categories");
            keyboard.add(row);
            markup(chatId, keyboard, text);
        }
        else if(userCondition.getLanguage().equals("russian"))
        {
            text = "Офисные ноутбуки";
            row.add("Lenovo ideapad 3");
            row.add("Acer extensa");
            row.add("Asus vivobook");
            keyboard.add(row);
            row = new KeyboardRow();
            row.add("HP 15dw");
            row.add("Dell vostro");
            row.add("Huawei matebook");
            keyboard.add(row);
            row = new KeyboardRow();
            row.add("Назад в категорию ноутбуков");
            keyboard.add(row);
            markup(chatId, keyboard, text);
        }
    }

    public void laptopInfo(String chatId, UserCondition userCondition, String message)
    {
        String text1 = "Добавить в корзину";
        String text2 = "Заказать";
        String description = "acer aspire 7";
        if(userCondition.getLanguage().equals("english"))
        {
            text1 = "Add to cart";
            text2 = "Order";
        }
        switch(message)
        {
            case "acer aspire 7":
                sendImg(chatId, description, "AgACAgIAAxkBAAEZs1RjZ926Mv7rHfJ7aSfm9net9Po8YwAC9sExG8tlQUuDSxT-iLsM6gEAAwIAA3MAAysE");
                break;
            case "acer nitro 5":
                sendImg(chatId, description, "AgACAgIAAxkBAAEZs1pjZ95sErKj0xgCL1a1Gq9dC1CYAQAC-cExG8tlQUvs7DgzCX95ygEAAwIAA3MAAysE");
                break;
            case "asus tuf gaming":
                sendImg(chatId, description, "AgACAgIAAxkBAAEZs1xjZ96LilUMVEgjEld8vVHIHkBQgAAC-sExG8tlQUu1HodAjJ0wOwEAAwIAA3MAAysE");
                break;
            case "asus rog strix":
                sendImg(chatId, description, "AgACAgIAAxkBAAEZs2BjZ97FtNU1cJsPxdskMl9N3goOoAAC_MExG8tlQUvhN68Y6CLsygEAAwIAA3MAAysE");
                break;
            case "msi alpha 15":
                sendImg(chatId, description, "AgACAgIAAxkBAAEZs2RjZ99CJDlpT-zZR8a8MuwJojTneQACD8IxG8tlQUvA9FQuPbcQxgEAAwIAA3MAAysE");
                break;
            case "dell xps 13":
                sendImg(chatId, description, "AgACAgIAAxkBAAEZs2ZjZ99fQUd1sZvL0LReScLCO-5RZQACEMIxG8tlQUs2fXFL_GtY9wEAAwIAA3MAAysE");
                break;
            case "lenovo ideapad 3":
                sendImg(chatId, description, "AgACAgIAAxkBAAEZs2xjZ-KVi9bxDhxYMslGI82p7OPK0gACH8IxG8tlQUsijM7lYiACagEAAwIAA3MAAysE");
                break;
            case "acer extensa":
                sendImg(chatId, description, "AgACAgIAAxkBAAEZs25jZ-LV5sV4P0ghe_ThIsA88-gDtwACIMIxG8tlQUtpmtMAAUGmcbQBAAMCAANzAAMrBA");
                break;
            case "asus vivobook":
                sendImg(chatId, description, "AgACAgIAAxkBAAEZs3BjZ-LsQI-F5dgn8pYWjzms43T1NAACI8IxG8tlQUtX8Sg68NfFJAEAAwIAA3MAAysE");
                break;
            case "hp 15dw":
                sendImg(chatId, description, "AgACAgIAAxkBAAEZs3JjZ-MDJe3L5_WeaikMXlCBLrvGhwACJcIxG8tlQUswWH8nGRXkeQEAAwIAA3MAAysE");
                break;
            case "dell vostro":
                sendImg(chatId, description, "AgACAgIAAxkBAAEZs3ZjZ-NAWGhJrCtnNKcdBSusS9iRqgACJsIxG8tlQUuBTyBmzYn40AEAAwIAA3MAAysE");
                break;
            case "huawei matebook":
                sendImg(chatId, description, "AgACAgIAAxkBAAEZs3hjZ-NI5RY_cihXam03cK0_QPXwTgACKMIxG8tlQUv5AjDM2AAB_e0BAAMCAANzAAMrBA");
                break;
        }
    }

    public void sendImg(String chatId, String description, String fileId)
    {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(new InputFile(fileId));
        sendPhoto.setCaption(description);
        sendPhoto.setChatId(chatId);
        try{
            execute(sendPhoto);
        }
        catch(TelegramApiException e)
        {
            e.printStackTrace();
        }
    }
}
