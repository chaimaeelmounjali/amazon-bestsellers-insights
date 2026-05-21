import React, { useState } from 'react';
import { X } from 'lucide-react';

const ManualSaleModal = ({ isOpen, onClose, onSubmit, products }) => {
    const [form, setForm] = useState({ produitId: '', quantite: 1, prix: 0 });

    if (!isOpen) return null;

    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit(form);
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content glass-panel" style={{ background: 'white', padding: '2rem', borderRadius: '12px', width: '400px' }}>
                <div className="flex justify-between mb-4">
                    <h3>Nouvelle Vente Manuelle</h3>
                    <button onClick={onClose}><X size={20} /></button>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="form-group mb-2">
                        <label>Produit</label>
                        <select
                            className="w-full p-2 border rounded"
                            onChange={e => setForm({ ...form, produitId: e.target.value })}
                            required
                        >
                            <option value="">Choisir un produit...</option>
                            {products.map(p => <option key={p.id} value={p.id}>{p.nom} ({p.prix}€)</option>)}
                        </select>
                    </div>
                    <div className="form-group mb-2">
                        <label>Quantité</label>
                        <input
                            type="number"
                            className="w-full p-2 border rounded"
                            value={form.quantite}
                            onChange={e => setForm({ ...form, quantite: Number(e.target.value) })}
                        />
                    </div>
                    <div className="form-group mb-4">
                        <label>Prix vente total (€)</label>
                        <input
                            type="number"
                            step="0.01"
                            className="w-full p-2 border rounded"
                            value={form.prix}
                            onChange={e => setForm({ ...form, prix: Number(e.target.value) })}
                        />
                    </div>
                    <button type="submit" className="btn-primary w-full">Créer Vente</button>
                </form>
            </div>
        </div>
    );
};

export default ManualSaleModal;
