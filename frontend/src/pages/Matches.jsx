import { useEffect, useState } from "react";
import api from "../services/api";
import Navbar from "../components/Navbar";
import "./Matches.css";

function Matches() {
  const [matches, setMatches] = useState([]);
  const [connections, setConnections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");

  const userId = localStorage.getItem("userId");

  useEffect(() => {
    const loadData = async () => {
      try {
        const [matchesResponse, connectionsResponse] =
          await Promise.all([
            api.get(`/matches/${userId}`),
            api.get(`/connections/${userId}`),
          ]);

        setMatches(matchesResponse.data);
        setConnections(connectionsResponse.data);
      } catch (error) {
        console.error(error);

        setMessage("Failed to load matches.");
      } finally {
        setLoading(false);
      }
    };

    if (userId) {
      loadData();
    }
  }, [userId]);

  const getConnectionStatus = (otherUserId) => {
    const currentUserId = Number(userId);

    const connection = connections.find((connection) => {
      const senderId = connection.sender?.id;
      const receiverId = connection.receiver?.id;

      return (
        (senderId === currentUserId &&
          receiverId === otherUserId) ||
        (senderId === otherUserId &&
          receiverId === currentUserId)
      );
    });

    return connection || null;
  };

  const handleConnect = async (receiverId) => {
    setMessage("");

    try {
      const response = await api.post("/connections", {
        senderId: Number(userId),
        receiverId: receiverId,
      });

      setConnections((previousConnections) => [
        ...previousConnections,
        response.data,
      ]);

      setMessage(
        "Connection request sent successfully."
      );
    } catch (error) {
      console.error(error);

      const backendMessage = error.response?.data;

      if (typeof backendMessage === "string") {
        setMessage(backendMessage);
      } else {
        setMessage(
          backendMessage?.message ||
            "Unable to send connection request."
        );
      }
    }
  };

  const renderConnectionButton = (match) => {
    const connection = getConnectionStatus(
      match.userId
    );

    if (!connection) {
      return (
        <button
          className="connect-btn"
          onClick={() =>
            handleConnect(match.userId)
          }
        >
          Connect
        </button>
      );
    }

    if (connection.status === "PENDING") {
      return (
        <button
          className="connect-btn"
          disabled
        >
          Pending
        </button>
      );
    }

    if (connection.status === "ACCEPTED") {
      return (
        <button
          className="connect-btn"
          disabled
        >
          Connected
        </button>
      );
    }

    if (connection.status === "REJECTED") {
      return (
        <button
          className="connect-btn"
          disabled
        >
          Rejected
        </button>
      );
    }

    return null;
  };

  return (
    <div className="matches-page">
      <Navbar />

      <main className="matches-container">

        <div className="matches-header">

          <p className="matches-label">
            Intelligent Matching
          </p>

          <h1>
            Recommended Skill Partners
          </h1>

          <p>
            Students are ranked using skill
            compatibility, learning goals,
            availability, skill level, ratings
            and preferences.
          </p>

        </div>

        {message && (
          <div className="matches-message">
            {message}
          </div>
        )}

        {loading ? (
          <div className="matches-loading">
            Loading matches...
          </div>
        ) : matches.length === 0 ? (
          <div className="matches-empty">
            No matches available yet.
          </div>
        ) : (
          <div className="matches-list">

            {matches.map((match) => {

              const connection =
                getConnectionStatus(
                  match.userId
                );

              return (
                <div
                  className="match-card-large"
                  key={match.userId}
                >

                  <div className="match-profile-section">

                    <div className="match-avatar-large">
                      {match.name
                        ?.charAt(0)
                        .toUpperCase()}
                    </div>

                    <div>

                      <h2>
                        {match.name}
                      </h2>

                      <p>
                        {match.department ||
                          "Student"}
                      </p>

                      <span>
                        {match.college ||
                          "College not added"}
                      </span>

                    </div>

                  </div>

                  <div className="match-info-section">

                    <div className="match-rating">
                      ⭐{" "}
                      {match.averageRating || 0}
                    </div>

                    <div className="match-percentage">
                      {match.matchPercentage}%
                      <span> Match</span>
                    </div>

                    {connection && (
                      <div
                        style={{
                          marginTop: "10px",
                          fontSize: "13px",
                          color: "#6b7280",
                        }}
                      >
                        Status:{" "}
                        <strong>
                          {connection.status}
                        </strong>
                      </div>
                    )}

                  </div>

                  <div className="match-actions">

                    <button
                      className="profile-btn"
                      onClick={() =>
                        alert(
                          `Name: ${match.name}\nEmail: ${match.email}\nDepartment: ${
                            match.department ||
                            "Not added"
                          }\nCollege: ${
                            match.college ||
                            "Not added"
                          }\nRating: ${
                            match.averageRating || 0
                          }\nMatch: ${
                            match.matchPercentage
                          }%`
                        )
                      }
                    >
                      View Profile
                    </button>

                    {renderConnectionButton(
                      match
                    )}

                  </div>

                </div>
              );
            })}

          </div>
        )}

      </main>
    </div>
  );
}

export default Matches;