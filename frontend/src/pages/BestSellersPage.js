import React, { useState, useEffect, useCallback } from 'react';
import ProductService from '../services/product.service';
import AlertService from '../services/alertService';
import useAuth from '../hooks/useAuth';
import { Bar } from 'react-chartjs-2';
import { Award, AlertCircle, ShoppingBag, DollarSign, Star } from 'lucide-react';
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

const BestSellersPage = () => {
    const { user } = useAuth();
    const [loading, setLoading] = useState(true);
    const [globalStats, setGlobalStats] = useState({
        totalProduits: 0,
        nombreCategories: 0,
        prixMoyen: 0,
        noteMoyenne: 0,
        top10: []
    });
    const [categoryStats, setCategoryStats] = useState({
        prixMoyen: 0,
        produitMieuxNote: null,
        produitPlusVendu: null,
        distributionNotes: []
    });
    const [categories, setCategories] = useState([]);
    const [selectedCategory, setSelectedCategory] = useState('');
    const [alerts, setAlerts] = useState([]);

    const loadGlobalData = useCallback(async () => {
        setLoading(true);
        try {
            // Independant Error Handling for Robustness
            const statsPromise = ProductService.getGlobalStats().catch(err => {
                console.error("Stats failed", err);
                return { totalProduits: 0, nombreCategories: 0, prixMoyen: 0, noteMoyenne: 0 };
            });
            const catsPromise = ProductService.getCategories().catch(err => {
                console.error("Categories failed", err);
                return [];
            });
            const top10Promise = ProductService.getTopProducts(10).catch(err => {
                console.error("Top 10 failed", err);
                return [];
            });

            // Only fetch alerts if user is present
            const alertsPromise = user && user.id
                ? AlertService.getUnreadAlerts(user.id).catch(err => {
                    console.error("Alerts failed", err);
                    return [];
                })
                : Promise.resolve([]);

            const [stats, cats, top10Prods, userAlerts] = await Promise.all([
                statsPromise,
                catsPromise,
                top10Promise,
                alertsPromise
            ]);

            setGlobalStats({
                ...stats,
                top10: top10Prods
            });
            setCategories(cats);
            setAlerts(userAlerts);
            if (cats.length > 0) setSelectedCategory(cats[0]);
        } catch (error) {
            console.error("Critical Error loading global data", error);
        } finally {
            setLoading(false);
        }
    }, [user]);

    useEffect(() => {
        loadGlobalData();
    }, [loadGlobalData]);

    useEffect(() => {
        if (selectedCategory) {
            loadCategoryData(selectedCategory);
        }
    }, [selectedCategory]);

    const loadCategoryData = async (category) => {
        try {
            console.log("Loading category data for:", category);
            const stats = await ProductService.getCategoryAnalysis(category);
            console.log("Received category stats:", stats);
            setCategoryStats(stats);
        } catch (error) {
            console.error("Error loading category data for", category, ":", error);
            // Set empty stats on error to show "no data" message
            setCategoryStats({
                prixMoyen: 0,
                produitMieuxNote: null,
                produitPlusVendu: null,
                distributionNotes: [],
                nombreProduits: 0
            });
        }
    };

    if (loading) return <div className="loading-spinner">Chargement du Dashboard...</div>;

    const distributionData = categoryStats.distributionNotes || [0, 0, 0, 0, 0];

    const categoryChartData = {
        labels: ['1★', '2★', '3★', '4★', '5★'],
        datasets: [
            {
                label: 'Distribution des Notes',
                data: distributionData,
                backgroundColor: ['#ff4d4d', '#ffad33', '#ffff66', '#99ff99', '#33cc33'],
                borderWidth: 1,
            },
        ],
    };

    return (
        <div className="page-container fade-in">
            <header className="dashboard-header mb-8">
                <div>
                    <h1>🏆 Dashboard Best Sellers</h1>
                    <p>Analyse globale du marché et tendances</p>
                </div>
            </header>

            {/* GLOBAL STATS CARDS */}
            <div className="stats-grid mb-8">
                <div className="stat-card">
                    <div className="stat-icon bg-blue">
                        <ShoppingBag size={24} color="white" />
                    </div>
                    <div className="stat-info">
                        <h3>Total Produits</h3>
                        <p className="stat-value">{globalStats.totalProduits}</p>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon bg-green">
                        <DollarSign size={24} color="white" />
                    </div>
                    <div className="stat-info">
                        <h3>Prix Moyen Global</h3>
                        <p className="stat-value">{globalStats.prixMoyen} €</p>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon bg-yellow">
                        <Star size={24} color="white" />
                    </div>
                    <div className="stat-info">
                        <h3>Note Moyenne</h3>
                        <p className="stat-value">{globalStats.noteMoyenne} / 5</p>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon bg-purple">
                        <Award size={24} color="white" />
                    </div>
                    <div className="stat-info">
                        <h3>Catégories</h3>
                        <p className="stat-value">{globalStats.nombreCategories}</p>
                    </div>
                </div>
            </div>

            <div className="dashboard-content-grid">
                {/* TOP 10 TABLE */}
                <div className="card-simple" style={{ gridColumn: 'span 2' }}>
                    <h3>🔥 Top 10 Best Sellers</h3>
                    <table className="w-full mt-4" style={{ borderCollapse: 'collapse' }}>
                        <thead>
                            <tr className="text-left text-gray-500 border-b">
                                <th className="p-2">Rang</th>
                                <th className="p-2">Produit</th>
                                <th className="p-2">Catégorie</th>
                                <th className="p-2">Prix</th>
                                <th className="p-2">Note</th>
                            </tr>
                        </thead>
                        <tbody>
                            {globalStats.top10.map((p, index) => (
                                <tr key={p.id} className="border-b hover:bg-gray-50">
                                    <td className="p-2 font-bold text-amazon">#{index + 1}</td>
                                    <td className="p-2 font-medium">{p.nom}</td>
                                    <td className="p-2 text-sm text-gray-500">{p.categorie}</td>
                                    <td className="p-2">{p.prix} €</td>
                                    <td className="p-2 flex items-center gap-1"><Star size={12} fill="orange" stroke="none" /> {p.note}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                {/* CATEGORY ANALYSIS */}
                <div className="card-simple">
                    <h3>📊 Analyse par Catégorie</h3>
                    <div className="mb-4">
                        <label className="block text-sm font-medium mb-1">Sélectionner une catégorie</label>
                        <select
                            className="w-full p-2 border rounded"
                            value={selectedCategory}
                            onChange={(e) => setSelectedCategory(e.target.value)}
                        >
                            {categories.map(cat => <option key={cat} value={cat}>{cat}</option>)}
                        </select>
                    </div>

                    {categoryStats.nombreProduits > 0 ? (
                        <div className="space-y-4">
                            <div className="flex justify-between items-center p-3 bg-gray-50 rounded">
                                <span>Prix Moyen</span>
                                <span className="font-bold">{categoryStats.prixMoyen} €</span>
                            </div>
                            <div className="flex justify-between items-center p-3 bg-gray-50 rounded">
                                <span>Top Rated</span>
                                <span className="font-bold text-sm truncate max-w-[150px]" title={categoryStats.produitMieuxNote?.nom}>{categoryStats.produitMieuxNote?.nom || 'N/A'}</span>
                            </div>
                            <div className="flex justify-between items-center p-3 bg-gray-50 rounded">
                                <span>Plus Vendu</span>
                                <span className="font-bold text-sm truncate max-w-[150px]" title={categoryStats.produitLePlusVendu?.nom}>{categoryStats.produitLePlusVendu?.nom || 'N/A'}</span>
                            </div>

                            {categoryStats.top5Produits && categoryStats.top5Produits.length > 0 && (
                                <div className="mt-4 p-3 bg-white border rounded-lg shadow-sm">
                                    <h4 className="font-bold text-gray-700 mb-3 flex items-center gap-2">
                                        <Award size={16} className="text-yellow-500" />
                                        Top 5 Catégorie
                                    </h4>
                                    <div className="space-y-2">
                                        {categoryStats.top5Produits.map((p, index) => (
                                            <div key={p.id} className="flex justify-between items-center text-sm border-b border-gray-100 last:border-0 pb-1 last:pb-0">
                                                <div className="flex items-center gap-2 overflow-hidden">
                                                    <span className={`font-bold w-5 h-5 flex items-center justify-center rounded-full text-xs ${index === 0 ? 'bg-yellow-100 text-yellow-700' : 'bg-gray-100 text-gray-600'}`}>
                                                        {index + 1}
                                                    </span>
                                                    <span className="truncate text-gray-700" title={p.nom}>{p.nom}</span>
                                                </div>
                                                <span className="font-bold text-amazon whitespace-nowrap ml-2">{p.prix} €</span>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}

                            <div className="mt-4 h-48">
                                <Bar
                                    data={categoryChartData}
                                    options={{
                                        responsive: true,
                                        maintainAspectRatio: false,
                                        plugins: { legend: { display: false }, title: { display: true, text: 'Distribution des Notes' } }
                                    }}
                                />
                            </div>
                        </div>
                    ) : (
                        <p className="text-gray-500 text-center py-8">Aucune donnée pour cette catégorie</p>
                    )}
                </div>
            </div>

            {/* ALERTS SECTION */}
            <div className="mt-8">
                <h3 className="flex items-center gap-2 mb-4"><AlertCircle className="text-amazon" /> Alertes du Marché</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {alerts.length > 0 ? alerts.map(alert => (
                        <div key={alert.id} className="bg-white p-4 rounded-lg shadow border-l-4 border-yellow-400 flex gap-3">
                            <AlertCircle size={20} className="text-yellow-500 shrink-0 mt-1" />
                            <div>
                                <h4 className="font-bold text-gray-800">{alert.type || 'Alerte'}</h4>
                                <p className="text-sm text-gray-600">{alert.message}</p>
                                <span className="text-xs text-gray-400 mt-2 block">{new Date(alert.dateCreation).toLocaleDateString()}</span>
                            </div>
                        </div>
                    )) : (
                        <p className="text-gray-500">Aucune alerte récente.</p>
                    )}
                </div>
            </div>
        </div>
    );
};

export default BestSellersPage;
