import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'https://sos-semali-backend.azurewebsites.net/api',
});

export default api;