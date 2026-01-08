package org.tech.techlogist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tech.techlogist.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
}
