import { useEffect, useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { ShieldCheck, ShieldAlert, LogOut, Users, TriangleAlert } from 'lucide-react'
import api from '../api/axios'
import { triggerAlert, getAlerts, cancelAlert, resolveAlert } from '../api/alerts'

function Dashboard() {
  const navigate = useNavigate()
  const [activeAlert, setActiveAlert] = useState(null)
  const [triggering, setTriggering] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    const token = localStorage.getItem('accessToken')
    if (!token) {
      navigate('/login')
    }
  }, [navigate])

  const checkForActiveAlert = useCallback(async () => {
    try {
      const response = await getAlerts()
      const active = response.data.find(
        (a) => a.status === 'ACTIVE' || a.status === 'ESCALATED'
      )
      setActiveAlert(active || null)
    } catch (err) {
      // Silent — status polling shouldn't interrupt the page with an error banner
    }
  }, [])

  useEffect(() => {
    checkForActiveAlert()
    const interval = setInterval(checkForActiveAlert, 5000)
    return () => clearInterval(interval)
  }, [checkForActiveAlert])

  const handleTrigger = () => {
    setError('')
    setTriggering(true)

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        try {
          const response = await triggerAlert({
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
          })
          setActiveAlert(response.data)
        } catch (err) {
          setError(err.response?.data?.message || 'Could not trigger alert.')
        } finally {
          setTriggering(false)
        }
      },
      async () => {
        // Location denied/unavailable — still trigger the alert without it
        try {
          const response = await triggerAlert({})
          setActiveAlert(response.data)
        } catch (err) {
          setError(err.response?.data?.message || 'Could not trigger alert.')
        } finally {
          setTriggering(false)
        }
      }
    )
  }

  const handleCancel = async () => {
    try {
      await cancelAlert(activeAlert.id)
      setActiveAlert(null)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not cancel alert.')
    }
  }

  const handleResolve = async () => {
    try {
      await resolveAlert(activeAlert.id)
      setActiveAlert(null)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not resolve alert.')
    }
  }

  const handleLogout = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      await api.post('/auth/logout', null, {
        headers: { Authorization: `Bearer ${token}` },
      })
    } catch (err) {
      console.error('Logout request failed:', err)
    } finally {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      navigate('/login')
    }
  }

  return (
    <div className="min-h-screen bg-background px-4 py-10">
      <div className="max-w-2xl mx-auto">
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center gap-2">
            <ShieldCheck className="text-primary" size={28} />
            <h1 className="text-xl font-semibold text-text">SOS Safety</h1>
          </div>
          <button
            onClick={handleLogout}
            className="flex items-center gap-2 text-text-secondary hover:text-text text-sm font-medium"
          >
            <LogOut size={16} />
            Log out
          </button>
        </div>

        {error && (
          <div className="bg-red-50 border border-danger/20 text-danger text-sm rounded-lg px-4 py-3 mb-4">
            {error}
          </div>
        )}

        {activeAlert ? (
          <div className="bg-red-50 border border-danger/30 rounded-xl shadow-sm p-8 text-center">
            <ShieldAlert className="text-danger mx-auto mb-3" size={40} />
            <h2 className="text-2xl font-semibold text-danger mb-1">
              {activeAlert.status === 'ESCALATED' ? 'Alert Escalated' : 'Alert Active'}
            </h2>
            <p className="text-text-secondary text-sm mb-6">
              Triggered at {new Date(activeAlert.triggeredAt).toLocaleTimeString()}
              {activeAlert.status === 'ESCALATED' && ' · Trusted contacts have been notified'}
            </p>

            <div className="flex gap-3 justify-center">
              <button
                onClick={handleCancel}
                className="bg-white border border-border text-text font-medium rounded-lg px-5 py-2.5 hover:bg-gray-50 transition-colors"
              >
                Cancel — False Alarm
              </button>
              <button
                onClick={handleResolve}
                className="bg-success hover:bg-green-700 text-white font-medium rounded-lg px-5 py-2.5 transition-colors"
              >
                I'm Safe Now
              </button>
            </div>
          </div>
        ) : (
          <div className="bg-surface border border-border rounded-xl shadow-sm p-8 text-center">
            <p className="text-text-secondary mb-1">👋 Welcome back</p>
            <h2 className="text-2xl font-semibold text-success mb-6">You're Safe</h2>

            <button
              onClick={handleTrigger}
              disabled={triggering}
              className="flex items-center gap-2 justify-center w-full bg-danger hover:bg-red-700 text-white font-semibold text-lg rounded-xl px-6 py-4 transition-colors disabled:opacity-60"
            >
              <TriangleAlert size={22} />
              {triggering ? 'Triggering...' : 'Trigger SOS'}
            </button>
          </div>
        )}

        <button
          onClick={() => navigate('/trusted-contacts')}
          className="flex items-center gap-2 mt-4 text-primary hover:text-primary-hover text-sm font-medium mx-auto"
        >
          <Users size={16} />
          Manage Trusted Contacts
        </button>
      </div>
    </div>
  )
}

export default Dashboard