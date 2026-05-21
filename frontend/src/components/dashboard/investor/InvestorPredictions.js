import React from 'react';
import { TrendingUp, BarChart2 } from 'lucide-react';
import './InvestorDashboard.css';

const InvestorPredictions = ({ predictions }) => {
    const hasData = predictions?.categoriesEnForteCroissance?.length > 0;

    return (
        <div className="investor-card-panel">
            <div className="card-header-icon">
                <TrendingUp size={20} className="text-green" />
                <h3>Prédictions de Croissance</h3>
            </div>

            {hasData ? (
                <div className="prediction-list">
                    {predictions.categoriesEnForteCroissance.map((categorie, idx) => {
                        const taux = predictions.tauxCroissanceParCategorie?.[categorie] || 0;
                        return (
                            <div key={idx} className="prediction-item">
                                <div className="flex items-center gap-3">
                                    <BarChart2 size={18} className="text-gray-500" />
                                    <div>
                                        <p className="font-semibold text-gray-800 text-sm">{categorie}</p>
                                        <p className="text-xs text-gray-500">Tendance positive</p>
                                    </div>
                                </div>
                                <div className="text-right">
                                    <p className="text-lg font-bold text-green-600">+{taux.toFixed(1)}%</p>
                                </div>
                            </div>
                        );
                    })}
                </div>
            ) : (
                <div className="flex flex-col items-center justify-center h-full text-gray-400">
                    <TrendingUp size={40} className="mb-2 opacity-50" />
                    <p>Aucune donnée prévisionnelle</p>
                </div>
            )}
        </div>
    );
};

export default InvestorPredictions;
