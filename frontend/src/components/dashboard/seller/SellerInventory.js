import React, { useState } from 'react';
import { Edit, Zap } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const SellerInventory = ({ products, onOpenPrediction }) => {
    const [searchTerm, setSearchTerm] = useState('');
    const navigate = useNavigate();

    const filteredProducts = products.filter(p =>
        p.nom.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.categorie.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (p.quantiteStock && p.quantiteStock.toString().includes(searchTerm))
    );

    return (
        <div className="card-simple glass-panel">
            <h3>📦 Mon Inventaire</h3>
            <input
                type="text"
                placeholder="Filtrer stock..."
                className="w-full p-2 mb-4 border rounded"
                value={searchTerm}
                onChange={e => setSearchTerm(e.target.value)}
            />
            <div className="top-prods-list">
                {filteredProducts.slice(0, 5).map(p => (
                    <div key={p.id} className="prod-rank-item">
                        <span className="rank-num">#{p.rang || '-'}</span>
                        <div className="prod-details">
                            <span className="prod-name">{p.nom}</span>
                            <span className="prod-rev">{p.prix} € • Stock: {p.quantiteStock}</span>
                        </div>
                        <button className="btn-text" onClick={() => navigate(`/seller/products/edit/${p.id}`)}><Edit size={14} /></button>
                        <button className="btn-text" style={{ color: '#8884d8' }} onClick={() => onOpenPrediction(p)} title="Voir Prédictions IA"><Zap size={14} /></button>
                    </div>
                ))}
            </div>
            <button className="btn-link w-full mt-2" onClick={() => navigate('/seller/products')}>Voir tout l'inventaire</button>
        </div>
    );
};

export default SellerInventory;
