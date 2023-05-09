package com.dhl.fin.api.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author becui
 * @date 9/25/2020
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SocketServiceImpl {

    @Autowired
    private SimpMessagingTemplate template;

    public void sendMessage(String destination, Object message) {
        template.convertAndSend(destination, message);
    }

}

