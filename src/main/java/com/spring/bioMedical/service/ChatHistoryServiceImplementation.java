/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.ChatMessage;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.repository.ChatMessageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatHistoryServiceImplementation implements ChatHistoryService {

    private final ChatMessageRepository chatMessageRepository;

    @Override
    public void saveUserMessage(Users user, String message) {
        ChatMessage msg = new ChatMessage();
        msg.setUser(user);
        msg.setSender("USER");
        msg.setMessage(message);
        chatMessageRepository.save(msg);
    }

    @Override
    public void saveBotMessage(Users user, String message) {
        ChatMessage msg = new ChatMessage();
        msg.setUser(user);
        msg.setSender("BOT");
        msg.setMessage(message);
        chatMessageRepository.save(msg);
    }

    @Override
    public List<ChatMessage> getHistory(Users user) {
        return chatMessageRepository
                .findTop50ByUser_UserIdOrderByCreatedAtAsc(user.getUserId());
    }
}
