package com.skillswap.skillswap.repository;

import com.skillswap.skillswap.entity.Connection;
import com.skillswap.skillswap.entity.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    List<Connection> findBySenderIdOrReceiverId(
            Long senderId,
            Long receiverId
    );

    List<Connection> findByReceiverIdAndStatus(
            Long receiverId,
            ConnectionStatus status
    );

    Optional<Connection> findBySenderIdAndReceiverId(
            Long senderId,
            Long receiverId
    );

    boolean existsBySenderIdAndReceiverId(
            Long senderId,
            Long receiverId
    );
}