import React from 'react';
import { Doughnut, Bar } from 'react-chartjs-2';
import { PieChart, BarChart2 } from 'lucide-react';

const InvestorCharts = ({ doughnutData, barData }) => {
    const doughnutOptions = {
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'bottom',
                labels: {
                    font: { size: 11, weight: '600' },
                    color: '#64748b',
                    padding: 12
                }
            }
        }
    };

    const barOptions = {
        maintainAspectRatio: false,
        plugins: {
            legend: { display: false }
        },
        scales: {
            y: {
                beginAtZero: true,
                grid: { color: '#f1f5f9' }
            },
            x: {
                grid: { display: false }
            }
        }
    };

    return (
        <div className="investor-card-panel">
            {/* Mix Produits */}
            <div style={{ marginBottom: '2rem' }}>
                <div className="card-header-icon">
                    <PieChart size={22} style={{ color: '#3b82f6' }} />
                    <h3>Mix Produits par Catégorie</h3>
                </div>
                <div className="chart-container-investor">
                    <Doughnut data={doughnutData} options={doughnutOptions} />
                </div>
            </div>

            {/* Contribution CA */}
            <div style={{ marginTop: '2rem', paddingTop: '2rem', borderTop: '2px solid #f1f5f9' }}>
                <div className="card-header-icon">
                    <BarChart2 size={22} style={{ color: '#10b981' }} />
                    <h3>Contribution au Chiffre d'Affaire</h3>
                </div>
                <div className="chart-container-investor">
                    <Bar data={barData} options={barOptions} />
                </div>
            </div>
        </div>
    );
};

export default InvestorCharts;
