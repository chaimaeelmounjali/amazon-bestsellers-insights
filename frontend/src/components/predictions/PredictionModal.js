import React, { useState, useEffect } from 'react';
import { X, TrendingUp, DollarSign, AlertCircle } from 'lucide-react';
import predictionService from '../../services/predictionService';

const PredictionModal = ({ isOpen, onClose, product }) => {
    const [loading, setLoading] = useState(false);
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        const loadPredictions = async () => {
            setLoading(true);
            setError(null);
            setData(null);
            try {
                const [rankData, priceData] = await Promise.all([
                    predictionService.getRankPrediction(product.id),
                    predictionService.getPriceRecommendation(product.id)
                ]);
                setData({ rank: rankData, price: priceData });
            } catch (err) {
                console.error("Prediction Error:", err);
                setError("Impossible de charger les prédictions. Le service est peut-être indisponible.");
            } finally {
                setLoading(false);
            }
        };

        if (isOpen && product) {
            loadPredictions();
        }
    }, [isOpen, product]);

    if (!isOpen) return null;

    return (
        <div className="modal-overlay">
            <div className="modal-content glass-panel fade-in" style={{ background: 'white', padding: '2rem', borderRadius: '12px', width: '500px', maxWidth: '90%' }}>
                <div className="flex justify-between items-center mb-6">
                    <h3 className="text-xl font-bold flex items-center gap-2">
                        🔮 Prédictions IA & Stratégie
                    </h3>
                    <button onClick={onClose} className="p-1 hover:bg-gray-100 rounded-full transition-colors">
                        <X size={20} />
                    </button>
                </div>

                {/* Error Message - Always at the top */}
                {error && (
                    <div className="mb-6 p-4 bg-red-50 text-red-700 border border-red-200 rounded-lg flex items-start gap-3 shadow-sm">
                        <AlertCircle size={20} className="mt-1 shrink-0" />
                        <div>
                            <h4 className="font-bold text-sm">Erreur</h4>
                            <p className="text-sm">{error}</p>
                        </div>
                    </div>
                )}

                {loading ? (
                    <div className="p-8 text-center">
                        <div className="animate-pulse mb-2 text-4xl">🧠</div>
                        <p className="text-gray-500">Analyse de <span className="font-semibold">{product?.nom}</span> en cours...</p>
                    </div>
                ) : !error && data ? (
                    <div className="prediction-results space-y-6">
                        {/* Rank Prediction */}
                        <div className="p-5 bg-gradient-to-br from-blue-50 to-indigo-50 rounded-xl border border-blue-100">
                            <h4 className="flex items-center gap-2 font-bold mb-3 text-blue-900">
                                <TrendingUp size={18} className="text-blue-600" /> Tendance de Classement
                            </h4>
                            <div className="flex justify-between items-end mb-2">
                                <div>
                                    <div className="text-sm text-gray-500 mb-1">Actuel</div>
                                    <div className="font-mono text-2xl font-bold">#{data.rank.rangActuel}</div>
                                </div>
                                <div className="text-2xl text-blue-300 mb-1">→</div>
                                <div className="text-right">
                                    <div className="text-sm text-gray-500 mb-1">Projection (30j)</div>
                                    <div className={`font-mono text-2xl font-bold ${data.rank.tendance.includes('HAUSSE') ? 'text-green-600' : 'text-red-500'}`}>
                                        #{data.rank.rangPredit}
                                    </div>
                                </div>
                            </div>
                            <div className="pt-3 border-t border-blue-100 text-sm text-blue-800 flex justify-between">
                                <span>Tendance: <strong>{data.rank.tendance}</strong></span>
                                <span className="text-blue-600">Confiance: {data.rank.confiance}%</span>
                            </div>
                        </div>

                        {/* Price Recommendation */}
                        <div className="p-5 bg-gradient-to-br from-purple-50 to-fuchsia-50 rounded-xl border border-purple-100">
                            <h4 className="flex items-center gap-2 font-bold mb-3 text-purple-900">
                                <DollarSign size={18} className="text-purple-600" /> Stratégie Prix
                            </h4>
                            <div className="grid grid-cols-2 gap-4 mb-4">
                                <div className="bg-white/50 p-3 rounded-lg">
                                    <div className="text-sm text-gray-500">Prix Actuel</div>
                                    <div className="font-bold text-lg">{data.price.prixActuel} €</div>
                                </div>
                                <div className="bg-white p-3 rounded-lg border border-purple-100 shadow-sm">
                                    <div className="text-sm text-purple-600 font-medium">Prix Conseillé</div>
                                    <div className="font-bold text-lg text-purple-700">{data.price.prixRecommande} €</div>
                                </div>
                            </div>
                            <div className="text-sm text-gray-700 italic mb-3 bg-white/40 p-3 rounded border border-purple-100">
                                "{data.price.justification}"
                            </div>
                            <div className="text-xs bg-purple-100 text-purple-800 p-2 rounded inline-block font-semibold">
                                Stratégie {data.price.strategie} • Impact estimé: {data.price.impactEstime}
                            </div>
                        </div>
                    </div>
                ) : null}
            </div>
        </div>
    );
};

export default PredictionModal;
