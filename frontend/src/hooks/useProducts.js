import { useState, useCallback } from 'react';
import productService from '../services/productService';

const useProducts = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const fetchAllProducts = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await productService.getAllProducts();
            setProducts(data);
        } catch (err) {
            setError(err.message || 'Erreur lors du chargement des produits');
        } finally {
            setLoading(false);
        }
    }, []);

    const searchProducts = async (keyword) => {
        setLoading(true);
        try {
            const data = await productService.searchProducts(keyword);
            setProducts(data);
        } catch (err) {
            setError(err.message || 'Erreur lors de la recherche');
        } finally {
            setLoading(false);
        }
    };

    const getStats = async () => {
        try {
            return await productService.getStats();
        } catch (err) {
            console.error("Erreur stats", err);
            return null;
        }
    };

    return {
        products,
        loading,
        error,
        fetchAllProducts,
        searchProducts,
        getStats
    };
};

export default useProducts;
