# springbootTelegrambot
<a href="http://qrcoder.ru" target="_blank"><img src="http://qrcoder.ru/code/?https%3A%2F%2Ft.me%2FspringbootProject_bot&4&0" width="148" height="148" border="0" title="QR код"></a>

## About

This is a simple telegram polling bot that I made using spring-boot. Worked with java 17, spring-boot, telegram-bot API and gmail API. The project is built in maven. You can find this bot by the QR-code above or by the link below\
https://t.me/springbootProject_bot

All methods and instructions on how to use API were taken from the page below\
https://core.telegram.org/

## Description
This is a telegram bot for a laptop shop. Here you can find and order laptops and their components. Each user has their own shopping cart, purchase history.\
When you start the bot for the first time, the language will be requested, then the interface will be configured based on the choice.
>The main menu looks like this:
><img src="https://user-images.githubusercontent.com/90541044/201676295-b5f35276-d332-4603-ba53-4272fe942fbd.png">

From here you can go to the catalogue which is divided into gaming and office laptops.  Depending on the choice, a catalogue of laptops is provided. After selecting a specific laptop, a message is sent to the user with a description of the laptop. Below the description is sent to the inlineKeyboard with graphs to buy and add to cart. 

## Difficulties
I ran into several difficulties when writing the logic of the bot. Problems arose due to the fact that the solution to these problems was not described in the telegram API itself.
* <b>Storing the bot state and intercepting the user's message depending on this state.</b>\
If you do not determine the state of the bot, then it turns out that all messages are mixed, and it is impossible to determine what data the user entered.
* <b>Storing the bot state for each user separately.</b>\
The problem was that if several users use the bot, the logic breaks, and data from one user's chat is mixed with data from another user's chat.

Below I have provided the solutions I came up with

## Solutions
Solving the first problem
>To solve the first problem, I created the BotCondition interface, in which I created the fields below.
<img src="https://user-images.githubusercontent.com/90541044/201696117-60662a27-6901-4d22-a7c9-2894e24c868d.png" width="200px">\
In the example below, if /start was pressed by the user, then the bot state changes to SELECT_LANG. The next message sent by the user will be taken as the language choice, since the bot's condition is SELECT_LANG. After choosing the language, the state of the bot will change again, and subsequent messages from the user will be received properly. The state of the bot needs to be checked with conditional constructs with each update. The state of the bot can be moved both forward and backward, based on the logic of your bot.
<img src="https://user-images.githubusercontent.com/90541044/201696711-7bb4e5e8-746c-4b4e-9e33-32c1562d4b3d.png">

Solving the second problem
>



 
