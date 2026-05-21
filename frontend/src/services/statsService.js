import api from '../api/axiosConfig';

const statsService = {
    // Dashboard général
    getDashboardStats: async () => {
        const response = await api.get('/dashboard');
        return response.data;
    },

    // Analyse Prédictive
    getSalesPrediction: async (productId, days) => {
        const response = await api.get(`/analyse-predictive/ventes/${productId}?jours=${days}`);
        return response.data;
    },

    getTrendAnalysis: async (category) => {
        const response = await api.get(`/analyse-predictive/tendances/${category}`);
        return response.data;
    },

    // Ventes (VenteController)
    createSale: async (saleData) => {
        const response = await api.post('/ventes', saleData);
        return response.data;
    },

    getSalesHistory: async (userId) => {
        // Supposons un endpoint historique
        const response = await api.get(`/ventes/historique/${userId}`);
        return response.data;
    }
};

export default statsService;
