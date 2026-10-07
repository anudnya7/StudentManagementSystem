package org.example.studentmanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.studentmanagementsystem.dto.NotificationResponse;
import org.example.studentmanagementsystem.entity.Notification;
import org.example.studentmanagementsystem.repository.NotificationRepository;
import org.example.studentmanagementsystem.service.NotificationService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public List<NotificationResponse> getAll() {
        return notificationRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<NotificationResponse> getByEmail(String email) {
        return notificationRepository.findByRecipientEmailIgnoreCaseOrderByIdDesc(email)
                .stream().map(this::toResponse).toList();
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(n.getId(), n.getRecipientEmail(), n.getMessage(), n.getCreatedAt());
    }
}