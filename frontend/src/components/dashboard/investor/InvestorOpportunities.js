import React from 'react';
import { Zap, Award, ArrowRight } from 'lucide-react';

const InvestorOpportunities = ({ opportunities, topProducts }) => {
    return (
        <div className="investor-card-panel">
            <div className="card-header-icon">
                <Zap size={20} className="text-gold" />
                <h3>Pépites du Marché (Opportunités)</h3>
            </div>
            <ul className="opportunity-list-lux mb-6">
                {opportunities?.map((opp, index) => (
                    <li className="opp-item-lux" key={index}>
                        <div className="opp-type-badge">{opp.type.replace('_', ' ')}</div>
                        <div className="opp-content">
                            <h4>{opp.titre}</h4>
                            <p>{opp.sousTitre}</p>
                            <div className="opp-roi">ROI Potentiel: <span>{opp.roiEstime}</span></div>
                        </div>
                        <ArrowRight size={16} className="opp-arrow" />
                    </li>
                ))}
            </ul>

            <div className="card-header-icon mt-4 border-t pt-4">
                <Award size={20} className="text-purple" />
                <h3>Top Revenu par Produit</h3>
            </div>
            <div className="top-prods-list">
                {topProducts?.map((prod, idx) => (
                    <div className="prod-rank-item" key={prod.id}>
                        <span className="rank-num">#{idx + 1}</span>
                        <img
                            src={prod.urlImage}
                            alt=""
                            className="w-10 h-10 object-cover rounded"
                            onError={(e) => e.target.src = 'https://placehold.co/40'}
                        />
                        <div className="flex-1 min-w-0">
                            <p className="font-medium text-sm text-gray-800 truncate" title={prod.nom}>{prod.nom}</p>
                            <p className="text-xs text-blue-600 font-bold">{prod.revenu?.toLocaleString()} €</p>
                        </div>
                        <div className="rev-bar-mini" style={{ width: `${(prod.revenu / (topProducts[0]?.revenu || 1)) * 60}px` }}></div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default InvestorOpportunities;
