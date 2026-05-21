import api from '../api/axiosConfig';

const stockService = {
    // === STOCK ===
    updateStock: async (productId, quantity) => {
        await api.put(`/produits/${productId}/stock?quantite=${quantity}`);
    },

    // Récupérer le stock (souvent via produit, mais si endpoint dédié existe)
    // Ici on suppose que l'info est dans le produit, mais on peut avoir des endpoints spécifiques Dashboard

    // === ALERTES (AlerteController) ===
    createAlert: async (alertData) => {
        const response = await api.post('/alertes', alertData);
        return response.data;
    },

    getUserAlerts: async (userId) => {
        const response = await api.get(`/alertes/utilisateur/${userId}`);
        return response.data;
    },

    deleteAlert: async (id) => {
        await api.delete(`/alertes/${id}`);
    }
};

export default stockService;
