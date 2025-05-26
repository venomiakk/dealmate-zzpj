<template>
    <div>
        <h1>Main page</h1>
        <div v-if="authState.isLoading">Loading...</div>
        <div v-else-if="authState.isAuthenticated">
            <p>Welcome, {{ authState.user.profile.sub }}!</p>
            <button @click="logout">Logout</button>
        </div>
        <div v-else>
            <button @click="login">Zaloguj się</button>
        </div>
    </div>
</template>

<script setup>
import { authState, auth } from '../auth/auth.js'

const login = auth.login
const logout = auth.logout
</script>
