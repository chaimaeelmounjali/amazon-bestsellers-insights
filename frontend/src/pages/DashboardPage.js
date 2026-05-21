import React from 'react';
import useAuth from '../hooks/useAuth';
import AdminDashboard from '../components/dashboard/AdminDashboard';
import SellerDashboard from '../components/dashboard/SellerDashboard';
import BuyerDashboard from '../components/dashboard/BuyerDashboard';
import InvestorDashboard from '../components/dashboard/InvestorDashboard';

const DashboardRedirect = () => {
    const { user } = useAuth();

    if (!user) return <div className="loading-screen">Chargement du profil...</div>;

    switch (user.role) {
        case 'ACHETEUR':
            return <BuyerDashboard user={user} />;
        case 'VENDEUR':
            return <SellerDashboard user={user} />;
        case 'ADMIN':
            return <AdminDashboard user={user} />;
        case 'INVESTISSEUR':
            return <InvestorDashboard user={user} />;
        default:
            return (
                <div className="error-container">
                    <h2>Rôle non reconnu</h2>
                    <p>Votre compte n'a pas de rôle assigné valide ({user.role}).</p>
                </div>
            );
    }
};

export default DashboardRedirect;
