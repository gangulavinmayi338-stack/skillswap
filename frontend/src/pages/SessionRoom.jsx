import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useParams } from "react-router-dom";
import Navbar from "../components/Navbar";
import api, { webSocketBaseUrl } from "../services/api";
import "./SessionRoom.css";

function SessionRoom() {
  const { sessionId } = useParams();
  const userId = Number(localStorage.getItem("userId"));

  const [room, setRoom] = useState(null);
  const [quiz, setQuiz] = useState({ questions: [], available: false, attempt: null });
  const [notice, setNotice] = useState("");
  const [socketStatus, setSocketStatus] = useState("Connecting...");
  const [onlineUsers, setOnlineUsers] = useState(new Set());
  const [remoteDraft, setRemoteDraft] = useState("");
  const [remoteTyping, setRemoteTyping] = useState(false);
  const [handRaisedBy, setHandRaisedBy] = useState(null);
  const [elapsed, setElapsed] = useState(0);

  const [topic, setTopic] = useState("");
  const [note, setNote] = useState("");
  const [task, setTask] = useState({ title: "", description: "" });
  const [resource, setResource] = useState({ title: "", url: "", resourceType: "LINK" });
  const [chatText, setChatText] = useState("");
  const [question, setQuestion] = useState({
    question: "",
    optionA: "",
    optionB: "",
    optionC: "",
    optionD: "",
    correctOption: "A",
  });
  const [answers, setAnswers] = useState({});

  const socketRef = useRef(null);
  const typingTimerRef = useRef(null);
  const chatEndRef = useRef(null);

  const load = async (quiet = false) => {
    try {
      const [roomResponse, quizResponse] = await Promise.all([
        api.get(`/session-room/${sessionId}`),
        api.get(`/quizzes/session/${sessionId}?userId=${userId}`),
      ]);
      setRoom(roomResponse.data);
      setQuiz(quizResponse.data);
      if (!quiet) setNotice("");
    } catch (error) {
      setNotice(error.response?.data?.message || "Unable to load the interactive session room.");
    }
  };

  useEffect(() => {
    load();
  }, [sessionId]);

  useEffect(() => {
    const socket = new WebSocket(`${webSocketBaseUrl}/ws/classroom?sessionId=${sessionId}&userId=${userId}`);
    socketRef.current = socket;

    socket.onopen = () => {
      setSocketStatus("Live connected");
      socket.send(JSON.stringify({ type: "presence-request" }));
    };

    socket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        if (data.type === "presence") {
          setOnlineUsers((previous) => {
            const next = new Set(previous);
            if (data.online) next.add(Number(data.userId));
            else next.delete(Number(data.userId));
            return next;
          });
        }

        if (["room-refresh", "class-chat-refresh", "class-started", "class-ended", "quiz-refresh"].includes(data.type)) {
          load(true);
        }

        if (data.type === "note-draft" && Number(data.userId) !== userId) {
          setRemoteDraft(data.content || "");
          setRemoteTyping(true);
          window.clearTimeout(typingTimerRef.current);
          typingTimerRef.current = window.setTimeout(() => setRemoteTyping(false), 1600);
        }

        if (data.type === "note-stop" && Number(data.userId) !== userId) {
          setRemoteTyping(false);
          setRemoteDraft("");
        }

        if (data.type === "raise-hand" && Number(data.userId) !== userId) {
          setHandRaisedBy(Number(data.userId));
        }

        if (data.type === "lower-hand" && Number(data.userId) !== userId) {
          setHandRaisedBy(null);
        }
      } catch (error) {
        console.error("Classroom socket message error", error);
      }
    };

    socket.onerror = () => setSocketStatus("Live connection error");
    socket.onclose = () => setSocketStatus("Live disconnected");

    return () => {
      window.clearTimeout(typingTimerRef.current);
      if (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING) socket.close();
    };
  }, [sessionId, userId]);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [room?.classMessages?.length]);

  useEffect(() => {
    const startedAt = room?.live?.startedAt;
    if (!startedAt) {
      setElapsed(0);
      return;
    }

    const calculate = () => {
      const start = new Date(startedAt).getTime();
      const end = room?.live?.endedAt ? new Date(room.live.endedAt).getTime() : Date.now();
      setElapsed(Math.max(0, Math.floor((end - start) / 1000)));
    };

    calculate();
    if (!room?.live?.active) return;
    const timer = window.setInterval(calculate, 1000);
    return () => window.clearInterval(timer);
  }, [room?.live?.startedAt, room?.live?.endedAt, room?.live?.active]);

  const session = room?.session;
  const isTeacher = session?.teacher?.id === userId;
  const isLearner = session?.learner?.id === userId;
  const canEdit = session?.status === "CONFIRMED" || session?.status === "COMPLETED";
  const liveActive = Boolean(room?.live?.active);

  const partner = useMemo(() => {
    if (!session) return null;
    return isTeacher ? session.learner : session.teacher;
  }, [session, isTeacher]);

  const sendSocket = (payload) => {
    if (socketRef.current?.readyState === WebSocket.OPEN) {
      socketRef.current.send(JSON.stringify(payload));
    }
  };

  const formatTime = (seconds) => {
    const h = String(Math.floor(seconds / 3600)).padStart(2, "0");
    const m = String(Math.floor((seconds % 3600) / 60)).padStart(2, "0");
    const s = String(seconds % 60).padStart(2, "0");
    return `${h}:${m}:${s}`;
  };

  const startClass = async () => {
    try {
      await api.post(`/session-room/${sessionId}/live/start`, { userId });
      setNotice("Live class started. Your learner can now follow updates in real time.");
      await load(true);
    } catch (error) {
      setNotice(error.response?.data?.message || "Unable to start class.");
    }
  };

  const endClass = async () => {
    if (!window.confirm("End this live class and mark the session completed?")) return;
    try {
      await api.post(`/session-room/${sessionId}/live/end`, { userId });
      setNotice("Class completed. The post-session quiz is now available.");
      await load(true);
    } catch (error) {
      setNotice(error.response?.data?.message || "Unable to end class.");
    }
  };

  const addTopic = async (event) => {
    event.preventDefault();
    if (!topic.trim()) return;
    await api.post(`/session-room/${sessionId}/topics`, { userId, title: topic });
    setTopic("");
    load(true);
  };

  const addNote = async (event) => {
    event.preventDefault();
    if (!note.trim()) return;
    await api.post(`/session-room/${sessionId}/notes`, { userId, content: note });
    setNote("");
    sendSocket({ type: "note-stop", userId });
    load(true);
  };

  const changeNote = (value) => {
    setNote(value);
    sendSocket({ type: "note-draft", userId, content: value });
  };

  const addTask = async (event) => {
    event.preventDefault();
    if (!task.title.trim()) return;
    await api.post(`/session-room/${sessionId}/tasks`, { userId, ...task });
    setTask({ title: "", description: "" });
    load(true);
  };

  const addResource = async (event) => {
    event.preventDefault();
    await api.post(`/session-room/${sessionId}/resources`, { userId, ...resource });
    setResource({ title: "", url: "", resourceType: "LINK" });
    load(true);
  };

  const toggle = async (type, id) => {
    await api.put(`/session-room/${type}/${id}/toggle`, { userId });
    load(true);
  };

  const remove = async (type, id) => {
    await api.delete(`/session-room/${type}/${id}?userId=${userId}`);
    load(true);
  };

  const sendClassMessage = async (event) => {
    event.preventDefault();
    if (!chatText.trim()) return;
    await api.post(`/session-room/${sessionId}/chat`, { userId, message: chatText });
    setChatText("");
    load(true);
  };

  const raiseHand = () => {
    setHandRaisedBy(userId);
    sendSocket({ type: "raise-hand", userId });
  };

  const lowerHand = () => {
    setHandRaisedBy(null);
    sendSocket({ type: "lower-hand", userId });
  };

  const addQuestion = async (event) => {
    event.preventDefault();
    try {
      await api.post(`/quizzes/session/${sessionId}/questions`, { userId, ...question });
      setQuestion({ question: "", optionA: "", optionB: "", optionC: "", optionD: "", correctOption: "A" });
      setNotice("Quiz question added. It will unlock for the learner after the class is completed.");
      load(true);
    } catch (error) {
      setNotice(error.response?.data?.message || "Unable to add quiz question.");
    }
  };

  const submitQuiz = async () => {
    try {
      const response = await api.post(`/quizzes/session/${sessionId}/submit`, { userId, answers });
      setNotice(`Quiz submitted: ${response.data.score}/${response.data.total}`);
      load(true);
    } catch (error) {
      setNotice(error.response?.data?.message || "Unable to submit quiz.");
    }
  };

  if (!room) {
    return <div><Navbar /><div className="room-loading">Loading live interactive classroom...</div></div>;
  }

  return (
    <div className="room-page">
      <Navbar />
      <main className="room-container">
        <Link to="/sessions" className="room-back">← Back to sessions</Link>

        <section className="room-hero">
          <div className="room-hero-main">
            <div className="room-live-line">
              <span className={`live-pill ${liveActive ? "active" : ""}`}>{liveActive ? "● LIVE" : session.status}</span>
              <span className="socket-label">{socketStatus}</span>
            </div>
            <span className="room-kicker">Interactive Classroom</span>
            <h1>{session.skill?.skillName} Session</h1>
            <p>{session.teacher?.name} teaches {session.learner?.name} · {session.sessionDate} at {session.startTime} · {session.duration} min</p>
            <div className="participant-row">
              {[session.teacher, session.learner].map((person) => (
                <span className="participant-chip" key={person.id}>
                  <span className={onlineUsers.has(person.id) ? "presence-dot online" : "presence-dot"} />
                  {person.name} · {person.id === session.teacher.id ? "Teacher" : "Learner"}
                </span>
              ))}
            </div>
          </div>

          <div className="live-control-card">
            <strong className="class-clock">{formatTime(elapsed)}</strong>
            <span>{liveActive ? "Class in progress" : session.status === "COMPLETED" ? "Class completed" : "Ready for class"}</span>
            {isTeacher && session.status === "CONFIRMED" && !liveActive && <button onClick={startClass}>Start Live Class</button>}
            {isTeacher && liveActive && <button className="danger" onClick={endClass}>End Class & Unlock Quiz</button>}
            {isLearner && canEdit && (
              handRaisedBy === userId
                ? <button className="secondary" onClick={lowerHand}>Lower Hand ✋</button>
                : <button className="secondary" onClick={raiseHand}>Raise Hand ✋</button>
            )}
          </div>
        </section>

        {handRaisedBy && isTeacher && (
          <div className="hand-alert">✋ {handRaisedBy === session.learner?.id ? session.learner.name : "A participant"} raised a hand.</div>
        )}
        {notice && <div className="room-notice">{notice}</div>}
        {!canEdit && <div className="room-notice">The interactive room becomes editable after the session is confirmed.</div>}

        <div className="class-layout">
          <div className="class-main-column">
            <div className="room-grid">
              <section className="room-card">
                <div className="card-heading"><h2>Live Topics</h2><span>{room.topics?.filter((t) => t.completed).length || 0}/{room.topics?.length || 0}</span></div>
                <div className="mini-progress"><div style={{ width: `${room.progress}%` }} /></div>
                {room.topics?.map((t) => (
                  <div className="check-row" key={t.id}>
                    <button disabled={!canEdit} className={t.completed ? "check done" : "check"} onClick={() => toggle("topics", t.id)}>{t.completed ? "✓" : ""}</button>
                    <span className={t.completed ? "done-text" : ""}>{t.title}</span>
                    <button className="icon-btn" disabled={!canEdit} onClick={() => remove("topics", t.id)}>×</button>
                  </div>
                ))}
                <form className="inline-form" onSubmit={addTopic}>
                  <input disabled={!canEdit} value={topic} onChange={(e) => setTopic(e.target.value)} placeholder="Add a lesson topic" />
                  <button disabled={!canEdit}>Add</button>
                </form>
              </section>

              <section className="room-card">
                <div className="card-heading"><h2>Live Tasks</h2><span>Shared checklist</span></div>
                {room.tasks?.map((t) => (
                  <div className="task-item" key={t.id}>
                    <div className="check-row">
                      <button disabled={!canEdit} className={t.completed ? "check done" : "check"} onClick={() => toggle("tasks", t.id)}>{t.completed ? "✓" : ""}</button>
                      <div><strong className={t.completed ? "done-text" : ""}>{t.title}</strong>{t.description && <p>{t.description}</p>}</div>
                      <button className="icon-btn" disabled={!canEdit} onClick={() => remove("tasks", t.id)}>×</button>
                    </div>
                  </div>
                ))}
                <form className="stack-form" onSubmit={addTask}>
                  <input disabled={!canEdit} value={task.title} onChange={(e) => setTask({ ...task, title: e.target.value })} placeholder="Task title" />
                  <input disabled={!canEdit} value={task.description} onChange={(e) => setTask({ ...task, description: e.target.value })} placeholder="Optional instructions" />
                  <button disabled={!canEdit}>Push task to class</button>
                </form>
              </section>

              <section className="room-card wide">
                <div className="card-heading"><h2>Live Shared Notes</h2><span>{remoteTyping ? `${partner?.name || "Partner"} is typing...` : "Changes appear live"}</span></div>
                {remoteTyping && <div className="live-draft"><strong>Live draft from {partner?.name || "partner"}</strong><p>{remoteDraft || "Typing..."}</p></div>}
                <form className="note-form" onSubmit={addNote}>
                  <textarea disabled={!canEdit} value={note} onChange={(e) => changeNote(e.target.value)} placeholder="Type class notes here. Your partner sees the draft live..." />
                  <button disabled={!canEdit}>Save note</button>
                </form>
                <div className="notes-list">
                  {room.notes?.map((n) => (
                    <article className="note-item" key={n.id}>
                      <div><strong>{n.createdBy?.name || "Student"}</strong><span>{n.updatedAt?.replace("T", " ").slice(0, 16)}</span></div>
                      <p>{n.content}</p>
                      <button disabled={!canEdit} onClick={() => remove("notes", n.id)}>Delete</button>
                    </article>
                  ))}
                </div>
              </section>

              <section className="room-card wide">
                <div className="card-heading"><h2>Learning Resources</h2><span>Push links instantly</span></div>
                <div className="resource-list">
                  {room.resources?.map((r) => (
                    <div className="resource-item" key={r.id}>
                      <span className="resource-type">{r.resourceType || "LINK"}</span>
                      <a href={r.url} target="_blank" rel="noreferrer">{r.title}</a>
                      <button disabled={!canEdit} onClick={() => remove("resources", r.id)}>Remove</button>
                    </div>
                  ))}
                </div>
                <form className="resource-form" onSubmit={addResource}>
                  <input disabled={!canEdit} required value={resource.title} onChange={(e) => setResource({ ...resource, title: e.target.value })} placeholder="Resource title" />
                  <input disabled={!canEdit} required type="url" value={resource.url} onChange={(e) => setResource({ ...resource, url: e.target.value })} placeholder="https://..." />
                  <select disabled={!canEdit} value={resource.resourceType} onChange={(e) => setResource({ ...resource, resourceType: e.target.value })}>
                    <option>LINK</option><option>VIDEO</option><option>DOCUMENT</option><option>PRACTICE</option>
                  </select>
                  <button disabled={!canEdit}>Share</button>
                </form>
              </section>
            </div>
          </div>

          <aside className="class-chat-card">
            <div className="card-heading"><h2>Class Chat</h2><span>{onlineUsers.size} online</span></div>
            <div className="class-chat-list">
              {room.classMessages?.length === 0 && <p className="chat-empty">Use this chat for questions during the lesson.</p>}
              {room.classMessages?.map((message) => {
                const mine = message.sender?.id === userId;
                return (
                  <div className={mine ? "class-message mine" : "class-message"} key={message.id}>
                    <strong>{mine ? "You" : message.sender?.name}</strong>
                    <p>{message.message}</p>
                    <span>{message.sentAt?.replace("T", " ").slice(11, 16)}</span>
                  </div>
                );
              })}
              <div ref={chatEndRef} />
            </div>
            <form className="class-chat-form" onSubmit={sendClassMessage}>
              <input disabled={!canEdit} value={chatText} onChange={(e) => setChatText(e.target.value)} placeholder="Message the class..." />
              <button disabled={!canEdit}>Send</button>
            </form>
          </aside>
        </div>

        <section className="quiz-card">
          <div className="card-heading">
            <div><span className="room-kicker">Post-session assessment</span><h2>Session Quiz</h2></div>
            <span>{quiz.questions?.length || 0} questions</span>
          </div>

          {isTeacher && (
            <form className="quiz-builder" onSubmit={addQuestion}>
              <p className="quiz-help">Prepare the assessment during class. The learner can answer only after you end the class.</p>
              <input required value={question.question} onChange={(e) => setQuestion({ ...question, question: e.target.value })} placeholder="Question" />
              <div className="option-grid">
                {["A", "B", "C", "D"].map((x) => (
                  <input key={x} required value={question[`option${x}`]} onChange={(e) => setQuestion({ ...question, [`option${x}`]: e.target.value })} placeholder={`Option ${x}`} />
                ))}
              </div>
              <div className="quiz-builder-footer">
                <label>Correct answer <select value={question.correctOption} onChange={(e) => setQuestion({ ...question, correctOption: e.target.value })}><option>A</option><option>B</option><option>C</option><option>D</option></select></label>
                <button>Add question</button>
              </div>
            </form>
          )}

          {session.status !== "COMPLETED" ? (
            <div className="quiz-locked">🔒 End the live class to unlock the post-session quiz.</div>
          ) : quiz.questions?.length === 0 ? (
            <div className="quiz-locked">The teacher has not added quiz questions yet.</div>
          ) : quiz.attempt ? (
            <div className="quiz-score"><strong>{quiz.attempt.score}/{quiz.attempt.total}</strong><span>Quiz completed</span></div>
          ) : isTeacher ? (
            <div className="quiz-locked">Quiz is now available to the learner.</div>
          ) : (
            <div className="quiz-list">
              {quiz.questions.map((q, i) => (
                <div className="quiz-question" key={q.id}>
                  <h3>{i + 1}. {q.question}</h3>
                  {["A", "B", "C", "D"].map((x) => (
                    <label key={x}>
                      <input type="radio" name={`q-${q.id}`} checked={answers[q.id] === x} onChange={() => setAnswers({ ...answers, [q.id]: x })} />
                      <span>{x}. {q[`option${x}`]}</span>
                    </label>
                  ))}
                </div>
              ))}
              <button className="submit-quiz" onClick={submitQuiz}>Submit quiz</button>
            </div>
          )}
        </section>
      </main>
    </div>
  );
}

export default SessionRoom;
