package com.skillswap.skillswap.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_topics")
public class SessionTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * The session this topic belongs to.
     *
     * Example:
     * Java Session #5
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "session_id",
            nullable = false
    )
    @JsonIgnore
    private Session session;

    /*
     * Example:
     * Variables
     * Loops
     * Arrays
     */
    @Column(
            nullable = false,
            length = 255
    )
    private String title;

    /*
     * false = not completed
     * true  = completed
     */
    @Column(nullable = false)
    private Boolean completed = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public SessionTopic() {
    }

    public SessionTopic(
            Session session,
            String title
    ) {
        this.session = session;
        this.title = title;
        this.completed = false;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {

        if (completed == null) {
            completed = false;
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(
            Session session
    ) {
        this.session = session;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(
            String title
    ) {
        this.title = title;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(
            Boolean completed
    ) {
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }
}