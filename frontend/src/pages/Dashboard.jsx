import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import Navbar from "../components/Navbar";
import "./Dashboard.css";

function Dashboard() {
  const navigate = useNavigate();

  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const userId = localStorage.getItem("userId");

    if (!userId) {
      navigate("/login");
      return;
    }

    const loadDashboard = async () => {
      try {
        const response = await api.get(
          `/dashboard/${userId}`
        );

        setDashboard(response.data);
      } catch (error) {
        console.error(error);

        setError(
          "Failed to load dashboard"
        );
      } finally {
        setLoading(false);
      }
    };

    loadDashboard();
  }, [navigate]);

  if (loading) {
    return (
      <div>
        <Navbar />

        <div className="dashboard-loading">
          Loading dashboard...
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div>
        <Navbar />

        <div className="dashboard-error">
          {error}
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-page">

      <Navbar />

      <main className="dashboard-container">

        <div className="dashboard-header">

          <div>
            <p className="dashboard-label">
              Dashboard
            </p>

            <h1>
              Welcome, {dashboard?.name}
            </h1>

            <p>
              Track your skills, sessions,
              connections and recommended partners.
            </p>
          </div>

        </div>

        <section className="stats-grid">

          <div className="stat-card">
            <span>Skills I Teach</span>

            <h2>
              {dashboard?.teachingSkillsCount || 0}
            </h2>
          </div>

          <div className="stat-card">
            <span>Learning Goals</span>

            <h2>
              {dashboard?.learningSkillsCount || 0}
            </h2>
          </div>

          <div className="stat-card">
            <span>Connections</span>

            <h2>
              {dashboard?.connectionsCount || 0}
            </h2>
          </div>

          <div className="stat-card">
            <span>Completed Sessions</span>

            <h2>
              {dashboard?.completedSessions || 0}
            </h2>
          </div>

          <div className="stat-card">
            <span>Learning Hours</span>

            <h2>
              {dashboard?.learningHours || 0}
            </h2>
          </div>

          <div className="stat-card">
            <span>Average Rating</span>

            <h2>
              {dashboard?.averageRating || 0}
            </h2>
          </div>

        </section>

        <section className="dashboard-section">

          <div className="section-header">
            <div>
              <h2>Recommended Matches</h2>

              <p>
                Students selected by the SkillSwap
                matching engine.
              </p>
            </div>
          </div>

          {dashboard?.recommendedMatches?.length > 0 ? (

            <div className="matches-grid">

              {dashboard.recommendedMatches.map(
                (match) => (

                  <div
                    className="dashboard-match-card"
                    key={match.userId}
                  >

                    <div className="match-card-top">

                      <div className="match-avatar">
                        {match.name
                          ?.charAt(0)
                          .toUpperCase()}
                      </div>

                      <div className="dashboard-match-score">
                        {match.matchPercentage}%
                      </div>

                    </div>

                    <h3>{match.name}</h3>

                    <p>
                      {match.department ||
                        "Student"}
                    </p>

                    <div className="match-details">

                      <span>
                        {match.college ||
                          "College not added"}
                      </span>

                      <span>
                        ⭐ {match.averageRating || 0}
                      </span>

                    </div>

                    <button
                      className="view-matches-btn"
                      onClick={() =>
                        navigate("/matches")
                      }
                    >
                      View Match
                    </button>

                  </div>

                )
              )}

            </div>

          ) : (

            <div className="empty-dashboard">
              No recommendations available yet.
            </div>

          )}

        </section>

      </main>

    </div>
  );
}

export default Dashboard;