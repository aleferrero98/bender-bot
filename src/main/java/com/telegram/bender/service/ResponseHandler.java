package com.telegram.bender.service;

import java.util.ArrayList;
import java.util.List;

import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.telegram.bender.model.EBotCommand;

public class ResponseHandler {

   private static final String BOT_EMOJI = "\uD83E\uDD16";

   private static final String FUCK_YOU_EMOJI = "\uD83D\uDD95";

   private static final String THERMOMETER_EMOJI = "\uD83C\uDF21";

   private static final String STATISTICS_EMOJI = "\uD83D\uDCCA";

   private static final String MARKDOWN = "Markdown";

   private final CommandExecutorService commandExecutorService;

   // private final Map<Long, EUserStatus> chatStatus;

   private final SilentSender sender;

   public ResponseHandler(SilentSender sender, DBContext db) {
      this.sender = sender;
      this.commandExecutorService = new CommandExecutorService();
      // this.chatStatus = db.getMap("chatStatus");
   }

   public void replyToStart(long chatId) { // TODO agregar mensaje de bienvenida con comandos posibles y funcionalidades
      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText("Hola soy Bender! " + BOT_EMOJI);

      sender.execute(message);
      // chatStatus.put(chatId, AWAITING_NAME);
   }

   public void replyToInfo(long chatId) {
      String response = commandExecutorService.executeFastfetch();
      StringBuilder text = new StringBuilder(STATISTICS_EMOJI + " *Información del sistema* " + STATISTICS_EMOJI + "\n\n");
      text.append("```\n").append(response).append("\n```\n\n");
      text.append("_proporcionado por Fastfetch_");

      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText(text.toString());
      message.setParseMode(MARKDOWN);

      sender.execute(message);
   }

   public void replyToTemperature(long chatId) {
      String response = commandExecutorService.executeSensors();
      StringBuilder text = new StringBuilder(THERMOMETER_EMOJI + " *Temperatura del sistema* " + THERMOMETER_EMOJI + "\n\n");
      text.append(response).append("\n");
      text.append("_proporcionado por lm-sensors_");

      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText(text.toString());
      message.setParseMode(MARKDOWN);

      sender.execute(message);
   }

   public void handleUnknownMessage(long chatId) {
      SendMessage sendMessage = new SendMessage();
      sendMessage.setChatId(chatId);
      sendMessage.setText("No estoy programado para entender eso 😕");

      sender.execute(sendMessage);
   }

   public void replyToHelp(long chatId) {
      StringBuilder helpText = new StringBuilder("🤟🏻 *Comandos disponibles* 🤟🏻\n\n");

      for (EBotCommand command : EBotCommand.values()) {
         helpText.append(String.format("/%s - %s\n", command.getName(), command.getDescription()));
      }

      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText(helpText.toString());
      message.setParseMode(MARKDOWN);

      sender.execute(message);
   }

   public void replyToTurnOffLeds(long chatId) {
      boolean success = commandExecutorService.executeTurnOffLeds();

      SendMessage message = new SendMessage();
      message.setChatId(chatId);

      if (success) {
         message.setText("✅ LEDs apagados exitosamente");
      } else {
         message.setText("❌ No se pudo apagar los LEDs");
      }

      sender.execute(message);
   }

   public void replyToRebootConfirmation(long chatId) {
      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText("¿Estás seguro que querés reiniciar el servidor?");

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      InlineKeyboardButton confirmButton = new InlineKeyboardButton();
      confirmButton.setText("✅ Confirmar Reboot");
      confirmButton.setCallbackData("confirm_reboot");

      keyboard.add(List.of(confirmButton));
      markup.setKeyboard(keyboard);
      message.setReplyMarkup(markup);

      sender.execute(message);
   }

   public void replyToShutdownConfirmation(long chatId) {
      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText("¿Estás seguro que querés apagar el servidor?");

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      InlineKeyboardButton confirmButton = new InlineKeyboardButton();
      confirmButton.setText("✅ Confirmar Shutdown");
      confirmButton.setCallbackData("confirm_shutdown");

      keyboard.add(List.of(confirmButton));
      markup.setKeyboard(keyboard);
      message.setReplyMarkup(markup);

      sender.execute(message);
   }

   public void replyToCoolerMenu(long chatId) {
      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText("Selecciona una opción para el cooler:");

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      InlineKeyboardButton setSpeedButton = new InlineKeyboardButton();
      setSpeedButton.setText("🎛️ Setear Velocidad");
      setSpeedButton.setCallbackData("cooler_set_speed");

      InlineKeyboardButton tempControllerStartButton = new InlineKeyboardButton();
      tempControllerStartButton.setText("🌡️ Habilitar Temperature Controller");
      tempControllerStartButton.setCallbackData("cooler_temp_controller_start");

      InlineKeyboardButton tempControllerStopButton = new InlineKeyboardButton();
      tempControllerStopButton.setText("🌡️ Deshabilitar Temperature Controller");
      tempControllerStopButton.setCallbackData("cooler_temp_controller_stop");

      keyboard.add(List.of(setSpeedButton));
      keyboard.add(List.of(tempControllerStartButton));
      keyboard.add(List.of(tempControllerStopButton));
      markup.setKeyboard(keyboard);
      message.setReplyMarkup(markup);

      sender.execute(message);
   }

