package com.realestate.backend.service.notification;

import com.realestate.backend.entity.InquiryEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void notifyAgencyOfNewInquiry(InquiryEntity inquiry) {

        log.info("New inquiry {} received for property {} — notifying agency {}",
                inquiry.getId(), inquiry.getProperty().getId(), inquiry.getAgency().getId());

    }
}
