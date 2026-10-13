package com.skillswap.skillswap.service;

import com.skillswap.skillswap.entity.Connection;
import com.skillswap.skillswap.entity.ConnectionStatus;
import com.skillswap.skillswap.entity.User;
import com.skillswap.skillswap.repository.ConnectionRepository;
import com.skillswap.skillswap.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;

    public ConnectionService(
            ConnectionRepository connectionRepository,
            UserRepository userRepository
    ) {
        this.connectionRepository = connectionRepository;
        this.userRepository = userRepository;
    }

    public Connection sendConnectionRequest(
            Long senderId,
            Long receiverId
    ) {

        if (senderId == null || receiverId == null) {
            throw new RuntimeException("Sender and receiver ids are required");
        }

        if (Objects.equals(senderId, receiverId)) {
            throw new RuntimeException(
                    "You cannot send a connection request to yourself"
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

        boolean alreadyExists =
                connectionRepository
                        .existsBySenderIdAndReceiverId(
                                senderId,
                                receiverId
                        )
                        ||
                        connectionRepository
                                .existsBySenderIdAndReceiverId(
                                        receiverId,
                                        senderId
                                );

        if (alreadyExists) {
            throw new RuntimeException(
                    "A connection request already exists between these users"
            );
        }

        Connection connection = new Connection();

        connection.setSender(sender);
        connection.setReceiver(receiver);
        connection.setStatus(ConnectionStatus.PENDING);

        return connectionRepository.save(connection);
    }

    public List<Connection> getUserConnections(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException(
                    "User not found with id: " + userId
            );
        }

        return connectionRepository
                .findBySenderIdOrReceiverId(
                        userId,
                        userId
                );
    }

    public List<Connection> getPendingRequests(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException(
                    "User not found with id: " + userId
            );
        }

        return connectionRepository
                .findByReceiverIdAndStatus(
                        userId,
                        ConnectionStatus.PENDING
                );
    }

    public Connection acceptConnection(Long connectionId) {

        Connection connection =
                getConnectionById(connectionId);

        if (connection.getStatus()
                != ConnectionStatus.PENDING) {

            throw new RuntimeException(
                    "Only pending connection requests can be accepted"
            );
        }

        connection.setStatus(
                ConnectionStatus.ACCEPTED
        );

        return connectionRepository.save(connection);
    }

    public Connection rejectConnection(Long connectionId) {

        Connection connection =
                getConnectionById(connectionId);

        if (connection.getStatus()
                != ConnectionStatus.PENDING) {

            throw new RuntimeException(
                    "Only pending connection requests can be rejected"
            );
        }

        connection.setStatus(
                ConnectionStatus.REJECTED
        );

        return connectionRepository.save(connection);
    }

    public Connection getConnectionById(Long id) {

        return connectionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Connection not found with id: " + id
                        )
                );
    }

    public boolean areUsersConnected(
            Long user1Id,
            Long user2Id
    ) {

        List<Connection> connections =
                connectionRepository
                        .findBySenderIdOrReceiverId(
                                user1Id,
                                user1Id
                        );

        return connections.stream().anyMatch(connection -> {

            boolean usersMatch =
                    (connection.getSender().getId().equals(user1Id)
                            && connection.getReceiver().getId().equals(user2Id))
                    ||
                    (connection.getSender().getId().equals(user2Id)
                            && connection.getReceiver().getId().equals(user1Id));

            return usersMatch
                    && connection.getStatus()
                    == ConnectionStatus.ACCEPTED;
        });
    }
}