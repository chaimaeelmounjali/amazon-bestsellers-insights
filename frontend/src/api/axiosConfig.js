import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080/api', // URL du Backend Spring Boot
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true, // Crucial pour les cookies de session (CORS)
});

// Intercepteur pour gérer les erreurs (ex: 401 Unauthorized)
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            // Optionnel : Rediriger vers login si session expirée
            console.warn("Session expirée ou non autorisé");
        }
        return Promise.reject(error);
    }
);

export default api;
