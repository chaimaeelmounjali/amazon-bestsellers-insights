import api from '../api/axiosConfig';

const userManagementService = {
    getAllUsers: async (page = 0, size = 10, search = '', role = '', active = null) => {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString()
        });

        if (search) params.append('search', search);
        if (role) params.append('role', role);
        if (active !== null) params.append('active', active.toString());

        const response = await api.get(`/admin/users?${params}`);
        return response.data;
    },

    getUserById: async (id) => {
        const response = await api.get(`/admin/users/${id}`);
        return response.data;
    },

    createUser: async (userData) => {
        const response = await api.post('/admin/users', userData);
        return response.data;
    },

    updateUser: async (id, userData) => {
        const response = await api.put(`/admin/users/${id}`, userData);
        return response.data;
    },

    deleteUser: async (id) => {
        await api.delete(`/admin/users/${id}`);
    },

    toggleUserStatus: async (id) => {
        await api.put(`/admin/users/${id}/toggle-status`);
    },

    getUserStatistics: async () => {
        const response = await api.get('/admin/users/statistics');
        return response.data;
    },

    getUserPredictions: async () => {
        const response = await api.get('/admin/users/predictions');
        return response.data;
    }
};

export default userManagementService;
