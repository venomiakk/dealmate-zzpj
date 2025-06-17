<template>
    <div>
        <Navbar />
        <main class="container mt-4">
            <router-view></router-view>
        </main>
    </div>
</template>

<script setup>
import Navbar from './components/NavBar.vue'
</script>
