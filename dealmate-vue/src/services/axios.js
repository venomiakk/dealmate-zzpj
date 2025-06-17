import axios from 'axios'
import { auth } from '@/auth/auth'

// Globalny interceptor dodający token do każdego żądania i logujący go
axios.interceptors.request.use(
    async (config) => {
        if (config.headers && config.headers['Skip-Auth']) {
            delete config.headers['Skip-Auth'] // nie wysyłaj go do serwera
            return config
        }

        const token = await auth.getToken() // czekamy aż Promise się rozwiąże
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        console.error('Request error:', error)
        return Promise.reject(error)
    },
)

export default axios
