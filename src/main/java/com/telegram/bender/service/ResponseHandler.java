package com.telegram.bender.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.telegram.bender.model.EBotCommand;
import com.telegram.bender.model.FrequentAppEntity;
import com.telegram.bender.model.TunnelEntity;

public class ResponseHandler {

   private static final String MARKDOWN = "Markdown";
   private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
   private static final int MIN_PORT = 0;
   private static final int MAX_PORT = 65535;

   private final CommandExecutorService commandExecutorService;
   private final TunnelService tunnelService;
   private final FrequentAppService frequentAppService;
   private final Map<Long, String> awaitingCustomPort;

   private final SilentSender sender;

   public ResponseHandler(SilentSender sender, DBContext db, TunnelService tunnelService, FrequentAppService frequentAppService) {
      this.sender = sender;
      this.commandExecutorService = new CommandExecutorService();
      this.tunnelService = tunnelService;
      this.frequentAppService = frequentAppService;
      this.awaitingCustomPort = new ConcurrentHashMap<>();
   }

   public void replyToStart(long chatId) {
      StringBuilder text = new StringBuilder();
      text.append("🤖 *¿Qué hacemos, jefe?*\n\n");
      text.append("⚡ /info - Ver estado del sistema\n");
      text.append("🎛️ /cooler - Controlar ventilación\n");
      text.append("🔗 /tunnel - Gestionar túneles\n");
      text.append("⚙️ /manage - Reiniciar, apagar, LEDs\n");
      text.append("💾 /backup - Respaldar datos\n");
      text.append("❓ /help - Lista completa de comandos\n\n");
      text.append("_¿Querés que haga algo o solo mirás?_");

      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText(text.toString());
      message.setParseMode(MARKDOWN);

      sender.execute(message);
   }

   public void replyToInfo(long chatId) {
      String response = commandExecutorService.executeFastfetch();
      StringBuilder text = new StringBuilder("📊 *Información del sistema* 📊\n\n");
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
      StringBuilder text = new StringBuilder("🌡️ *Temperatura del sistema* 🌡️\n\n");
      text.append(response).append("\n");
      text.append("_proporcionado por lm-sensors_");

      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText(text.toString());
      message.setParseMode(MARKDOWN);

      sender.execute(message);
   }

   public void replyToInfoMenu(long chatId) {
      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText("Selecciona una opción:");

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      InlineKeyboardButton infoButton = new InlineKeyboardButton();
      infoButton.setText("📊 Información del sistema");
      infoButton.setCallbackData("info_system");

      InlineKeyboardButton tempButton = new InlineKeyboardButton();
      tempButton.setText("🌡️ Temperatura del sistema");
      tempButton.setCallbackData("info_temperature");

      keyboard.add(List.of(infoButton));
      keyboard.add(List.of(tempButton));
      markup.setKeyboard(keyboard);
      message.setReplyMarkup(markup);

      sender.execute(message);
   }

   public void handleUnknownMessage(long chatId) {
      SendMessage sendMessage = new SendMessage();
      sendMessage.setChatId(chatId);
      sendMessage.setText("No estoy programado para entender eso 😕");

      sender.execute(sendMessage);
   }

   public void handleUnauthorizedAccess(long chatId) {
      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText("🖕🏻🖕🏻🖕🏻");
      sender.execute(message);
   }

   public void replyToManageMenu(long chatId) {
      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText("Selecciona una opción:");

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      InlineKeyboardButton rebootButton = new InlineKeyboardButton();
      rebootButton.setText("🔄 Reiniciar servidor");
      rebootButton.setCallbackData("reboot");

      InlineKeyboardButton shutdownButton = new InlineKeyboardButton();
      shutdownButton.setText("⏻ Apagar servidor");
      shutdownButton.setCallbackData("shutdown");

      InlineKeyboardButton ledsButton = new InlineKeyboardButton();
      ledsButton.setText("💡 Apagar LEDs");
      ledsButton.setCallbackData("turn_off_leds");

      keyboard.add(List.of(rebootButton));
      keyboard.add(List.of(shutdownButton));
      keyboard.add(List.of(ledsButton));
      markup.setKeyboard(keyboard);
      message.setReplyMarkup(markup);

      sender.execute(message);
   }

