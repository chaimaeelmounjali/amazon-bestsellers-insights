import React from 'react';
import { Line, Doughnut } from 'react-chartjs-2';
import { LineChart, PieChart } from 'lucide-react';
import './SellerDashboard.css';

const SellerCharts = ({ salesData, categoryData }) => {
    const lineOptions = {
        maintainAspectRatio: false,
        responsive: true,
        plugins: {
            legend: {
                display: true,
                position: 'top',
                labels: {
                    font: { size: 12, weight: '600' },
                    color: '#64748b'
                }
            }
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

    const doughnutOptions = {
        maintainAspectRatio: false,
        responsive: true,
        plugins: {
            legend: {
                position: 'bottom',
                labels: {
                    font: { size: 11, weight: '600' },
                    color: '#64748b',
                    padding: 15
                }
            }
        }
    };

    return (
        <div className="seller-grid-section">
            {/* Sales Performance Chart */}
            <div className="seller-card-panel">
                <div className="seller-card-header">
                    <h3>
                        <LineChart size={22} style={{ color: '#3b82f6' }} />
                        Performance des Ventes
                    </h3>
                </div>
                <div className="chart-container-large">
                    {salesData ? (
                        <Line data={salesData} options={lineOptions} />
                    ) : (
                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: '#94a3b8' }}>
                            Chargement des données...
                        </div>
                    )}
                </div>
            </div>

            {/* Category Distribution Chart */}
            <div className="seller-card-panel">
                <div className="seller-card-header">
                    <h3>
                        <PieChart size={22} style={{ color: '#8b5cf6' }} />
                        Ventes par Catégorie
                    </h3>
                </div>
                <div className="chart-container-small">
                    {categoryData ? (
                        <Doughnut data={categoryData} options={doughnutOptions} />
                    ) : (
                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: '#94a3b8' }}>
                            Aucune donnée disponible
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default SellerCharts;
