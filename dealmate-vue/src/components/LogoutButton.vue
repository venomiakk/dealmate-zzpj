<template>
    <button @click="logout" class="logout-btn">Logout</button>
</template>

<script setup>
const logout = async () => {
    console.log('Logout button clicked')
}
</script>

<style scoped>
.logout-btn {
    padding: 0.5rem 1rem;
    background-color: #e74c3c;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

.logout-btn:hover {
    background-color: #c0392b;
}
</style>
