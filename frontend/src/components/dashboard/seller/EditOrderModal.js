import React from 'react';

const EditOrderModal = ({ order, isOpen, onClose, onUpdate }) => {
    const [localOrder, setLocalOrder] = React.useState(order);

    React.useEffect(() => {
        setLocalOrder(order);
    }, [order]);

    if (!isOpen || !order) return null;

    const handleSubmit = (e) => {
        e.preventDefault();
        onUpdate(localOrder);
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content glass-panel" style={{ background: 'white', padding: '2rem', borderRadius: '12px', width: '400px' }}>
                <h3>Modifier Commande #{localOrder?.id}</h3>
                <form onSubmit={handleSubmit}>
                    <div className="form-group mb-4">
                        <label className="block mb-1">Statut</label>
                        <select
                            className="w-full p-2 border rounded"
                            value={localOrder?.statut}
                            onChange={e => setLocalOrder({ ...localOrder, statut: e.target.value })}
                        >
                            <option value="EN_ATTENTE">En Attente</option>
                            <option value="CONFIRMEE">Confirmée</option>
                            <option value="LIVREE">Livrée</option>
                            <option value="ANNULEE">Annulée</option>
                        </select>
                    </div>
                    <div className="form-group mb-4">
                        <label className="block mb-1">Quantité (1er produit)</label>
                        <input
                            type="number"
                            className="w-full p-2 border rounded"
                            value={localOrder?.lignesCommande?.[0]?.quantite || localOrder?.quantite || 1}
                            onChange={e => {
                                const q = Number(e.target.value);
                                if (localOrder.lignesCommande && localOrder.lignesCommande.length > 0) {
                                    const newLignes = [...localOrder.lignesCommande];
                                    newLignes[0].quantite = q;
                                    setLocalOrder({ ...localOrder, lignesCommande: newLignes, quantite: q });
                                } else {
                                    setLocalOrder({ ...localOrder, quantite: q });
                                }
                            }}
                        />
                    </div>
                    <div className="flex gap-2 justify-end">
                        <button type="button" className="btn-secondary" onClick={onClose}>Annuler</button>
                        <button type="submit" className="btn-primary">Enregistrer</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default EditOrderModal;
