package org.example.studentmanagementsystem.repository;

import org.example.studentmanagementsystem.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByRecipientEmailIgnoreCaseOrderByIdDesc(String recipientEmail);
}