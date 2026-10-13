import axios from "axios";

const backendUrl = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

export const webSocketBaseUrl = backendUrl.replace(/\/api\/?$/, "").replace(/^http/, "ws");

const api = axios.create({
  baseURL: backendUrl,
  headers: {
    "Content-Type": "application/json",
  },
});

export default api;