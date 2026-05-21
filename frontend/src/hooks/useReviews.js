import { useState, useCallback } from 'react';
import reviewService from '../services/reviewService';

const useReviews = () => {
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const fetchProductReviews = useCallback(async (productId) => {
        setLoading(true);
        try {
            const data = await reviewService.getProductReviews(productId);
            setReviews(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, []);

    const addReview = async (reviewData) => {
        try {
            const newReview = await reviewService.addReview(reviewData);
            setReviews(prev => [newReview, ...prev]);
            return newReview;
        } catch (err) {
            setError(err.message);
            throw err;
        }
    };

    const markHelpful = async (id) => {
        try {
            const updatedReview = await reviewService.markAsHelpful(id);
            setReviews(prev => prev.map(r => r.id === id ? updatedReview : r));
        } catch (err) {
            console.error(err);
        }
    };

    return {
        reviews,
        loading,
        error,
        fetchProductReviews,
        addReview,
        markHelpful
    };
};

export default useReviews;