   public void replyToCoolerSpeedSelection(long chatId, int messageId) {
      EditMessageText editMessage = new EditMessageText();
      editMessage.setChatId(chatId);
      editMessage.setMessageId(messageId);
      editMessage.setText("Selecciona la velocidad del cooler:");

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      List<InlineKeyboardButton> row1 = new ArrayList<>();
      row1.add(createSpeedButton("0%", "cooler_speed_0"));
      row1.add(createSpeedButton("25%", "cooler_speed_25"));
      row1.add(createSpeedButton("50%", "cooler_speed_50"));

      List<InlineKeyboardButton> row2 = new ArrayList<>();
      row2.add(createSpeedButton("70%", "coiler_speed_70"));
      row2.add(createSpeedButton("85%", "cooler_speed_85"));
      row2.add(createSpeedButton("100%", "cooler_speed_100"));

      keyboard.add(row1);
      keyboard.add(row2);
      markup.setKeyboard(keyboard);
      editMessage.setReplyMarkup(markup);

      sender.execute(editMessage);
   }

   private InlineKeyboardButton createSpeedButton(String label, String callbackData) {
      InlineKeyboardButton button = new InlineKeyboardButton();
      button.setText(label);
      button.setCallbackData(callbackData);
      return button;
   }

   public void handleCallbackQuery(long chatId, int messageId, String callbackData, String callbackQueryId) {
      switch (callbackData) {
         case "confirm_reboot":
            answerCallbackQuery(callbackQueryId);
            sendRebootRequestMessage(chatId);
            break;
         case "confirm_shutdown":
            answerCallbackQuery(callbackQueryId);
            sendShutdownRequestMessage(chatId);
            break;
         case "cooler_set_speed":
            answerCallbackQuery(callbackQueryId);
            replyToCoolerSpeedSelection(chatId, messageId);
            break;
         case "cooler_temp_controller_start":
            answerCallbackQuery(callbackQueryId);
            handleTempControllerStart(chatId);
            break;
         case "cooler_temp_controller_stop":
            answerCallbackQuery(callbackQueryId);
            handleTempControllerStop(chatId);
            break;
         case "cooler_speed_0":
            answerCallbackQuery(callbackQueryId);
            handleCoolerSpeed(chatId, 0);
            break;
         case "cooler_speed_25":
            answerCallbackQuery(callbackQueryId);
            handleCoolerSpeed(chatId, 25);
            break;
         case "cooler_speed_50":
            answerCallbackQuery(callbackQueryId);
            handleCoolerSpeed(chatId, 50);
            break;
         case "coiler_speed_70":
            answerCallbackQuery(callbackQueryId);
            handleCoolerSpeed(chatId, 70);
            break;
         case "cooler_speed_85":
            answerCallbackQuery(callbackQueryId);
            handleCoolerSpeed(chatId, 85);
            break;
         case "cooler_speed_100":
            answerCallbackQuery(callbackQueryId);
            handleCoolerSpeed(chatId, 100);
            break;
         default:
            answerCallbackQuery(callbackQueryId);
            break;
      }
   }

   private void answerCallbackQuery(String callbackQueryId) {
      AnswerCallbackQuery answer = new AnswerCallbackQuery();
      answer.setCallbackQueryId(callbackQueryId);
      sender.execute(answer);
   }

   private void sendRebootRequestMessage(long chatId) {
      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText("🔄 Reiniciando el servidor...");
      sender.execute(message);

      boolean success = commandExecutorService.executeReboot();
      if (!success) {
         SendMessage errorMsg = new SendMessage();
         errorMsg.setChatId(chatId);
         errorMsg.setText("❌ No se pudo reiniciar el servidor");
         sender.execute(errorMsg);
      }
   }

   private void sendShutdownRequestMessage(long chatId) {
      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText("⏻ Apagando el servidor...");
      sender.execute(message);

      boolean success = commandExecutorService.executeShutdown();
      if (!success) {
         SendMessage errorMsg = new SendMessage();
         errorMsg.setChatId(chatId);
         errorMsg.setText("❌ No se pudo apagar el servidor");
         sender.execute(errorMsg);
      }
   }

   private void handleCoolerSpeed(long chatId, int speed) {
      boolean success = commandExecutorService.setCoolerSpeed(speed);

      SendMessage message = new SendMessage();
      message.setChatId(chatId);

      if (success) {
         message.setText("✅ Cooler seteado al **" + speed + "%**");
         message.setParseMode(MARKDOWN);
      } else {
         message.setText("❌ No se pudo setear la velocidad del cooler");
      }

      sender.execute(message);
   }

   private void handleTempControllerStart(long chatId) {
      boolean success = commandExecutorService.enableTempController();

      SendMessage message = new SendMessage();
      message.setChatId(chatId);

      if (success) {
         message.setText("✅ Temperature controller iniciado");
      } else {
         message.setText("❌ No se pudo iniciar el Temperature controller");
      }

      sender.execute(message);
   }

   private void handleTempControllerStop(long chatId) {
      boolean success = commandExecutorService.disableTempController();

      SendMessage message = new SendMessage();
      message.setChatId(chatId);

      if (success) {
         message.setText("✅ Temperature controller deshabilitado");
      } else {
         message.setText("❌ No se pudo deshabilitar el Temperature controller");
      }

      sender.execute(message);
   }
}
