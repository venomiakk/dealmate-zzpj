<template>
    <div>Logging in...</div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { auth } from '../auth/auth.js'

const router = useRouter()

onMounted(async () => {
    try {
        await auth.handleCallback()
        router.push('/')
    } catch (error) {
        console.error('Callback error:', error)
        router.push('/')
    }
})
</script>
