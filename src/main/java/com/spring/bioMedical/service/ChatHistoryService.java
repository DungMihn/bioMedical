/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.ChatMessage;
import com.spring.bioMedical.entity.Users;

import java.util.List;

public interface ChatHistoryService {

    void saveUserMessage(Users user, String message);

    void saveBotMessage(Users user, String message);

    List<ChatMessage> getHistory(Users user);
}

