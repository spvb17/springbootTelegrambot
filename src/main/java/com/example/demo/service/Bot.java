package com.example.demo.service;

import com.example.demo.config.BotConfig;
import com.sun.tools.javac.Main;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
        catch(TelegramApiException e){e.printStackTrace();}

    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }
    List<UserCondition>users = new ArrayList<>();
    List<String>userCart = new ArrayList<>();
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
                    sendMessage(chatId, "???? ?????????????? ?????????????? ????????");
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
                if(message.equals("?????????? ??????????") || message.equals("select language"))
                {
                    userCondition.setCondition(BotCondition.SELECT_LANG);
                    chooseLanguage(chatId);
                }
                else if(message.equals("??????????????") || message.equals("catalogue"))
                {
                    userCondition.setCondition(BotCondition.SELECT_CATALOGUE);
                    chooseCatalogue(chatId, userCondition);
                }
                else if(message.equals("?????????????????????? ???? ????????????????") || message.equals("subscribe to newsletter"))
                {
                    if(userCondition.getLanguage().equals("english"))
                    {
                        sendMessage(chatId, "Enter your email \n/back - to exit");
                    }
                    else
                    {
                        sendMessage(chatId, "?????????????? ???????? ?????????? \n/back - ?????????? ??????????");
                    }
                    userCondition.setCondition(BotCondition.ENTER_EMAIL);
                }

            }

            else if(userCondition.getCondition().equals(BotCondition.SELECT_CATALOGUE))
            {
                if(message.equals("?????????? ?? ?????????????? ????????") || message.equals("back to main menu"))
                {
                    userCondition.setCondition(BotCondition.CHOOSE_OPERATION);
                    chooseOperation(chatId, userCondition);
                }
                else if(message.equals("?????????????? ????????????????") || message.equals("gaming laptops"))
                {
                    userCondition.setCondition(BotCondition.GAMING_LAPS);
                    chooseGamingLaps(chatId, userCondition);
                }
                else if(message.equals("?????????????? ????????????????") || message.equals("office laptops"))
                {
                    userCondition.setCondition(BotCondition.OFFICE_LAPS);
                    chooseOfficeLaps(chatId, userCondition);
                }
            }

            else if(userCondition.getCondition().equals(BotCondition.GAMING_LAPS))
            {
                if(message.equals("?????????? ?? ?????????????????? ??????????????????") || message.equals("back to laptop categories"))
                {
                    userCondition.setCondition(BotCondition.SELECT_CATALOGUE);
                    chooseCatalogue(chatId, userCondition);
                }
                laptopInfo(chatId, userCondition, message);
            }

            else if(userCondition.getCondition().equals(BotCondition.OFFICE_LAPS))
            {
                if(message.equals("?????????? ?? ?????????????????? ??????????????????") || message.equals("back to laptop categories"))
                {
                    userCondition.setCondition(BotCondition.SELECT_CATALOGUE);
                    chooseCatalogue(chatId, userCondition);
                }
                laptopInfo(chatId, userCondition, message);
            }

            else if(userCondition.getCondition().equals(BotCondition.ENTER_NAME))
            {
                if(userCondition.getLanguage().equals("russian"))
                {
                    if(message.equals("?????????? ?? ?????????????????? ??????????????????"))
                    {
                        userCondition.setCondition(BotCondition.SELECT_CATALOGUE);
                        chooseCatalogue(chatId, userCondition);
                    }
                    {
                        sendMessage(chatId, "?????????????? ?????????? ?????????? ?????????????? ???????????? ??????????????????. ?????????? ?????????? ???????????? ???????????????? ???? 12 ????????");
                        userCondition.setCondition(BotCondition.ENTER_CARD);
                    }
                }
                else
                {
                    if(message.equals("back to laptop categories"))
                    {
                        userCondition.setCondition(BotCondition.SELECT_CATALOGUE);
                        chooseCatalogue(chatId, userCondition);
                    }
                    else
                    {
                        sendMessage(chatId, "Enter the number of the card you want to link. Card number should contain 12 digits");
                        userCondition.setCondition(BotCondition.ENTER_CARD);
                    }
                }
            }

            else if(userCondition.getCondition().equals(BotCondition.ENTER_CARD))
            {
                if(userCondition.getLanguage().equals("russian"))
                {
                    if(message.equals("?????????? ?? ?????????????????? ??????????????????"))
                    {
                        userCondition.setCondition(BotCondition.SELECT_CATALOGUE);
                        chooseCatalogue(chatId, userCondition);
                    }
                    else
                    {
                        if(checkCardNumber(message))
                        {
                            sendMessage(chatId, "?????????? ????????????! ???????????????? ???????????????? ??????????");
                            userCondition.setCardNumber(message);
                            userCondition.setCondition(BotCondition.SELECT_CATALOGUE);
                            chooseCatalogue(chatId, userCondition);
                        }
                        else
                        {
                            sendMessage(chatId, "?????????? ?????????????? ??????????????! ???????????????????? ????????????");
                        }
                    }
                }
                else
                {
                    if(message.equals("back to laptop categories"))
                    {
                        userCondition.setCondition(BotCondition.SELECT_CATALOGUE);
                        chooseCatalogue(chatId, userCondition);
                    }
                    else
                    {
                        if(checkCardNumber(message))
                        {
                            sendMessage(chatId, "Order is accepted! Expect feedback");
                            userCondition.setCardNumber(message);
                            userCondition.setCondition(BotCondition.SELECT_CATALOGUE);
                            chooseCatalogue(chatId, userCondition);
                        }
                        else
                        {
                            sendMessage(chatId, "Card has entered wrong! Try again");
                        }
                    }
                }
            }

            else if(userCondition.getCondition().equals(BotCondition.ENTER_EMAIL))
            {
                if(userCondition.getLanguage().equals("english"))
                {
                    if(!message.equals("/back"))
                    {
                        if(isValid(message))
                        {
                            sendMessage(chatId, "Your email accepted, check your email");
                            userCondition.setCondition(BotCondition.CHOOSE_OPERATION);
                        }
                        else
                        {
                            sendMessage(chatId, "Email address entered wrong, try again!");
                        }
                    }
                    else
                    {
                        userCondition.setCondition(BotCondition.CHOOSE_OPERATION);
                    }
                }
                else
                {
                    if(!message.equals("/back"))
                    {
                        if(isValid(message))
                        {
                            sendMessage(chatId, "???????? ?????????? ??????????????????, ?????????????????? ????");
                            userCondition.setCondition(BotCondition.CHOOSE_OPERATION);
                        }
                        else
                        {
                            sendMessage(chatId, "?????????????????????? ??????????, ???????????????????? ??????????!");
                        }
                    }
                    else
                    {
                        userCondition.setCondition(BotCondition.CHOOSE_OPERATION);
                    }
                }
            }

            else
            {
                sendMessage(chatId, "Command not found");
            }
        }
        //=================================================================================callbackquery
        else if(update.hasCallbackQuery())
        {
            String chatId = update.getCallbackQuery().getFrom().getId().toString();
            String data = update.getCallbackQuery().getData();
            UserCondition userCondition = saveUser(chatId);
            if(userCondition.getCondition().equals(BotCondition.GAMING_LAPS) || userCondition.getCondition().equals(BotCondition.OFFICE_LAPS))
            {
                if(data.equals("order laptop"))
                {
                    if(userCondition.getLanguage().equals("english"))
                    {
                        sendMessage(chatId,"Enter your name: ");
                        userCondition.setCondition(BotCondition.ENTER_NAME);
                    }
                    else if(userCondition.getLanguage().equals("russian"))
                    {
                        sendMessage(chatId, "?????????????? ???????? ??????: ");
                        userCondition.setCondition(BotCondition.ENTER_NAME);
                    }
                }
                else if(data.equals("add laptop to cart"))
                {
                    if(userCondition.getLanguage().equals("english"))
                    {
                        sendMessage(chatId,"Laptop has been added to cart");
                    }
                    else
                    {
                        sendMessage(chatId, "?????????????? ?????? ???????????????? ?? ??????????????");
                    }
                }
            }
        }
        //-----------------------------------------callbackquery
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
            catch(TelegramApiException e){e.printStackTrace();}
        }

        //???????????????? ??????????????????, ???????????????????? ?????????????????????????? ????????
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
                row.add("Subscribe to newsletter");
                keyboard.add(row);
                row = new KeyboardRow();
                row.add("Select language");
                keyboard.add(row);
                markup(chatId, keyboard, text);
            }
            else if(userCondition.getLanguage().equals("russian"))
            {
                text = "?????????????? ????????";
                row.add("??????????????");
                keyboard.add(row);
                row = new KeyboardRow();
                row.add("??????????????");
                row.add("?????????????????? ??????????????");
                row.add("?????????????????????? ???? ????????????????");
                keyboard.add(row);
                row = new KeyboardRow();
                row.add("?????????? ??????????");
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
            text = "?????????????????? ??????????????????";
            row.add("?????????????? ????????????????");
            row.add("?????????????? ????????????????");
            keyboard.add(row);
            row = new KeyboardRow();
            row.add("?????????? ?? ?????????????? ????????");
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
            text = "?????????????? ????????????????";
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
            row.add("?????????? ?? ?????????????????? ??????????????????");
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
            text = "?????????????? ????????????????";
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
            row.add("?????????? ?? ?????????????????? ??????????????????");
            keyboard.add(row);
            markup(chatId, keyboard, text);
        }
    }

    public void laptopInfo(String chatId, UserCondition userCondition, String message)
    {
        String description = "";
        switch(message)
        {
            case "acer aspire 7":
                description = "<b>Acer aspire 7</b> \n??????1920x1080, IPS, 60hz, 15.6\n??????Core i7-9750h, 6x12 2.6ghz\n??????GeForce GTX1650 4gb\n??????RAM 16gb 3.2hz\n??????SSD 512gb NVME\n<b>Price: 480000tg</b>";
                sendImg(chatId, description, "AgACAgIAAxkBAAEZ8QJjckXvWJ5gi5YILggNNe1w6r540AAC8MExG6sVmUv8Aca8sKL9jgEAAwIAA3MAAysE");
                showInlineKeyboard(chatId, userCondition);
                break;
            case "acer nitro 5":
                description = "<b>Acer nitro 5</b> \n??????1920x1080, IPS, 144hz, 15.6\n??????Core i5-12500h, 8x16 2.5ghz\n??????GeForce RTX3060 6gb\n??????RAM 16gb 3.2hz\n??????SSD 512gb NVME\n<b>Price: 779000tg</b>";
                sendImg(chatId, description, "AgACAgIAAxkBAAEZ9BRjcqU4wZqesonhoJF9bWGoiSIKrwAC8sExG6sVmUufvX3QWa-kNgEAAwIAA3MAAysE");
                showInlineKeyboard(chatId, userCondition);
                break;
            case "asus tuf gaming":
                description = "<b>Asus tuf gaming</b>\n??????1920x1080, IPS, 144hz, 15.6\n??????Core i5-11400h, 6x12 2.7ghz\n??????GeForce RTX3060 6gb\n??????RAM 16gb 2.6hz\n??????SSD 512gb NVME\n<b>Price: 645000tg</b>";
                sendImg(chatId, description, "AgACAgIAAxkBAAEZ9BhjcqWJDenhQB-cF9G5nwfrV9y7bQAC88ExG6sVmUtKxbEi_7BtjwEAAwIAA3MAAysE");
                showInlineKeyboard(chatId, userCondition);
                break;
            case "asus rog strix":
                description = "<b>Asus rog strix</b>\n??????2560x1440, IPS, 144hz, 15.6\n??????Ryzen7 6800h, 8x16 3.2ghz\n??????GeForce RTX 3080 8gb\n??????RAM 16gb 3.2hz\n??????SSD 512gb NVME\n<b>Price: 925000tg</b>";
                sendImg(chatId, description, "AgACAgIAAxkBAAEZ9BxjcqWgRDi9VUvsiAMbxd_o2Dh-UgAC9MExG6sVmUvVehlZFVYCbQEAAwIAA3MAAysE");
                showInlineKeyboard(chatId, userCondition);
                break;
            case "msi alpha 15":
                description = "<b>msi alpha 15</b>\n??????1920x1080, IPS, 144hz, 15.6\n??????Ryzen5 5600h, 6x12 3.3ghz\n??????Radeon RX 6600M 8gb\n??????RAM 8gb 3.2hz\n??????SSD 512gb NVME\n<b>Price: 700000tg</b>";
                sendImg(chatId, description, "AgACAgIAAxkBAAEZ9B5jcqW2YAhRqdzd9SJoFU0Q__rpwgAC9cExG6sVmUszZUlnzpoU8wEAAwIAA3MAAysE");
                showInlineKeyboard(chatId, userCondition);
                break;
            case "dell xps 13":
                description = "<b>Dell xps 13</b>\n??????1920x1080, IPS, 60hz, 15.6\n??????Core i5-10310u, 4x8 1.7ghz\n??????GeForce GTX1650 4gb\n??????RAM 8gb 2.6hz\n??????SSD 512gb NVME\n<b>Price: 450000tg</b>";
                sendImg(chatId, description, "AgACAgIAAxkBAAEZ9CBjcqXLjiu5r-MzGjlrgNvUqcNtwwAC9sExG6sVmUuAkVdAqmk9lAEAAwIAA3MAAysE");
                showInlineKeyboard(chatId, userCondition);
                break;
            case "lenovo ideapad 3":
                description = "<b>Lenovo ideapad 3</b>\n??????1920x1080, IPS, 60hz, 15.6\n??????Core i3-10110u, 2x4 2.1ghz\n??????Intel UHD Graphics\n??????RAM 8gb 2.6hz\n??????SSD 256gb m2\n<b>Price: 230000tg</b>";
                sendImg(chatId, description, "AgACAgIAAxkBAAEZ9CJjcqYTzOOygCmuKHAqkQn6ppZ5uQACecQxG6sVmUuVwEkfHAABqV4BAAMCAANzAAMrBA");
                showInlineKeyboard(chatId, userCondition);
                break;
            case "acer extensa":
                description = "<b>Acer extensa</b>\n??????1920x1080, IPS, 60hz, 15.6\n??????Ryzen5 3500u, 2x4 2.1ghz\n??????AMD Radeon Graphics\n??????RAM 8gb 2.6hz\n??????SSD 256gb m2\n<b>Price: 279000tg</b>";
                sendImg(chatId, description, "AgACAgIAAxkBAAEZ9CRjcqYnwh6HL_afdKX-eTegBjHT6QACesQxG6sVmUtcV0aey7MLBgEAAwIAA3MAAysE");
                showInlineKeyboard(chatId, userCondition);
                break;
            case "asus vivobook":
                description = "<b>Asus vivobook</b>\n??????1920x1080, IPS, 60hz, 15.6\n??????Ryzen 5 5500u, 6x12 2.1ghz\n??????AMD Radeon Graphics\n??????RAM 16gb 3.2hz\n??????SSD 512gb NVME\n<b>Price: 400000tg</b>";
                sendImg(chatId, description, "AgACAgIAAxkBAAEZ9CZjcqY8LURLzjSjKVLsKVEPDx7B-gACe8QxG6sVmUtrpQdaQS_tFQEAAwIAA3MAAysE");
                showInlineKeyboard(chatId, userCondition);
                break;
            case "hp 15dw":
                description = "<b>Hp 15dw</b>\n??????1920x1080, TN, 60hz, 15.6\n??????Core i3-1005G1, 2x4 1.2ghz\n??????Intel HD Graphics\n??????RAM 8gb 2.6hz\n??????SSD 256gb m2\n<b>Price: 275000tg</b>";
                sendImg(chatId, description, "AgACAgIAAxkBAAEZ9ChjcqZQ9bua91KbsgEXl_Aw0TeDYgACfMQxG6sVmUuJKAk2NhtOFwEAAwIAA3MAAysE");
                showInlineKeyboard(chatId, userCondition);
                break;
            case "dell vostro":
                description = "<b>Dell vostro</b>\n??????1920x1080, TN, 60hz, 15.6\n??????Core i5-1135G7 4x8 2.4ghz\n??????Intel Iris xe Graphics\n??????RAM 8gb 3.2hz\n??????SSD 1024gb m2\n<b>Price: 410000tg</b>";
                sendImg(chatId, description, "AgACAgIAAxkBAAEZ9CpjcqZi5YJGtrMVhGoP7TYxwbl0EgACfcQxG6sVmUtC1OTiSS301QEAAwIAA3MAAysE");
                showInlineKeyboard(chatId, userCondition);
                break;
            case "huawei matebook":
                description = "<b>Huawei matebook</b>\n??????2160x1440, IPS, 60hz, 14\n??????Core i5-1135G7 4x8 2.4ghz\n??????Intel Iris xe Graphics\n??????RAM 16gb 2.6hz\n??????SSD 512gb NVME\n<b>Price: 530000tg</b>";
                sendImg(chatId, description, "AgACAgIAAxkBAAEZ9CxjcqZ33ApZMVBPexfZDVlakA8w7gACfsQxG6sVmUvJgtGj90pzsgEAAwIAA3MAAysE");
                showInlineKeyboard(chatId, userCondition);
                break;
        }
    }

    private void sendImg(String chatId, String description, String fileId)
    {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(new InputFile(fileId));
        sendPhoto.setCaption(description);
        sendPhoto.setChatId(chatId);
        sendPhoto.setParseMode("HTML");
        try{
            execute(sendPhoto);
        }
        catch(TelegramApiException e)
        {
            e.printStackTrace();
        }
    }

    private void showInlineKeyboard(String chatId, UserCondition userCondition)
    {
        String text1 = "Want to buy?";
        String text2 = "Order???";
        String text3 = "Add to cart\uD83D\uDED2";
        if(userCondition.getLanguage().equals("russian"))
        {
            text1 = "???????????? ?????????????";
            text2 = "???????????????";
            text3 = "???????????????? ?? ??????????????\uD83D\uDED2";
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text1);
        sendMessage.setChatId(chatId);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton>inlineKeyboardButtons = new ArrayList<>();

        InlineKeyboardButton inlineKeyboardButtonOrder = new InlineKeyboardButton();
        inlineKeyboardButtonOrder.setText(text2);
        inlineKeyboardButtonOrder.setCallbackData("order laptop");

        InlineKeyboardButton inlineKeyboardButtonToCart = new InlineKeyboardButton();
        inlineKeyboardButtonToCart.setText(text3);
        inlineKeyboardButtonToCart.setCallbackData("add laptop to cart");

        inlineKeyboardButtons.add(inlineKeyboardButtonToCart);
        inlineKeyboardButtons.add(inlineKeyboardButtonOrder);

        List<List<InlineKeyboardButton>> table = new ArrayList<>();
        table.add(inlineKeyboardButtons);
        inlineKeyboardMarkup.setKeyboard(table);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        try{
            execute(sendMessage);
        }
        catch(TelegramApiException e)
        {
            e.printStackTrace();
        }
    }

    private boolean checkCardNumber(String message)
    {
        if(message.matches("[0-9]*") && message.length()==12)
        {
            return true;
        }
        return false;
    }

    private boolean isValid(String email)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
        {
            return false;
        }
        return pat.matcher(email).matches();
    }
}
