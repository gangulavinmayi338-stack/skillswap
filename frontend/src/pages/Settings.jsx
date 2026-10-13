import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import "./Settings.css";

function Settings() {
  const navigate = useNavigate();

  const userData = localStorage.getItem("skillswapUser");

  const user = userData
    ? JSON.parse(userData)
    : null;

  const handleLogout = () => {
    localStorage.removeItem("userId");
    localStorage.removeItem("skillswapUser");

    navigate("/login");
  };

  return (
    <div className="settings-page">
      <Navbar />

      <main className="settings-container">

        <div className="settings-header">
          <p>Account</p>

          <h1>Settings</h1>

          <span>
            Manage your SkillSwap account.
          </span>
        </div>

        <section className="settings-card">
          <h2>Account Information</h2>

          <div className="settings-row">
            <span>Name</span>
            <strong>
              {user?.name || "Student"}
            </strong>
          </div>

          <div className="settings-row">
            <span>Email</span>
            <strong>
              {user?.email || "Not available"}
            </strong>
          </div>
        </section>

        <section className="settings-card">
          <h2>Session</h2>

          <p className="settings-description">
            Logout from your current SkillSwap session.
          </p>

          <button
            className="settings-logout-btn"
            onClick={handleLogout}
          >
            Logout
          </button>
        </section>

      </main>
    </div>
  );
}

export default Settings;