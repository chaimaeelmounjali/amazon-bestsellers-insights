import React, { useState, useEffect } from 'react';
import { Plus, Lock } from 'lucide-react';
import ProductService from '../../services/product.service';
import PredictionModal from '../predictions/PredictionModal';
import ChangePasswordModal from '../ChangePasswordModal';
import SellerStats from './seller/SellerStats';
import SellerOrders from './seller/SellerOrders';
import SellerInventory from './seller/SellerInventory';
import SellerCharts from './seller/SellerCharts';
import EditOrderModal from './seller/EditOrderModal';
import ManualSaleModal from './seller/ManualSaleModal';
import OrderService from '../../services/orderService';
import { useNavigate } from 'react-router-dom';

import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    BarElement,
    ArcElement,
    Title,
    Tooltip,
    Legend,
} from 'chart.js';

import './seller/SellerDashboard.css';

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    BarElement,
    ArcElement,
    Title,
    Tooltip,
    Legend
);

const SellerDashboard = ({ user }) => {
    const [products, setProducts] = useState([]);
    const [stats, setStats] = useState({ totalVentes: 0, revenuTotal: 0 });
    const [loading, setLoading] = useState(true);
    const [showManualSaleModal, setShowManualSaleModal] = useState(false);
    const [showEditOrderModal, setShowEditOrderModal] = useState(false);
    const [editingOrder, setEditingOrder] = useState(null);
    const [sales, setSales] = useState([]);
    const [chartData, setChartData] = useState(null);
    const [categoryChartData, setCategoryChartData] = useState(null);

    // Modal States
    const [showPredictionModal, setShowPredictionModal] = useState(false);
    const [selectedProductForPrediction, setSelectedProductForPrediction] = useState(null);
    const [showPasswordModal, setShowPasswordModal] = useState(false);

    const navigate = useNavigate();

    useEffect(() => {
        loadData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const loadData = async () => {
        if (!user || !user.id) return;
        setLoading(true);

        try {
            const dashboardData = await ProductService.getSellerDashboard(user.id);

            if (dashboardData.performance) {
                setStats({
                    totalVentes: dashboardData.performance.nombreVentesMois || 0,
                    revenuTotal: dashboardData.performance.chiffreAffaireMois || 0
                });
            }

            if (dashboardData.evolutionVentes) {
                const labels = Object.keys(dashboardData.evolutionVentes);
                const dataPoints = Object.values(dashboardData.evolutionVentes);
                setChartData({
                    labels: labels.map(l => new Date(l).toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' })),
                    datasets: [{
                        label: 'Ventes Mensuelles (€)',
                        data: dataPoints,
                        borderColor: 'rgb(75, 192, 192)',
                        backgroundColor: 'rgba(75, 192, 192, 0.2)',
                        tension: 0.3,
                        fill: true
                    }],
                });
            }

            if (dashboardData.ventesParCategorie) {
                const labels = Object.keys(dashboardData.ventesParCategorie);
                const data = Object.values(dashboardData.ventesParCategorie);
                setCategoryChartData({
                    labels,
                    datasets: [{
                        data,
                        backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF'],
                    }]
                });
            }

            setSales(dashboardData.commandesEnAttente || []);
            const prods = await ProductService.getProductsBySeller(user.id);
            setProducts(prods || []);
        } catch (error) {
            console.error("Erreur lors du chargement du dashboard:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleEditOrder = (order) => {
        setEditingOrder({ ...order });
        setShowEditOrderModal(true);
    };

    const handleUpdateOrder = async (updatedOrder) => {
        try {
            const updatedQuantite = updatedOrder.lignesCommande?.[0]?.quantite || updatedOrder.quantite;

            await OrderService.updateOrder(updatedOrder.id, {
                statut: updatedOrder.statut,
                quantite: updatedQuantite
            });

            alert('Commande modifiée avec succès !');
            setShowEditOrderModal(false);
            loadData();
        } catch (error) {
            console.error('Erreur modification commande:', error);
            const errorMsg = error.response?.data?.message || error.response?.data || error.message || "Erreur lors de la modification";
            alert("Erreur: " + errorMsg);
        }
    };

    const handleCancelOrder = async (id) => {
        if (window.confirm("Annuler cette commande ?")) {
            await OrderService.cancelOrder(id);
            loadData();
        }
    };

    const handleManualSaleSubmit = async (manualSaleForm) => {
        try {
            await OrderService.createManualSale({
                vendeurId: Number(user.id),
                produitId: Number(manualSaleForm.produitId),
                quantite: Number(manualSaleForm.quantite),
                prix: Number(manualSaleForm.prix)
            });
            alert('Vente manuelle enregistrée !');
            setShowManualSaleModal(false);
            loadData();
        } catch (error) {
            alert("Erreur vente manuelle");
        }
    };

    const openPredictionModal = (product) => {
        setSelectedProductForPrediction(product);
        setShowPredictionModal(true);
    };

    if (loading) return <div className="loading-spinner">Chargement...</div>;

    return (
        <div className="seller-dashboard-container fade-in">
            <header className="dashboard-header">
                <div>
                    <h1>Espace Vendeur</h1>
                    <p>Gérez votre boutique et optimisez vos ventes.</p>
                </div>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button
                        onClick={() => setShowPasswordModal(true)}
                        className="seller-header-btn"
                    >
                        <Lock size={16} /> Changer mot de passe
                    </button>
                    <button
                        className="seller-action-btn"
                        onClick={() => navigate('/seller/products/new')}
                    >
                        <Plus size={18} /> Ajouter un produit
                    </button>
                </div>
            </header>

            <SellerStats stats={stats} />

            <SellerCharts salesData={chartData} categoryData={categoryChartData} />

            <div className="seller-grid-section">
                <SellerOrders
                    sales={sales}
                    onEditOrder={handleEditOrder}
                    onCancelOrder={handleCancelOrder}
                    onCreateManualSale={() => setShowManualSaleModal(true)}
                />

                <SellerInventory
                    products={products}
                    onOpenPrediction={openPredictionModal}
                />
            </div>

            <EditOrderModal
                isOpen={showEditOrderModal}
                order={editingOrder}
                onClose={() => setShowEditOrderModal(false)}
                onUpdate={handleUpdateOrder}
            />

            <ManualSaleModal
                isOpen={showManualSaleModal}
                onClose={() => setShowManualSaleModal(false)}
                onSubmit={handleManualSaleSubmit}
                products={products}
            />

            <PredictionModal
                isOpen={showPredictionModal}
                onClose={() => setShowPredictionModal(false)}
                product={selectedProductForPrediction}
            />

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

export default SellerDashboard;
