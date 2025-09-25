package org.qualitydxb.dal.Repositories;

import org.qualitydxb.dal.Models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    // Custom query methods can be defined here if needed

    List<Notification> findByScheduleTimeBetweenAndStatus(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer status
    );

    List<Notification> findByClientIdAndUserIdAndNotifyAllAndStatus(
            Integer clientId,
            Integer userId,
            boolean notifyAll,
            Integer status
    );

    List<Notification> findByClientIdAndUserId(
            Integer clientId,
            Integer userId
    );

    List<Notification> findByClientId(
        Integer clientId
    );

    List<Notification> findByClientIdAndNotifyAllAndStatus(
            Integer clientId,
            boolean notifyAll,
            Integer status
    );

    @Query(value = """
        SELECT *
          FROM qualitydxb.notification
         WHERE clientid = :clientId
           AND status   = :status
           AND (notifyall = true OR recipientid = :userId)
         ORDER BY createdat DESC
        """, nativeQuery = true)
    List<Notification> findForSalesman(Integer clientId,
                                       Integer userId,
                                       Integer status);
}
