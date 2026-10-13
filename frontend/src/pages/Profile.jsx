import { useEffect, useState } from "react";
import api from "../services/api";
import Navbar from "../components/Navbar";
import "./Profile.css";

function Profile() {
  const userId = Number(localStorage.getItem("userId"));

  const [profile, setProfile] = useState({
    name: "",
    email: "",
    college: "",
    department: "",
    year: "",
    bio: "",
    preferences: "",
  });

  const [allSkills, setAllSkills] = useState([]);
  const [userSkills, setUserSkills] = useState([]);

  const [newSkill, setNewSkill] = useState({
    skillId: "",
    type: "TEACH",
    level: "BEGINNER",
  });

  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);

  const loadProfile = async () => {
    try {
      const response = await api.get(`/users/${userId}`);

      setProfile({
        name: response.data.name || "",
        email: response.data.email || "",
        college: response.data.college || "",
        department: response.data.department || "",
        year: response.data.year || "",
        bio: response.data.bio || "",
        preferences: response.data.preferences || "",
      });
    } catch (error) {
      console.error(error);
      setMessage("Failed to load profile.");
    }
  };

  const loadAllSkills = async () => {
    try {
      const response = await api.get("/skills");

      setAllSkills(response.data);
    } catch (error) {
      console.error(error);
    }
  };

  const loadUserSkills = async () => {
    try {
      const response = await api.get(
        `/users/${userId}/skills`
      );

      setUserSkills(response.data);
    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    const loadData = async () => {
      await Promise.all([
        loadProfile(),
        loadAllSkills(),
        loadUserSkills(),
      ]);

      setLoading(false);
    };

    if (userId) {
      loadData();
    }
  }, [userId]);

  const handleProfileChange = (event) => {
    const { name, value } = event.target;

    setProfile((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const handleProfileSubmit = async (event) => {
    event.preventDefault();

    setMessage("");

    try {
      await api.put(`/users/${userId}`, {
        name: profile.name,
        college: profile.college,
        department: profile.department,
        year: profile.year
          ? Number(profile.year)
          : null,
        bio: profile.bio,
        preferences: profile.preferences,
      });

      setMessage("Profile updated successfully.");

      loadProfile();
    } catch (error) {
      console.error(error);

      const backendMessage = error.response?.data;

      if (typeof backendMessage === "string") {
        setMessage(backendMessage);
      } else {
        setMessage(
          backendMessage?.message ||
            "Failed to update profile."
        );
      }
    }
  };

  const handleSkillChange = (event) => {
    const { name, value } = event.target;

    setNewSkill((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const handleAddSkill = async (event) => {
    event.preventDefault();

    setMessage("");

    try {
      await api.post(`/users/${userId}/skills`, {
        skillId: newSkill.skillId,
        type: newSkill.type,
        level: newSkill.level,
      });

      setMessage("Skill added successfully.");

      setNewSkill({
        skillId: "",
        type: "TEACH",
        level: "BEGINNER",
      });

      loadUserSkills();
    } catch (error) {
      console.error(error);

      const backendMessage = error.response?.data;

      if (typeof backendMessage === "string") {
        setMessage(backendMessage);
      } else {
        setMessage(
          backendMessage?.message ||
            "Unable to add skill."
        );
      }
    }
  };

  const handleDeleteSkill = async (skillId) => {
    try {
      await api.delete(
        `/users/${userId}/skills/${skillId}`
      );

      setMessage("Skill removed successfully.");

      loadUserSkills();
    } catch (error) {
      console.error(error);

      setMessage("Unable to remove skill.");
    }
  };

  const teachingSkills = userSkills.filter(
    (item) => item.type === "TEACH"
  );

  const learningSkills = userSkills.filter(
    (item) => item.type === "LEARN"
  );

  if (loading) {
    return (
      <div>
        <Navbar />

        <div className="profile-loading">
          Loading profile...
        </div>
      </div>
    );
  }

  return (
    <div className="profile-page">
      <Navbar />

      <main className="profile-container">

        <div className="profile-header">
          <p className="profile-label">
            My Account
          </p>

          <h1>Profile</h1>

          <p>
            Manage your personal information and skills.
          </p>
        </div>

        {message && (
          <div className="profile-message">
            {message}
          </div>
        )}

        <section className="profile-card">

          <h2>Personal Information</h2>

          <form
            className="profile-form"
            onSubmit={handleProfileSubmit}
          >

            <div className="profile-form-grid">

              <div className="profile-field">
                <label>Name</label>

                <input
                  type="text"
                  name="name"
                  value={profile.name}
                  onChange={handleProfileChange}
                  required
                />
              </div>

              <div className="profile-field">
                <label>Email</label>

                <input
                  type="email"
                  value={profile.email}
                  disabled
                />
              </div>

              <div className="profile-field">
                <label>College</label>

                <input
                  type="text"
                  name="college"
                  value={profile.college}
                  onChange={handleProfileChange}
                />
              </div>

              <div className="profile-field">
                <label>Department</label>

                <input
                  type="text"
                  name="department"
                  value={profile.department}
                  onChange={handleProfileChange}
                />
              </div>

              <div className="profile-field">
                <label>Year</label>

                <input
                  type="number"
                  name="year"
                  value={profile.year}
                  onChange={handleProfileChange}
                  min="1"
                />
              </div>

              <div className="profile-field">
                <label>Preferences</label>

                <input
                  type="text"
                  name="preferences"
                  value={profile.preferences}
                  onChange={handleProfileChange}
                  placeholder="Example: Online evening"
                />
              </div>

            </div>

            <div className="profile-field full-width">
              <label>Bio</label>

              <textarea
                name="bio"
                value={profile.bio}
                onChange={handleProfileChange}
                rows="4"
              />
            </div>

            <button
              type="submit"
              className="profile-save-btn"
            >
              Save Profile
            </button>

          </form>

        </section>

        <section className="profile-card">

          <h2>Add Skill</h2>

          <form
            className="skill-add-form"
            onSubmit={handleAddSkill}
          >

            <div className="profile-field">
              <label>Skill</label>

              <select
                name="skillId"
                value={newSkill.skillId}
                onChange={handleSkillChange}
                required
              >
                <option value="">
                  Select skill
                </option>

                {allSkills.map((skill) => (
                  <option
                    key={skill.id}
                    value={skill.id}
                  >
                    {skill.skillName}
                  </option>
                ))}

              </select>
            </div>

            <div className="profile-field">
              <label>Type</label>

              <select
                name="type"
                value={newSkill.type}
                onChange={handleSkillChange}
              >
                <option value="TEACH">
                  I Can Teach
                </option>

                <option value="LEARN">
                  I Want to Learn
                </option>
              </select>
            </div>

            <div className="profile-field">
              <label>Level</label>

              <select
                name="level"
                value={newSkill.level}
                onChange={handleSkillChange}
              >
                <option value="BEGINNER">
                  Beginner
                </option>

                <option value="INTERMEDIATE">
                  Intermediate
                </option>

                <option value="ADVANCED">
                  Advanced
                </option>
              </select>
            </div>

            <button
              type="submit"
              className="profile-save-btn"
            >
              Add Skill
            </button>

          </form>

        </section>

        <section className="skills-grid">

          <div className="profile-card">

            <h2>Skills I Can Teach</h2>

            {teachingSkills.length === 0 ? (
              <div className="skills-empty">
                No teaching skills added.
              </div>
            ) : (
              <div className="profile-skills-list">

                {teachingSkills.map((item) => (
                  <div
                    className="profile-skill-item"
                    key={item.id}
                  >
                    <div>
                      <strong>
                        {item.skill?.skillName}
                      </strong>

                      <span>
                        {item.level}
                      </span>
                    </div>

                    <button
                      onClick={() =>
                        handleDeleteSkill(
                          item.skill?.id
                        )
                      }
                    >
                      Remove
                    </button>
                  </div>
                ))}

              </div>
            )}

          </div>

          <div className="profile-card">

            <h2>Skills I Want to Learn</h2>

            {learningSkills.length === 0 ? (
              <div className="skills-empty">
                No learning skills added.
              </div>
            ) : (
              <div className="profile-skills-list">

                {learningSkills.map((item) => (
                  <div
                    className="profile-skill-item"
                    key={item.id}
                  >
                    <div>
                      <strong>
                        {item.skill?.skillName}
                      </strong>

                      <span>
                        {item.level}
                      </span>
                    </div>

                    <button
                      onClick={() =>
                        handleDeleteSkill(
                          item.skill?.id
                        )
                      }
                    >
                      Remove
                    </button>
                  </div>
                ))}

              </div>
            )}

          </div>

        </section>

      </main>
    </div>
  );
}

export default Profile;