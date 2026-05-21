import React, { createContext, useState, useEffect } from 'react';
import authService from '../services/authService';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    // Vérifier la session au chargement de l'app
    useEffect(() => {
        const checkUser = async () => {
            try {
                const currentUser = await authService.getCurrentUser();
                setUser(currentUser);
            } catch (error) {
                console.log("Utilisateur non connecté");
                setUser(null);
            } finally {
                setLoading(false);
            }
        };
        checkUser();
    }, []);

    const login = async (loginData) => {
        const userData = await authService.login(loginData);
        setUser(userData);
        return userData;
    };

    const register = async (registerData) => {
        const userData = await authService.register(registerData);
        setUser(userData); // Optionnel : connecter directement après inscription
        return userData;
    };

    const logout = async () => {
        await authService.logout();
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, register, logout, loading, isAuthenticated: !!user }}>
            {!loading && children}
        </AuthContext.Provider>
    );
};
