import axios from 'axios'
import { auth } from '@/auth/auth'

// Globalny interceptor dodający token do każdego żądania i logujący go
axios.interceptors.request.use(
    async (config) => {
        const token = await auth.getToken() // czekamy aż Promise się rozwiąże
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    },
)

export default axios
