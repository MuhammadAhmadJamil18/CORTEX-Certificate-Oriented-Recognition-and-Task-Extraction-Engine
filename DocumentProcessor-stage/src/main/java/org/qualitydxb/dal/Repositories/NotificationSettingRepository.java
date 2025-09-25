package org.qualitydxb.dal.Repositories;

import org.qualitydxb.dal.Models.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSettingRepository
        extends JpaRepository<NotificationSetting, Integer> {

    NotificationSetting findFirstByClientId(Integer clientId);
}
