import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import Navbar from "../components/Navbar";
import "./Sessions.css";

function Sessions() {
  const navigate = useNavigate();
  const userId = Number(localStorage.getItem("userId"));

  const [sessions, setSessions] = useState([]);
  const [connections, setConnections] = useState([]);
  const [skills, setSkills] = useState([]);

  const [formData, setFormData] = useState({
    teacherId: "",
    learnerId: "",
    skillId: "",
    sessionDate: "",
    startTime: "",
    duration: 60,
    mode: "Online",
  });

  const [ratingForm, setRatingForm] = useState({
    sessionId: null,
    reviewedUserId: null,
    rating: 5,
    feedback: "",
  });

  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);

  const loadSessions = async () => {
    try {
      const response = await api.get(
        `/sessions/${userId}`
      );

      setSessions(response.data);
    } catch (error) {
      console.error(error);
      setMessage("Failed to load sessions.");
    }
  };

  const loadConnections = async () => {
    try {
      const response = await api.get(
        `/connections/${userId}`
      );

      const accepted =
        response.data.filter(
          (connection) =>
            connection.status === "ACCEPTED"
        );

      setConnections(accepted);
    } catch (error) {
      console.error(error);
    }
  };

  const loadSkills = async () => {
    try {
      const response = await api.get("/skills");

      setSkills(response.data);
    } catch (error) {
      console.error(error);
      setMessage("Unable to load available skills. Please refresh and try again.");
    }
  };

  useEffect(() => {
    const loadData = async () => {
      await Promise.all([
        loadSessions(),
        loadConnections(),
        loadSkills(),
      ]);

      setLoading(false);
    };

    if (userId) {
      loadData();
    }
  }, [userId]);

  const getOtherUser = (connection) => {
    if (connection.sender?.id === userId) {
      return connection.receiver;
    }

    return connection.sender;
  };

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const handleCreateSession = async (event) => {
    event.preventDefault();

    setMessage("");

    try {
      await api.post("/sessions", {
        teacherId: Number(formData.teacherId),
        learnerId: Number(formData.learnerId),
        skillId: Number(formData.skillId),
        sessionDate: formData.sessionDate,
        startTime: formData.startTime,
        duration: Number(formData.duration),
        mode: formData.mode,
      });

      setMessage(
        "Session created successfully."
      );

      setFormData({
        teacherId: "",
        learnerId: "",
        skillId: "",
        sessionDate: "",
        startTime: "",
        duration: 60,
        mode: "Online",
      });

      loadSessions();
    } catch (error) {
      console.error(error);

      const backendMessage = error.response?.data;

      if (typeof backendMessage === "string") {
        setMessage(backendMessage);
      } else {
        setMessage(
          backendMessage?.message ||
            "Unable to create session."
        );
      }
    }
  };

  const updateSessionStatus = async (
    sessionId,
    action
  ) => {
    try {
      await api.put(
        `/sessions/${sessionId}/${action}`
      );

      setMessage(
        `Session ${action} successful.`
      );

      loadSessions();
    } catch (error) {
      console.error(error);

      const backendMessage = error.response?.data;

      if (typeof backendMessage === "string") {
        setMessage(backendMessage);
      } else {
        setMessage(
          backendMessage?.message ||
            "Unable to update session."
        );
      }
    }
  };

  const openRatingForm = (session) => {
    const reviewedUser =
      session.teacher?.id === userId
        ? session.learner
        : session.teacher;

    setRatingForm({
      sessionId: session.id,
      reviewedUserId: reviewedUser.id,
      rating: 5,
      feedback: "",
    });
  };

  const closeRatingForm = () => {
    setRatingForm({
      sessionId: null,
      reviewedUserId: null,
      rating: 5,
      feedback: "",
    });
  };

  const handleRatingSubmit = async (event) => {
    event.preventDefault();

    try {
      await api.post("/ratings", {
        sessionId: ratingForm.sessionId,
        reviewerId: userId,
        reviewedUserId:
          ratingForm.reviewedUserId,
        rating: Number(ratingForm.rating),
        feedback: ratingForm.feedback,
      });

      setMessage(
        "Rating submitted successfully."
      );

      closeRatingForm();
    } catch (error) {
      console.error(error);

      const backendMessage = error.response?.data;

      if (typeof backendMessage === "string") {
        setMessage(backendMessage);
      } else {
        setMessage(
          backendMessage?.message ||
            "Unable to submit rating."
        );
      }
    }
  };

  return (
    <div className="sessions-page">
      <Navbar />

      <main className="sessions-container">

        <div className="sessions-header">
          <p className="sessions-label">
            Learning Sessions
          </p>

          <h1>Sessions</h1>

          <p>
            Schedule and manage your peer learning
            sessions.
          </p>
        </div>

        {message && (
          <div className="sessions-message">
            {message}
          </div>
        )}

        <section className="session-create-card">

          <h2>Schedule a Session</h2>

          <form
            className="session-form"
            onSubmit={handleCreateSession}
          >

            <div className="session-form-grid">

              <div className="session-field">
                <label>Teacher</label>

                <select
                  name="teacherId"
                  value={formData.teacherId}
                  onChange={handleChange}
                  required
                >
                  <option value="">
                    Select teacher
                  </option>

                  <option value={userId}>
                    Me
                  </option>

                  {connections.map(
                    (connection) => {
                      const user =
                        getOtherUser(connection);

                      return (
                        <option
                          key={user.id}
                          value={user.id}
                        >
                          {user.name}
                        </option>
                      );
                    }
                  )}
                </select>
              </div>

              <div className="session-field">
                <label>Learner</label>

                <select
                  name="learnerId"
                  value={formData.learnerId}
                  onChange={handleChange}
                  required
                >
                  <option value="">
                    Select learner
                  </option>

                  <option value={userId}>
                    Me
                  </option>

                  {connections.map(
                    (connection) => {
                      const user =
                        getOtherUser(connection);

                      return (
                        <option
                          key={user.id}
                          value={user.id}
                        >
                          {user.name}
                        </option>
                      );
                    }
                  )}
                </select>
              </div>

              <div className="session-field">
                <label>Skill</label>

                <select
                  name="skillId"
                  value={formData.skillId}
                  onChange={handleChange}
                  required
                >
                  <option value="">
                    {skills.length ? "Select skill" : "No skills available"}
                  </option>

                  {skills.map((skill) => (
                    <option
                      key={skill.id}
                      value={skill.id}
                    >
                      {skill.skillName}
                    </option>
                  ))}
                </select>
              </div>

              <div className="session-field">
                <label>Date</label>

                <input
                  type="date"
                  name="sessionDate"
                  value={formData.sessionDate}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="session-field">
                <label>Time</label>

                <input
                  type="time"
                  name="startTime"
                  value={formData.startTime}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="session-field">
                <label>
                  Duration (minutes)
                </label>

                <input
                  type="number"
                  name="duration"
                  value={formData.duration}
                  onChange={handleChange}
                  min="15"
                  required
                />
              </div>

              <div className="session-field">
                <label>Mode</label>

                <select
                  name="mode"
                  value={formData.mode}
                  onChange={handleChange}
                >
                  <option value="Online">
                    Online
                  </option>

                  <option value="In Person">
                    In Person
                  </option>
                </select>
              </div>

            </div>

            <button
              type="submit"
              className="create-session-btn"
            >
              Schedule Session
            </button>

          </form>

        </section>

        <section className="sessions-list-section">

          <h2>My Sessions</h2>

          {loading ? (
            <div className="sessions-empty">
              Loading sessions...
            </div>
          ) : sessions.length === 0 ? (
            <div className="sessions-empty">
              No sessions scheduled yet.
            </div>
          ) : (
            <div className="sessions-list">

              {sessions.map((session) => (
                <div
                  className="session-card"
                  key={session.id}
                >

                  <div className="session-card-main">

                    <div>
                      <span className="session-skill">
                        {session.skill?.skillName}
                      </span>

                      <h3>
                        {session.teacher?.name}
                        {" → "}
                        {session.learner?.name}
                      </h3>

                      <p>
                        {session.sessionDate}
                        {" • "}
                        {session.startTime}
                      </p>

                      <p>
                        {session.duration} minutes
                        {" • "}
                        {session.mode}
                      </p>
                    </div>

                    <span
                      className={`session-status ${session.status.toLowerCase()}`}
                    >
                      {session.status}
                    </span>

                  </div>

                  <div className="session-actions">

                    {(session.status === "CONFIRMED" || session.status === "COMPLETED") && (
                      <button
                        className="session-room-btn"
                        onClick={() => navigate(`/sessions/${session.id}/room`)}
                      >
                        Open Interactive Class
                      </button>
                    )}

                    {session.status ===
                      "REQUESTED" && (
                      <>
                        <button
                          onClick={() =>
                            updateSessionStatus(
                              session.id,
                              "confirm"
                            )
                          }
                          className="session-confirm-btn"
                        >
                          Confirm
                        </button>

                        <button
                          onClick={() =>
                            updateSessionStatus(
                              session.id,
                              "cancel"
                            )
                          }
                          className="session-secondary-btn"
                        >
                          Cancel
                        </button>
                      </>
                    )}

                    {session.status ===
                      "CONFIRMED" && (
                      <>
                        <button
                          onClick={() =>
                            updateSessionStatus(
                              session.id,
                              "complete"
                            )
                          }
                          className="session-confirm-btn"
                        >
                          Complete
                        </button>

                        <button
                          onClick={() =>
                            updateSessionStatus(
                              session.id,
                              "cancel"
                            )
                          }
                          className="session-secondary-btn"
                        >
                          Cancel
                        </button>
                      </>
                    )}

                    {session.status ===
                      "COMPLETED" && (
                      <button
                        className="session-rate-btn"
                        onClick={() =>
                          openRatingForm(session)
                        }
                      >
                        Rate Student
                      </button>
                    )}

                  </div>

                  {ratingForm.sessionId ===
                    session.id && (
                    <form
                      className="rating-form"
                      onSubmit={
                        handleRatingSubmit
                      }
                    >

                      <h3>
                        Rate this session
                      </h3>

                      <div className="rating-stars-input">

                        {[1, 2, 3, 4, 5].map(
                          (star) => (
                            <button
                              key={star}
                              type="button"
                              className={
                                star <=
                                ratingForm.rating
                                  ? "star active"
                                  : "star"
                              }
                              onClick={() =>
                                setRatingForm(
                                  (previous) => ({
                                    ...previous,
                                    rating: star,
                                  })
                                )
                              }
                            >
                              ★
                            </button>
                          )
                        )}

                      </div>

                      <textarea
                        value={
                          ratingForm.feedback
                        }
                        onChange={(event) =>
                          setRatingForm(
                            (previous) => ({
                              ...previous,
                              feedback:
                                event.target.value,
                            })
                          )
                        }
                        placeholder="Write feedback..."
                        rows="3"
                      />

                      <div className="rating-form-actions">

                        <button
                          type="submit"
                          className="session-confirm-btn"
                        >
                          Submit Rating
                        </button>

                        <button
                          type="button"
                          className="session-secondary-btn"
                          onClick={closeRatingForm}
                        >
                          Cancel
                        </button>

                      </div>

                    </form>
                  )}

                </div>
              ))}

            </div>
          )}

        </section>

      </main>
    </div>
  );
}

export default Sessions;