   public void replyToHelp(long chatId) {
      StringBuilder helpText = new StringBuilder("🤟🏻 *Comandos disponibles* 🤟🏻\n\n");

      for (EBotCommand command : EBotCommand.values()) {
         helpText.append(String.format("%s /%s - %s\n", command.getEmoji(), command.getName(), command.getDescription()));
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
      confirmButton.setText("✅");
      confirmButton.setCallbackData("confirm_reboot");

      InlineKeyboardButton cancelButton = new InlineKeyboardButton();
      cancelButton.setText("❌");
      cancelButton.setCallbackData("reboot_cancel");

      keyboard.add(List.of(confirmButton, cancelButton));
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
      confirmButton.setText("✅");
      confirmButton.setCallbackData("confirm_shutdown");

      InlineKeyboardButton cancelButton = new InlineKeyboardButton();
      cancelButton.setText("❌");
      cancelButton.setCallbackData("shutdown_cancel");

      keyboard.add(List.of(confirmButton, cancelButton));
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

      InlineKeyboardButton tempControllerButton = new InlineKeyboardButton();
      tempControllerButton.setText("🌡️ Temperature Controller");
      tempControllerButton.setCallbackData("cooler_temp_controller");

      keyboard.add(List.of(setSpeedButton));
      keyboard.add(List.of(tempControllerButton));
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
      answerCallbackQuery(callbackQueryId);

      switch (callbackData) {
         case "confirm_reboot":
            sendRebootRequestMessage(chatId);
            break;
         case "confirm_shutdown":
            sendShutdownRequestMessage(chatId);
            break;
         case "reboot":
            replyToRebootConfirmation(chatId);
            break;
         case "shutdown":
            replyToShutdownConfirmation(chatId);
            break;
         case "turn_off_leds":
            replyToTurnOffLeds(chatId);
            break;
         case "info_system":
            replyToInfo(chatId);
            break;
         case "info_temperature":
            replyToTemperature(chatId);
            break;
         case "cooler_set_speed":
            replyToCoolerSpeedSelection(chatId, messageId);
            break;
         case "cooler_temp_controller":
            replyToTempControllerStatus(chatId);
            break;
         case "cooler_temp_controller_start":
            handleTempControllerStart(chatId);
            break;
         case "cooler_temp_controller_stop":
            handleTempControllerStop(chatId);
            break;
         case "cooler_speed_0":
            handleCoolerSpeed(chatId, 0);
            break;
         case "cooler_speed_25":
            handleCoolerSpeed(chatId, 25);
            break;
         case "cooler_speed_50":
            handleCoolerSpeed(chatId, 50);
            break;
         case "coiler_speed_70":
            handleCoolerSpeed(chatId, 70);
            break;
         case "cooler_speed_85":
            handleCoolerSpeed(chatId, 85);
            break;
         case "cooler_speed_100":
            handleCoolerSpeed(chatId, 100);
            break;
         case "tunnel_menu":
            replyToTunnelMenu(chatId);
            break;
         case "tunnel_list":
            replyToTunnelList(chatId);
            break;
         case "tunnel_create":
            replyToTunnelCreatePortSelection(chatId, messageId);
            break;
         case "tunnel_frequent":
            replyToTunnelFrequentAppSelection(chatId, messageId);
            break;
         case "tunnel_cancel":
            replyToTunnelCancelSelection(chatId);
            break;
         case "reboot_cancel":
         case "shutdown_cancel":
         case "tunnel_list_dismiss":
         case "tunnel_cancel_dismiss":
         case "tunnel_cancel_discard":
         case "tunnel_create_dismiss":
         case "tunnel_frequent_dismiss":
            handleCancelConfirmation(chatId, messageId);
            break;
         case "backup_menu":
            replyToBackupMenu(chatId, messageId);
            break;
         case "backup_service":
            replyToServiceBackupSelection(chatId, messageId);
            break;
         case "backup_cold":
            replyToColdBackupConfirmation(chatId, messageId);
            break;
         case "backup_service_confirm_immich":
            handleServiceBackup(chatId, "Immich", commandExecutorService.executeImmichBackup());
            break;
         case "backup_service_confirm_nextcloud":
            handleServiceBackup(chatId, "Nextcloud", commandExecutorService.executeNextcloudBackup());
            break;
         case "backup_service_confirm_docker":
            handleServiceBackup(chatId, "Docker", commandExecutorService.executeDockerBackup());
            break;
         case "backup_cold_confirm":
            handleColdBackup(chatId);
            break;
         default:
            if (callbackData.startsWith("tunnel_cancel_select_")) {
               replyToTunnelCancelConfirmation(chatId, callbackData);
            } else if (callbackData.startsWith("tunnel_cancel_confirm_")) {
               handleTunnelCancel(chatId, callbackData);
            } else if (callbackData.equals("tunnel_create_port_custom")) {
               replyToTunnelCreateCustomPort(chatId, messageId);
            } else if (callbackData.startsWith("tunnel_create_port_")) {
               handleTunnelCreatePortSelection(chatId, callbackData);
            } else if (callbackData.startsWith("tunnel_create_duration_")) {
               handleTunnelCreateDurationSelection(chatId, callbackData);
            } else if (callbackData.startsWith("tunnel_create_confirm_")) {
               handleTunnelCreateConfirm(chatId, callbackData);
            } else if (callbackData.startsWith("tunnel_frequent_app_")) {
               handleTunnelFrequentAppSelection(chatId, messageId, callbackData);
            } else if (callbackData.startsWith("tunnel_frequent_duration_")) {
               handleTunnelFrequentDurationSelection(chatId, messageId, callbackData);
            } else if (callbackData.startsWith("tunnel_frequent_confirm_")) {
               handleTunnelFrequentConfirm(chatId, callbackData);
            } else if (callbackData.equals("backup_service_immich")) {
               replyToServiceBackupConfirmation(chatId, "Immich", "backup_service_confirm_immich");
            } else if (callbackData.equals("backup_service_nextcloud")) {
               replyToServiceBackupConfirmation(chatId, "Nextcloud", "backup_service_confirm_nextcloud");
            } else if (callbackData.equals("backup_service_docker")) {
               replyToServiceBackupConfirmation(chatId, "Docker", "backup_service_confirm_docker");
            }
            break;
      }
   }

   private void answerCallbackQuery(String callbackQueryId) {
      AnswerCallbackQuery answer = new AnswerCallbackQuery();
      answer.setCallbackQueryId(callbackQueryId);
      sender.execute(answer);
   }

   private void handleCancelConfirmation(long chatId, int messageId) {
      DeleteMessage deleteMessage = new DeleteMessage();
      deleteMessage.setChatId(chatId);
      deleteMessage.setMessageId(messageId);
      sender.execute(deleteMessage);
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
         message.setText("✅ Cooler seteado al *" + speed + "%*");
         message.setParseMode(MARKDOWN);
      } else {
         message.setText("❌ No se pudo setear la velocidad del cooler");
      }

      sender.execute(message);
   }

