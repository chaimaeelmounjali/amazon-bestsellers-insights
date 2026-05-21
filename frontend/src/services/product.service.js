import api from '../api/axiosConfig';

const ProductService = {
    // --- Produits ---
    getAllProducts: async () => {
        const response = await api.get('/produits');
        return response.data;
    },

    getTopProducts: async (limit = 10) => {
        const response = await api.get(`/produits/top?limit=${limit}`);
        return response.data;
    },

    getProductById: async (id) => {
        const response = await api.get(`/produits/${id}`);
        return response.data;
    },

    getProductByAsin: async (asin) => {
        const response = await api.get(`/produits/asin/${asin}`);
        return response.data;
    },

    // --- Filtrage & Recherche ---
    getCategories: async () => {
        const response = await api.get('/filtrage/categories');
        return response.data;
    },

    getPriceRange: async () => {
        const response = await api.get('/filtrage/prix-range');
        return response.data;
    },

    filterProducts: async (filters) => {
        // filtres: { categories: [], minPrix, maxPrix, minNote, minAvis }
        const response = await api.post('/filtrage/produits', filters);
        return response.data;
    },

    searchProducts: async (keyword) => {
        // Recherche Sémantique (Plus avancé que le simple LIKE)
        const response = await api.get(`/recherche/semantique?query=${keyword}`);
        return response.data;
    },

    getSuggestions: async (partialQuery) => {
        const response = await api.get(`/recherche/suggestions?partialQuery=${partialQuery}`);
        return response.data;
    },

    // --- Statistiques & Dashboard ---
    getGlobalStats: async () => {
        const response = await api.get('/produits/statistiques');
        return response.data;
    },

    getCategoryAnalysis: async (category) => {
        const response = await api.get(`/produits/analyse-categorie?categorie=${encodeURIComponent(category)}`);
        return response.data;
    },

    // --- Analyse Prédictive (IA) ---
    getFutureBestsellers: async () => {
        const response = await api.get('/analyse-predictive/futurs-bestsellers');
        return response.data;
    },

    getFutureRankPrediction: async (productId) => {
        const response = await api.get(`/analyse-predictive/position-future/${productId}`);
        return response.data;
    },

    getIdealPriceRecommendation: async (productId) => {
        const response = await api.get(`/analyse-predictive/prix-ideal/${productId}`);
        return response.data;
    },

    // --- Gestion des produits (CRUD) ---
    addProduct: async (productData) => {
        const response = await api.post('/produits', productData);
        return response.data;
    },

    updateProduct: async (id, productData) => {
        const response = await api.put(`/produits/${id}`, productData);
        return response.data;
    },

    deleteProduct: async (id) => {
        await api.delete(`/produits/${id}`);
    },

    // --- Ventes ---
    getSalesStats: async (startDate, endDate) => {
        const response = await api.get(`/ventes/statistiques?dateDebut=${startDate}&dateFin=${endDate}`);
        return response.data;
    },


    getSellerAnalytics: async (sellerId) => {
        const response = await api.get(`/ventes/vendeur/${sellerId}/analyse`);
        return response.data;
    },

    getSellerDashboard: async (sellerId) => {
        const response = await api.get(`/dashboard/vendeur/${sellerId}`);
        return response.data;
    },

    getAdminDashboard: async () => {
        const response = await api.get('/dashboard/admin');
        return response.data;
    },

    // --- Isolation Vendeur & Dashboard ---
    getProductsBySeller: async (sellerId) => {
        const response = await api.get(`/produits/vendeur/${sellerId}`);
        return response.data;
    },

    // --- Dashboard Investisseur ---
    getInvestorDashboard: async (investorId) => {
        const response = await api.get(`/dashboard/investisseur/${investorId}`);
        return response.data;
    },

    // --- Dashboard Acheteur ---
    getBuyerDashboardMe: async () => {
        const response = await api.get('/dashboard/acheteur/me');
        return response.data;
    },

    getBuyerDashboard: async (buyerId) => {
        const response = await api.get(`/dashboard/acheteur/${buyerId}`);
        return response.data;
    }
};

export default ProductService;
