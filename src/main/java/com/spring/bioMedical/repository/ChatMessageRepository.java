/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // lấy tối đa 50 tin nhắn gần nhất theo user
    List<ChatMessage> findTop50ByUser_UserIdOrderByCreatedAtAsc(Long userId);
}