   private void replyToTempControllerStatus(long chatId) {
      boolean isRunning = commandExecutorService.isTempControllerRunning();

      SendMessage message = new SendMessage();
      message.setChatId(chatId);

      StringBuilder text = new StringBuilder();
      text.append("🌡️ *Temperature Controller*\n\n");
      if (isRunning) {
         text.append("Estado: *Activo* 🟢");
      } else {
         text.append("Estado: *Inactivo* 🔴");
      }
      message.setText(text.toString());
      message.setParseMode(MARKDOWN);

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      InlineKeyboardButton actionButton = new InlineKeyboardButton();
      if (isRunning) {
         actionButton.setText("❌ Deshabilitar");
         actionButton.setCallbackData("cooler_temp_controller_stop");
      } else {
         actionButton.setText("✅ Habilitar");
         actionButton.setCallbackData("cooler_temp_controller_start");
      }

      keyboard.add(List.of(actionButton));
      markup.setKeyboard(keyboard);
      message.setReplyMarkup(markup);

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

   public void replyToTunnelMenu(long chatId) {
      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText("🔗 *Túneles SSH*\n\nSelecciona una opción:");

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      InlineKeyboardButton listButton = new InlineKeyboardButton();
      listButton.setText("📋 Listar túneles activos");
      listButton.setCallbackData("tunnel_list");

      InlineKeyboardButton createButton = new InlineKeyboardButton();
      createButton.setText("➕ Crear nuevo túnel");
      createButton.setCallbackData("tunnel_create");

      InlineKeyboardButton frequentButton = new InlineKeyboardButton();
      frequentButton.setText("⚡ Levantar app frecuente");
      frequentButton.setCallbackData("tunnel_frequent");

      keyboard.add(List.of(listButton));
      keyboard.add(List.of(createButton));
      keyboard.add(List.of(frequentButton));
      markup.setKeyboard(keyboard);
      message.setReplyMarkup(markup);
      message.setParseMode(MARKDOWN);

      sender.execute(message);
   }

   private void replyToTunnelList(long chatId) {
      List<TunnelEntity> activeTunnels = tunnelService.getActiveTunnels();

      if (activeTunnels.isEmpty()) {
         SendMessage message = new SendMessage();
         message.setChatId(chatId);
         message.setText("📋 No hay túneles activos en este momento.");
         sender.execute(message);
         return;
      }

      StringBuilder text = new StringBuilder("📋 *Túneles activos*\n\n");
      String tunnelsText = activeTunnels.stream().map(
         tunnel -> String.format(
               "  🆔 *ID:* %d\n  🔗 *URL:* %s\n  🔌 *Puerto:* %d\n  ⏰ *Expira:* %s\n",
               tunnel.getId(), tunnel.getUrl(), tunnel.getExposedPort(),
               tunnel.getExpiresAt().format(DATE_FORMATTER))
      ).collect(Collectors.joining("\n───────────────────\n\n"));
      text.append(tunnelsText);

      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText(text.toString());
      message.setParseMode(MARKDOWN);

       InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      InlineKeyboardButton cancelButton = new InlineKeyboardButton();
      cancelButton.setText("❌ Cancelar túnel");
      cancelButton.setCallbackData("tunnel_cancel");
      keyboard.add(List.of(cancelButton));

      InlineKeyboardButton backButton = new InlineKeyboardButton();
      backButton.setText("🔙 Volver al menú");
      backButton.setCallbackData("tunnel_list_dismiss");
      keyboard.add(List.of(backButton));

      markup.setKeyboard(keyboard);
      message.setReplyMarkup(markup);

      sender.execute(message);
   }

   private void handleTunnelCancel(long chatId, String callbackData) {
      String tunnelIdStr = callbackData.replace("tunnel_cancel_confirm_", "");
      try {
         Integer tunnelId = Integer.parseInt(tunnelIdStr);
         tunnelService.cancelTunnel(tunnelId);
         SendMessage message = new SendMessage();
         message.setChatId(chatId);
         message.setText("✅ Túnel " + tunnelId + " cancelado exitosamente.");
         sender.execute(message);
      } catch (Exception e) {
         SendMessage message = new SendMessage();
         message.setChatId(chatId);
         message.setText("❌ Error al cancelar el túnel: " + e.getMessage());
         sender.execute(message);
      }
      replyToTunnelList(chatId);
   }

   private void replyToTunnelCancelSelection(long chatId) {
      List<TunnelEntity> activeTunnels = tunnelService.getActiveTunnels();

      if (activeTunnels.isEmpty()) {
         SendMessage message = new SendMessage();
         message.setChatId(chatId);
         message.setText("📋 No hay túneles activos para cancelar.");
         sender.execute(message);
         return;
      }

      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText("❌ Selecciona el túnel a cancelar:");

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      for (TunnelEntity tunnel : activeTunnels) {
         InlineKeyboardButton tunnelButton = new InlineKeyboardButton();
         tunnelButton.setText("Túnel " + tunnel.getId() + " (🔌" + tunnel.getExposedPort() + ")");
         tunnelButton.setCallbackData("tunnel_cancel_select_" + tunnel.getId());
         keyboard.add(List.of(tunnelButton));
      }

      InlineKeyboardButton backButton = new InlineKeyboardButton();
      backButton.setText("🔙 Volver");
      backButton.setCallbackData("tunnel_cancel_dismiss");
      keyboard.add(List.of(backButton));

      markup.setKeyboard(keyboard);
      message.setReplyMarkup(markup);

      sender.execute(message);
   }

   private void replyToTunnelCancelConfirmation(long chatId, String callbackData) {
      String tunnelIdStr = callbackData.replace("tunnel_cancel_select_", "");
      Integer tunnelId = Integer.parseInt(tunnelIdStr);

      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText("¿Estás seguro que querés cancelar el túnel " + tunnelId + "?");

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      InlineKeyboardButton confirmButton = new InlineKeyboardButton();
      confirmButton.setText("✅");
      confirmButton.setCallbackData("tunnel_cancel_confirm_" + tunnelId);

      InlineKeyboardButton cancelButton = new InlineKeyboardButton();
      cancelButton.setText("❌");
      cancelButton.setCallbackData("tunnel_cancel_discard");

      keyboard.add(List.of(confirmButton, cancelButton));
      markup.setKeyboard(keyboard);
      message.setReplyMarkup(markup);

      sender.execute(message);
   }

   private void replyToTunnelCreatePortSelection(long chatId, int messageId) {
      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText("🔌 Selecciona el puerto a exponer:");

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      List<InlineKeyboardButton> row1 = new ArrayList<>();
      row1.add(createPortButton("22 (SSH)", "tunnel_create_port_22"));
      row1.add(createPortButton("80 (HTTP)", "tunnel_create_port_80"));
      row1.add(createPortButton("443 (HTTPS)", "tunnel_create_port_443"));

      List<InlineKeyboardButton> row2 = new ArrayList<>();
      row2.add(createPortButton("✏️ Otro", "tunnel_create_port_custom"));

      List<InlineKeyboardButton> row3 = new ArrayList<>();
      row3.add(createPortButton("🔙 Volver", "tunnel_create_dismiss"));

      keyboard.add(row1);
      keyboard.add(row2);
      keyboard.add(row3);
      markup.setKeyboard(keyboard);
      message.setReplyMarkup(markup);

      sender.execute(message);
   }

   private InlineKeyboardButton createPortButton(String label, String callbackData) {
      InlineKeyboardButton button = new InlineKeyboardButton();
      button.setText(label);
      button.setCallbackData(callbackData);
      return button;
   }

   private void handleTunnelCreatePortSelection(long chatId, String callbackData) {
      String portStr = callbackData.replace("tunnel_create_port_", "");
      int port = Integer.parseInt(portStr);
      replyToTunnelCreateDurationSelection(chatId, port);
   }

   private void replyToTunnelCreateCustomPort(long chatId, int messageId) {
      awaitingCustomPort.put(chatId, "tunnel_create");

      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText("✏️ Ingresa el número de puerto (0-65535):");

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      InlineKeyboardButton cancelButton = new InlineKeyboardButton();
      cancelButton.setText("🔙 Cancelar");
      cancelButton.setCallbackData("tunnel_create_dismiss");
      keyboard.add(List.of(cancelButton));

      markup.setKeyboard(keyboard);
      message.setReplyMarkup(markup);

      sender.execute(message);
   }

   public boolean isAwaitingCustomPort(long chatId) {
      return awaitingCustomPort.containsKey(chatId);
   }

   public void handleCustomPortInput(long chatId, String input) {
      String action = awaitingCustomPort.remove(chatId);
      if (action == null) {
         return;
      }

      try {
         int port = Integer.parseInt(input.trim());
         if (port < MIN_PORT || port > MAX_PORT) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("❌ Puerto inválido. Debe estar entre 0 y 65535.");
            sender.execute(message);
            return;
         }
         replyToTunnelCreateDurationSelection(chatId, port);
      } catch (NumberFormatException e) {
         SendMessage message = new SendMessage();
         message.setChatId(chatId);
         message.setText("❌ Puerto inválido. Debe ser un número entero.");
         sender.execute(message);
      }
   }

