import { useState, useCallback } from 'react';
import orderService from '../services/orderService';

const useOrders = () => {
    const [orders, setOrders] = useState([]);
    const [currentOrder, setCurrentOrder] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // Récupérer les commandes d'un acheteur
    const fetchBuyerOrders = useCallback(async (buyerId) => {
        setLoading(true);
        try {
            const data = await orderService.getBuyerOrders(buyerId);
            setOrders(data);
        } catch (err) {
            setError(err.message || 'Erreur chargement commandes');
        } finally {
            setLoading(false);
        }
    }, []);

    // Créer une commande
    const createOrder = async (orderData) => {
        setLoading(true);
        try {
            const newOrder = await orderService.createOrder(orderData);
            setOrders(prev => [...prev, newOrder]);
            return newOrder;
        } catch (err) {
            setError(err.message || 'Erreur création commande');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    // Actions sur commande (confirmer/annuler)
    const updateOrderStatus = async (id, action) => {
        try {
            let updatedOrder;
            if (action === 'confirm') updatedOrder = await orderService.confirmOrder(id);
            if (action === 'cancel') updatedOrder = await orderService.cancelOrder(id);

            // Mettre à jour la liste locale
            setOrders(prev => prev.map(o => o.id === id ? updatedOrder : o));
            return updatedOrder;
        } catch (err) {
            setError(err.message);
        }
    };

    return {
        orders,
        currentOrder,
        loading,
        error,
        fetchBuyerOrders,
        createOrder,
        updateOrderStatus
    };
};

export default useOrders;
