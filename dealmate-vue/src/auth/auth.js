import { UserManager } from 'oidc-client-ts'
import { reactive } from 'vue'

const config = {
    authority: 'http://127.0.0.1:9000',
    client_id: 'public-client',
    redirect_uri: 'http://localhost:5173/callback',
    response_type: 'code',
    scope: 'openid profile email',
    post_logout_redirect_uri: 'http://localhost:5173/',
    automaticSilentRenew: true,
}

// Manager użytkowników
const userManager = new UserManager(config)

// Globalny stan
export const authState = reactive({
    user: null,
    isAuthenticated: false,
    isLoading: true,
    login: null,
})

// Funkcje auth
export const auth = {
    async init() {
        try {
            const user = await userManager.getUser()
            authState.user = user
            authState.isAuthenticated = !!user && !user.expired
            authState.login = user?.profile?.sub || null
        } catch (error) {
            console.error('Auth init error:', error)
        } finally {
            authState.isLoading = false
        }
    },

    async login() {
        await userManager.signinRedirect()
    },

    async handleCallback() {
        const user = await userManager.signinRedirectCallback()
        authState.user = user
        authState.isAuthenticated = true
        authState.login = user?.profile?.sub || null
        // Usuń parametry z URL
        window.history.replaceState({}, document.title, window.location.pathname)
    },

    async logout() {
        await userManager.signoutRedirect()
    },

    async getToken() {
        const user = await userManager.getUser()
        return user?.access_token
    },
}