   private void replyToTunnelCreateDurationSelection(long chatId, int port) {
      String text = "⏰ Selecciona el período disponible para el puerto " + port + ":";
      InlineKeyboardMarkup markup = buildCreateDurationKeyboard(port);

      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText(text);
      message.setReplyMarkup(markup);
      sender.execute(message);
   }

   private InlineKeyboardMarkup buildCreateDurationKeyboard(int port) {
      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      List<InlineKeyboardButton> row1 = new ArrayList<>();
      row1.add(createDurationButton("15 min", "tunnel_create_duration_" + port + "_15"));
      row1.add(createDurationButton("30 min", "tunnel_create_duration_" + port + "_30"));
      row1.add(createDurationButton("1 hora", "tunnel_create_duration_" + port + "_60"));

      List<InlineKeyboardButton> row2 = new ArrayList<>();
      row2.add(createDurationButton("6 horas", "tunnel_create_duration_" + port + "_360"));
      row2.add(createDurationButton("24 horas", "tunnel_create_duration_" + port + "_1440"));
      row2.add(createDurationButton("7 días", "tunnel_create_duration_" + port + "_10080"));

      List<InlineKeyboardButton> row3 = new ArrayList<>();
      row3.add(createDurationButton("🔙 Volver", "tunnel_create_dismiss"));

      keyboard.add(row1);
      keyboard.add(row2);
      keyboard.add(row3);
      markup.setKeyboard(keyboard);

      return markup;
   }

