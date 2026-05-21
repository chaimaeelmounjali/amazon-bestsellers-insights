import React from 'react';
import { Zap } from 'lucide-react';
import './InvestorDashboard.css';

const InvestorPotentialProduct = ({ products }) => {
    if (!products || products.length === 0) return null;

    return (
        <div className="mt-8">
            <div className="glass-panel p-6">
                <div className="card-header-icon mb-4">
                    <Zap size={20} className="text-yellow-500" />
                    <h3>⚡ Produits à Fort Potentiel</h3>
                </div>

                <div className="potential-products-grid">
                    {products.slice(0, 6).map((produit, idx) => (
                        <div key={idx} className="potential-product-card">
                            <img
                                src={produit.urlImage}
                                alt={produit.nom}
                                className="w-12 h-12 object-cover rounded shadow-sm flex-shrink-0"
                                onError={(e) => e.target.src = 'https://placehold.co/64?text=Produit'}
                            />
                            <div className="flex-1 overflow-hidden">
                                <p className="font-semibold text-xs text-gray-800 truncate" title={produit.nom}>
                                    {produit.nom}
                                </p>
                                <div className="flex items-center justify-between mt-1">
                                    <span className="text-[10px] bg-green-100 text-green-700 px-1.5 py-0.5 rounded">
                                        +{produit.croissance}
                                    </span>
                                    <span className="text-[10px] text-gray-600 font-bold">
                                        {produit.prix} €
                                    </span>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default InvestorPotentialProduct;
