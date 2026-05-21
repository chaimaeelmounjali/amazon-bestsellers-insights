import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import ProductService from '../../services/product.service';
import { AuthContext } from '../../context/AuthContext';
import { Edit, Trash2, Plus, Search, Package, Zap } from 'lucide-react';
const InventoryPage = () => {
    const { user } = useContext(AuthContext);
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');

    const navigate = useNavigate();

    useEffect(() => {
        if (user && user.id) {
            loadProducts();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [user]);

    const loadProducts = async () => {
        try {
            const data = await ProductService.getProductsBySeller(user.id);
            setProducts(data || []);
        } catch (error) {
            console.error("Erreur chargement inventaire:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm("Supprimer ce produit définitivement ?")) {
            try {
                await ProductService.deleteProduct(id);
                setProducts(products.filter(p => p.id !== id));
            } catch (error) {
                alert("Erreur lors de la suppression");
            }
        }
    };



    const filteredProducts = products.filter(p =>
        p.nom.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.categorie.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.asin.toLowerCase().includes(searchTerm.toLowerCase())
    );

    if (loading) return <div className="loading-state">Chargement de votre inventaire...</div>;

    return (
        <div className="container py-8 fade-in">
            <div className="flex justify-between items-center mb-8">
                <div>
                    <h1 className="text-3xl font-bold">Mon Inventaire</h1>
                    <p className="text-gray-600">Gérez vos {products.length} références actives.</p>
                </div>
                <button className="btn-action-primary mr-2" onClick={() => navigate('/seller/sales/new')}>
                    <Plus size={20} /> Créer une vente
                </button>
                <button className="btn-action-secondary" onClick={() => navigate('/seller/products/new')}>
                    <Plus size={20} /> Nouveau produit
                </button>
            </div>

            <div className="glass-panel p-4 mb-6 relative">
                <Search className="absolute left-7 top-7 text-gray-400" size={20} />
                <input
                    type="text"
                    placeholder="Rechercher par nom, catégorie ou ASIN..."
                    className="w-full pl-12 pr-4 py-3 bg-white/50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />
            </div>

            <div className="glass-panel overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr className="bg-gray-50/50 border-b border-gray-100">
                            <th className="p-4 font-semibold text-gray-700">Produit</th>
                            <th className="p-4 font-semibold text-gray-700">Catégorie</th>
                            <th className="p-4 font-semibold text-gray-700 text-center">Prix</th>
                            <th className="p-4 font-semibold text-gray-700 text-center">Stock</th>
                            <th className="p-4 font-semibold text-gray-700 text-center">Note</th>
                            <th className="p-4 font-semibold text-gray-700 text-right">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredProducts.map((product) => (
                            <tr key={product.id} className="border-b border-gray-50 hover:bg-gray-50/30 transition-colors">
                                <td className="p-4">
                                    <div className="flex items-center gap-3">
                                        <div className="w-12 h-12 rounded bg-gray-100 flex items-center justify-center overflow-hidden">
                                            {product.urlImage ? (
                                                <img src={product.urlImage} alt="" className="object-cover w-full h-full" />
                                            ) : (
                                                <Package className="text-gray-400" size={24} />
                                            )}
                                        </div>
                                        <div>
                                            <div className="font-medium text-gray-900">{product.nom}</div>
                                            <div className="text-xs text-gray-500 font-mono">{product.asin}</div>
                                        </div>
                                    </div>
                                </td>
                                <td className="p-4">
                                    <span className="px-2 py-1 bg-blue-50 text-blue-700 text-xs rounded-full">
                                        {product.categorie}
                                    </span>
                                </td>
                                <td className="p-4 text-center font-semibold">{product.prix} €</td>
                                <td className="p-4 text-center">
                                    <span className={`font-medium ${product.quantiteStock < 10 ? 'text-red-600' : 'text-green-600'}`}>
                                        {product.quantiteStock}
                                    </span>
                                </td>
                                <td className="p-4 text-center">
                                    <div className="flex items-center justify-center gap-1">
                                        <span className="text-yellow-500">★</span>
                                        <span>{product.note || '0.0'}</span>
                                    </div>
                                </td>
                                <td className="p-4 text-right">
                                    <div className="flex justify-end gap-2">
                                        <button
                                            className="p-2 hover:bg-purple-50 text-purple-600 rounded-lg transition-colors"
                                            onClick={() => navigate(`/seller/products/prediction/${product.id}`)}
                                            title="Prédictions IA"
                                        >
                                            <Zap size={18} />
                                        </button>
                                        <button
                                            className="p-2 hover:bg-blue-50 text-blue-600 rounded-lg transition-colors"
                                            onClick={() => navigate(`/seller/products/edit/${product.id}`)}
                                        >
                                            <Edit size={18} />
                                        </button>
                                        <button
                                            className="p-2 hover:bg-red-50 text-red-600 rounded-lg transition-colors"
                                            onClick={() => handleDelete(product.id)}
                                        >
                                            <Trash2 size={18} />
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                {filteredProducts.length === 0 && (
                    <div className="p-12 text-center text-gray-500">
                        Aucun produit trouvé.
                    </div>
                )}
            </div>


        </div>
    );
};

export default InventoryPage;
