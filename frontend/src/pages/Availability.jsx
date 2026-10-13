import { useEffect, useState } from "react";
import api from "../services/api";
import Navbar from "../components/Navbar";
import "./Availability.css";

function Availability() {
  const userId = Number(localStorage.getItem("userId"));

  const [availability, setAvailability] = useState([]);
  const [formData, setFormData] = useState({
    day: "MONDAY",
    startTime: "",
    endTime: "",
  });

  const [editingId, setEditingId] = useState(null);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);

  const loadAvailability = async () => {
    try {
      const response = await api.get(
        `/users/${userId}/availability`
      );

      setAvailability(response.data);
    } catch (error) {
      console.error(error);
      setMessage("Failed to load availability.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (userId) {
      loadAvailability();
    }
  }, [userId]);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const resetForm = () => {
    setFormData({
      day: "MONDAY",
      startTime: "",
      endTime: "",
    });

    setEditingId(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    setMessage("");

    try {
      const payload = {
        day: formData.day,
        startTime: formData.startTime,
        endTime: formData.endTime,
      };

      if (editingId) {
        await api.put(
          `/availability/${editingId}`,
          payload
        );

        setMessage(
          "Availability updated successfully."
        );
      } else {
        await api.post(
          `/users/${userId}/availability`,
          payload
        );

        setMessage(
          "Availability added successfully."
        );
      }

      resetForm();
      loadAvailability();
    } catch (error) {
      console.error(error);

      const backendMessage = error.response?.data;

      if (typeof backendMessage === "string") {
        setMessage(backendMessage);
      } else {
        setMessage(
          backendMessage?.message ||
            "Unable to save availability."
        );
      }
    }
  };

  const handleEdit = (item) => {
    setEditingId(item.id);

    setFormData({
      day: item.day,
      startTime: item.startTime,
      endTime: item.endTime,
    });

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  const handleDelete = async (id) => {
    const confirmed = window.confirm(
      "Delete this availability slot?"
    );

    if (!confirmed) {
      return;
    }

    try {
      await api.delete(`/availability/${id}`);

      setMessage(
        "Availability deleted successfully."
      );

      if (editingId === id) {
        resetForm();
      }

      loadAvailability();
    } catch (error) {
      console.error(error);

      setMessage(
        "Unable to delete availability."
      );
    }
  };

  const formatTime = (time) => {
    if (!time) {
      return "";
    }

    const [hour, minute] = time.split(":");

    const date = new Date();

    date.setHours(
      Number(hour),
      Number(minute),
      0
    );

    return date.toLocaleTimeString([], {
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const formatDay = (day) => {
    if (!day) {
      return "";
    }

    return (
      day.charAt(0) +
      day.slice(1).toLowerCase()
    );
  };

  return (
    <div className="availability-page">
      <Navbar />

      <main className="availability-container">

        <div className="availability-header">
          <p className="availability-label">
            Schedule
          </p>

          <h1>Availability</h1>

          <p>
            Set the days and times when you are
            available for SkillSwap sessions.
          </p>
        </div>

        {message && (
          <div className="availability-message">
            {message}
          </div>
        )}

        <section className="availability-form-card">

          <h2>
            {editingId
              ? "Edit Availability"
              : "Add Availability"}
          </h2>

          <form
            className="availability-form"
            onSubmit={handleSubmit}
          >

            <div className="availability-field">
              <label>Day</label>

              <select
                name="day"
                value={formData.day}
                onChange={handleChange}
              >
                <option value="MONDAY">
                  Monday
                </option>

                <option value="TUESDAY">
                  Tuesday
                </option>

                <option value="WEDNESDAY">
                  Wednesday
                </option>

                <option value="THURSDAY">
                  Thursday
                </option>

                <option value="FRIDAY">
                  Friday
                </option>

                <option value="SATURDAY">
                  Saturday
                </option>

                <option value="SUNDAY">
                  Sunday
                </option>
              </select>
            </div>

            <div className="availability-field">
              <label>Start Time</label>

              <input
                type="time"
                name="startTime"
                value={formData.startTime}
                onChange={handleChange}
                required
              />
            </div>

            <div className="availability-field">
              <label>End Time</label>

              <input
                type="time"
                name="endTime"
                value={formData.endTime}
                onChange={handleChange}
                required
              />
            </div>

            <div className="availability-form-actions">

              <button
                type="submit"
                className="availability-save-btn"
              >
                {editingId
                  ? "Update Slot"
                  : "Add Slot"}
              </button>

              {editingId && (
                <button
                  type="button"
                  className="availability-cancel-btn"
                  onClick={resetForm}
                >
                  Cancel Edit
                </button>
              )}

            </div>

          </form>

        </section>

        <section className="availability-list-section">

          <h2>My Weekly Availability</h2>

          {loading ? (
            <div className="availability-empty">
              Loading availability...
            </div>
          ) : availability.length === 0 ? (
            <div className="availability-empty">
              No availability slots added yet.
            </div>
          ) : (
            <div className="availability-grid">

              {availability.map((item) => (
                <div
                  className="availability-card"
                  key={item.id}
                >

                  <div className="availability-day">
                    {formatDay(item.day)}
                  </div>

                  <div className="availability-time">
                    {formatTime(item.startTime)}
                    {" - "}
                    {formatTime(item.endTime)}
                  </div>

                  <div className="availability-actions">

                    <button
                      className="availability-edit-btn"
                      onClick={() =>
                        handleEdit(item)
                      }
                    >
                      Edit
                    </button>

                    <button
                      className="availability-delete-btn"
                      onClick={() =>
                        handleDelete(item.id)
                      }
                    >
                      Delete
                    </button>

                  </div>

                </div>
              ))}

            </div>
          )}

        </section>

      </main>
    </div>
  );
}

export default Availability;