import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import { ShoppingCart, User, LogOut, Menu, Search } from 'lucide-react';

const Navbar = () => {
    const { user, logout, isAuthenticated } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <nav className="bg-amazon text-white shadow-lg sticky top-0 z-50">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center justify-between h-16">
                    {/* Logo */}
                    <Link to="/" className="flex items-center gap-2 group flex-shrink-0">
                        <span className="text-2xl font-bold tracking-tighter text-white group-hover:text-primary transition-colors">
                            Amazon<span className="text-primary">BestSeller</span>
                        </span>
                    </Link>

                    {/* Search Bar (Desktop) */}
                    <div className="hidden md:flex flex-1 max-w-2xl mx-8">
                        <form action="/exploration" method="GET" className="w-full relative group">
                            <input
                                type="text"
                                name="query"
                                placeholder="Rechercher un produit (ex: 'Ordinateur pas cher', 'Nouveautés sport')..."
                                className="w-full px-4 py-2 rounded-l-md text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary transition-all"
                            />
                            <button
                                type="submit"
                                className="absolute right-0 top-0 h-full px-5 bg-primary hover:bg-primary-hover text-white rounded-r-md font-bold transition-colors"
                            >
                                <Search size={20} />
                            </button>
                        </form>
                    </div>

                    {/* Navigation Links (Desktop) */}
                    <div className="hidden md:flex items-center space-x-6">
                        <Link to="/" className="text-gray-300 hover:text-white hover:underline transition-all font-medium">
                            Accueil
                        </Link>
                        <Link to="/produits" className="text-gray-300 hover:text-white hover:underline transition-all font-medium">
                            Explorer
                        </Link>
                        <Link to="/best-sellers" className="text-gray-300 hover:text-white hover:underline transition-all font-medium">
                            Best Sellers
                        </Link>

                        {isAuthenticated ? (
                            <div className="flex items-center gap-6">
                                <Link
                                    to="/dashboard"
                                    className="px-4 py-2 bg-white/10 hover:bg-white/20 rounded-md transition-colors text-sm font-semibold"
                                >
                                    Mon Espace
                                </Link>

                                <div className="flex items-center gap-2 text-sm text-gray-300 border-l border-gray-600 pl-6">
                                    <User size={16} className="text-primary" />
                                    <span className="max-w-[100px] truncate">{user?.nomUtilisateur}</span>
                                </div>

                                <button
                                    onClick={handleLogout}
                                    className="p-2 text-gray-400 hover:text-red-400 transition-colors"
                                    title="Se déconnecter"
                                >
                                    <LogOut size={20} />
                                </button>
                            </div>
                        ) : (
                            <Link
                                to="/login"
                                className="px-5 py-2.5 bg-primary hover:bg-primary-hover text-white font-bold rounded-lg shadow-md transition-all transform hover:scale-105"
                            >
                                Connexion
                            </Link>
                        )}

                        <Link to="/panier" className="relative p-2 text-gray-300 hover:text-white transition-colors">
                            <ShoppingCart size={24} />
                            <span className="absolute top-0 right-0 bg-primary text-xs font-bold w-5 h-5 flex items-center justify-center rounded-full text-white transform translate-x-1 -translate-y-1">
                                0
                            </span>
                        </Link>
                    </div>

                    {/* Mobile Menu Button (Hamburger) - Simplified for now */}
                    <div className="md:hidden flex items-center">
                        <button className="text-gray-300 hover:text-white">
                            <Menu size={24} />
                        </button>
                    </div>
                </div>
            </div>
        </nav>
    );
};

export default Navbar;
