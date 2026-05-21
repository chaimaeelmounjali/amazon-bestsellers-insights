import api from '../api/axiosConfig';

const predictionService = {
    // Prédire le rang futur
    getRankPrediction: async (productId) => {
        const response = await api.get(`/analyse-predictive/position-future/${productId}`);
        return response.data;
    },

    // Recommander un prix idéal
    getPriceRecommendation: async (productId) => {
        const response = await api.get(`/analyse-predictive/prix-ideal/${productId}`);
        return response.data;
    }
};

export default predictionService;
