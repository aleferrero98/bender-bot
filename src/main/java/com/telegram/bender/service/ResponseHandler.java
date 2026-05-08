package com.telegram.bender.service;

import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

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
      text.append(response).append("\n\n");
      text.append("_proporcionado por Fastfetch_");

      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText(text.toString());
      message.setParseMode(MARKDOWN);

      sender.execute(message);
   }

   public void replyToTemperature(long chatId) {
      String response = commandExecutorService.executeSensors();
      StringBuilder text = new StringBuilder(THERMOMETER_EMOJI + " *Temperaturas del sistema* " + THERMOMETER_EMOJI + "\n\n");
      text.append(response).append("\n\n");
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
}
