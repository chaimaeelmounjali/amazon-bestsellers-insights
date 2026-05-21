import React from 'react';
import { DollarSign, Package } from 'lucide-react';

const SellerStats = ({ stats }) => {
    return (
        <div className="seller-stats-grid">
            <div className="seller-stat-card">
                <div className="seller-stat-icon" style={{
                    background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
                    color: 'white'
                }}>
                    <DollarSign size={28} />
                </div>
                <div className="seller-stat-content">
                    <h4>Revenu Mensuel</h4>
                    <p className="stat-value">{stats.revenuTotal?.toLocaleString() || 0} €</p>
                    <span className="stat-subtitle">Chiffre d'affaires du mois</span>
                </div>
            </div>

            <div className="seller-stat-card">
                <div className="seller-stat-icon" style={{
                    background: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
                    color: 'white'
                }}>
                    <Package size={28} />
                </div>
                <div className="seller-stat-content">
                    <h4>Ventes du Mois</h4>
                    <p className="stat-value">{stats.totalVentes || 0}</p>
                    <span className="stat-subtitle">Commandes traitées</span>
                </div>
            </div>
        </div>
    );
};

export default SellerStats;
