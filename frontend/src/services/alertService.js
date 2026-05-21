import api from '../api/axiosConfig';

const AlertService = {
    getUserAlerts: async (userId) => {
        const response = await api.get(`/alertes/utilisateur/${userId}`);
        return response.data;
    },

    getUnreadAlerts: async (userId) => {
        const response = await api.get(`/alertes/non-lues/${userId}`);
        return response.data;
    },

    markAsRead: async (alertId) => {
        await api.put(`/alertes/${alertId}/lire`);
    },

    markAllAsRead: async (userId) => {
        await api.put(`/alertes/lire-toutes/${userId}`);
    }
};

export default AlertService;
