import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import ProductService from '../services/product.service';

const ProductDetailPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [imageError, setImageError] = useState(false);

    useEffect(() => {
        const fetchProduct = async () => {
            try {
                setLoading(true);
                setError(null);
                const data = await ProductService.getProductById(id);
                setProduct(data);
            } catch (err) {
                console.error('Error fetching product:', err);
                setError(err.response?.data?.message || 'Produit introuvable');
            } finally {
                setLoading(false);
            }
        };

        if (id) {
            fetchProduct();
        }
    }, [id]);

    const handleImageError = () => {
        setImageError(true);
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 py-12 px-4">
                <div className="max-w-6xl mx-auto">
                    <div className="animate-pulse">
                        <div className="h-8 bg-gray-200 rounded w-1/4 mb-8"></div>
                        <div className="bg-white rounded-2xl shadow-xl p-8">
                            <div className="grid md:grid-cols-2 gap-8">
                                <div className="h-96 bg-gray-200 rounded-lg"></div>
                                <div className="space-y-4">
                                    <div className="h-8 bg-gray-200 rounded w-3/4"></div>
                                    <div className="h-4 bg-gray-200 rounded w-1/2"></div>
                                    <div className="h-6 bg-gray-200 rounded w-1/4"></div>
                                    <div className="h-24 bg-gray-200 rounded"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 py-12 px-4">
                <div className="max-w-2xl mx-auto text-center">
                    <div className="bg-white rounded-2xl shadow-xl p-12">
                        <div className="text-6xl mb-4">😞</div>
                        <h2 className="text-2xl font-bold text-gray-800 mb-4">Produit introuvable</h2>
                        <p className="text-gray-600 mb-8">{error}</p>
                        <button
                            onClick={() => navigate('/produits')}
                            className="px-6 py-3 bg-primary hover:bg-primary-hover text-white font-semibold rounded-lg transition-colors"
                        >
                            Retour aux produits
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    if (!product) {
        return null;
    }

    const imageUrl = imageError || !product.urlImage
        ? 'https://placehold.co/400x400?text=Image+Non+Disponible'
        : product.urlImage;

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 py-12 px-4">
            <div className="max-w-6xl mx-auto">
                {/* Breadcrumb */}
                <nav className="mb-8 flex items-center gap-2 text-sm">
                    <Link to="/" className="text-gray-600 hover:text-primary transition-colors">
                        Accueil
                    </Link>
                    <span className="text-gray-400">/</span>
                    <Link to="/produits" className="text-gray-600 hover:text-primary transition-colors">
                        Produits
                    </Link>
                    <span className="text-gray-400">/</span>
                    <span className="text-gray-800 font-medium">{product.nom}</span>
                </nav>

                {/* Product Details */}
                <div className="bg-white rounded-2xl shadow-xl overflow-hidden">
                    <div className="grid md:grid-cols-2 gap-8 p-8">
                        {/* Product Image */}
                        <div className="flex items-center justify-center bg-gray-50 rounded-xl p-8">
                            <img
                                src={imageUrl}
                                alt={product.nom}
                                onError={handleImageError}
                                className="max-w-full h-auto max-h-96 object-contain"
                            />
                        </div>

                        {/* Product Info */}
                        <div className="flex flex-col">
                            <div className="mb-4">
                                <span className="inline-block px-3 py-1 bg-blue-100 text-blue-800 text-sm font-semibold rounded-full">
                                    {product.categorie}
                                </span>
                            </div>

                            <h1 className="text-3xl font-bold text-gray-900 mb-4">
                                {product.nom}
                            </h1>

                            {/* Rating */}
                            {product.note > 0 && (
                                <div className="flex items-center gap-2 mb-4">
                                    <div className="flex items-center">
                                        {[...Array(5)].map((_, i) => (
                                            <svg
                                                key={i}
                                                className={`w-5 h-5 ${i < Math.floor(product.note)
                                                        ? 'text-yellow-400'
                                                        : 'text-gray-300'
                                                    }`}
                                                fill="currentColor"
                                                viewBox="0 0 20 20"
                                            >
                                                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                                            </svg>
                                        ))}
                                    </div>
                                    <span className="text-gray-600">
                                        {product.note.toFixed(1)} ({product.nombreAvis?.toLocaleString() || 0} avis)
                                    </span>
                                </div>
                            )}

                            {/* Price */}
                            <div className="mb-6">
                                <span className="text-4xl font-bold text-primary">
                                    ${product.prix}
                                </span>
                            </div>

                            {/* Description */}
                            {product.description && (
                                <div className="mb-6">
                                    <h3 className="text-lg font-semibold text-gray-900 mb-2">Description</h3>
                                    <p className="text-gray-700 leading-relaxed">
                                        {product.description}
                                    </p>
                                </div>
                            )}

                            {/* Product Details */}
                            <div className="border-t border-gray-200 pt-6 mb-6 space-y-3">
                                <div className="flex justify-between">
                                    <span className="text-gray-600">ASIN:</span>
                                    <span className="font-semibold text-gray-900">{product.asin}</span>
                                </div>
                                {product.rang > 0 && (
                                    <div className="flex justify-between">
                                        <span className="text-gray-600">Rang:</span>
                                        <span className="font-semibold text-gray-900">#{product.rang}</span>
                                    </div>
                                )}
                                <div className="flex justify-between">
                                    <span className="text-gray-600">Disponibilité:</span>
                                    <span className={`font-semibold ${product.estDisponible ? 'text-green-600' : 'text-red-600'}`}>
                                        {product.estDisponible ? 'En stock' : 'Rupture de stock'}
                                    </span>
                                </div>
                            </div>

                            {/* Action Buttons */}
                            <div className="flex gap-4 mt-auto">
                                {product.urlProduit && (
                                    <a
                                        href={product.urlProduit}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className="flex-1 px-6 py-3 bg-primary hover:bg-primary-hover text-white font-semibold rounded-lg transition-colors text-center"
                                    >
                                        Voir sur Amazon
                                    </a>
                                )}
                                <button
                                    onClick={() => navigate(-1)}
                                    className="px-6 py-3 bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold rounded-lg transition-colors"
                                >
                                    Retour
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ProductDetailPage;
