<template>
    <div class="rooms-list-container">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h3>Available Rooms</h3>
            <button @click="createRoom" class="btn btn-primary btn-sm">Create Room</button>
        </div>

        <div class="rooms-list" :class="{ empty: rooms.length === 0 }">
            <div v-if="rooms.length === 0" class="text-center text-muted py-5">
                <i class="fas fa-home fa-3x mb-3"></i>
                <p>No rooms available.</p>
            </div>

            <RoomComponent
                v-for="room in rooms"
                :key="room.id"
                :room="room"
                @join="joinRoom"
                class="mb-3"
            />
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import RoomComponent from './RoomComponent.vue'

// Przykładowe dane pokoi
const rooms = ref([
    {
        id: 1,
        name: 'Game 1',
        players: 2,
        capacity: 4,
        type: 'Holdem Texas',
    },
    {
        id: 2,
        name: 'Game 2',
        players: 4,
        capacity: 4,
        type: 'Omaha',
    },
    {
        id: 3,
        name: 'Room 3',
        players: 3,
        capacity: 4,
        type: 'Seven-card stud',
    },
])

const createRoom = () => {
    const newRoom = {
        id: Date.now(),
        name: `Room ${rooms.value.length + 1}`,
        type: 'New Game Type',
        players: 0,
        capacity: 8,
    }
    rooms.value.push(newRoom)
}

const joinRoom = (room) => {
    console.log(`Attempting to join room: ${room.name}`)
    // Tu możesz dodać logikę dołączania do pokoju
    // Na przykład: router.push(`/room/${room.id}`)
}
</script>

<style scoped>
.rooms-list-container {
    max-width: 800px;
    margin: 0 auto;
}

.rooms-list {
    max-height: 600px;
    overflow-y: auto;
    border: 1px solid #dee2e6;
    border-radius: 0.375rem;
    padding: 1rem;
    background-color: #f8f9fa;
}

.rooms-list.empty {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 300px;
}

/* Scrollbar styling dla webkit browsers */
.rooms-list::-webkit-scrollbar {
    width: 8px;
}

.rooms-list::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
}

.rooms-list::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 4px;
}

.rooms-list::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}
</style>
