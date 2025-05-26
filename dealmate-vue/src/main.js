import { createApp } from 'vue'
import { auth } from './auth/auth.js' // Import auth module
import App from './App.vue'
import router from './router'

// Inicjalizacja
auth.init().then(() => {
    const app = createApp(App)
    app.use(router)
    app.mount('#app')
})
