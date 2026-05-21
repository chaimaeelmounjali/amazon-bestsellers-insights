import api from '../api/axiosConfig';

const productService = {
    // Récupérer tous les produits
    getAllProducts: async () => {
        const response = await api.get('/produits');
        return response.data;
    },

    // Récupérer par ID
    getProductById: async (id) => {
        const response = await api.get(`/produits/${id}`);
        return response.data;
    },

    // Recherche
    searchProducts: async (keyword) => {
        const response = await api.get(`/produits/recherche?keyword=${keyword}`);
        return response.data;
    },

    // Filtrer
    filterProducts: async (filters) => {
        // Construit la query string (ex: ?categorie=Livre&minPrix=10)
        const params = new URLSearchParams(filters).toString();
        const response = await api.get(`/filtrage/produits?${params}`);
        return response.data;
    },

    // Dashboard Statistiques
    getStats: async () => {
        const response = await api.get('/produits/statistiques');
        return response.data;
    }
};

export default productService;
