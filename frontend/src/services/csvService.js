import api from '../api/axiosConfig';

const csvService = {
    // Charger un fichier CSV
    uploadCsv: async (file) => {
        const formData = new FormData();
        formData.append('file', file);

        const response = await api.post('/csv/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
        return response.data;
    },

    // Déclencher un scraping (si endpoint existe dans ce controlleur)
    // Sinon c'est souvent lié

    // === EXPORTS (ExportController) ===
    exportProductsCsv: async () => {
        const response = await api.get('/export/produits/csv', { responseType: 'blob' });
        return response.data;
    },

    exportProductsExcel: async () => {
        const response = await api.get('/export/produits/excel', { responseType: 'blob' });
        return response.data;
    },

    exportSalesCsv: async (startDate, endDate) => {
        const response = await api.get(`/export/ventes/csv?dateDebut=${startDate}&dateFin=${endDate}`, { responseType: 'blob' });
        return response.data;
    },

    exportFullReport: async (startDate, endDate) => {
        const response = await api.get(`/export/rapport-complet?dateDebut=${startDate}&dateFin=${endDate}`, { responseType: 'blob' });
        return response.data;
    }
};

export default csvService;
