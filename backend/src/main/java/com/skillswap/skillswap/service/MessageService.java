package com.skillswap.skillswap.service;

import com.skillswap.skillswap.entity.Message;
import com.skillswap.skillswap.entity.User;
import com.skillswap.skillswap.repository.MessageRepository;
import com.skillswap.skillswap.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ConnectionService connectionService;

    public MessageService(
            MessageRepository messageRepository,
            UserRepository userRepository,
            ConnectionService connectionService
    ) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.connectionService = connectionService;
    }

    public Message sendMessage(
            Long senderId,
            Long receiverId,
            String messageText
    ) {

        if (senderId == null || receiverId == null) {
            throw new RuntimeException("Sender and receiver ids are required");
        }

        if (Objects.equals(senderId, receiverId)) {
            throw new RuntimeException(
                    "You cannot send a message to yourself"
            );
        }

        if (messageText == null || messageText.trim().isEmpty()) {
            throw new RuntimeException(
                    "Message cannot be empty"
            );
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Sender not found with id: " + senderId
                        )
                );

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Receiver not found with id: " + receiverId
                        )
                );

        if (!connectionService.areUsersConnected(
                senderId,
                receiverId
        )) {
            throw new RuntimeException(
                    "Users must be connected before messaging"
            );
        }

        Message message = new Message();

        message.setSender(sender);
        message.setReceiver(receiver);
        message.setMessage(messageText.trim());

        return messageRepository.save(message);
    }

    public List<Message> getConversation(
            Long user1Id,
            Long user2Id
    ) {

        if (!userRepository.existsById(user1Id)) {
            throw new RuntimeException(
                    "User not found with id: " + user1Id
            );
        }

        if (!userRepository.existsById(user2Id)) {
            throw new RuntimeException(
                    "User not found with id: " + user2Id
            );
        }

        return messageRepository
                .findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderBySentAtAsc(
                        user1Id,
                        user2Id,
                        user2Id,
                        user1Id
                );
    }
}