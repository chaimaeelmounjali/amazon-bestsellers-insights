import React from 'react';
import { DollarSign } from 'lucide-react';
import './InvestorDashboard.css';

const InvestorProjections = ({ projections }) => {
    return (
        <div className="investor-card-panel">
            <div className="card-header-icon">
                <DollarSign size={20} className="text-blue" />
                <h3>Projections de Revenus</h3>
            </div>

            <div className="projections-grid">
                <div className="projection-card blue">
                    <p className="text-xs text-gray-600 mb-1">Revenu Moyen/Jour</p>
                    <p className="text-xl font-bold text-blue-600">
                        {projections?.revenuMoyenJour?.toLocaleString() || '0'} €
                    </p>
                </div>

                <div className="projection-card purple">
                    <p className="text-xs text-gray-600 mb-1">Projection 30 Jours</p>
                    <p className="text-xl font-bold text-purple-600">
                        {projections?.projection30Jours?.toLocaleString() || '0'} €
                    </p>
                </div>
            </div>

            <div className="mt-4 flex items-center gap-2 p-3 bg-gray-50 rounded-lg">
                <span className="text-lg">
                    {projections?.tendance === 'POSITIVE' ? '📈' : '📊'}
                </span>
                <p className="text-sm">
                    Tendance: <span className="font-semibold">{projections?.tendance || 'STABLE'}</span>
                </p>
            </div>
        </div>
    );
};

export default InvestorProjections;
