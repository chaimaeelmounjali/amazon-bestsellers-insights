import api from '../api/axiosConfig';

const reviewService = {
    // Ajouter un avis
    addReview: async (reviewData) => {
        const response = await api.post('/avis', reviewData);
        return response.data;
    },

    // Modifier un avis
    updateReview: async (id, reviewData) => {
        const response = await api.put(`/avis/${id}`, reviewData);
        return response.data;
    },

    // Supprimer un avis
    deleteReview: async (id) => {
        await api.delete(`/avis/${id}`);
    },

    // Marquer comme utile
    markAsHelpful: async (id) => {
        const response = await api.put(`/avis/${id}/utile`);
        return response.data;
    },

    // Avis d'un produit
    getProductReviews: async (productId) => {
        const response = await api.get(`/avis/produit/${productId}`);
        return response.data;
    },

    // Note moyenne
    getAverageRating: async (productId) => {
        const response = await api.get(`/avis/note-moyenne/${productId}`);
        return response.data;
    }
};

export default reviewService;
