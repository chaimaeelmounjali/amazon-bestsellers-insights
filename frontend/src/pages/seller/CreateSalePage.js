import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import ProductService from '../../services/product.service';
import SaleService from '../../services/saleService';
import { AuthContext } from '../../context/AuthContext';
import { ArrowLeft, Save, User, ShoppingBag } from 'lucide-react';

const CreateSalePage = () => {
    const { user } = useContext(AuthContext);
    const navigate = useNavigate();

    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);

    const [formData, setFormData] = useState({
        produitId: '',
        nomAcheteur: '',
        quantite: 1
    });

    const loadProducts = React.useCallback(async () => {
        if (!user || !user.id) return;
        try {
            const data = await ProductService.getProductsBySeller(user.id);
            setProducts(data || []);
            if (data && data.length > 0) {
                setFormData(prev => ({ ...prev, produitId: data[0].id }));
            }
        } catch (error) {
            console.error("Erreur chargement produits:", error);
        } finally {
            setLoading(false);
        }
    }, [user]);

    useEffect(() => {
        loadProducts();
    }, [loadProducts]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const saleData = {
                ...formData,
                vendeurId: user.id
            };

            await SaleService.createSale(saleData);
            alert("Vente créée avec succès ! Stock mis à jour.");
            navigate('/seller/products');
        } catch (error) {
            console.error("Erreur création vente:", error);
            const message = error.response?.data?.message || "Erreur lors de la création de la vente.";
            alert("Erreur: " + message);
        }
    };

    if (loading) return <div className="loading-state">Chargement...</div>;

    const selectedProduct = products.find(p => p.id === parseInt(formData.produitId));

    return (
        <div className="container py-8 fade-in">
            <button
                onClick={() => navigate('/seller/products')}
                className="flex items-center gap-2 text-gray-600 hover:text-primary transition-colors mb-6"
            >
                <ArrowLeft size={20} /> Retour à l'inventaire
            </button>

            <div className="max-w-2xl mx-auto">
                <div className="flex justify-between items-center mb-8">
                    <h1 className="text-3xl font-bold">Nouvelle Vente</h1>
                </div>

                <form onSubmit={handleSubmit} className="glass-panel p-8 space-y-6">
                    <div className="space-y-4">
                        <h3 className="text-lg font-semibold border-b border-gray-100 pb-2">Détails de la vente</h3>

                        <div className="space-y-2">
                            <label className="text-sm font-medium text-gray-700 flex items-center gap-2">
                                <User size={16} /> Nom du Client
                            </label>
                            <input
                                name="nomAcheteur"
                                type="text"
                                required
                                placeholder="Nom complet du client"
                                className="w-full p-3 bg-gray-50 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                                value={formData.nomAcheteur}
                                onChange={handleChange}
                            />
                            <p className="text-xs text-gray-500">
                                Un compte client sera automatiquement créé s'il n'existe pas.
                            </p>
                        </div>

                        <div className="space-y-2">
                            <label className="text-sm font-medium text-gray-700 flex items-center gap-2">
                                <ShoppingBag size={16} /> Produit
                            </label>
                            <select
                                name="produitId"
                                required
                                className="w-full p-3 bg-gray-50 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                                value={formData.produitId}
                                onChange={handleChange}
                            >
                                <option value="" disabled>Sélectionner un produit</option>
                                {products.map(product => (
                                    <option key={product.id} value={product.id}>
                                        {product.nom} (Stock: {product.quantiteStock}) - {product.prix} €
                                    </option>
                                ))}
                            </select>
                        </div>

                        {selectedProduct && (
                            <div className="bg-primary/5 p-4 rounded-lg border border-primary/10">
                                <p className="text-sm font-medium text-primary">
                                    Prix unitaire: {selectedProduct.prix} €
                                </p>
                                <p className="text-xs text-gray-600 mt-1">
                                    Stock disponible: {selectedProduct.quantiteStock}
                                </p>
                            </div>
                        )}

                        <div className="space-y-2">
                            <label className="text-sm font-medium text-gray-700">Quantité</label>
                            <input
                                name="quantite"
                                type="number"
                                min="1"
                                max={selectedProduct ? selectedProduct.quantiteStock : 999}
                                required
                                className="w-full p-3 bg-gray-50 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                                value={formData.quantite}
                                onChange={handleChange}
                            />
                        </div>

                        {selectedProduct && (
                            <div className="pt-4 border-t border-gray-100 flex justify-between items-center text-lg font-bold text-gray-900">
                                <span>Total estimé:</span>
                                <span>
                                    {(selectedProduct.prix * formData.quantite).toFixed(2)} €
                                </span>
                            </div>
                        )}
                    </div>

                    <button
                        type="submit"
                        className="btn-action-primary w-full py-4 text-lg"
                        disabled={!selectedProduct || selectedProduct.quantiteStock < formData.quantite}
                    >
                        <Save size={20} /> Valider la vente
                    </button>
                </form>
            </div>
        </div>
    );
};

export default CreateSalePage;
