import React, { useState, useEffect, useCallback } from 'react';
import userManagementService from '../../services/userManagementService';
import ChangePasswordModal from '../../components/ChangePasswordModal';

const UserManagementPage = () => {
    const [users, setUsers] = useState([]);
    const [statistics, setStatistics] = useState(null);
    const [predictions, setPredictions] = useState(null);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [searchTerm, setSearchTerm] = useState('');
    const [roleFilter, setRoleFilter] = useState('');
    const [statusFilter, setStatusFilter] = useState(null);
    const [showPasswordModal, setShowPasswordModal] = useState(false);
    const [showUserModal, setShowUserModal] = useState(false);
    const [userForm, setUserForm] = useState({
        nomUtilisateur: '',
        email: '',
        password: '',
        role: 'ACHETEUR'
    });

    const fetchData = useCallback(async () => {
        setLoading(true);

        // Fetch users (Critical)
        try {
            const usersData = await userManagementService.getAllUsers(page, 10, searchTerm, roleFilter, statusFilter);
            setUsers(usersData.users);
            setTotalPages(usersData.totalPages);
        } catch (error) {
            console.error('Error fetching users:', error);
            // Optionally set error state for users
        }

        // Fetch statistics (Non-critical)
        try {
            const statsData = await userManagementService.getUserStatistics();
            setStatistics(statsData);
        } catch (error) {
            console.error('Error fetching statistics:', error);
        }

        // Fetch predictions (Non-critical)
        try {
            const predsData = await userManagementService.getUserPredictions();
            setPredictions(predsData);
        } catch (error) {
            console.error('Error fetching predictions:', error);
        }

        setLoading(false);
    }, [page, searchTerm, roleFilter, statusFilter]);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    const handleCreateUser = async (e) => {
        e.preventDefault();
        try {
            await userManagementService.createUser(userForm);
            setShowUserModal(false);
            setUserForm({ nomUtilisateur: '', email: '', password: '', role: 'ACHETEUR' });
            fetchData();
        } catch (error) {
            console.error('Error creating user:', error);
            alert('Erreur lors de la création de l\'utilisateur');
        }
    };

    const handleDeleteUser = async (userId) => {
        if (window.confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) {
            try {
                await userManagementService.deleteUser(userId);
                fetchData();
            } catch (error) {
                console.error('Error deleting user:', error);
                alert('Erreur lors de la suppression');
            }
        }
    };

    const handleToggleStatus = async (userId) => {
        try {
            await userManagementService.toggleUserStatus(userId);
            fetchData();
        } catch (error) {
            console.error('Error toggling status:', error);
        }
    };

    const getRoleBadgeColor = (role) => {
        switch (role) {
            case 'ADMIN': return 'bg-purple-100 text-purple-800';
            case 'VENDEUR': return 'bg-blue-100 text-blue-800';
            case 'ACHETEUR': return 'bg-green-100 text-green-800';
            case 'INVESTISSEUR': return 'bg-yellow-100 text-yellow-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 py-8 px-4">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="flex justify-between items-center mb-8">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900">Gestion des Utilisateurs</h1>
                        <p className="text-gray-600 mt-1">Gérez tous les utilisateurs de la plateforme</p>
                    </div>
                    <button
                        onClick={() => setShowPasswordModal(true)}
                        className="px-4 py-2 bg-gray-200 hover:bg-gray-300 text-gray-800 rounded-lg transition-colors"
                    >
                        🔒 Changer mon mot de passe
                    </button>
                </div>

                {/* Statistics Cards */}
                {statistics && (
                    <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                        <div className="bg-white rounded-xl shadow-md p-6">
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-gray-600 text-sm">Total Utilisateurs</p>
                                    <p className="text-3xl font-bold text-gray-900 mt-1">{statistics.total}</p>
                                </div>
                                <div className="bg-blue-100 p-3 rounded-lg">
                                    <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                                    </svg>
                                </div>
                            </div>
                        </div>

                        <div className="bg-white rounded-xl shadow-md p-6">
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-gray-600 text-sm">Admins</p>
                                    <p className="text-3xl font-bold text-purple-600 mt-1">{statistics.byRole?.ADMIN || 0}</p>
                                </div>
                                <div className="bg-purple-100 p-3 rounded-lg">
                                    <svg className="w-8 h-8 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                                    </svg>
                                </div>
                            </div>
                        </div>

                        <div className="bg-white rounded-xl shadow-md p-6">
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-gray-600 text-sm">Vendeurs</p>
                                    <p className="text-3xl font-bold text-blue-600 mt-1">{statistics.byRole?.VENDEUR || 0}</p>
                                </div>
                                <div className="bg-blue-100 p-3 rounded-lg">
                                    <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
                                    </svg>
                                </div>
                            </div>
                        </div>

                        <div className="bg-white rounded-xl shadow-md p-6">
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-gray-600 text-sm">Acheteurs</p>
                                    <p className="text-3xl font-bold text-green-600 mt-1">{statistics.byRole?.ACHETEUR || 0}</p>
                                </div>
                                <div className="bg-green-100 p-3 rounded-lg">
                                    <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                                    </svg>
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {/* Predictions Section */}
                {predictions && (
                    <div className="bg-white rounded-xl shadow-md p-6 mb-8">
                        <h2 className="text-xl font-bold text-gray-900 mb-4">📈 Prédictions de Croissance</h2>
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                            <div className="text-center p-4 bg-blue-50 rounded-lg">
                                <p className="text-sm text-gray-600">Dans 1 mois</p>
                                <p className="text-2xl font-bold text-blue-600">+{predictions.predictions['1month']}</p>
                            </div>
                            <div className="text-center p-4 bg-green-50 rounded-lg">
                                <p className="text-sm text-gray-600">Dans 3 mois</p>
                                <p className="text-2xl font-bold text-green-600">+{predictions.predictions['3months']}</p>
                            </div>
                            <div className="text-center p-4 bg-yellow-50 rounded-lg">
                                <p className="text-sm text-gray-600">Dans 6 mois</p>
                                <p className="text-2xl font-bold text-yellow-600">+{predictions.predictions['6months']}</p>
                            </div>
                            <div className="text-center p-4 bg-purple-50 rounded-lg">
                                <p className="text-sm text-gray-600">Dans 12 mois</p>
                                <p className="text-2xl font-bold text-purple-600">+{predictions.predictions['12months']}</p>
                            </div>
                        </div>
                        <p className="text-sm text-gray-600 mt-4">
                            Taux de croissance moyen: <span className="font-semibold text-primary">{predictions.growthRate?.toFixed(2)}%</span> par mois
                        </p>
                    </div>
                )}

                {/* User Management Table */}
                <div className="bg-white rounded-xl shadow-md overflow-hidden">
                    {/* Filters and Search */}
                    <div className="p-6 border-b border-gray-200">
                        <div className="flex flex-col md:flex-row gap-4">
                            <input
                                type="text"
                                placeholder="Rechercher par nom ou email..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
                            />
                            <select
                                value={roleFilter}
                                onChange={(e) => setRoleFilter(e.target.value)}
                                className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
                            >
                                <option value="">Tous les rôles</option>
                                <option value="ADMIN">Admin</option>
                                <option value="VENDEUR">Vendeur</option>
                                <option value="ACHETEUR">Acheteur</option>
                                <option value="INVESTISSEUR">Investisseur</option>
                            </select>
                            <select
                                value={statusFilter === null ? '' : statusFilter.toString()}
                                onChange={(e) => setStatusFilter(e.target.value === '' ? null : e.target.value === 'true')}
                                className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
                            >
                                <option value="">Tous les statuts</option>
                                <option value="true">Actifs</option>
                                <option value="false">Inactifs</option>
                            </select>
                            <button
                                onClick={() => setShowUserModal(true)}
                                className="px-6 py-2 bg-primary hover:bg-primary-hover text-white rounded-lg transition-colors whitespace-nowrap"
                            >
                                + Ajouter
                            </button>
                        </div>
                    </div>

                    {/* Table */}
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Utilisateur</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Rôle</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Statut</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date création</th>
                                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                                </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                                {loading ? (
                                    <tr>
                                        <td colSpan="6" className="px-6 py-12 text-center text-gray-500">
                                            Chargement...
                                        </td>
                                    </tr>
                                ) : users.length === 0 ? (
                                    <tr>
                                        <td colSpan="6" className="px-6 py-12 text-center text-gray-500">
                                            Aucun utilisateur trouvé
                                        </td>
                                    </tr>
                                ) : (
                                    users.map((user) => (
                                        <tr key={user.id} className="hover:bg-gray-50">
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <div className="font-medium text-gray-900">{user.nomUtilisateur}</div>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                                                {user.email}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getRoleBadgeColor(user.role)}`}>
                                                    {user.role}
                                                </span>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <button
                                                    onClick={() => handleToggleStatus(user.id)}
                                                    className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${user.estActif
                                                        ? 'bg-green-100 text-green-800'
                                                        : 'bg-red-100 text-red-800'
                                                        }`}
                                                >
                                                    {user.estActif ? 'Actif' : 'Inactif'}
                                                </button>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                                                {new Date(user.dateCreation).toLocaleDateString('fr-FR')}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                                <button
                                                    onClick={() => handleDeleteUser(user.id)}
                                                    className="text-red-600 hover:text-red-900 transition-colors"
                                                >
                                                    Supprimer
                                                </button>
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>

                    {/* Pagination */}
                    {totalPages > 1 && (
                        <div className="px-6 py-4 border-t border-gray-200 flex justify-between items-center">
                            <button
                                onClick={() => setPage(Math.max(0, page - 1))}
                                disabled={page === 0}
                                className="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors"
                            >
                                Précédent
                            </button>
                            <span className="text-sm text-gray-600">
                                Page {page + 1} sur {totalPages}
                            </span>
                            <button
                                onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                                disabled={page >= totalPages - 1}
                                className="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors"
                            >
                                Suivant
                            </button>
                        </div>
                    )}
                </div>
            </div>

            {/* Password Change Modal */}
            <ChangePasswordModal
                isOpen={showPasswordModal}
                onClose={() => setShowPasswordModal(false)}
                onSuccess={() => alert('Mot de passe changé avec succès!')}
            />

            {/* Create User Modal */}
            {showUserModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                    <div className="bg-white rounded-2xl shadow-2xl p-8 max-w-md w-full mx-4">
                        <h2 className="text-2xl font-bold text-gray-900 mb-6">Nouvel Utilisateur</h2>
                        <form onSubmit={handleCreateUser} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Nom</label>
                                <input
                                    type="text"
                                    required
                                    value={userForm.nomUtilisateur}
                                    onChange={(e) => setUserForm({ ...userForm, nomUtilisateur: e.target.value })}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Email</label>
                                <input
                                    type="email"
                                    required
                                    value={userForm.email}
                                    onChange={(e) => setUserForm({ ...userForm, email: e.target.value })}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Mot de passe</label>
                                <input
                                    type="password"
                                    required
                                    value={userForm.password}
                                    onChange={(e) => setUserForm({ ...userForm, password: e.target.value })}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Rôle</label>
                                <select
                                    value={userForm.role}
                                    onChange={(e) => setUserForm({ ...userForm, role: e.target.value })}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
                                >
                                    <option value="ACHETEUR">Acheteur</option>
                                    <option value="VENDEUR">Vendeur</option>
                                    <option value="ADMIN">Admin</option>
                                    <option value="INVESTISSEUR">Investisseur</option>
                                </select>
                            </div>
                            <div className="flex gap-3 mt-6">
                                <button
                                    type="button"
                                    onClick={() => setShowUserModal(false)}
                                    className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                                >
                                    Annuler
                                </button>
                                <button
                                    type="submit"
                                    className="flex-1 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-hover transition-colors"
                                >
                                    Créer
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default UserManagementPage;
