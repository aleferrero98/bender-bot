package com.telegram.bender.service;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.util.AbilityUtils;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import com.telegram.bender.model.EBotCommand;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("BenderBot")
public class BenderBot extends AbilityBot {

   private final Long creatorId;

   private final ResponseHandler responseHandler;

   public BenderBot(@Value("${telegram.bot.token}") String botToken,
         @Value("${telegram.bot.username}") String botUsername, @Value("${telegram.bot.creator}") Long creatorId) {
      super(botToken, botUsername);
      this.creatorId = creatorId;
      this.responseHandler = new ResponseHandler(silent, db);
      this.registerBotCommands();
   }

   @Override
   public long creatorId() {
      return this.creatorId;
   }

   private void registerBotCommands() {
      try {
         SetMyCommands setMyCommands = new SetMyCommands();
         List<BotCommand> commands = Arrays.stream(EBotCommand.values())
                                           .map(command ->  new BotCommand(command.getName(), command.getDescription()))
                                           .collect(Collectors.toList());
         setMyCommands.setCommands(commands);
         sender.execute(setMyCommands);
         log.info("Bot commands registered successfully");

      } catch (Exception ex) {
         log.error("Error registering bot commands: {}", ex.getMessage());
      }
   }

   // Command /start
   public Ability start() {
      return Ability.builder()
                    .name(EBotCommand.START.getName())
                    .info(EBotCommand.START.getDescription())
                    .locality(Locality.USER)
                    .privacy(Privacy.CREATOR)
                    .action(ctx -> {
                       responseHandler.replyToStart(ctx.chatId());
                    })
                    .build();
   }

   // Command /info
   public Ability info() {
      return Ability.builder()
                    .name(EBotCommand.INFO.getName())
                    .info(EBotCommand.INFO.getDescription())
                    .locality(Locality.USER)
                    .privacy(Privacy.CREATOR)
                    .action(ctx -> {
                       responseHandler.replyToInfoMenu(ctx.chatId());
                    })
                     .build();
   }

   // Command /manage
   public Ability manage() {
      return Ability.builder()
                    .name(EBotCommand.MANAGE.getName())
                    .info(EBotCommand.MANAGE.getDescription())
                    .locality(Locality.USER)
                    .privacy(Privacy.CREATOR)
                    .action(ctx -> {
                       responseHandler.replyToManageMenu(ctx.chatId());
                    })
                    .build();
   }

   // Command /cooler
   public Ability cooler() {
      return Ability.builder()
                    .name(EBotCommand.COOLER.getName())
                    .info(EBotCommand.COOLER.getDescription())
                    .locality(Locality.USER)
                    .privacy(Privacy.CREATOR)
                    .action(ctx -> {
                       responseHandler.replyToCoolerMenu(ctx.chatId());
                    })
                    .build();
   }

   // Command /help
   public Ability help() {
      return Ability.builder()
                    .name(EBotCommand.HELP.getName())
                    .info(EBotCommand.HELP.getDescription())
                    .locality(Locality.USER)
                    .privacy(Privacy.CREATOR)
                    .action(ctx -> {
                       responseHandler.replyToHelp(ctx.chatId());
                    })
                    .build();
   }

   // This method allows filtering by buttons (not commands or text)
   private Predicate<Update> isCallbackQuery() {
      return upd -> upd.hasCallbackQuery();
   }

   private Predicate<Update> isNotCommand() {
      return upd -> {
         if (!upd.hasMessage() || !upd.getMessage().hasText()) {
            return false;
         }
         String text = upd.getMessage().getText();
         String commandName = text.substring(1); // removes the slash

         return !EBotCommand.isValidCommand(commandName);
      };
   }

   public Reply handleUnknownMessage() {
      return Reply.of((bot, upd) -> {
         long chatId = upd.getMessage().getChatId();
         responseHandler.handleUnknownMessage(chatId);
      }, Flag.TEXT, isNotCommand());
   }

   public Reply handleCallbackQuery() {
      return Reply.of((bot, upd) -> {
         long chatId = upd.getCallbackQuery().getMessage().getChatId();
         int messageId = upd.getCallbackQuery().getMessage().getMessageId();
         String callbackData = upd.getCallbackQuery().getData();
         String callbackQueryId = upd.getCallbackQuery().getId();

         responseHandler.handleCallbackQuery(chatId, messageId, callbackData, callbackQueryId);
      }, Flag.CALLBACK_QUERY);
   }

   @Override
   public List<Reply> replies() {
      return List.of(
            handleUnknownMessage(),
            handleCallbackQuery()
      );
   }

   @Override
   public void onUpdateReceived(Update update) {
      User user = AbilityUtils.getUser(update);

      if (!creatorId.equals(user.getId())) {
         Long chatId = AbilityUtils.getChatId(update);
         responseHandler.handleUnauthorizedAccess(chatId);
         return;
      }

      super.onUpdateReceived(update);
   }

}
