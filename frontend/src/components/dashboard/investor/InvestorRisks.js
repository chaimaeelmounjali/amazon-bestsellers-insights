import React from 'react';
import { Award } from 'lucide-react';

const InvestorRisks = ({ analysis }) => {
    return (
        <div className="mt-8 mb-12">
            <div className="glass-panel p-6">
                <div className="card-header-icon mb-4">
                    <Award size={20} className="text-red-500" />
                    <h3>🛡️ Analyse de Risque & Sécurité</h3>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {/* Niveau de Risque Global */}
                    <div className="p-4 rounded-lg border-2 flex flex-col justify-center" style={{
                        borderColor: analysis?.niveauRisqueGlobal === 'FAIBLE' ? '#10b981' :
                            analysis?.niveauRisqueGlobal === 'MODÉRÉ' ? '#f59e0b' : '#ef4444',
                        backgroundColor: analysis?.niveauRisqueGlobal === 'FAIBLE' ? '#f0fdf4' :
                            analysis?.niveauRisqueGlobal === 'MODÉRÉ' ? '#fffbeb' : '#fef2f2'
                    }}>
                        <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-1">Niveau Risque</p>
                        <p className="text-3xl font-black" style={{
                            color: analysis?.niveauRisqueGlobal === 'FAIBLE' ? '#10b981' :
                                analysis?.niveauRisqueGlobal === 'MODÉRÉ' ? '#f59e0b' : '#ef4444'
                        }}>
                            {analysis?.niveauRisqueGlobal || 'FAIBLE'}
                        </p>
                    </div>

                    {/* Opportunités Sûres */}
                    <div className="p-4 bg-green-50 rounded-lg border border-green-200 shadow-inner">
                        <p className="text-xs font-semibold text-green-700 uppercase tracking-wider mb-3">✅ Opportunités Sûres</p>
                        {analysis?.opportunitesSures?.length > 0 ? (
                            <ul className="space-y-2">
                                {analysis.opportunitesSures.slice(0, 3).map((produit, idx) => (
                                    <li key={idx} className="text-xs text-gray-700 flex items-center gap-2">
                                        <span className="w-1.5 h-1.5 rounded-full bg-green-500"></span>
                                        <span className="truncate">{produit}</span>
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p className="text-xs text-gray-500 italic">Aucune opportunité identifiée</p>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default InvestorRisks;
