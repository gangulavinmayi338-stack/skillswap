package com.skillswap.skillswap.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempts", uniqueConstraints = @UniqueConstraint(columnNames = {"session_id","user_id"}))
public class QuizAttempt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name="session_id", nullable=false) private Session session;
    @ManyToOne(optional = false) @JoinColumn(name="user_id", nullable=false) private User user;
    @Column(nullable=false) private Integer score;
    @Column(nullable=false) private Integer total;
    @Column(nullable=false) private LocalDateTime submittedAt;
    public QuizAttempt() {}
    @PrePersist public void prePersist(){ if(submittedAt==null) submittedAt=LocalDateTime.now(); }
    public Long getId(){ return id; } public void setId(Long id){this.id=id;}
    public Session getSession(){return session;} public void setSession(Session session){this.session=session;}
    public User getUser(){return user;} public void setUser(User user){this.user=user;}
    public Integer getScore(){return score;} public void setScore(Integer score){this.score=score;}
    public Integer getTotal(){return total;} public void setTotal(Integer total){this.total=total;}
    public LocalDateTime getSubmittedAt(){return submittedAt;} public void setSubmittedAt(LocalDateTime submittedAt){this.submittedAt=submittedAt;}
}
