import React from 'react';
import { Briefcase, TrendingUp, DollarSign } from 'lucide-react';

const InvestorStats = ({ user }) => {
    return (
        <div className="investor-stats-grid">
            <div className="investor-stat-card">
                <div className="seller-stat-icon" style={{
                    background: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
                    color: 'white'
                }}>
                    <Briefcase size={28} />
                </div>
                <div className="seller-stat-content">
                    <h4>Portefeuille</h4>
                    <p className="stat-value">{user?.montantInvestissement?.toLocaleString() || '0'} €</p>
                    <span className="stat-subtitle">Capital investi</span>
                </div>
            </div>

            <div className="investor-stat-card">
                <div className="seller-stat-icon" style={{
                    background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
                    color: 'white'
                }}>
                    <TrendingUp size={28} />
                </div>
                <div className="seller-stat-content">
                    <h4>ROI Global</h4>
                    <p className="stat-value">{user?.roi || '0'}%</p>
                    <span className="stat-subtitle">Performance annuelle</span>
                </div>
            </div>

            <div className="investor-stat-card">
                <div className="seller-stat-icon" style={{
                    background: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)',
                    color: 'white'
                }}>
                    <DollarSign size={28} />
                </div>
                <div className="seller-stat-content">
                    <h4>Rendement Estimé</h4>
                    <p className="stat-value">
                        {user?.montantInvestissement && user?.roi
                            ? (user.montantInvestissement * (user.roi / 100)).toLocaleString()
                            : '0'} €
                    </p>
                    <span className="stat-subtitle">Dividendes projetés</span>
                </div>
            </div>
        </div>
    );
};

export default InvestorStats;
