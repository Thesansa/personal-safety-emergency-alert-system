import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import api from '../api/axios'

function Login() {
  const navigate = useNavigate()
  const [formData, setFormData] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const response = await api.post('/auth/login', formData)
      localStorage.setItem('accessToken', response.data.accessToken)
      localStorage.setItem('refreshToken', response.data.refreshToken)
      navigate('/dashboard')
    } catch (err) {
      const message = err.response?.data?.message || 'Login failed. Please try again.'
      setError(message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-background flex items-center justify-center px-4">
      <div className="bg-surface border border-border rounded-xl shadow-sm p-8 w-full max-w-md">
        <h1 className="text-2xl font-semibold text-text mb-1">Welcome back</h1>
        <p className="text-text-secondary mb-6">Log in to your safety profile</p>

        {error && (
          <div className="bg-red-50 border border-danger/20 text-danger text-sm rounded-lg px-4 py-3 mb-4">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <input
            type="email"
            name="email"
            placeholder="Email"
            value={formData.email}
            onChange={handleChange}
            required
            className="w-full border border-border rounded-lg px-4 py-2.5 text-text focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
          />
          <input
            type="password"
            name="password"
            placeholder="Password"
            value={formData.password}
            onChange={handleChange}
            required
            className="w-full border border-border rounded-lg px-4 py-2.5 text-text focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
          />

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-primary hover:bg-primary-hover text-white font-medium rounded-lg px-4 py-2.5 transition-colors disabled:opacity-60"
          >
            {loading ? 'Logging in...' : 'Log In'}
          </button>
        </form>

        <p className="text-center text-text-secondary text-sm mt-6">
          Don't have an account?{' '}
          <Link to="/register" className="text-primary hover:text-primary-hover font-medium">
            Register
          </Link>
        </p>
      </div>
    </div>
  )
}





export default Login