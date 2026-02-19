import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token && !config.url?.includes('/auth/')) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const authApi = {
  register: (data: {
    email: string;
    password: string;
    businessName: string;
    contactPerson: string;
    phone: string;
    address: string;
  }) => api.post('/auth/register', data),

  login: (data: { email: string; password: string }) =>
    api.post('/auth/login', data),
};

export const stallApi = {
  getAll: () => api.get('/stalls'),
  getAvailable: () => api.get('/stalls/available'),
};

export const reservationApi = {
  create: (stallIds: number[]) => api.post('/reservations', { stallIds }),
  getMyReservations: () => api.get('/reservations/my-reservations'),
};

export const genreApi = {
  add: (genreName: string) => api.post('/genres', { genreName }),
  getMyGenres: () => api.get('/genres/my-genres'),
};

export default api;
