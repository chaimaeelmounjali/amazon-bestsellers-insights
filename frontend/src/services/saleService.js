import api from '../api/axiosConfig';

const saleService = {
    // Créer une vente
    createSale: async (saleData) => {
        const response = await api.post('/ventes', saleData);
        return response.data;
    },

    // Récupérer les ventes d'un vendeur
    getSalesBySeller: async (sellerId) => {
        const response = await api.get(`/ventes/vendeur/${sellerId}`);
        return response.data;
    },

    // Récupérer une vente par ID
    getSaleById: async (id) => {
        const response = await api.get(`/ventes/${id}`);
        return response.data;
    }
};

export default saleService;
