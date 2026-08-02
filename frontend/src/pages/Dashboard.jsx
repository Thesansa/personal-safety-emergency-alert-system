import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { ShieldCheck, LogOut } from 'lucide-react'
import api from '../api/axios'

function Dashboard() {
  const navigate = useNavigate()

  useEffect(() => {
    // Basic route protection — no token means no business being here
    const token = localStorage.getItem('accessToken')
    if (!token) {
      navigate('/login')
    }
  }, [navigate])

  const handleLogout = async () => {
    try {
      const token = localStorage.getItem('accessToken')
      await api.post('/auth/logout', null, {
        headers: { Authorization: `Bearer ${token}` },
      })
    } catch (err) {
      // Even if the server call fails, still clear local tokens and log out client-side
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

        <div className="bg-surface border border-border rounded-xl shadow-sm p-8 text-center">
          <p className="text-text-secondary mb-1">👋 Welcome back</p>
          <h2 className="text-2xl font-semibold text-success mb-4">You're Safe</h2>
          <p className="text-text-secondary text-sm">No active alerts</p>
        </div>
      </div>
    </div>
  )
}

export default Dashboard