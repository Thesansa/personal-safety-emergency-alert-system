import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft, Plus, Trash2, Pencil, X } from 'lucide-react'
import {
    getTrustedContacts,
    createTrustedContact,
    updateTrustedContact,
    deleteTrustedContact,
} from '../api/trustedContacts'

const emptyForm = { name: '', contactNo: '', email: '', relation: '', priorityOrder: '' }

function TrustedContacts() {
    const navigate = useNavigate()
    const [contacts, setContacts] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    const [showForm, setShowForm] = useState(false)
    const [editingId, setEditingId] = useState(null)
    const [formData, setFormData] = useState(emptyForm)
    const [saving, setSaving] = useState(false)



    const refreshContacts = async () => {
        try {
            const response = await getTrustedContacts()
            setContacts(response.data)
        } catch (err) {
            setError(err.response?.data?.message || 'Could not load trusted contacts.')
        }
    }

    useEffect(() => {
        let ignore = false

        const load = async () => {
            setLoading(true)
            try {
                const response = await getTrustedContacts()
                if (!ignore) setContacts(response.data)
            } catch (err) {
                if (!ignore) setError(err.response?.data?.message || 'Could not load trusted contacts.')
            } finally {
                if (!ignore) setLoading(false)
            }
        }

        load()
        return () => { ignore = true }
    }, [])

    const openAddForm = () => {
        setEditingId(null)
        setFormData(emptyForm)
        setShowForm(true)
    }

    const openEditForm = (contact) => {
        setEditingId(contact.id)
        setFormData({
            name: contact.name,
            contactNo: contact.contactNo,
            email: contact.email || '',
            relation: contact.relation || '',
            priorityOrder: contact.priorityOrder ?? '',
        })
        setShowForm(true)
    }

    const closeForm = () => {
        setShowForm(false)
        setEditingId(null)
        setFormData(emptyForm)
    }

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value })
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        setSaving(true)
        setError('')

        const payload = {
            ...formData,
            priorityOrder: formData.priorityOrder ? Number(formData.priorityOrder) : null,
        }

        try {
            if (editingId) {
                await updateTrustedContact(editingId, payload)
            } else {
                await createTrustedContact(payload)
            }
            closeForm()
            await refreshContacts()
        } catch (err) {
            const message = err.response?.data?.message || 'Could not save contact.'
            setError(message)
        } finally {
            setSaving(false)
        }
    }

    const handleDelete = async (id) => {
        if (!window.confirm('Remove this trusted contact?')) return

        try {
            await deleteTrustedContact(id)
            setContacts(contacts.filter((c) => c.id !== id))
        } catch (err) {
            setError(err.response?.data?.message || 'Could not delete contact.')
        }
    }

    return (
        <div className="min-h-screen bg-background px-4 py-10">
            <div className="max-w-2xl mx-auto">
                <div className="flex items-center gap-3 mb-8">
                    <button
                        onClick={() => navigate('/dashboard')}
                        className="text-text-secondary hover:text-text"
                    >
                        <ArrowLeft size={20} />
                    </button>
                    <h1 className="text-xl font-semibold text-text">Trusted Contacts</h1>
                </div>

                {error && (
                    <div className="bg-red-50 border border-danger/20 text-danger text-sm rounded-lg px-4 py-3 mb-4">
                        {error}
                    </div>
                )}

                {!showForm && (
                    <button
                        onClick={openAddForm}
                        className="flex items-center gap-2 bg-primary hover:bg-primary-hover text-white font-medium rounded-lg px-4 py-2.5 mb-6 transition-colors"
                    >
                        <Plus size={18} />
                        Add Contact
                    </button>
                )}

                {showForm && (
                    <form
                        onSubmit={handleSubmit}
                        className="bg-surface border border-border rounded-xl shadow-sm p-6 mb-6 space-y-4"
                    >
                        <div className="flex items-center justify-between mb-2">
                            <h2 className="font-medium text-text">
                                {editingId ? 'Edit Contact' : 'New Contact'}
                            </h2>
                            <button type="button" onClick={closeForm} className="text-text-secondary hover:text-text">
                                <X size={18} />
                            </button>
                        </div>

                        <input
                            type="text"
                            name="name"
                            placeholder="Full Name"
                            value={formData.name}
                            onChange={handleChange}
                            required
                            className="w-full border border-border rounded-lg px-4 py-2.5 text-text focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                        />
                        <input
                            type="tel"
                            name="contactNo"
                            placeholder="Contact Number"
                            value={formData.contactNo}
                            onChange={handleChange}
                            required
                            className="w-full border border-border rounded-lg px-4 py-2.5 text-text focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                        />
                        <input
                            type="email"
                            name="email"
                            placeholder="Email (optional)"
                            value={formData.email}
                            onChange={handleChange}
                            className="w-full border border-border rounded-lg px-4 py-2.5 text-text focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                        />
                        <input
                            type="text"
                            name="relation"
                            placeholder="Relationship (e.g. Mother, Friend)"
                            value={formData.relation}
                            onChange={handleChange}
                            className="w-full border border-border rounded-lg px-4 py-2.5 text-text focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                        />
                        <input
                            type="number"
                            name="priorityOrder"
                            placeholder="Priority order (e.g. 1)"
                            value={formData.priorityOrder}
                            onChange={handleChange}
                            min={1}
                            className="w-full border border-border rounded-lg px-4 py-2.5 text-text focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                        />

                        <button
                            type="submit"
                            disabled={saving}
                            className="w-full bg-primary hover:bg-primary-hover text-white font-medium rounded-lg px-4 py-2.5 transition-colors disabled:opacity-60"
                        >
                            {saving ? 'Saving...' : editingId ? 'Save Changes' : 'Add Contact'}
                        </button>
                    </form>
                )}

                {loading ? (
                    <p className="text-text-secondary text-sm">Loading...</p>
                ) : contacts.length === 0 ? (
                    <div className="bg-surface border border-border rounded-xl p-8 text-center text-text-secondary text-sm">
                        No trusted contacts yet. Add someone who should be notified in an emergency.
                    </div>
                ) : (
                    <div className="space-y-3">
                        {contacts.map((contact) => (
                            <div
                                key={contact.id}
                                className="bg-surface border border-border rounded-xl p-4 flex items-center justify-between"
                            >
                                <div>
                                    <p className="font-medium text-text">{contact.name}</p>
                                    <p className="text-text-secondary text-sm">
                                        {contact.contactNo}
                                        {contact.relation ? ` · ${contact.relation}` : ''}
                                        {contact.priorityOrder ? ` · Priority ${contact.priorityOrder}` : ''}
                                    </p>
                                </div>
                                <div className="flex items-center gap-3">
                                    <button
                                        onClick={() => openEditForm(contact)}
                                        className="text-text-secondary hover:text-primary"
                                    >
                                        <Pencil size={18} />
                                    </button>
                                    <button
                                        onClick={() => handleDelete(contact.id)}
                                        className="text-text-secondary hover:text-danger"
                                    >
                                        <Trash2 size={18} />
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    )
}

export default TrustedContacts