   private InlineKeyboardButton createDurationButton(String label, String callbackData) {
      InlineKeyboardButton button = new InlineKeyboardButton();
      button.setText(label);
      button.setCallbackData(callbackData);
      return button;
   }

   private void handleTunnelCreateDurationSelection(long chatId, String callbackData) {
      String[] parts = callbackData.replace("tunnel_create_duration_", "").split("_");
      int port = Integer.parseInt(parts[0]);
      int duration = Integer.parseInt(parts[1]);
      replyToTunnelCreateConfirmation(chatId, port, duration);
   }

   private void replyToTunnelCreateConfirmation(long chatId, int port, int durationMinutes) {
      String durationText = formatDuration(durationMinutes);

      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText(String.format(
            "📋 *Resumen del túnel*\n\n" +
            "🔌 *Puerto:* %d\n" +
            "⏰ *Duración:* %s\n" +
            "☁️ *Proveedor:* Cloudflare\n\n" +
            "¿Confirmás la creación del túnel?",
            port, durationText
      ));
      message.setParseMode(MARKDOWN);

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      InlineKeyboardButton confirmButton = new InlineKeyboardButton();
      confirmButton.setText("✅");
      confirmButton.setCallbackData("tunnel_create_confirm_" + port + "_" + durationMinutes);

      InlineKeyboardButton cancelButton = new InlineKeyboardButton();
      cancelButton.setText("❌");
      cancelButton.setCallbackData("tunnel_create_dismiss");

      keyboard.add(List.of(confirmButton, cancelButton));
      markup.setKeyboard(keyboard);
      message.setReplyMarkup(markup);

      sender.execute(message);
   }

