import api from './axios'

export const getTrustedContacts = () => api.get('/trusted-contacts')

export const getTrustedContact = (id) => api.get(`/trusted-contacts/${id}`)

export const createTrustedContact = (data) => api.post('/trusted-contacts', data)

export const updateTrustedContact = (id, data) => api.put(`/trusted-contacts/${id}`, data)

export const deleteTrustedContact = (id) => api.delete(`/trusted-contacts/${id}`)