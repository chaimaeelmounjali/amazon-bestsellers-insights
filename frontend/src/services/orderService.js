import api from '../api/axiosConfig';

const orderService = {
    // Créer une commande
    createOrder: async (orderData) => {
        const response = await api.post('/commandes', orderData);
        return response.data;
    },

    // Confirmer une commande
    confirmOrder: async (id) => {
        const response = await api.put(`/commandes/${id}/confirmer`);
        return response.data;
    },

    // Annuler une commande
    cancelOrder: async (id) => {
        const response = await api.put(`/commandes/${id}/annuler`);
        return response.data;
    },

    // Récupérer une commande par ID
    getOrderById: async (id) => {
        const response = await api.get(`/commandes/${id}`);
        return response.data;
    },

    // Commandes d'un acheteur
    getBuyerOrders: async (buyerId) => {
        const response = await api.get(`/commandes/acheteur/${buyerId}`);
        return response.data;
    },

    // Commandes d'un vendeur
    getSellerOrders: async (sellerId) => {
        const response = await api.get(`/commandes/vendeur/${sellerId}`);
        return response.data;
    },

    // Statistiques des commandes (Admin)
    getStats: async (startDate, endDate) => {
        const response = await api.get(`/commandes/statistiques?debut=${startDate}&fin=${endDate}`);
        return response.data;
    },

    // Vente Manuelle (Dashboard Vendeur)
    createManualSale: async (saleData) => {
        // saleData: { vendeurId, produitId, quantite, prix }
        const response = await api.post('/commandes/manuelle', saleData);
        return response.data;
    },

    // Modifier une commande (Modification/Annulation dynamique)
    updateOrder: async (id, orderData) => {
        const response = await api.put(`/commandes/${id}`, orderData);
        return response.data;
    }
};

export default orderService;
