import { createApp } from 'vue'
import { auth } from './auth/auth.js' // Import auth module
import App from './App.vue'
import router from './router'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

// Inicjalizacja
auth.init().then(() => {
    const app = createApp(App)
    app.use(router)
    app.mount('#app')
})
