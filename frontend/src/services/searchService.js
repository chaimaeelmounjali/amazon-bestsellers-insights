import api from '../api/axiosConfig';

const searchService = {
    // Filtrage et Tri (FiltrageTriController)
    filterProducts: async (filters) => {
        // filters: { categorie, minPrix, maxPrix, minNote, minAvis }
        const params = new URLSearchParams(filters).toString();
        const response = await api.get(`/filtrage/produits?${params}`);
        return response.data;
    },

    // Recherche Intelligence (RechercheIntelligenceController)
    analyzeTrends: async () => {
        const response = await api.get('/recherche-intelligence/tendances');
        return response.data;
    },

    suggestKeywords: async (input) => {
        const response = await api.get(`/recherche-intelligence/mots-cles?input=${input}`);
        return response.data;
    },

    analyzeSentiment: async (productId) => {
        const response = await api.get(`/recherche-intelligence/sentiment/${productId}`);
        return response.data;
    }
};

export default searchService;
