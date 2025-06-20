<template>
    <div class="rooms-list-container">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h3>Available Rooms</h3>
            <button @click="showCreateRoomModal = true" class="btn btn-primary btn-sm">
                Create Room
            </button>
        </div>

        <div class="rooms-list" :class="{ empty: rooms.length === 0 }">
            <div v-if="rooms.length === 0" class="text-center text-muted py-5">
                <i class="fas fa-home fa-3x mb-3"></i>
                <p>No rooms available.</p>
            </div>

            <RoomComponent
                v-for="room in publicRooms"
                :key="room.roomId"
                :room="room"
                @join="joinRoom"
                class="mb-3"
            />
        </div>

        <CreateRoomModal
            :show="showCreateRoomModal"
            @close="showCreateRoomModal = false"
            @create="createRoom"
        />
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/services/axios'
import RoomComponent from './RoomCard.vue'
import CreateRoomModal from './CreateRoomModal.vue'
import { gameService } from '@/api/endpoints'
import { useRouter } from 'vue-router'
import { authState } from '@/auth/auth'

const router = useRouter()
const rooms = ref([])
const publicRooms = ref([])
const showCreateRoomModal = ref(false)

const fetchRooms = async () => {
    try {
        console.log('Fetching rooms...')
        const response = await axios.get(gameService.fetchAllRooms)
        console.log('Rooms fetched:', response.data)
        rooms.value = response.data
        publicRooms.value = response.data.filter((room) => room.isPublic)
    } catch (error) {
        console.error('Error fetching rooms:', error)
    }
}

const createRoom = async (roomConfig) => {
    try {
        const requestPayload = {
            ...roomConfig,
            ownerLogin: authState.login 
        };
        console.log('Creating room with config:', requestPayload);

        // POPRAWKA: Usuwamy nawiasy () po gameService.createRoom
        const response = await axios.post(gameService.createRoom, requestPayload);
        
        console.log('Room created:', response.data);
        router.push({
            name: 'room', 
            params: { roomId: response.data.roomId },
        });
    } catch (error) {
        console.error('Error creating room:', error.response?.data || error.message);
        alert('Failed to create room. ' + (error.response?.data || ''));
    }
};


const joinRoom = async (room) => {
    try {
        console.log('Joining room:', room)
        const response = await axios.post(gameService.joinRoom(room.roomId))
        console.log(`Joined room response:`, response.data)
        router.push({
            name: 'room',
            params: { roomId: room.roomId },
            state: { roomData: room },
        })
    } catch (error) {
        console.error(`Error joining room ${room.roomId}:`, error)
        alert('Failed to join room.')
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
