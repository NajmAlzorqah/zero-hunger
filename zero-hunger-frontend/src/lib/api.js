import axios from "axios";

// Jakarta EE GlassFish server URL (running on port 9090)
const API_URL =
  process.env.NEXT_PUBLIC_API_URL ||
  "http://localhost:9090/zero-hunger-1.0-SNAPSHOT/api/v1";
console.log("API URL:", API_URL);

const api = axios.create({
  baseURL: API_URL,
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
  withCredentials: false,
});

// Add token to requests if it exists
api.interceptors.request.use((config) => {
  if (typeof window !== "undefined") {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

// API methods
export const authAPI = {
  register: (data) => api.post("/register", data),
  login: (data) => api.post("/login", data),
  logout: () => api.post("/logout"),
  me: () => api.get("/me"),
};

export const donationAPI = {
  list: () => api.get("/donations"),
  create: (data) => api.post("/donations", data),
  claim: (id) => api.post(`/donations/${id}/claim`),
  myDonations: () => api.get("/my-donations"),
  nearby: (lat, lng, radius = 10) =>
    api.get(
      `/donations/nearby?latitude=${lat}&longitude=${lng}&radius=${radius}`
    ),
};

export const claimAPI = {
  list: () => api.get("/claims"),
  markPickedUp: (id, pickupCode) =>
    api.post(`/claims/${id}/pickup`, { pickup_code: pickupCode }),
  markDelivered: (id, notes = "") =>
    api.post(`/claims/${id}/deliver`, { notes }),
  cancel: (id) => api.delete(`/claims/${id}`),
};

export default api;
