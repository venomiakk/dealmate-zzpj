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
                :key="room.roomId"
                :room="room"
                @join="joinRoom"
                class="mb-3"
            />
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/services/axios'
import RoomComponent from './RoomComponent.vue'
import { gameService } from '@/api/endpoints'

const rooms = ref([])

const fetchRooms = async () => {
    try {
        console.log('Fetching rooms...')
        const response = await axios.get(gameService.fetchAllRooms)
        console.log('Rooms fetched:', response.data)
        rooms.value = response.data
    } catch (error) {
        console.error('Error fetching rooms:', error)
        alert('Failed to fetch rooms. Check console for details.')
    }
}

const createRoom = async () => {
    try {
        console.log('Creating room...')
        const response = await axios.post(gameService.createRoom)
        console.log('Room created:', response.data)
        alert('Room created successfully')
        await fetchRooms()
    } catch (error) {
        console.error('Error creating room:', error)
        alert('Failed to create room. Check console for details.')
    }
}

const joinRoom = async (room) => {
    try {
        console.log('Joining room:', room.roomId)
        const response = await axios.post(gameService.joinRoom(room.roomId))
        console.log(`Joined room ${room.roomId}:`, response.data)
        alert(`Joined room ${room.roomId} successfully`)
    } catch (error) {
        console.error(`Error joining room ${room.roomId}:`, error)
        alert('Failed to join room. Check console for details.')
    }
}

onMounted(() => {
    console.log('Component mounted')
    fetchRooms()
})
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
