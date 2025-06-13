<template>
    <nav class="navbar navbar-expand-lg navbar-light bg-light border-bottom">
        <div class="container">
            <router-link to="/" class="navbar-brand fw-bold">DealMate</router-link>

            <div class="navbar-nav ms-auto d-flex flex-row align-items-center">
                <router-link to="/" class="nav-link me-3">Home</router-link>

                <div v-if="authState.isAuthenticated" class="d-flex align-items-center">
                    <router-link to="/profile" class="nav-link me-3">{{ authState.user.profile.sub }}</router-link>
                    <button @click="logout" class="btn btn-outline-danger btn-sm">Logout</button>
                </div>
                <div v-else>
                    <button @click="login" class="btn btn-primary btn-sm">Login</button>
                    <router-link to="/register" class="btn btn-primary btn-sm ms-2"
                        >Register</router-link
                    >
                </div>
            </div>
        </div>
    </nav>
</template>

<script setup>
import { authState, auth } from '../auth/auth.js'

const login = auth.login
const logout = auth.logout
</script>

<style scoped>
/* Możesz usunąć wszystkie style - Bootstrap się tym zajmie */
</style>
