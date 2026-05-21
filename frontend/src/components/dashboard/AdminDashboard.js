import React, { useState, useEffect } from 'react';
import { ShoppingBag, DollarSign, Activity, Star, TrendingUp } from 'lucide-react';
import { Bar } from 'react-chartjs-2';
import { useNavigate } from 'react-router-dom';
import ProductService from '../../services/product.service';
import ChangePasswordModal from '../ChangePasswordModal';
import './AdminDashboard.css';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
} from 'chart.js';

ChartJS.register(
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend
);

const AdminDashboard = ({ user }) => {
    const navigate = useNavigate();
    const [stats, setStats] = useState({
        totalProduits: 0,
        nombreCategories: 0,
        prixMoyen: 0,
        noteMoyenne: 0
    });
    const [topProducts, setTopProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showPasswordModal, setShowPasswordModal] = useState(false);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [adminData, top] = await Promise.all([
                    ProductService.getAdminDashboard(),
                    ProductService.getTopProducts(5)
                ]);

                // Mapper le format de getAdminDashboard aux stats attendues
                setStats({
                    totalProduits: adminData.produits?.total || 0,
                    nombreCategories: adminData.produits?.categories || 0,
                    prixMoyen: adminData.produits?.prixMoyen || 0,
                    noteMoyenne: adminData.produits?.noteMoyenne || 0,
                    revenueMensuel: adminData.ventes?.montantTotal || 0,
                    commandesMois: adminData.ventes?.nombreVentes || 0
                });

                setTopProducts(top);
                setLoading(false);
            } catch (error) {
                console.error("Erreur dashboard admin:", error);
                setLoading(false);
            }
        };
        fetchData();
    }, []);

    const data = {
        labels: topProducts.map(p => p.nom.substring(0, 15) + '...'),
        datasets: [
            {
                label: 'Note Moyenne',
                data: topProducts.map(p => p.note),
                backgroundColor: 'rgba(255, 153, 0, 0.6)',
                borderColor: 'rgba(255, 153, 0, 1)',
                borderWidth: 1,
            },
        ],
    };

    if (loading) return <div className="loading-state-premium"><div className="spinner-premium"></div><div className="loading-text-premium">Chargement du tableau de bord...</div></div>;

    return (
        <div className="admin-dashboard-container fade-in">
            <header className="dashboard-header">
                <div>
                    <h1>Tableau de Bord Best Sellers</h1>
                    <p>Analyse globale du catalogue Amazon.</p>
                </div>
                <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                    <button onClick={() => navigate('/admin/users')} className="admin-primary-btn">
                        👥 Gestion Utilisateurs
                    </button>
                    <button onClick={() => setShowPasswordModal(true)} className="admin-header-btn">
                        🔒 Changer mot de passe
                    </button>
                    <div className="admin-date-badge">
                        {new Date().toLocaleDateString('fr-FR', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
                    </div>
                </div>
            </header>

            {/* Stats Grid */}
            <div className="admin-stats-grid">
                <div className="admin-stat-card">
                    <div className="admin-stat-icon" style={{
                        background: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)',
                        color: 'white'
                    }}>
                        <ShoppingBag size={28} />
                    </div>
                    <div className="admin-stat-content">
                        <h4>Total Produits</h4>
                        <p className="stat-value">{stats.totalProduits}</p>
                        <span className="stat-subtitle">Dans le catalogue</span>
                    </div>
                </div>

                <div className="admin-stat-card">
                    <div className="admin-stat-icon" style={{
                        background: 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)',
                        color: 'white'
                    }}>
                        <Activity size={28} />
                    </div>
                    <div className="admin-stat-content">
                        <h4>Catégories</h4>
                        <p className="stat-value">{stats.nombreCategories}</p>
                        <span className="stat-subtitle">Diversité</span>
                    </div>
                </div>

                <div className="admin-stat-card">
                    <div className="admin-stat-icon" style={{
                        background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
                        color: 'white'
                    }}>
                        <DollarSign size={28} />
                    </div>
                    <div className="admin-stat-content">
                        <h4>Prix Moyen</h4>
                        <p className="stat-value">{stats.prixMoyen} €</p>
                        <span className="stat-subtitle">Global</span>
                    </div>
                </div>

                <div className="admin-stat-card">
                    <div className="admin-stat-icon" style={{
                        background: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
                        color: 'white'
                    }}>
                        <Star size={28} />
                    </div>
                    <div className="admin-stat-content">
                        <h4>Note Moyenne</h4>
                        <p className="stat-value">{stats.noteMoyenne} / 5</p>
                        <span className="stat-subtitle">Qualité</span>
                    </div>
                </div>

                <div className="admin-stat-card">
                    <div className="admin-stat-icon" style={{
                        background: 'linear-gradient(135deg, #ec4899 0%, #db2777 100%)',
                        color: 'white'
                    }}>
                        <TrendingUp size={28} />
                    </div>
                    <div className="admin-stat-content">
                        <h4>Revenu Mensuel</h4>
                        <p className="stat-value">{stats.revenueMensuel?.toLocaleString()} €</p>
                        <span className="stat-subtitle">{stats.commandesMois} Commandes</span>
                    </div>
                </div>
            </div>

            {/* Charts & Top Products */}
            <div className="admin-content-grid">
                <div className="admin-card-panel">
                    <div className="admin-card-header">
                        <h3>Top 5 Produits (Qualité)</h3>
                    </div>
                    <div className="admin-chart-container">
                        <Bar data={data} options={{ responsive: true, maintainAspectRatio: false }} />
                    </div>
                </div>

                <div className="admin-card-panel">
                    <div className="admin-card-header">
                        <h3>🏆 Top Produits</h3>
                    </div>
                    <ul className="admin-products-list">
                        {topProducts.map((produit, index) => (
                            <li key={produit.id} className="admin-product-item">
                                <div className="admin-product-rank">#{index + 1}</div>
                                <div className="admin-product-info">
                                    <strong>{produit.nom}</strong>
                                    <div className="admin-product-meta">Rang #{produit.rang} • {produit.prix} €</div>
                                </div>
                            </li>
                        ))}
                    </ul>
                </div>
            </div>

            {/* Password Change Modal */}
            <ChangePasswordModal
                isOpen={showPasswordModal}
                onClose={() => setShowPasswordModal(false)}
                onSuccess={() => {
                    setShowPasswordModal(false);
                    alert('Mot de passe changé avec succès!');
                }}
            />
        </div>
    );
};

export default AdminDashboard;
