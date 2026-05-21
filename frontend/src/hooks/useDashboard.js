import { useState, useEffect } from 'react';
import statsService from '../services/statsService';

const useDashboard = () => {
    const [stats, setStats] = useState(null);
    const [predictions, setPredictions] = useState(null);
    const [loading, setLoading] = useState(false);

    const fetchGlobalStats = async () => {
        setLoading(true);
        try {
            const data = await statsService.getDashboardStats();
            setStats(data);
        } catch (error) {
            console.error("Erreur stats dashboard", error);
        } finally {
            setLoading(false);
        }
    };

    const fetchPrediction = async (productId, days) => {
        try {
            const data = await statsService.getSalesPrediction(productId, days);
            setPredictions(data);
            return data;
        } catch (error) {
            console.error("Erreur prédiction", error);
        }
    };

    return {
        stats,
        predictions,
        loading,
        fetchGlobalStats,
        fetchPrediction
    };
};

export default useDashboard;