   private void handleTunnelCreateConfirm(long chatId, String callbackData) {
      String[] parts = callbackData.replace("tunnel_create_confirm_", "").split("_");
      int port = Integer.parseInt(parts[0]);
      int durationMinutes = Integer.parseInt(parts[1]);

      SendMessage waitMessage = new SendMessage();
      waitMessage.setChatId(chatId);
      waitMessage.setText("⏳ Creando túnel en Cloudflare...");
      sender.execute(waitMessage);

      try {
         TunnelEntity tunnel = tunnelService.createTunnel(port, durationMinutes);

         SendMessage message = new SendMessage();
         message.setChatId(chatId);
         message.setText(String.format(
               "✅ *Túnel creado exitosamente*\n\n" +
               "🆔 *ID:* %d\n" +
               "🔗 *URL:* %s\n" +
               "🔌 *Puerto:* %d\n" +
               "⏰ *Expira:* %s\n" +
               "☁️ *Proveedor:* Cloudflare",
               tunnel.getId(), tunnel.getUrl(), tunnel.getExposedPort(),
               tunnel.getExpiresAt().format(DATE_FORMATTER)
         ));
         message.setParseMode(MARKDOWN);
         sender.execute(message);
      } catch (Exception e) {
         SendMessage message = new SendMessage();
         message.setChatId(chatId);
         message.setText("❌ Error al crear el túnel");
         sender.execute(message);
      }
   }

   private void replyToTunnelFrequentAppSelection(long chatId, int messageId) {
      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText("⚡ Selecciona la app frecuente:");

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      List<FrequentAppEntity> enabledApps = frequentAppService.getEnabledApps();
      List<InlineKeyboardButton> currentRow = new ArrayList<>();

      InlineKeyboardButton backButton = new InlineKeyboardButton();
      backButton.setText("🔙 Volver");
      backButton.setCallbackData("tunnel_frequent_dismiss");

      for (FrequentAppEntity app : enabledApps) {
         InlineKeyboardButton appButton = new InlineKeyboardButton();
         appButton.setText(app.getName());
         appButton.setCallbackData("tunnel_frequent_app_" + app.getName());
         currentRow.add(appButton);

         if (currentRow.size() == 2) {
            keyboard.add(currentRow);
            currentRow = new ArrayList<>();
         }
      }

      if (currentRow.size() == 1) {
         currentRow.add(backButton);
         keyboard.add(currentRow);
      } else {
         keyboard.add(List.of(backButton));
      }

      markup.setKeyboard(keyboard);
      message.setReplyMarkup(markup);

      sender.execute(message);
   }

   private void handleTunnelFrequentAppSelection(long chatId, int messageId, String callbackData) {
      String appName = callbackData.replace("tunnel_frequent_app_", "");
      replyToTunnelFrequentDurationSelection(chatId, messageId, appName);
   }

   private void replyToTunnelFrequentDurationSelection(long chatId, int messageId, String serviceName) {
      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText("⏰ Selecciona el período disponible para " + serviceName + ":");

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      List<InlineKeyboardButton> row1 = new ArrayList<>();
      row1.add(createDurationButton("15 min", "tunnel_frequent_duration_" + serviceName + "_15"));
      row1.add(createDurationButton("30 min", "tunnel_frequent_duration_" + serviceName + "_30"));
      row1.add(createDurationButton("1 hora", "tunnel_frequent_duration_" + serviceName + "_60"));

      List<InlineKeyboardButton> row2 = new ArrayList<>();
      row2.add(createDurationButton("6 horas", "tunnel_frequent_duration_" + serviceName + "_360"));
      row2.add(createDurationButton("24 horas", "tunnel_frequent_duration_" + serviceName + "_1440"));
      row2.add(createDurationButton("7 días", "tunnel_frequent_duration_" + serviceName + "_10080"));

      List<InlineKeyboardButton> row3 = new ArrayList<>();
      row3.add(createDurationButton("🔙 Volver", "tunnel_frequent_dismiss"));

      keyboard.add(row1);
      keyboard.add(row2);
      keyboard.add(row3);
      markup.setKeyboard(keyboard);
      message.setReplyMarkup(markup);

      sender.execute(message);
   }

