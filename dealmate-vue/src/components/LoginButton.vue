<template>
    <button @click="login" class="login-btn">Login</button>
</template>

<script setup>
const login = async () => {
    console.log('Login button clicked')
}
</script>

<style scoped>
.login-btn {
    padding: 0.5rem 1rem;
    background-color: #42b883;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

.login-btn:hover {
    background-color: #369870;
}
</style>
