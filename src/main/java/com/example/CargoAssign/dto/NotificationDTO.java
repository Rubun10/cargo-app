package com.example.CargoAssign.dto;


import java.time.LocalDateTime;

public class NotificationDTO {

    private Long id;
    private String type;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    private Long senderId;
    private String senderName;
    private String entityType;
    private Long entityId;

    public NotificationDTO(Long id,
                           String type,
                           String message,
                           boolean read,
                           LocalDateTime createdAt,
                           Long senderId,
                           String senderName,
                           String entityType,
                           Long entityId) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
        this.senderId = senderId;
        this.senderName = senderName;
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getEntityType() { return entityType; }
    public Long getEntityId() { return entityId; }
}
