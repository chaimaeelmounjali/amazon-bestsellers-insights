import { useState } from 'react';
import searchService from '../services/searchService';

const useSearch = () => {
    const [results, setResults] = useState([]);
    const [trends, setTrends] = useState([]);
    const [suggestions, setSuggestions] = useState([]);
    const [loading, setLoading] = useState(false);

    const filterProducts = async (filters) => {
        setLoading(true);
        try {
            const data = await searchService.filterProducts(filters);
            setResults(data);
        } catch (error) {
            console.error("Erreur filtrage", error);
        } finally {
            setLoading(false);
        }
    };

    const fetchTrends = async () => {
        try {
            const data = await searchService.analyzeTrends();
            setTrends(data);
        } catch (error) {
            console.error(error);
        }
    };

    const fetchSuggestions = async (input) => {
        if (!input) {
            setSuggestions([]);
            return;
        }
        try {
            const data = await searchService.suggestKeywords(input);
            setSuggestions(data);
        } catch (error) {
            console.error(error);
        }
    };

    return {
        results,
        trends,
        suggestions,
        loading,
        filterProducts,
        fetchTrends,
        fetchSuggestions
    };
};

export default useSearch;
