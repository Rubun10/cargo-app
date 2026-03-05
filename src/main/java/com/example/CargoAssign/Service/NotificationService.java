package com.example.CargoAssign.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import com.example.CargoAssign.dto.NotificationDTO;
import com.example.CargoAssign.Model.Notification;
import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.repo.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void createNotification(User receiver,
                                   User sender,
                                   String entityType,
                                   Long entityId,
                                   String type,
                                   String message) {

        if (receiver == null || receiver.getId() == null) {
            throw new IllegalArgumentException("Invalid receiver");
        }
        if (sender == null || sender.getId() == null) {
            throw new IllegalArgumentException("Invalid sender");
        }

        Notification notification = new Notification();
        notification.setReceiver(receiver);
        notification.setSender(sender);
        notification.setEntityType(entityType);
        notification.setEntityId(entityId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
    }


    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(User user) {

        return notificationRepository
                .findByReceiverWithSenderOrderByCreatedAtDesc(user)
                .stream()
                .map(n -> new NotificationDTO(
                        n.getId(),
                        n.getType(),
                        n.getMessage(),
                        n.isRead(),
                        n.getCreatedAt(),
                        n.getSender() != null ? n.getSender().getId() : null,
                        n.getSender() != null ? n.getSender().getName() : null,
                        n.getEntityType(),
                        n.getEntityId()
                ))
                .collect(Collectors.toList());
    }


    public void markAsRead(Long notificationId, User user) {

        Notification notification =
                notificationRepository
                        .findByIdAndReceiver(notificationId, user)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Unauthorized"));

        notification.setRead(true);
        notificationRepository.save(notification);
    }
    
    @Transactional
    public void deleteDriverRequestNotification(User receiver, Long loadEntityId) {
        if (receiver == null || receiver.getId() == null || loadEntityId == null) {
            return;
        }
        notificationRepository.deleteByReceiverAndTypeAndEntityTypeAndEntityId(
                receiver,
                "DRIVER_REQUEST",
                "LOAD",
                loadEntityId
        );
        // Fallback for legacy rows where entityType may be null/mismatched.
        notificationRepository.deleteByReceiverAndTypeAndEntityId(
                receiver,
                "DRIVER_REQUEST",
                loadEntityId
        );
    }
}