   private void handleTunnelFrequentDurationSelection(long chatId, int messageId, String callbackData) {
      String[] parts = callbackData.replace("tunnel_frequent_duration_", "").split("_");
      String serviceName = parts[0];
      int durationMinutes = Integer.parseInt(parts[1]);
      replyToTunnelFrequentConfirmation(chatId, messageId, serviceName, durationMinutes);
   }

   private void replyToTunnelFrequentConfirmation(long chatId, int messageId, String serviceName, int durationMinutes) {
      FrequentAppEntity app = frequentAppService.getByName(serviceName);

      String durationText = formatDuration(durationMinutes);

      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText(String.format(
            "📋 *Resumen de la app frecuente*\n\n" +
            "⚡ *App:* %s\n" +
            "🔌 *Puerto:* %d\n" +
            "⏰ *Duración:* %s\n" +
            "🔗 *URL corta:* %s\n" +
            "☁️ *Proveedor:* Cloudflare\n\n" +
            "¿Confirmás la creación del túnel?",
            serviceName, app.getPort(), durationText, app.getShortIoUrl()
      ));
      message.setParseMode(MARKDOWN);

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      InlineKeyboardButton confirmButton = new InlineKeyboardButton();
      confirmButton.setText("✅");
      confirmButton.setCallbackData("tunnel_frequent_confirm_" + serviceName + "_" + durationMinutes);

      InlineKeyboardButton cancelButton = new InlineKeyboardButton();
      cancelButton.setText("❌");
      cancelButton.setCallbackData("tunnel_frequent_dismiss");

      keyboard.add(List.of(confirmButton, cancelButton));
      markup.setKeyboard(keyboard);
      message.setReplyMarkup(markup);

      sender.execute(message);
   }

   private void handleTunnelFrequentConfirm(long chatId, String callbackData) {
      String[] parts = callbackData.replace("tunnel_frequent_confirm_", "").split("_");
      String serviceName = parts[0];
      int durationMinutes = Integer.parseInt(parts[1]);

      FrequentAppEntity app = frequentAppService.getByName(serviceName);

      SendMessage waitMessage = new SendMessage();
      waitMessage.setChatId(chatId);
      waitMessage.setText("⏳ Creando túnel para " + serviceName + "...");
      sender.execute(waitMessage);

      try {
         TunnelEntity tunnel = tunnelService.createFrequentAppTunnel(serviceName, durationMinutes);

         SendMessage message = new SendMessage();
         message.setChatId(chatId);
         message.setText(String.format(
               "✅ *Túnel creado exitosamente*\n\n" +
               "🆔 *ID:* %d\n" +
               "🌐 *URL:* %s\n" +
               "🔗 *URL corta:* %s\n" +
               "🔌 *Puerto:* %d\n" +
               "⏰ *Expira:* %s\n" +
               "☁️ *Proveedor:* Cloudflare",
               tunnel.getId(), tunnel.getUrl(), app.getShortIoUrl(),
               tunnel.getExposedPort(), tunnel.getExpiresAt().format(DATE_FORMATTER)
         ));
         message.setParseMode(MARKDOWN);
         sender.execute(message);
      } catch (Exception e) {
         SendMessage message = new SendMessage();
         message.setChatId(chatId);
         message.setText("❌ Error al crear el túnel");
         sender.execute(message);
      }
   }

   private String formatDuration(int minutes) {
      if (minutes < 60) {
         return minutes + " min";
      } else if (minutes < 1440) {
         int hours = minutes / 60;
         return hours + (hours == 1 ? " hora" : " horas");
      } else {
         int days = minutes / 1440;
         return days + (days == 1 ? " día" : " días");
      }
   }

   public void replyToBackupMenu(long chatId) {
      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText("💾 *Backup*\n\nSelecciona una opción:");
      message.setReplyMarkup(buildBackupMenuMarkup());
      message.setParseMode(MARKDOWN);

      sender.execute(message);
   }

   private void replyToBackupMenu(long chatId, int messageId) {
      EditMessageText editMessage = new EditMessageText();
      editMessage.setChatId(chatId);
      editMessage.setMessageId(messageId);
      editMessage.setText("💾 *Backup*\n\nSelecciona una opción:");
      editMessage.setReplyMarkup(buildBackupMenuMarkup());
      editMessage.setParseMode(MARKDOWN);

      sender.execute(editMessage);
   }

