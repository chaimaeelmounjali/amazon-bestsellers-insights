import api from '../api/axiosConfig';

const passwordService = {
    changePassword: async (oldPassword, newPassword, confirmPassword) => {
        const response = await api.post('/users/change-password', {
            oldPassword,
            newPassword,
            confirmPassword
        });
        return response.data;
    },

    validatePasswordStrength: async (password) => {
        const response = await api.post('/users/validate-password-strength', {
            password
        });
        return response.data;
    }
};

export default passwordService;
