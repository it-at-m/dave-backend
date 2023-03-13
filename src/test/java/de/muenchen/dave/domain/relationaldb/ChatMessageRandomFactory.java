package de.muenchen.dave.domain.relationaldb;

import com.github.javafaker.Faker;
import de.muenchen.dave.domain.ChatMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatMessageRandomFactory {

    public static ChatMessage getOne() {
        ChatMessage c = new ChatMessage();

        c.setId(UUID.randomUUID());
        c.setZaehlungId(UUID.randomUUID());
        c.setContent("test");
        c.setParticipantId(1);
        c.setType("text");
        c.setTimestamp(LocalDateTime.of(2021, 01, 01, 10, 30, 0, 0));
        c.setUploaded(true);
        c.setViewed(false);

        return c;
    }

    public static List<ChatMessage> getSome() {
        List<ChatMessage> chatMessageList = new ArrayList<>();

        int x = Faker.instance().number().numberBetween(1, 8);
        for (int i = 0; i < x; i++) {
            chatMessageList.add(getOne());
        }
        return chatMessageList;
    }
}
