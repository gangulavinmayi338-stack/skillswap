package com.skillswap.skillswap.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_resources")
public class SessionResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * The session this resource belongs to.
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
     * "Java Documentation"
     */
    @Column(
            nullable = false,
            length = 255
    )
    private String title;

    /*
     * Example:
     * https://docs.oracle.com/javase/
     */
    @Column(
            nullable = false,
            length = 2000
    )
    private String url;

    /*
     * Optional resource type.
     *
     * Examples:
     * LINK
     * VIDEO
     * DOCUMENT
     * PRACTICE
     */
    @Column(
            length = 50
    )
    private String resourceType;

    /*
     * User who added the resource.
     */
    @ManyToOne
    @JoinColumn(
            name = "created_by",
            nullable = false
    )
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public SessionResource() {
    }

    public SessionResource(
            Session session,
            String title,
            String url,
            String resourceType,
            User createdBy
    ) {
        this.session = session;
        this.title = title;
        this.url = url;
        this.resourceType = resourceType;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(
            Long id
    ) {
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

    public String getUrl() {
        return url;
    }

    public void setUrl(
            String url
    ) {
        this.url = url;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(
            String resourceType
    ) {
        this.resourceType = resourceType;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(
            User createdBy
    ) {
        this.createdBy = createdBy;
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