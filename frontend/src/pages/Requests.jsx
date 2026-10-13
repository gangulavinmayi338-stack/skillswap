import { useEffect, useState } from "react";
import api from "../services/api";
import Navbar from "../components/Navbar";
import "./Requests.css";

function Requests() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");

  const userId = localStorage.getItem("userId");

  const loadRequests = async () => {
    try {
      const response = await api.get(
        `/connections/${userId}/pending`
      );

      setRequests(response.data);
    } catch (error) {
      console.error(error);

      setMessage("Failed to load connection requests.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (userId) {
      loadRequests();
    }
  }, [userId]);

  const handleAccept = async (connectionId) => {
    try {
      await api.put(
        `/connections/${connectionId}/accept`
      );

      setMessage("Connection request accepted.");

      loadRequests();
    } catch (error) {
      console.error(error);

      setMessage(
        error.response?.data?.message ||
          "Unable to accept request."
      );
    }
  };

  const handleReject = async (connectionId) => {
    try {
      await api.put(
        `/connections/${connectionId}/reject`
      );

      setMessage("Connection request rejected.");

      loadRequests();
    } catch (error) {
      console.error(error);

      setMessage(
        error.response?.data?.message ||
          "Unable to reject request."
      );
    }
  };

  return (
    <div className="requests-page">
      <Navbar />

      <main className="requests-container">
        <div className="requests-header">
          <p className="requests-label">
            Connections
          </p>

          <h1>Connection Requests</h1>

          <p>
            Review students who want to connect with you.
          </p>
        </div>

        {message && (
          <div className="requests-message">
            {message}
          </div>
        )}

        {loading ? (
          <div className="requests-empty">
            Loading requests...
          </div>
        ) : requests.length === 0 ? (
          <div className="requests-empty">
            No pending connection requests.
          </div>
        ) : (
          <div className="requests-list">
            {requests.map((request) => (
              <div
                className="request-card"
                key={request.id}
              >
                <div className="request-user">
                  <div className="request-avatar">
                    {request.sender?.name
                      ?.charAt(0)
                      .toUpperCase()}
                  </div>

                  <div>
                    <h3>
                      {request.sender?.name}
                    </h3>

                    <p>
                      {request.sender?.department ||
                        "Student"}
                    </p>

                    <span>
                      {request.sender?.college ||
                        "College not added"}
                    </span>
                  </div>
                </div>

                <div className="request-text">
                  <strong>
                    {request.sender?.name}
                  </strong>{" "}
                  wants to connect with you.
                </div>

                <div className="request-actions">
                  <button
                    className="reject-btn"
                    onClick={() =>
                      handleReject(request.id)
                    }
                  >
                    Reject
                  </button>

                  <button
                    className="accept-btn"
                    onClick={() =>
                      handleAccept(request.id)
                    }
                  >
                    Accept
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}

export default Requests;