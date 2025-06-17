import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'home',
            component: HomeView,
        },
        {
            path: '/callback',
            name: 'callback',
            component: () => import('../views/CallbackView.vue'),
        },
        {
            path: '/profile/:login?',
            name: 'profile',
            component: () => import('../views/ProfileView.vue'),
        },
        {
            path: '/register',
            name: 'register',
            component: () => import('../views/RegisterForm.vue'),
        },
        {
            path: '/profile/edit',
            name: 'edit-profile',
            component: () => import('../views/EditProfileView.vue'),
        },
    ],
})

export default router
