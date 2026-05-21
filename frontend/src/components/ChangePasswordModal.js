import React, { useState, useEffect } from 'react';
import passwordService from '../services/passwordService';

const ChangePasswordModal = ({ isOpen, onClose, onSuccess }) => {
    const [formData, setFormData] = useState({
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
    });
    const [showPasswords, setShowPasswords] = useState({
        old: false,
        new: false,
        confirm: false
    });
    const [passwordStrength, setPasswordStrength] = useState(null);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (formData.newPassword) {
            checkPasswordStrength(formData.newPassword);
        } else {
            setPasswordStrength(null);
        }
    }, [formData.newPassword]);

    const checkPasswordStrength = async (password) => {
        try {
            const strength = await passwordService.validatePasswordStrength(password);
            setPasswordStrength(strength);
        } catch (err) {
            console.error('Error checking password strength:', err);
        }
    };

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
        setError('');
    };

    const togglePasswordVisibility = (field) => {
        setShowPasswords({
            ...showPasswords,
            [field]: !showPasswords[field]
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (formData.newPassword !== formData.confirmPassword) {
            setError('Les nouveaux mots de passe ne correspondent pas');
            return;
        }

        if (passwordStrength && passwordStrength.level === 'weak') {
            setError('Le mot de passe est trop faible');
            return;
        }

        setLoading(true);
        try {
            await passwordService.changePassword(
                formData.oldPassword,
                formData.newPassword,
                formData.confirmPassword
            );

            if (onSuccess) onSuccess();
            handleClose();
        } catch (err) {
            setError(err.response?.data?.error || 'Erreur lors du changement de mot de passe');
        } finally {
            setLoading(false);
        }
    };

    const handleClose = () => {
        setFormData({ oldPassword: '', newPassword: '', confirmPassword: '' });
        setError('');
        setPasswordStrength(null);
        onClose();
    };

    if (!isOpen) return null;

    const getStrengthColor = () => {
        if (!passwordStrength) return 'bg-gray-300';
        switch (passwordStrength.level) {
            case 'weak': return 'bg-red-500';
            case 'medium': return 'bg-yellow-500';
            case 'strong': return 'bg-green-500';
            default: return 'bg-gray-300';
        }
    };

    const getStrengthWidth = () => {
        if (!passwordStrength) return '0%';
        return `${(passwordStrength.score / 6) * 100}%`;
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-2xl shadow-2xl p-8 max-w-md w-full mx-4">
                <div className="flex justify-between items-center mb-6">
                    <h2 className="text-2xl font-bold text-gray-900">Changer le mot de passe</h2>
                    <button
                        onClick={handleClose}
                        className="text-gray-400 hover:text-gray-600 transition-colors"
                    >
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                    {/* Old Password */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Ancien mot de passe
                        </label>
                        <div className="relative">
                            <input
                                type={showPasswords.old ? 'text' : 'password'}
                                name="oldPassword"
                                value={formData.oldPassword}
                                onChange={handleChange}
                                required
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
                            />
                            <button
                                type="button"
                                onClick={() => togglePasswordVisibility('old')}
                                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-gray-700"
                            >
                                {showPasswords.old ? '👁️' : '👁️‍🗨️'}
                            </button>
                        </div>
                    </div>

                    {/* New Password */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Nouveau mot de passe
                        </label>
                        <div className="relative">
                            <input
                                type={showPasswords.new ? 'text' : 'password'}
                                name="newPassword"
                                value={formData.newPassword}
                                onChange={handleChange}
                                required
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
                            />
                            <button
                                type="button"
                                onClick={() => togglePasswordVisibility('new')}
                                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-gray-700"
                            >
                                {showPasswords.new ? '👁️' : '👁️‍🗨️'}
                            </button>
                        </div>

                        {/* Password Strength Indicator */}
                        {formData.newPassword && (
                            <div className="mt-2">
                                <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                                    <div
                                        className={`h-full ${getStrengthColor()} transition-all duration-300`}
                                        style={{ width: getStrengthWidth() }}
                                    ></div>
                                </div>
                                {passwordStrength && (
                                    <p className={`text-xs mt-1 ${passwordStrength.level === 'weak' ? 'text-red-600' :
                                            passwordStrength.level === 'medium' ? 'text-yellow-600' :
                                                'text-green-600'
                                        }`}>
                                        {passwordStrength.message}
                                    </p>
                                )}
                            </div>
                        )}
                    </div>

                    {/* Confirm Password */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Confirmer le mot de passe
                        </label>
                        <div className="relative">
                            <input
                                type={showPasswords.confirm ? 'text' : 'password'}
                                name="confirmPassword"
                                value={formData.confirmPassword}
                                onChange={handleChange}
                                required
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
                            />
                            <button
                                type="button"
                                onClick={() => togglePasswordVisibility('confirm')}
                                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-gray-700"
                            >
                                {showPasswords.confirm ? '👁️' : '👁️‍🗨️'}
                            </button>
                        </div>
                        {formData.confirmPassword && formData.newPassword !== formData.confirmPassword && (
                            <p className="text-xs text-red-600 mt-1">
                                Les mots de passe ne correspondent pas
                            </p>
                        )}
                    </div>

                    {/* Error Message */}
                    {error && (
                        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
                            {error}
                        </div>
                    )}

                    {/* Buttons */}
                    <div className="flex gap-3 mt-6">
                        <button
                            type="button"
                            onClick={handleClose}
                            className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                        >
                            Annuler
                        </button>
                        <button
                            type="submit"
                            disabled={loading || (passwordStrength && passwordStrength.level === 'weak')}
                            className="flex-1 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-hover transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed"
                        >
                            {loading ? 'Changement...' : 'Changer'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ChangePasswordModal;
