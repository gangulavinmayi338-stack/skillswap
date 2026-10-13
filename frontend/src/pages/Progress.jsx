import { useEffect, useMemo, useState } from "react";
import api from "../services/api";
import Navbar from "../components/Navbar";
import "./Progress.css";

function Progress() {
  const userId = Number(localStorage.getItem("userId"));

  const [sessions, setSessions] = useState([]);
  const [ratings, setRatings] = useState([]);
  const [userSkills, setUserSkills] = useState([]);
  const [profile, setProfile] = useState(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadProgressData = async () => {
      try {
        const [
          sessionsResponse,
          ratingsResponse,
          skillsResponse,
          profileResponse,
        ] = await Promise.all([
          api.get(`/sessions/${userId}`),
          api.get(`/users/${userId}/ratings`),
          api.get(`/users/${userId}/skills`),
          api.get(`/users/${userId}`),
        ]);

        setSessions(sessionsResponse.data);
        setRatings(ratingsResponse.data);
        setUserSkills(skillsResponse.data);
        setProfile(profileResponse.data);
      } catch (error) {
        console.error(error);

        setError(
          "Failed to load progress information."
        );
      } finally {
        setLoading(false);
      }
    };

    if (userId) {
      loadProgressData();
    }
  }, [userId]);

  const completedSessions = useMemo(() => {
    return sessions.filter(
      (session) =>
        session.status === "COMPLETED"
    );
  }, [sessions]);

  const totalLearningMinutes = useMemo(() => {
    return completedSessions.reduce(
      (total, session) => {
        if (session.learner?.id === userId) {
          return (
            total +
            Number(session.duration || 0)
          );
        }

        return total;
      },
      0
    );
  }, [completedSessions, userId]);

  const learningHours =
    Math.round(
      (totalLearningMinutes / 60) * 10
    ) / 10;

  const learnedSkills = useMemo(() => {
    const completedLearningSkillNames =
      completedSessions
        .filter(
          (session) =>
            session.learner?.id === userId
        )
        .map(
          (session) =>
            session.skill?.skillName
        )
        .filter(Boolean);

    return [
      ...new Set(
        completedLearningSkillNames
      ),
    ];
  }, [completedSessions, userId]);

  const learningGoals = userSkills.filter(
    (item) => item.type === "LEARN"
  );

  const formatDate = (dateValue) => {
    if (!dateValue) {
      return "";
    }

    return new Date(
      `${dateValue}T00:00:00`
    ).toLocaleDateString([], {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  if (loading) {
    return (
      <div>
        <Navbar />

        <div className="progress-loading">
          Loading progress...
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div>
        <Navbar />

        <div className="progress-error">
          {error}
        </div>
      </div>
    );
  }

  return (
    <div className="progress-page">
      <Navbar />

      <main className="progress-container">

        <div className="progress-header">
          <p className="progress-label">
            Learning Progress
          </p>

          <h1>My Progress</h1>

          <p>
            Track completed sessions,
            learning hours, skills and ratings.
          </p>
        </div>

        <section className="progress-stats">

          <div className="progress-stat-card">
            <span>
              Completed Sessions
            </span>

            <h2>
              {completedSessions.length}
            </h2>
          </div>

          <div className="progress-stat-card">
            <span>
              Learning Hours
            </span>

            <h2>
              {learningHours}
            </h2>
          </div>

          <div className="progress-stat-card">
            <span>
              Skills Learned
            </span>

            <h2>
              {learnedSkills.length}
            </h2>
          </div>

          <div className="progress-stat-card">
            <span>
              Average Rating
            </span>

            <h2>
              {profile?.averageRating || 0}
            </h2>
          </div>

        </section>

        <div className="progress-grid">

          <section className="progress-card">
            <h2>Skills Learned</h2>

            {learnedSkills.length === 0 ? (
              <div className="progress-empty">
                Complete learning sessions
                to build your learned skills.
              </div>
            ) : (
              <div className="learned-skills-list">
                {learnedSkills.map(
                  (skillName) => (
                    <span key={skillName}>
                      {skillName}
                    </span>
                  )
                )}
              </div>
            )}
          </section>

          <section className="progress-card">
            <h2>Current Learning Goals</h2>

            {learningGoals.length === 0 ? (
              <div className="progress-empty">
                No learning goals added.
              </div>
            ) : (
              <div className="learning-goals-list">
                {learningGoals.map(
                  (item) => (
                    <div
                      className="learning-goal-item"
                      key={item.id}
                    >
                      <strong>
                        {
                          item.skill
                            ?.skillName
                        }
                      </strong>

                      <span>
                        {item.level}
                      </span>
                    </div>
                  )
                )}
              </div>
            )}
          </section>

        </div>

        <section className="progress-card">
          <h2>Recent Completed Sessions</h2>

          {completedSessions.length === 0 ? (
            <div className="progress-empty">
              No completed sessions yet.
            </div>
          ) : (
            <div className="recent-session-list">

              {completedSessions
                .slice()
                .reverse()
                .slice(0, 5)
                .map((session) => (
                  <div
                    className="recent-session-item"
                    key={session.id}
                  >

                    <div>
                      <strong>
                        {
                          session.skill
                            ?.skillName
                        }
                      </strong>

                      <p>
                        Teacher:{" "}
                        {
                          session.teacher
                            ?.name
                        }
                      </p>

                      <p>
                        Learner:{" "}
                        {
                          session.learner
                            ?.name
                        }
                      </p>
                    </div>

                    <div className="recent-session-meta">
                      <span>
                        {formatDate(
                          session.sessionDate
                        )}
                      </span>

                      <span>
                        {
                          session.duration
                        }{" "}
                        minutes
                      </span>
                    </div>

                  </div>
                ))}

            </div>
          )}
        </section>

        <section className="progress-card">
          <h2>Ratings Received</h2>

          {ratings.length === 0 ? (
            <div className="progress-empty">
              You have not received any
              ratings yet.
            </div>
          ) : (
            <div className="ratings-list">

              {ratings.map((rating) => (
                <div
                  className="rating-item"
                  key={rating.id}
                >

                  <div className="rating-stars">
                    {"★".repeat(
                      rating.rating
                    )}
                    {"☆".repeat(
                      5 - rating.rating
                    )}
                  </div>

                  <p>
                    {rating.feedback ||
                      "No feedback provided."}
                  </p>

                  <span>
                    From:{" "}
                    {rating.reviewer?.name ||
                      "Student"}
                  </span>

                </div>
              ))}

            </div>
          )}
        </section>

      </main>
    </div>
  );
}

export default Progress;