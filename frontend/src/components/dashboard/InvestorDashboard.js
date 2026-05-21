import React, { useState, useEffect } from 'react';
import ProductService from '../../services/product.service';
import InvestorStats from './investor/InvestorStats';
import InvestorCharts from './investor/InvestorCharts';
import InvestorOpportunities from './investor/InvestorOpportunities';
import InvestorRisks from './investor/InvestorRisks';
import InvestorPredictions from './investor/InvestorPredictions';
import InvestorProjections from './investor/InvestorProjections';
import InvestorPotentialProduct from './investor/InvestorPotentialProduct';

import {
    Chart as ChartJS,
    ArcElement,
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
} from 'chart.js';

import './investor/InvestorDashboard.css';

ChartJS.register(
    ArcElement,
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend
);

const InvestorDashboard = ({ user }) => {
    const [dashboardData, setDashboardData] = useState({
        repartition: {},
        repartitionRevenu: {},
        topProduitsRevenu: [],
        categoriesPopulaires: {},
        opportunites: [],
        predictionsCroissance: {},
        projectionsRevenus: {},
        produitsFortPotentiel: [],
        analyseRisque: {}
    });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const loadData = async () => {
            try {
                const data = await ProductService.getInvestorDashboard(user.id);
                setDashboardData(data);
                setLoading(false);
            } catch (error) {
                console.error("Erreur dashboard investisseur:", error);
                setLoading(false);
            }
        };
        loadData();
    }, [user.id]);

    // Graphique Donut - Répartition par volume
    const labelsProduits = Object.keys(dashboardData.repartition || {});
    const valuesProduits = Object.values(dashboardData.repartition || {});

    const donutData = {
        labels: labelsProduits.length > 0 ? labelsProduits : ['Aucune donnée'],
        datasets: [{
            data: valuesProduits.length > 0 ? valuesProduits : [1],
            backgroundColor: [
                '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'
            ],
            hoverOffset: 10,
            borderWidth: 0
        }]
    };

    // Graphique Barres - Répartition par Revenu
    const labelsRevenu = Object.keys(dashboardData.repartitionRevenu || {});
    const valuesRevenu = Object.values(dashboardData.repartitionRevenu || {});

    const barData = {
        labels: labelsRevenu,
        datasets: [{
            label: 'Revenu par Catégorie (€)',
            data: valuesRevenu,
            backgroundColor: 'rgba(54, 162, 235, 0.6)',
            borderColor: 'rgba(54, 162, 235, 1)',
            borderWidth: 1,
            borderRadius: 5
        }]
    };

    if (loading) return <div className="loading-state">Analyse du marché en cours...</div>;

    return (
        <div className="investor-dashboard-container fade-in">
            <header className="dashboard-header">
                <div className="header-luxury">
                    <div className="badge-investor">Profil Premium</div>
                    <h1>Vision Stratégique</h1>
                    <p>Optimisez vos investissements basés sur la performance réelle du marché.</p>
                </div>
            </header>

            <InvestorStats user={user} />

            {/* Charts & Opportunities */}
            <div className="investor-grid-section">
                <InvestorCharts doughnutData={donutData} barData={barData} />
                <InvestorOpportunities
                    opportunities={dashboardData.opportunites}
                    topProducts={dashboardData.topProduitsRevenu}
                />
            </div>

            {/* Predictions & Projections */}
            <div className="investor-grid-section">
                <InvestorPredictions predictions={dashboardData.predictionsCroissance} />
                <InvestorProjections projections={dashboardData.projectionsRevenus} />
            </div>

            {/* Potential Products */}
            <InvestorPotentialProduct products={dashboardData.produitsFortPotentiel} />

            {/* Risks Analysis */}
            <div className="mt-8">
                <InvestorRisks analysis={dashboardData.analyseRisque} />
            </div>
        </div>
    );
};

export default InvestorDashboard;
