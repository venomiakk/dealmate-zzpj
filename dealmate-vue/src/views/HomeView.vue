<template>
    <div>
        <RoomsList v-if="authState.isAuthenticated" />
        <div v-else class="text-center mt-5">
            <h2>Welcome to Dealmate!</h2>
            <p>Please log in to view and join rooms.</p>
            <button @click="auth.login" class="btn btn-primary">Login</button>
            <router-link to="/register" class="btn btn-secondary ms-2">Register</router-link>
        </div>
    </div>
</template>

<script setup>
import RoomsList from '../components/RoomsList.vue'
import { authState, auth } from '@/auth/auth'
</script>