   private InlineKeyboardMarkup buildBackupMenuMarkup() {
      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      InlineKeyboardButton serviceButton = new InlineKeyboardButton();
      serviceButton.setText("🔧 Service Backup");
      serviceButton.setCallbackData("backup_service");

      InlineKeyboardButton coldButton = new InlineKeyboardButton();
      coldButton.setText("💿 Cold Backup");
      coldButton.setCallbackData("backup_cold");

      keyboard.add(List.of(serviceButton));
      keyboard.add(List.of(coldButton));
      markup.setKeyboard(keyboard);

      return markup;
   }

   private void replyToServiceBackupSelection(long chatId, int messageId) {
      EditMessageText editMessage = new EditMessageText();
      editMessage.setChatId(chatId);
      editMessage.setMessageId(messageId);
      editMessage.setText("🔧 *Service Backup*\n\nSelecciona el servicio a respaldar:");

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      InlineKeyboardButton immichButton = new InlineKeyboardButton();
      immichButton.setText("📸 Immich");
      immichButton.setCallbackData("backup_service_immich");

      InlineKeyboardButton nextcloudButton = new InlineKeyboardButton();
      nextcloudButton.setText("☁️ Nextcloud");
      nextcloudButton.setCallbackData("backup_service_nextcloud");

      InlineKeyboardButton dockerButton = new InlineKeyboardButton();
      dockerButton.setText("🐳 Docker");
      dockerButton.setCallbackData("backup_service_docker");

      InlineKeyboardButton backButton = new InlineKeyboardButton();
      backButton.setText("🔙 Volver");
      backButton.setCallbackData("backup_menu");

      keyboard.add(List.of(immichButton));
      keyboard.add(List.of(nextcloudButton));
      keyboard.add(List.of(dockerButton));
      keyboard.add(List.of(backButton));
      markup.setKeyboard(keyboard);
      editMessage.setReplyMarkup(markup);
      editMessage.setParseMode(MARKDOWN);

      sender.execute(editMessage);
   }

   private void replyToServiceBackupConfirmation(long chatId, String serviceName, String callbackData) {
      SendMessage message = new SendMessage();
      message.setChatId(chatId);
      message.setText("¿Estás seguro que querés ejecutar el backup de *" + serviceName + "*?");

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      InlineKeyboardButton confirmButton = new InlineKeyboardButton();
      confirmButton.setText("✅");
      confirmButton.setCallbackData(callbackData);

      InlineKeyboardButton cancelButton = new InlineKeyboardButton();
      cancelButton.setText("❌");
      cancelButton.setCallbackData("backup_service");

      keyboard.add(List.of(confirmButton, cancelButton));
      markup.setKeyboard(keyboard);
      message.setReplyMarkup(markup);
      message.setParseMode(MARKDOWN);

      sender.execute(message);
   }

   private void handleServiceBackup(long chatId, String serviceName, boolean success) {
      SendMessage message = new SendMessage();
      message.setChatId(chatId);

      if (success) {
         message.setText("✅ Backup de *" + serviceName + "* completado exitosamente");
         message.setParseMode(MARKDOWN);
      } else {
         message.setText("❌ Error al ejecutar el backup de " + serviceName);
      }

      sender.execute(message);
   }

   private void replyToColdBackupConfirmation(long chatId, int messageId) {
      EditMessageText editMessage = new EditMessageText();
      editMessage.setChatId(chatId);
      editMessage.setMessageId(messageId);
      editMessage.setText("💿 *Cold Backup*\n\n¿Desea comenzar la ejecución del backup externo?\n\n⚠️ _Asegúrese de que el disco esté conectado y montado antes de continuar_");

      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

      InlineKeyboardButton confirmButton = new InlineKeyboardButton();
      confirmButton.setText("✅");
      confirmButton.setCallbackData("backup_cold_confirm");

      InlineKeyboardButton cancelButton = new InlineKeyboardButton();
      cancelButton.setText("❌");
      cancelButton.setCallbackData("backup_menu");

      keyboard.add(List.of(confirmButton, cancelButton));
      markup.setKeyboard(keyboard);
      editMessage.setReplyMarkup(markup);
      editMessage.setParseMode(MARKDOWN);

      sender.execute(editMessage);
   }

   private void handleColdBackup(long chatId) {
      SendMessage waitMessage = new SendMessage();
      waitMessage.setChatId(chatId);
      waitMessage.setText("⏳ Ejecutando cold backup...");
      sender.execute(waitMessage);

      boolean success = commandExecutorService.executeColdBackup();

      SendMessage message = new SendMessage();
      message.setChatId(chatId);

      if (success) {
         message.setText("✅ Cold backup completado exitosamente");
      } else {
         message.setText("❌ Error al ejecutar el cold backup");
      }

      sender.execute(message);
   }
}
