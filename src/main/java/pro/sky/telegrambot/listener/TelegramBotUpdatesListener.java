package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.exception.WrongDataException;
import pro.sky.telegrambot.model.Notification;
import pro.sky.telegrambot.repository.NotificationRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    private static final Pattern PATTERN = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationRepository notificationRepository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            String text = update.message().text();
            Long id = update.message().chat().id();
            Matcher matcher = PATTERN.matcher(text);

            if ("/start".equalsIgnoreCase(text)) {
                telegramBot.execute(new SendMessage(id, "Привет, я напоминалка-бот"));
            } else if (matcher.matches()) {
                try {
                    String dateTimeString = matcher.group(1);

                    String reminderText = matcher.group(3);
                    LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                    Notification notification = new Notification();
                    notification.setChat_id(id);
                    notification.setMessageDate(dateTime);
                    notification.setText(reminderText);
                    notificationRepository.save(notification);
                    telegramBot.execute(new SendMessage(id, "сохранено"));
                } catch (WrongDataException e) {
                    telegramBot.execute(new SendMessage(id, "дата введена неверно"));
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(fixedDelay = 60_000L)
    public void schedule() {
        List<Notification> notifications = notificationRepository.findAllByMessageDate(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        notifications.forEach(t -> {
            SendResponse response = telegramBot.execute(new SendMessage(t.getChat_id(), t.getText()));
            if (response.isOk()) {
                notificationRepository.delete(t);
            }
        });
    }
}
