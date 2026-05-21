import React, { useState, useEffect } from 'react';
import {
    Package, Heart, ShoppingBag, CreditCard, Star,
    ArrowRight, TrendingUp, Bell, ShieldCheck, Zap, AlertTriangle
} from 'lucide-react';
import { useNavigate, Link } from 'react-router-dom';
import ChangePasswordModal from '../ChangePasswordModal';
import './BuyerDashboard.css';

const BuyerDashboard = ({ user }) => {
    const navigate = useNavigate();
    const [dashboardData, setDashboardData] = useState({
        commandes: { total: 0, montantTotal: 0, panierMoyen: 0 },
        dernieresCommandes: [],
        nombreAvis: 0,
        recommandations: [],
        favoris: [],
        alertes: []
    });
    const [loading, setLoading] = useState(true);
    const [showPasswordModal, setShowPasswordModal] = useState(false);
    const [usingDemoData, setUsingDemoData] = useState(false);
    const [error] = useState(null);

    useEffect(() => {
        const loadDashboardData = () => {
            setLoading(true);
            setUsingDemoData(true);

            // Realistic simulated data for Amazon Best Seller marketplace
            const now = Date.now();
            const oneDay = 86400000;

            setDashboardData({
                commandes: {
                    total: 24,
                    montantTotal: 1847.65,
                    panierMoyen: 76.98
                },
                dernieresCommandes: [
                    {
                        id: 10245,
                        dateCommande: new Date(now - oneDay * 2).toISOString(),
                        statut: 'EN_COURS',
                        montantTotal: 129.99
                    },
                    {
                        id: 10198,
                        dateCommande: new Date(now - oneDay * 5).toISOString(),
                        statut: 'LIVREE',
                        montantTotal: 45.50
                    },
                    {
                        id: 10156,
                        dateCommande: new Date(now - oneDay * 8).toISOString(),
                        statut: 'LIVREE',
                        montantTotal: 89.99
                    },
                    {
                        id: 10089,
                        dateCommande: new Date(now - oneDay * 12).toISOString(),
                        statut: 'LIVREE',
                        montantTotal: 234.00
                    },
                    {
                        id: 10034,
                        dateCommande: new Date(now - oneDay * 18).toISOString(),
                        statut: 'ANNULEE',
                        montantTotal: 67.50
                    }
                ],
                nombreAvis: 18,
                recommandations: [
                    {
                        id: 501,
                        nom: "Echo Dot (5ème génération) - Enceinte connectée Alexa",
                        prix: 59.99,
                        urlImage: "https://images.unsplash.com/photo-1543512214-318c7553f230?w=150&h=150&fit=crop",
                        note: 4.7
                    },
                    {
                        id: 502,
                        nom: "Kindle Paperwhite - Liseuse numérique étanche",
                        prix: 139.99,
                        urlImage: "https://images.unsplash.com/photo-1592496431122-2349e0fbc666?w=150&h=150&fit=crop",
                        note: 4.8
                    },
                    {
                        id: 503,
                        nom: "Apple AirPods Pro (2ème génération)",
                        prix: 279.00,
                        urlImage: "https://images.unsplash.com/photo-1606841837239-c5a1a4a07af7?w=150&h=150&fit=crop",
                        note: 4.9
                    },
                    {
                        id: 504,
                        nom: "Samsung Galaxy Watch 6 - Montre connectée",
                        prix: 299.00,
                        urlImage: "https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=150&h=150&fit=crop",
                        note: 4.6
                    },
                    {
                        id: 505,
                        nom: "Logitech MX Master 3S - Souris sans fil",
                        prix: 109.99,
                        urlImage: "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=150&h=150&fit=crop",
                        note: 4.8
                    },
                    {
                        id: 506,
                        nom: "Anker PowerCore 20000mAh - Batterie externe",
                        prix: 49.99,
                        urlImage: "https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?w=150&h=150&fit=crop",
                        note: 4.7
                    }
                ],
                favoris: [
                    {
                        id: 601,
                        nom: "Sony WH-1000XM5 - Casque à réduction de bruit",
                        prix: 399.00,
                        urlImage: "https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?w=150&h=150&fit=crop",
                        note: 4.9
                    },
                    {
                        id: 602,
                        nom: "iPad Air (5ème génération) - Tablette 10.9 pouces",
                        prix: 699.00,
                        urlImage: "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=150&h=150&fit=crop",
                        note: 4.8
                    },
                    {
                        id: 603,
                        nom: "Dyson V15 Detect - Aspirateur sans fil",
                        prix: 649.00,
                        urlImage: "https://images.unsplash.com/photo-1558317374-067fb5f30001?w=150&h=150&fit=crop",
                        note: 4.7
                    },
                    {
                        id: 604,
                        nom: "Nespresso Vertuo Next - Machine à café",
                        prix: 179.00,
                        urlImage: "https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=150&h=150&fit=crop",
                        note: 4.6
                    }
                ],
                alertes: [
                    {
                        type: "INFO",
                        message: "Mode Démonstration actif - Données simulées pour visualisation"
                    },
                    {
                        type: "PROMO",
                        message: "🎉 Ventes Flash : -30% sur une sélection d'électronique jusqu'à minuit !"
                    },
                    {
                        type: "PROMO",
                        message: "📦 Livraison gratuite sur toutes vos commandes ce week-end"
                    }
                ]
            });

            setLoading(false);
        };

        loadDashboardData();
    }, [user]);

    if (loading) {
        return (
            <div className="flex flex-col items-center justify-center min-h-screen bg-slate-50">
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-600 mb-4"></div>
                <p className="text-slate-600 font-medium animate-pulse">Chargement de votre espace personnel...</p>
            </div>
        );
    }

    const { commandes, dernieresCommandes, recommandations, nombreAvis, favoris, alertes } = dashboardData;

    return (
        <div className="buyer-dashboard-premium fade-in">
            {/* Header Section */}
            <header className="premium-header">
                <div className="welcome-section">
                    <h1>
                        Bonjour, <span className="user-name">{user?.nomUtilisateur || 'Client'}</span> ! 👋
                    </h1>
                    <p className="subtitle">
                        {usingDemoData
                            ? "Mode Démonstration - Explorez les fonctionnalités"
                            : "Voici votre activité récente et nos meilleures sélections."}
                    </p>
                </div>
                <div className="header-actions">
                    <button onClick={() => setShowPasswordModal(true)} className="btn-premium btn-premium-secondary">
                        <ShieldCheck size={18} /> Profil & Sécurité
                    </button>
                    <button onClick={() => navigate('/exploration')} className="btn-premium btn-premium-primary">
                        <ShoppingBag size={18} /> Boutique
                    </button>
                </div>
            </header>

            {/* Error / Demo Banner */}
            {usingDemoData && (
                <div className="mb-6 mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                    <div className="bg-amber-50 border-l-4 border-amber-400 p-4 rounded-r-md shadow-sm">
                        <div className="flex">
                            <div className="flex-shrink-0">
                                <AlertTriangle className="h-5 w-5 text-amber-400" aria-hidden="true" />
                            </div>
                            <div className="ml-3">
                                <p className="text-sm text-amber-700">
                                    Une erreur est survenue lors du chargement de vos données ({error || "Erreur inconnue"}). Vous visualisez actuellement un <strong>contenu de démonstration</strong>.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Alerts Section */}
            {alertes && alertes.length > 0 && (
                <div className="premium-alerts">
                    {alertes.map((alert, index) => (
                        <div key={index} className={`alert-pill ${alert.type.toLowerCase()}`}>
                            <Bell size={18} />
                            <span>{alert.message}</span>
                            {alert.type === 'PROMO' && <Zap size={16} className="text-orange-500 animate-pulse" />}
                        </div>
                    ))}
                </div>
            )}

            {/* Top Stats */}
            <div className="stats-container">
                <div className="stat-card-premium">
                    <div className="icon-wrapper-premium bg-blue-50 text-blue-600">
                        <Package size={24} />
                    </div>
                    <div className="stat-content-premium">
                        <h4>Commandes</h4>
                        <p className="stat-value">{commandes?.total || 0}</p>
                        <div className="stat-trend text-blue-600"><TrendingUp size={12} /> Total à ce jour</div>
                    </div>
                </div>

                <div className="stat-card-premium">
                    <div className="icon-wrapper-premium bg-green-50 text-green-600">
                        <CreditCard size={24} />
                    </div>
                    <div className="stat-content-premium">
                        <h4>Dépenses</h4>
                        <p className="stat-value">{commandes?.montantTotal?.toLocaleString() || '0'} €</p>
                        <div className="stat-trend text-green-600">Panier moyen: {commandes?.panierMoyen?.toLocaleString() || '0'} €</div>
                    </div>
                </div>

                <div className="stat-card-premium">
                    <div className="icon-wrapper-premium bg-pink-50 text-pink-600">
                        <Star size={24} />
                    </div>
                    <div className="stat-content-premium">
                        <h4>Avis Publiés</h4>
                        <p className="stat-value">{nombreAvis || 0}</p>
                        <div className="stat-trend text-pink-600">Votre impact communautaire</div>
                    </div>
                </div>
            </div>

            <div className="dashboard-main-grid">
                {/* Main Activities Column */}
                <div className="main-content-column flex flex-col gap-8">

                    {/* Recent Orders */}
                    <section className="card-premium">
                        <div className="card-title-premium">
                            <h3><Package size={22} className="text-slate-700" /> Vos Commandes Récentes</h3>
                            <Link to="/exploration" className="text-blue-600 text-sm font-semibold flex items-center gap-1 hover:underline">
                                Nouvelle commande <ArrowRight size={14} />
                            </Link>
                        </div>

                        <div className="orders-list-premium">
                            {dernieresCommandes && dernieresCommandes.length > 0 ? (
                                dernieresCommandes.map(order => (
                                    <div className="order-row-premium" key={order.id}>
                                        <div className="order-id-badge">#{order.id}</div>
                                        <div className="order-desc">
                                            <p>Commande du {new Date(order.dateCommande).toLocaleDateString()}</p>
                                            <span className="text-xs text-slate-400">Expédié par Amazon Best Sellers</span>
                                        </div>
                                        <div className="order-status text-center">
                                            <span className="status-badge-premium" data-status={order.statut}>
                                                {order.statut}
                                            </span>
                                        </div>
                                        <div className="order-price font-bold text-slate-800 text-right">
                                            {order.montantTotal} €
                                        </div>
                                        <button className="text-slate-300 hover:text-blue-600 transition-colors">
                                            <ArrowRight size={18} />
                                        </button>
                                    </div>
                                ))
                            ) : (
                                <div className="empty-state-premium">
                                    <Package size={48} className="mx-auto text-slate-300 mb-4" />
                                    <p className="text-slate-500">Vous n'avez pas encore passé de commande.</p>
                                    <button onClick={() => navigate('/exploration')} className="mt-4 btn-premium btn-premium-primary mx-auto">
                                        Commencer mon shopping
                                    </button>
                                </div>
                            )}
                        </div>
                    </section>

                    {/* Favorites Section */}
                    <section className="card-premium">
                        <div className="card-title-premium">
                            <h3><Heart size={22} className="text-rose-500" /> Mes Coups de Coeur</h3>
                        </div>
                        <div className="mini-product-grid">
                            {favoris && favoris.length > 0 ? (
                                favoris.map(prod => (
                                    <Link to={`/produit/${prod.id}`} className="product-item-premium" key={prod.id}>
                                        <div className="img-container-premium">
                                            <img src={prod.urlImage || 'https://placehold.co/150?text=Produit'} alt={prod.nom} />
                                            <div className="absolute top-2 right-2 bg-white/90 backdrop-blur rounded-full p-1.5 shadow-sm">
                                                <Heart size={14} className="text-rose-500 fill-rose-500" />
                                            </div>
                                        </div>
                                        <h4>{prod.nom}</h4>
                                        <div className="flex justify-between items-center mt-2">
                                            <span className="price">{prod.prix} €</span>
                                            <div className="flex items-center text-xs text-amber-500 font-bold bg-amber-50 px-1.5 py-0.5 rounded">
                                                <Star size={10} fill="currentColor" className="mr-1" /> {prod.note || '4.0'}
                                            </div>
                                        </div>
                                    </Link>
                                ))
                            ) : (
                                <div className="text-center py-8 w-full col-span-full">
                                    <Heart size={32} className="mx-auto text-slate-200 mb-2" />
                                    <p className="text-slate-500 italic">Pas encore de favoris.</p>
                                </div>
                            )}
                        </div>
                    </section>
                </div>

                {/* Recommendations Sidebar */}
                <aside className="sidebar-column flex flex-col gap-6">
                    <section className="card-premium bg-slate-50 border border-blue-100/50">
                        <div className="card-title-premium pb-2 border-b border-blue-100">
                            <h3 className="text-blue-800"><Zap size={20} className="text-blue-600 filled" /> Choisi pour vous</h3>
                        </div>
                        <div className="recommendation-list-premium flex flex-col gap-3 mt-4">
                            {recommandations && recommandations.slice(0, 4).map(prod => (
                                <Link to={`/produit/${prod.id}`} className="rec-card-premium group" key={prod.id}>
                                    <div className="relative">
                                        <img
                                            src={prod.urlImage || 'https://placehold.co/150?text=Produit'}
                                            alt=""
                                            className="rec-img-mini"
                                        />
                                    </div>
                                    <div className="rec-info-premium flex-1 min-w-0">
                                        <h5 className="truncate font-medium text-slate-800 group-hover:text-blue-600 transition-colors" title={prod.nom}>{prod.nom}</h5>
                                        <div className="flex items-center justify-between mt-1">
                                            <p className="price text-sm font-bold text-slate-700">{prod.prix} €</p>
                                            <span className="text-xs text-blue-600 font-medium opacity-0 group-hover:opacity-100 transition-opacity flex items-center">
                                                Voir <ArrowRight size={10} className="ml-1" />
                                            </span>
                                        </div>
                                    </div>
                                </Link>
                            ))}
                        </div>
                        <button
                            className="w-full mt-6 py-3 border border-blue-200 text-blue-600 rounded-xl font-bold hover:bg-blue-600 hover:text-white transition-all shadow-sm hover:shadow-md"
                            onClick={() => navigate('/exploration')}
                        >
                            Découvrir plus
                        </button>
                    </section>

                    {/* Promo Banner */}
                    <div className="rounded-xl overflow-hidden relative bg-gradient-to-br from-indigo-600 to-violet-700 text-white p-6 shadow-lg">
                        <div className="absolute top-0 right-0 -mt-4 -mr-4 w-24 h-24 bg-white/10 rounded-full blur-2xl"></div>
                        <div className="relative z-10">
                            <h4 className="text-xl font-bold mb-2">Offre Exclusive 🚀</h4>
                            <p className="text-indigo-100 text-sm mb-4 leading-relaxed">
                                Investissez dans le futur. Découvrez nos analyses prédictives sur les prochains best-sellers.
                            </p>
                            <Link to="/best-sellers" className="inline-block bg-white text-indigo-600 px-4 py-2 rounded-lg font-bold text-sm hover:bg-indigo-50 transition-colors shadow-sm">
                                Voir le Top 100
                            </Link>
                        </div>
                    </div>
                </aside>
            </div>

            {/* Modals */}
            <ChangePasswordModal
                isOpen={showPasswordModal}
                onClose={() => setShowPasswordModal(false)}
                onSuccess={() => {
                    setShowPasswordModal(false);
                }}
            />
        </div>
    );
};

export default BuyerDashboard;
