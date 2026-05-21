import React from 'react';
import { Navigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';

const ProtectedRoute = ({ children, allowedRoles = [] }) => {
    const { user, isAuthenticated, loading } = useAuth();

    if (loading) {
        return <div>Chargement...</div>;
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
        // Rediriger si on n'a pas le bon rôle (ex: vers dashboard par défaut)
        return <Navigate to="/dashboard" replace />;
    }

    return children;
};

export default ProtectedRoute;
