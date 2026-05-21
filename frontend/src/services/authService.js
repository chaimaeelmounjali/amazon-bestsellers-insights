import api from '../api/axiosConfig';

const authService = {
    // Inscription
    register: async (registerData) => {
        const response = await api.post('/auth/inscription', registerData);
        return response.data;
    },

    // Connexion
    login: async (loginData) => {
        const response = await api.post('/auth/connexion', loginData);
        return response.data;
    },

    // Déconnexion
    logout: async () => {
        await api.post('/auth/deconnexion');
    },

    // Récupérer l'utilisateur courant (via session)
    getCurrentUser: async () => {
        const response = await api.get('/auth/utilisateur-connecte');
        return response.data;
    }
};

export default authService;
