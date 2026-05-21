import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import ProductService from '../services/product.service';
import { TrendingUp, Package, Zap, Star, ArrowRight } from 'lucide-react';

const HomePage = () => {
    const [topProduits, setTopProduits] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchTopProducts = async () => {
            try {
                // Utiliser le service centralisé pour cohérence
                const data = await ProductService.getTopProducts(4);
                setTopProduits(data);
            } catch (error) {
                console.error("Erreur chargement top produits", error);
            } finally {
                setLoading(false);
            }
        };

        fetchTopProducts();
    }, []);

    return (
        <div className="bg-gray-50 min-h-screen">
            {/* Hero Section */}
            <div className="relative bg-gradient-to-r from-amazon to-amazon-light text-white overflow-hidden">
                <div className="absolute inset-0 bg-[url('https://images.unsplash.com/photo-1523474253046-8cd2748b5fd2?ixlib=rb-1.2.1&auto=format&fit=crop&w=1920&q=80')] opacity-10 bg-cover bg-center"></div>
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 relative z-10 flex flex-col items-center text-center">
                    <span className="bg-primary/20 text-primary border border-primary/40 px-4 py-1.5 rounded-full text-sm font-semibold mb-6 animate-pulse">
                        Nouvelle Version 2.0 avec IA
                    </span>
                    <h1 className="text-5xl md:text-6xl font-extrabold tracking-tight mb-6 leading-tight">
                        Dominez le marché <br />
                        <span className="text-transparent bg-clip-text bg-gradient-to-r from-primary to-yellow-300">
                            Amazon Best Sellers
                        </span>
                    </h1>
                    <p className="max-w-2xl text-lg text-gray-300 mb-10">
                        Analysez les tendances en temps réel, prédisez les futurs best-sellers et optimisez vos prix grâce à notre intelligence artificielle avancée.
                    </p>
                    <div className="flex gap-4 flex-col sm:flex-row">
                        <Link to="/produits" className="px-8 py-3.5 bg-primary hover:bg-primary-hover text-white font-bold rounded-lg shadow-lg shadow-primary/30 transition-all transform hover:-translate-y-1 flex items-center justify-center gap-2">
                            <Zap size={20} /> Commencer l'Exploration
                        </Link>
                        <Link to="/login" className="px-8 py-3.5 bg-white/10 hover:bg-white/20 backdrop-blur-sm text-white font-semibold rounded-lg border border-white/20 transition-all flex items-center justify-center">
                            Se Connecter
                        </Link>
                    </div>
                </div>

                {/* Wave Shape Divider */}
                <div className="absolute bottom-0 left-0 w-full overflow-hidden leading-[0]">
                    <svg className="relative block w-[calc(100%+1.3px)] h-[50px] md:h-[100px]" data-name="Layer 1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 120" preserveAspectRatio="none">
                        <path d="M321.39,56.44c58-10.79,114.16-30.13,172-41.86,82.39-16.72,168.19-17.73,250.45-.39C823.78,31,906.67,72,985.66,92.83c70.05,18.48,146.53,26.09,214.34,3V0H0V27.35A600.21,600.21,0,0,0,321.39,56.44Z" className="fill-gray-50"></path>
                    </svg>
                </div>
            </div>

            {/* Features Grid */}
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 -mt-10 relative z-20">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                    <FeatureCard
                        icon={<TrendingUp size={32} className="text-primary" />}
                        title="Analyse Prédictive"
                        desc="Utilisez nos algorithmes de Machine Learning pour anticiper les ventes et les tendances 30 jours à l'avance."
                    />
                    <FeatureCard
                        icon={<Package size={32} className="text-purple-500" />}
                        title="Gestion de Stock"
                        desc="Synchronisation automatique et alertes intelligentes pour éviter les ruptures de stock coûteuses."
                    />
                    <FeatureCard
                        icon={<Star size={32} className="text-blue-500" />}
                        title="Intelligence Marché"
                        desc="Espionnez légalement vos concurrents et découvrez les niches inexploitées à fort potentiel."
                    />
                </div>
            </div>

            {/* Top Products Preview */}
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
                <div className="flex justify-between items-end mb-8">
                    <div>
                        <h2 className="text-3xl font-bold text-gray-900">Produits en Vedette</h2>
                        <p className="text-gray-500 mt-2">Les articles les plus performants du moment.</p>
                    </div>
                    <Link to="/produits" className="text-primary font-semibold flex items-center hover:underline">
                        Voir tout <ArrowRight size={16} className="ml-1" />
                    </Link>
                </div>

                {loading ? (
                    <div className="flex justify-center py-12">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                        {topProduits.map(p => (
                            <Link key={p.id} to={`/produit/${p.id}`} className="group bg-white rounded-xl shadow-sm hover:shadow-xl transition-all duration-300 border border-gray-100 overflow-hidden flex flex-col h-full">
                                <div className="h-48 bg-gray-100 relative overflow-hidden">
                                    {/* Placeholder image si null ou url invalide */}
                                    <div className="absolute inset-0 flex items-center justify-center text-gray-400">
                                        {p.urlImage ?
                                            <img src={p.urlImage} alt={p.nom} className="w-full h-full object-contain p-4 group-hover:scale-105 transition-transform duration-300" /> :
                                            <Package size={48} opacity={0.2} />
                                        }
                                    </div>
                                    <div className="absolute top-2 right-2 bg-white px-2 py-1 rounded-md text-xs font-bold shadow-sm text-amazon">
                                        #{p.rang}
                                    </div>
                                </div>
                                <div className="p-5 flex-1 flex flex-col">
                                    <div className="text-xs font-medium text-gray-500 uppercase tracking-wide mb-1">{p.categorie}</div>
                                    <h3 className="font-bold text-gray-800 mb-2 line-clamp-2 group-hover:text-primary transition-colors">{p.nom}</h3>
                                    <div className="mt-auto flex items-center justify-between">
                                        <span className="text-xl font-bold text-amazon">{p.prix} €</span>
                                        <div className="flex items-center text-yellow-400 text-sm font-medium">
                                            <Star size={14} fill="currentColor" className="mr-1" />
                                            {p.note}
                                        </div>
                                    </div>
                                </div>
                            </Link>
                        ))}
                    </div>
                )}
            </div>

            {/* Footer Simple */}
            <footer className="bg-white border-t border-gray-200 mt-20 py-12">
                <div className="max-w-7xl mx-auto px-4 text-center text-gray-500 text-sm">
                    &copy; 2026 AmazonBestSeller. Tous droits réservés.
                </div>
            </footer>
        </div>
    );
};

// Sub-component for clean code
const FeatureCard = ({ icon, title, desc }) => (
    <div className="bg-white p-8 rounded-2xl shadow-lg border-b-4 border-transparent hover:border-primary transition-all duration-300 hover:transform hover:-translate-y-2 group">
        <div className="w-14 h-14 bg-gray-50 rounded-xl flex items-center justify-center mb-6 group-hover:bg-primary/10 transition-colors">
            {icon}
        </div>
        <h3 className="text-xl font-bold mb-3 text-gray-900">{title}</h3>
        <p className="text-gray-500 leading-relaxed">{desc}</p>
    </div>
);

export default HomePage;
