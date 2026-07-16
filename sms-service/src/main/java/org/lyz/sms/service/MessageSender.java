package org.lyz.sms.service;

import org.lyz.sms.dto.SmsSendRequest;

public interface MessageSender {
    void send(SmsSendRequest request);
}
