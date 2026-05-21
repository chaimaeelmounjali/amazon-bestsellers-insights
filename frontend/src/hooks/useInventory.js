import { useState, useCallback } from 'react';
import stockService from '../services/stockService';

const useInventory = () => {
    const [alerts, setAlerts] = useState([]);
    const [loading, setLoading] = useState(false);

    const updateStock = async (productId, quantity) => {
        try {
            await stockService.updateStock(productId, quantity);
            return true;
        } catch (error) {
            console.error(error);
            return false;
        }
    };

    const fetchUserAlerts = useCallback(async (userId) => {
        setLoading(true);
        try {
            const data = await stockService.getUserAlerts(userId);
            setAlerts(data);
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    }, []);

    const createAlert = async (alertData) => {
        try {
            const newAlert = await stockService.createAlert(alertData);
            setAlerts(prev => [...prev, newAlert]);
        } catch (error) {
            console.error(error);
        }
    };

    return {
        alerts,
        loading,
        updateStock,
        fetchUserAlerts,
        createAlert
    };
};

export default useInventory;
