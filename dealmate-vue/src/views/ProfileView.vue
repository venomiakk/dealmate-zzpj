<template>
    <div class="container mt-4">
        <ProfileCard :isPublicProfile="isPublicProfile" :userLogin="userLogin" />
    </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { authState } from '../auth/auth.js'
import ProfileCard from '@/components/ProfileCard.vue'

const route = useRoute()

const isPublicProfile = computed(() => {
    return !!route.params.login && route.params.login !== authState.user?.profile?.sub
})

const userLogin = computed(() => {
    if (isPublicProfile.value) {
        return route.params.login
    }
    return authState.user?.profile?.sub
})
</script>
