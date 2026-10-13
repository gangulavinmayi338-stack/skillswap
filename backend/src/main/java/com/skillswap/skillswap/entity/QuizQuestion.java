package com.skillswap.skillswap.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "quiz_questions")
public class QuizQuestion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    @JsonIgnore
    private Session session;

    @Column(nullable = false, length = 1000)
    private String question;
    @Column(nullable = false) private String optionA;
    @Column(nullable = false) private String optionB;
    @Column(nullable = false) private String optionC;
    @Column(nullable = false) private String optionD;
    @Column(nullable = false, length = 1) private String correctOption;

    public QuizQuestion() {}
    public Long getId(){ return id; }
    public void setId(Long id){ this.id=id; }
    public Session getSession(){ return session; }
    public void setSession(Session session){ this.session=session; }
    public String getQuestion(){ return question; }
    public void setQuestion(String question){ this.question=question; }
    public String getOptionA(){ return optionA; }
    public void setOptionA(String optionA){ this.optionA=optionA; }
    public String getOptionB(){ return optionB; }
    public void setOptionB(String optionB){ this.optionB=optionB; }
    public String getOptionC(){ return optionC; }
    public void setOptionC(String optionC){ this.optionC=optionC; }
    public String getOptionD(){ return optionD; }
    public void setOptionD(String optionD){ this.optionD=optionD; }
    public String getCorrectOption(){ return correctOption; }
    public void setCorrectOption(String correctOption){ this.correctOption=correctOption; }
}
