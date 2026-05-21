import React, { useState } from 'react';
import { Search, Edit, X } from 'lucide-react';

const SellerOrders = ({ sales, onEditOrder, onCancelOrder, onCreateManualSale }) => {
    const [orderSearchTerm, setOrderSearchTerm] = useState('');

    const filteredSales = sales.filter(s =>
        s.id.toString().includes(orderSearchTerm) ||
        s.statut.toLowerCase().includes(orderSearchTerm.toLowerCase()) ||
        (s.acheteur && s.acheteur.nomUtilisateur && s.acheteur.nomUtilisateur.toLowerCase().includes(orderSearchTerm.toLowerCase()))
    );

    return (
        <div className="card-simple glass-panel" style={{ flex: 2 }}>
            <div className="flex justify-between items-center mb-4">
                <h3>⚡ Commandes Récentes</h3>
                <div className="relative">
                    <Search size={14} className="absolute left-2 top-2 text-gray-400" />
                    <input
                        type="text"
                        placeholder="Rechercher commande..."
                        className="pl-8 p-1 text-sm border rounded"
                        value={orderSearchTerm}
                        onChange={e => setOrderSearchTerm(e.target.value)}
                    />
                </div>
            </div>
            <div className="orders-list-modern">
                {filteredSales.length > 0 ? filteredSales.map(sale => (
                    <div key={sale.id} className="order-item-modern">
                        <div style={{ flex: 1 }}>
                            <strong>Commande #{sale.id}</strong>
                            {sale.acheteur && sale.acheteur.nomUtilisateur && (
                                <span className="text-sm text-primary ml-2">• {sale.acheteur.nomUtilisateur}</span>
                            )}
                            <div className="text-sm text-gray">{new Date(sale.dateCommande).toLocaleDateString()} • {sale.montantTotal}€</div>
                            <span className="order-status-badge" data-status={sale.statut}>{sale.statut}</span>
                        </div>
                        <div className="flex gap-2">
                            <button className="btn-sm" onClick={() => onEditOrder(sale)} title="Modifier"><Edit size={16} /></button>
                            <button className="btn-sm secondary" style={{ color: 'red' }} onClick={() => onCancelOrder(sale.id)} title="Annuler"><X size={16} /></button>
                        </div>
                    </div>
                )) : <p>Aucune commande trouvée</p>}
            </div>
            <button className="btn-link mt-4 w-full" onClick={onCreateManualSale}>
                + Créer une Vente Manuelle
            </button>
        </div>
    );
};

export default SellerOrders;
