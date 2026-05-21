import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, TrendingUp, DollarSign, AlertCircle, Package } from 'lucide-react';
import predictionService from '../../services/predictionService';
import ProductService from '../../services/product.service';

const PredictionPage = () => {
    console.log("PredictionPage: Component Rendering...");
    const { id } = useParams();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [data, setData] = useState(null);
    const [product, setProduct] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        console.log("PredictionPage: Mount Effect Triggered");
        const loadData = async () => {
            setLoading(true);
            setError(null);
            console.log("Loading data for product:", id);
            try {
                // Fetch product details first
                const productData = await ProductService.getProductById(id);
                console.log("Product data received:", productData);
                setProduct(productData);

                if (!productData) {
                    throw new Error("Produit non trouvé");
                }

                // Then fetch predictions
                const [rankData, priceData] = await Promise.all([
                    predictionService.getRankPrediction(id),
                    predictionService.getPriceRecommendation(id)
                ]);
                console.log("Prediction data received:", { rank: rankData, price: priceData });
                setData({ rank: rankData, price: priceData });
            } catch (err) {
                console.error("Prediction Page Error:", err);
                setError("Impossible de charger les données. Le service est peut-être indisponible.");
            } finally {
                setLoading(false);
            }
        };

        if (id) {
            loadData();
        }
    }, [id]);

    if (loading) {
        return (
            <div className="container py-8 flex items-center justify-center min-h-[60vh] bg-white text-black border border-red-500">
                <div className="text-center">
                    <div className="animate-pulse mb-4 text-6xl">🧠</div>
                    <p className="text-xl text-gray-600">Analyse IA en cours (Debug Mode)...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="container py-8 fade-in">
                <button
                    onClick={() => navigate('/seller/products')}
                    className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-6 transition-colors"
                >
                    <ArrowLeft size={20} /> Retour à l'inventaire
                </button>
                <div className="p-6 bg-red-50 text-red-700 border border-red-200 rounded-xl flex items-center gap-4 shadow-sm">
                    <AlertCircle size={24} />
                    <div>
                        <h3 className="font-bold text-lg">Erreur</h3>
                        <p>{error}</p>
                    </div>
                </div>
            </div>
        );
    }

    // Guard against null data before rendering main content
    if (!data) {
        return (
            <div className="container py-8 fade-in text-center">
                <button
                    onClick={() => navigate('/seller/products')}
                    className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-6 mx-auto transition-colors"
                >
                    <ArrowLeft size={20} /> Retour à l'inventaire
                </button>
                <p className="text-gray-500">Aucune donnée de prédiction disponible pour ce produit.</p>
                <div className="mt-4 p-4 bg-gray-100 rounded text-left text-xs font-mono overflow-auto max-w-lg mx-auto">
                    DEBUG STATE:
                    <br />Loading: {loading ? 'true' : 'false'}
                    <br />Error: {error || 'null'}
                    <br />Product: {product ? 'Found' : 'Null'}
                    <br />Data: {data ? 'Found' : 'Null'}
                </div>
            </div>
        );
    }

    return (
        <div className="container py-8 fade-in max-w-5xl mx-auto">
            {/* Header / Navigation */}
            <div className="mb-8">
                <button
                    onClick={() => navigate('/seller/products')}
                    className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-4 transition-colors"
                >
                    <ArrowLeft size={20} /> Retour à l'inventaire
                </button>

                <div className="flex items-start gap-6">
                    <div className="w-24 h-24 rounded-lg bg-gray-100 flex items-center justify-center overflow-hidden shrink-0 border border-gray-200 shadow-sm">
                        {product?.urlImage ? (
                            <img src={product.urlImage} alt="" className="object-cover w-full h-full" />
                        ) : (
                            <Package className="text-gray-400" size={40} />
                        )}
                    </div>
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900 mb-2">{product?.nom}</h1>
                        <div className="flex gap-3">
                            <span className="px-3 py-1 bg-blue-50 text-blue-700 rounded-full text-sm font-medium">
                                {product?.categorie}
                            </span>
                            <span className="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm font-mono">
                                {product?.asin}
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            {/* Main Content Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

                {/* Section Rang & Tendance */}
                <div className="glass-panel p-6 border-t-4 border-blue-500 shadow-lg hover:shadow-xl transition-shadow duration-300">
                    <h2 className="text-2xl font-bold text-gray-800 mb-6 flex items-center gap-3">
                        <div className="p-2 bg-blue-100 rounded-lg text-blue-600">
                            <TrendingUp size={24} />
                        </div>
                        Tendance de Classement
                    </h2>

                    <div className="flex items-end justify-between mb-8 p-4 bg-gray-50 rounded-xl border border-gray-100">
                        <div>
                            <div className="text-gray-500 text-sm font-medium uppercase tracking-wide mb-1">Rang Actuel</div>
                            <div className="text-4xl font-black text-gray-800">#{data?.rank.rangActuel}</div>
                        </div>
                        <div className="text-3xl text-gray-300 mb-2">→</div>
                        <div className="text-right">
                            <div className="text-gray-500 text-sm font-medium uppercase tracking-wide mb-1">Projection (30j)</div>
                            <div className={`text-4xl font-black ${data?.rank?.tendance?.includes('HAUSSE') ? 'text-green-600' : 'text-red-500'}`}>
                                #{data?.rank?.rangPredit}
                            </div>
                        </div>
                    </div>

                    <div className="space-y-4">
                        <div className="flex justify-between items-center p-3 hover:bg-gray-50 rounded-lg transition-colors">
                            <span className="text-gray-600">Tendance détectée</span>
                            <span className="font-bold text-gray-900">{data?.rank?.tendance?.replace('_', ' ')}</span>
                        </div>
                        <div className="flex justify-between items-center p-3 hover:bg-gray-50 rounded-lg transition-colors">
                            <span className="text-gray-600">Niveau de confiance</span>
                            <span className="font-bold text-blue-600">{data?.rank?.confiance}%</span>
                        </div>
                        <div className="mt-4 pt-4 border-t border-gray-100">
                            <h4 className="font-semibold text-gray-700 mb-2">Facteurs Clés</h4>
                            <div className="grid grid-cols-2 gap-2 text-sm">
                                {Object.entries(data?.rank?.facteurs || {}).map(([key, value]) => (
                                    <div key={key} className="flex justify-between text-gray-600 bg-gray-50 p-2 rounded">
                                        <span className="capitalize">{key}</span>
                                        <span className="font-mono">{typeof value === 'number' ? value.toFixed(1) : value}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>

                {/* Section Stratégie Prix */}
                <div className="glass-panel p-6 border-t-4 border-purple-500 shadow-lg hover:shadow-xl transition-shadow duration-300">
                    <h2 className="text-2xl font-bold text-gray-800 mb-6 flex items-center gap-3">
                        <div className="p-2 bg-purple-100 rounded-lg text-purple-600">
                            <DollarSign size={24} />
                        </div>
                        Stratégie Prix Optimale
                    </h2>

                    <div className="grid grid-cols-2 gap-6 mb-8">
                        <div className="p-4 bg-white border border-gray-200 rounded-xl shadow-sm">
                            <div className="text-gray-500 text-sm font-medium uppercase tracking-wide mb-1">Prix Actuel</div>
                            <div className="text-3xl font-bold text-gray-800">{data?.price.prixActuel} €</div>
                        </div>
                        <div className="p-4 bg-purple-50 border border-purple-200 rounded-xl shadow-sm relative overflow-hidden">
                            <div className="absolute top-0 right-0 p-1 bg-purple-200 rounded-bl-lg">
                                <span className="text-xs font-bold text-purple-800 px-1">IA</span>
                            </div>
                            <div className="text-purple-700 text-sm font-medium uppercase tracking-wide mb-1">Prix Conseillé</div>
                            <div className="text-3xl font-bold text-purple-700">{data?.price.prixRecommande} €</div>
                        </div>
                    </div>

                    <div className="bg-gradient-to-r from-purple-50 to-pink-50 p-4 rounded-xl border border-purple-100 mb-6">
                        <h4 className="font-bold text-purple-900 mb-2 flex items-center gap-2">
                            <span className="text-xl">💡</span> Recommandation
                        </h4>
                        <p className="text-gray-800 italic">"{data?.price.justification}"</p>
                    </div>

                    <div className="space-y-3 text-sm">
                        <div className="flex justify-between items-center border-b border-gray-100 pb-2">
                            <span className="text-gray-600">Stratégie identifiée</span>
                            <span className="font-bold px-2 py-1 bg-gray-100 rounded text-gray-800">{data?.price.strategie}</span>
                        </div>
                        <div className="flex justify-between items-center border-b border-gray-100 pb-2">
                            <span className="text-gray-600">Impact estimé sur les ventes</span>
                            <span className="font-bold text-green-600">{data?.price.impactEstime}</span>
                        </div>
                        <div className="flex justify-between items-center pt-1">
                            <span className="text-gray-600">Prix Moyen du Marché</span>
                            <span className="font-mono text-gray-500">{data?.price.prixMoyen} €</span>
                        </div>
                    </div>
                </div>
            </div>

            {/* Disclaimer Footer */}
            <div className="mt-8 text-center text-xs text-gray-400 max-w-2xl mx-auto">
                Ces prédictions sont générées par une intelligence artificielle basée sur des données historiques.
                Les performances passées ne préjugent pas des résultats futurs.
            </div>
        </div>
    );
};

export default PredictionPage;
