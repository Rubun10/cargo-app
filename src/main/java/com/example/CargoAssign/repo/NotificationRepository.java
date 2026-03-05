package com.example.CargoAssign.repo;

import com.example.CargoAssign.Model.Notification;
import com.example.CargoAssign.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiverOrderByCreatedAtDesc(User receiver);

    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.sender WHERE n.receiver = :receiver ORDER BY n.createdAt DESC")
    List<Notification> findByReceiverWithSenderOrderByCreatedAtDesc(@Param("receiver") User receiver);

	Optional<Notification> findByIdAndReceiver(Long notificationId, User user);
	
	void deleteByReceiverAndTypeAndEntityTypeAndEntityId(
            User receiver,
            String type,
            String entityType,
            Long entityId
    );
	
	void deleteByReceiverAndTypeAndEntityId(
            User receiver,
            String type,
            Long entityId
    );
}
