import React, { useState, useEffect, useContext } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import ProductService from '../../services/product.service';
import { AuthContext } from '../../context/AuthContext';
import { ArrowLeft, Save, Image as ImageIcon } from 'lucide-react';

const ProductFormPage = () => {
    const { user } = useContext(AuthContext);
    const { id } = useParams();
    const navigate = useNavigate();
    const isEdit = !!id;

    const [formData, setFormData] = useState({
        nom: '',
        description: '',
        prix: '',
        categorie: '',
        asin: '',
        urlImage: '',
        quantiteStock: 100,
        estDisponible: true
    });
    const [loading, setLoading] = useState(isEdit);

    useEffect(() => {
        const loadProduct = async () => {
            try {
                const product = await ProductService.getProductById(id);
                setFormData({
                    ...product,
                    quantiteStock: product.quantiteStock || 0
                });
            } catch (error) {
                console.error("Erreur chargement produit:", error);
                alert("Erreur lors du chargement du produit.");
                navigate('/seller/products');
            } finally {
                setLoading(false);
            }
        };

        if (isEdit) {
            loadProduct();
        } else {
            setLoading(false);
        }
    }, [id, isEdit, navigate]);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const productData = {
                ...formData,
                prix: parseFloat(formData.prix) || 0,
                quantiteStock: parseInt(formData.quantiteStock) || 0,
                vendeurId: user.id
            };

            if (isEdit) {
                await ProductService.updateProduct(id, productData);
                alert("Produit mis à jour !");
            } else {
                await ProductService.addProduct(productData);
                alert("Produit créé !");
            }
            navigate('/seller/products');
        } catch (error) {
            console.error("Erreur sauvegarde produit:", error);
            const message = error.response?.data?.message || error.response?.data || "Erreur lors de la sauvegarde.";
            alert(message);
        }
    };

    if (loading) return <div className="loading-state">Chargement...</div>;

    return (
        <div className="container py-8 fade-in">
            <button
                onClick={() => navigate(-1)}
                className="flex items-center gap-2 text-gray-600 hover:text-primary transition-colors mb-6"
            >
                <ArrowLeft size={20} /> Retour
            </button>

            <div className="flex justify-between items-center mb-8">
                <h1 className="text-3xl font-bold">
                    {isEdit ? 'Modifier le Produit' : 'Nouveau Produit'}
                </h1>
            </div>

            <form onSubmit={handleSubmit} className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                <div className="lg:col-span-2 space-y-6">
                    <div className="glass-panel p-6 space-y-4">
                        <h3 className="text-lg font-semibold border-b border-gray-100 pb-2">Informations Générales</h3>

                        <div className="space-y-1">
                            <label className="text-sm font-medium text-gray-700">Nom du Produit</label>
                            <input
                                name="nom"
                                type="text"
                                required
                                className="w-full p-3 bg-gray-50 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                                value={formData.nom}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="space-y-1">
                            <label className="text-sm font-medium text-gray-700">Description</label>
                            <textarea
                                name="description"
                                rows="5"
                                className="w-full p-3 bg-gray-50 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                                value={formData.description}
                                onChange={handleChange}
                            ></textarea>
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-1">
                                <label className="text-sm font-medium text-gray-700">Catégorie</label>
                                <input
                                    name="categorie"
                                    type="text"
                                    required
                                    className="w-full p-3 bg-gray-50 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                                    value={formData.categorie}
                                    onChange={handleChange}
                                />
                            </div>
                            <div className="space-y-1">
                                <label className="text-sm font-medium text-gray-700">ASIN (Unique)</label>
                                <input
                                    name="asin"
                                    type="text"
                                    required
                                    disabled={isEdit}
                                    className="w-full p-3 bg-gray-50 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all disabled:opacity-50"
                                    value={formData.asin}
                                    onChange={handleChange}
                                />
                            </div>
                        </div>
                    </div>

                    <div className="glass-panel p-6 space-y-4">
                        <h3 className="text-lg font-semibold border-b border-gray-100 pb-2">Vente & Stock</h3>
                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-1">
                                <label className="text-sm font-medium text-gray-700">Prix (€)</label>
                                <input
                                    name="prix"
                                    type="number"
                                    step="0.01"
                                    required
                                    className="w-full p-3 bg-gray-50 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                                    value={formData.prix}
                                    onChange={handleChange}
                                />
                            </div>
                            <div className="space-y-1">
                                <label className="text-sm font-medium text-gray-700">Quantité en Stock</label>
                                <input
                                    name="quantiteStock"
                                    type="number"
                                    required
                                    className="w-full p-3 bg-gray-50 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                                    value={formData.quantiteStock}
                                    onChange={handleChange}
                                />
                            </div>
                        </div>
                    </div>
                </div>

                <div className="space-y-6">
                    <div className="glass-panel p-6 space-y-4">
                        <h3 className="text-lg font-semibold border-b border-gray-100 pb-2">Visibilité</h3>
                        <div className="flex items-center gap-3">
                            <input
                                name="estDisponible"
                                type="checkbox"
                                id="available"
                                className="w-5 h-5 rounded text-primary focus:ring-primary border-gray-300"
                                checked={formData.estDisponible}
                                onChange={handleChange}
                            />
                            <label htmlFor="available" className="text-sm text-gray-700 font-medium cursor-pointer">
                                Produit disponible à la vente
                            </label>
                        </div>

                        <div className="pt-4 border-t border-gray-50">
                            <label className="text-sm font-medium text-gray-700 block mb-2">Image du Produit (URL)</label>
                            <div className="flex flex-col gap-4">
                                <div className="w-full aspect-square rounded-xl bg-gray-100 border-2 border-dashed border-gray-200 flex items-center justify-center overflow-hidden">
                                    {formData.urlImage ? (
                                        <img src={formData.urlImage} alt="Aperçu" className="w-full h-full object-cover" />
                                    ) : (
                                        <ImageIcon className="text-gray-400" size={48} />
                                    )}
                                </div>
                                <input
                                    name="urlImage"
                                    type="text"
                                    placeholder="https://..."
                                    className="w-full p-3 bg-gray-50 border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all text-xs"
                                    value={formData.urlImage}
                                    onChange={handleChange}
                                />
                            </div>
                        </div>
                    </div>

                    <button type="submit" className="btn-action-primary w-full py-4 text-lg">
                        <Save size={20} /> {isEdit ? 'Mettre à jour' : 'Enregistrer le produit'}
                    </button>
                </div>
            </form>
        </div>
    );
};

export default ProductFormPage;
