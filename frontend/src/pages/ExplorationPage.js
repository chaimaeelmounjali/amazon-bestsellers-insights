import React, { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import ProductService from '../services/product.service';
import { Search, Filter, Star, TrendingUp } from 'lucide-react';

const ExplorationPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [categories, setCategories] = useState([]);

    // Filtres
    const [keyword, setKeyword] = useState('');
    const [selectedCategory, setSelectedCategory] = useState('');
    const [priceRange, setPriceRange] = useState({ min: 0, max: 1000 });
    const [minRating, setMinRating] = useState(0);

    const loadInitialData = useCallback(async () => {
        try {
            // Remove unused 'prices' variable
            const [cats, , prods] = await Promise.all([
                ProductService.getCategories(),
                ProductService.getPriceRange(),
                ProductService.getTopProducts(20)
            ]);
            setCategories(cats);
            // Don't overwrite products if search is active
            if (!searchParams.get('query')) {
                setProducts(prods);
            }
            setLoading(false);
        } catch (error) {
            console.error("Erreur chargement:", error);
            setLoading(false);
        }
    }, [searchParams]);

    useEffect(() => {
        loadInitialData();
    }, [loadInitialData]);

    const performSearch = useCallback(async (searchTerm) => {
        setLoading(true);
        try {
            // Unification : on utilise le même endpoint de filtrage pour la recherche
            const results = await ProductService.filterProducts({
                motCle: searchTerm,
                categorie: selectedCategory || null,
                prixMin: priceRange.min,
                prixMax: priceRange.max,
                noteMin: minRating
            });
            setProducts(results);
        } catch (error) {
            console.error("Erreur recherche :", error);
        } finally {
            setLoading(false);
        }
    }, [selectedCategory, priceRange, minRating]);

    // Effect to handle URL query params (Search from Navbar)
    useEffect(() => {
        const queryParam = searchParams.get('query');
        if (queryParam) {
            setKeyword(queryParam);
            performSearch(queryParam);
        }
    }, [searchParams, performSearch]);



    const handleSearch = async (e) => {
        e.preventDefault();
        setSearchParams({ query: keyword });
        // L'effet useEffect déclenchera performSearch
    };

    const handleApplyFilters = async () => {
        setLoading(true);
        try {
            const results = await ProductService.filterProducts({
                motCle: keyword, // Ajout du mot-clé aux filtres pour ne pas perdre la recherche
                categorie: selectedCategory || null,
                prixMin: priceRange.min,
                prixMax: priceRange.max,
                noteMin: minRating
            });
            setProducts(results);
        } catch (error) {
            console.error("Erreur filtre:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleExport = async () => {
        try {
            // Trigger download via new controller endpoint
            // We can open in new window or use fetch with blob
            // Using window.open for simplicity for GET requests with parameters
            window.location.href = `http://localhost:8080/api/export/produits/csv`;
        } catch (error) {
            console.error("Export failed", error);
            alert("Erreur lors de l'export des données.");
        }
    };

    return (
        <div className="page-container">
            <div className="exploration-layout">
                {/* Sidebar Filtres */}
                <aside className="filters-sidebar fade-in">
                    <div className="filter-header">
                        <h3><Filter size={18} /> Filtres</h3>
                        <button className="btn-text" onClick={loadInitialData}>Réinitialiser</button>
                    </div>

                    <div className="filter-group">
                        <label>Catégorie</label>
                        <select
                            value={selectedCategory}
                            onChange={(e) => setSelectedCategory(e.target.value)}
                            className="form-select"
                        >
                            <option value="">Toutes les catégories</option>
                            {categories.map(cat => (
                                <option key={cat} value={cat}>{cat}</option>
                            ))}
                        </select>
                    </div>

                    <div className="filter-group">
                        <label>Prix: {priceRange.min}€ - {priceRange.max}€</label>
                        <input
                            type="range"
                            min="0" max="2000"
                            value={priceRange.max}
                            onChange={(e) => setPriceRange({ ...priceRange, max: Number(e.target.value) })}
                            className="range-slider"
                        />
                    </div>

                    <div className="filter-group">
                        <label>Note minimale: {minRating} <Star size={12} fill="orange" stroke="none" /></label>
                        <input
                            type="range"
                            min="0" max="5" step="0.5"
                            value={minRating}
                            onChange={(e) => setMinRating(Number(e.target.value))}
                            className="range-slider"
                        />
                    </div>

                    <button className="btn-primary" onClick={handleApplyFilters}>
                        Appliquer les filtres
                    </button>

                    <div className="filter-divider" style={{ margin: '1rem 0', borderTop: '1px solid #eee' }}></div>

                    <button className="btn-secondary" onClick={handleExport} style={{ width: '100%', marginTop: '0.5rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}>
                        <TrendingUp size={16} /> Exporter données
                    </button>
                </aside>

                {/* Main Content */}
                <main className="exploration-content fade-in">
                    {/* Smart Search Bar */}
                    <div className="search-section">
                        <form onSubmit={handleSearch} className="search-bar-wrapper">
                            <Search className="search-icon" size={20} />
                            <input
                                type="text"
                                placeholder="Recherche intelligente (ex: 'Casque audio à moins de 50€' ou 'Meilleurs livres')"
                                value={keyword}
                                onChange={(e) => setKeyword(e.target.value)}
                                className="search-input"
                            />
                            <button type="submit" className="btn-search">Rechercher</button>
                        </form>
                        {keyword && <div className="search-hint">💡 Astuce: Notre IA comprend le langage naturel !</div>}
                    </div>

                    {/* Results Grid */}
                    <div className="results-header">
                        <h2>{products.length} Produits trouvés</h2>
                    </div>

                    {loading ? (
                        <div className="loading-spinner">Chargement des produits...</div>
                    ) : (
                        <div className="products-grid">
                            {products.map(product => {
                                // Détecter si le produit a des données enrichies
                                const isEnriched = product.nom &&
                                    !product.nom.includes('Best Seller #') &&
                                    !product.nom.includes('(ASIN:');

                                // Utiliser une meilleure image de fallback basée sur la catégorie
                                const getFallbackImage = (category) => {
                                    const categoryImages = {
                                        'Electronics': 'https://placehold.co/400x400/1a1a2e/ffffff?text=📱+Electronics',
                                        'Books': 'https://placehold.co/400x400/2d4059/ffffff?text=📚+Books',
                                        'Gift Cards': 'https://placehold.co/400x400/ea5455/ffffff?text=🎁+Gift+Card',
                                        'Video Games': 'https://placehold.co/400x400/6c5ce7/ffffff?text=🎮+Gaming',
                                        'Camera & Photo': 'https://placehold.co/400x400/00b894/ffffff?text=📷+Camera',
                                        'Toys & Games': 'https://placehold.co/400x400/fd79a8/ffffff?text=🧸+Toys',
                                        'Clothing, Shoes & Jewelry': 'https://placehold.co/400x400/a29bfe/ffffff?text=👕+Fashion'
                                    };
                                    return categoryImages[category] || 'https://placehold.co/400x400/636e72/ffffff?text=📦+Product';
                                };

                                // Améliorer le nom pour les produits non enrichis
                                const getDisplayName = (product) => {
                                    if (isEnriched) return product.nom;
                                    // Créer un nom plus descriptif
                                    return `${product.categorie} - Rang #${product.rang}`;
                                };

                                const imageUrl = (product.urlImage && !product.urlImage.includes('placehold.co'))
                                    ? product.urlImage
                                    : getFallbackImage(product.categorie);

                                return (
                                    <div key={product.id} className="product-card fade-in" style={!isEnriched ? { opacity: 0.9 } : {}}>
                                        <div className="product-badge rank-badge">#{product.rang}</div>
                                        {!isEnriched && (
                                            <div className="product-badge" style={{
                                                top: '10px',
                                                left: '10px',
                                                background: '#f39c12',
                                                fontSize: '10px',
                                                padding: '2px 6px'
                                            }}>⏳ En cours</div>
                                        )}
                                        <div className="product-image-container">
                                            <img
                                                src={imageUrl}
                                                alt={getDisplayName(product)}
                                                onError={(e) => {
                                                    e.target.onerror = null;
                                                    e.target.src = getFallbackImage(product.categorie);
                                                }}
                                            />
                                        </div>
                                        <div className="product-details">
                                            <div className="product-category">{product.categorie}</div>
                                            <h3 title={product.nom}>{getDisplayName(product)}</h3>
                                            {!isEnriched && (
                                                <div style={{ fontSize: '11px', color: '#7f8c8d', marginTop: '4px' }}>
                                                    💡 Données en cours d'enrichissement
                                                </div>
                                            )}
                                            <div className="product-meta">
                                                <div className="rating">
                                                    <Star size={16} fill="#FF9900" stroke="none" />
                                                    <span>{product.note}</span>
                                                    <span className="reviews">({product.nombreAvis?.toLocaleString()})</span>
                                                </div>
                                                <div className="price">{product.prix} €</div>
                                            </div>
                                            <div className="product-footer">
                                                <button className="btn-sm">Voir Détails</button>
                                                <button className="btn-sm secondary"><TrendingUp size={16} /></button>
                                            </div>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </main>
            </div>
        </div>
    );
};

export default ExplorationPage;
