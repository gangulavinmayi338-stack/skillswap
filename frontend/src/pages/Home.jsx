import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import "./Home.css";

function Home() {
  return (
    <div className="home-page">
      <Navbar />

      <main className="home-container">

        <section className="hero-section">

          <div className="hero-content">

            <div className="hero-badge">
              Student Skill Exchange Platform
            </div>

            <h1>
              Learn what you want.
              <br />
              Teach what you know.
            </h1>

            <p>
              SkillSwap connects students who want
              to learn from each other. Find the
              right learning partner based on skills,
              availability, experience, and
              compatibility.
            </p>

            <div className="hero-actions">

              <Link
                to="/register"
                className="primary-home-btn"
              >
                Get Started
              </Link>

              <Link
                to="/login"
                className="secondary-home-btn"
              >
                Login
              </Link>

            </div>

          </div>

        </section>

        <section className="how-section">

          <div className="section-heading">
            <p>How it works</p>

            <h2>
              Learn together with SkillSwap
            </h2>

            <span>
              Find students with complementary
              skills and build meaningful learning
              connections.
            </span>
          </div>

          <div className="how-grid">

            <div className="how-card">
              <div className="how-number">
                1
              </div>

              <h3>Create Your Profile</h3>

              <p>
                Add your college information,
                interests, and learning preferences.
              </p>
            </div>

            <div className="how-card">
              <div className="how-number">
                2
              </div>

              <h3>Add Your Skills</h3>

              <p>
                Choose the skills you can teach and
                the skills you want to learn.
              </p>
            </div>

            <div className="how-card">
              <div className="how-number">
                3
              </div>

              <h3>Find Matches</h3>

              <p>
                Our matching system recommends
                students based on compatibility.
              </p>
            </div>

            <div className="how-card">
              <div className="how-number">
                4
              </div>

              <h3>Connect & Learn</h3>

              <p>
                Message students, schedule sessions,
                and learn through interactive peer sessions.
              </p>
            </div>

          </div>

        </section>

      </main>
    </div>
  );
}

export default Home;