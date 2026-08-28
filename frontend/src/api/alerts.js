import api from './axios'

export const triggerAlert = (data) => api.post('/alerts/trigger', data)

export const getAlerts = () => api.get('/alerts')

export const cancelAlert = (id) => api.post(`/alerts/${id}/cancel`)

export const resolveAlert = (id) => api.post(`/alerts/${id}/resolve`)