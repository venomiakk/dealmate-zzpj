<template>
    <div class="game-room-container">
        <!-- Main content area -->
        <div class="game-room-layout">
            <!-- Left side - Game area -->
            <div class="game-area">
                <div class="game-content">
                    <div v-if="!gameStarted" class="waiting-area">
                        <div class="text-center">
                            <i class="fas fa-clock fa-3x mb-3 text-muted"></i>
                            <h4>Waiting for game to start...</h4>
                            <p class="text-muted">
                                {{
                                    isOwner
                                        ? 'Click "Start Game" when ready'
                                        : 'Waiting for room owner to start the game'
                                }}
                            </p>
                        </div>
                    </div>

                    <div v-else class="game-board">
                        <!-- Game board will be here -->
                        <div class="poker-table">
                            <div class="table-center">
                                <div class="community-cards">
                                    <!-- Community cards will be displayed here -->
                                </div>
                                <div class="pot-info">
                                    <h5>Pot: ${{ currentPot }}</h5>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Right side - Room info and chat -->
            <div class="sidebar">
                <!-- Room info panel -->
                <div class="room-info-panel">
                    <div class="room-header">
                        <div class="room-header-content">
                            <div>
                                <h4 class="room-title mb-1">{{ roomData.name }}</h4>
                                <span class="badge badge-info">{{ gameTypeName }}</span>
                            </div>
                            <div class="join-code mt-2">
                                <small class="text-muted">
                                    <i class="fas fa-users me-1"></i>
                                    Join Code: <strong>{{ roomData.joinCode }}</strong>
                                </small>
                            </div>
                        </div>
                    </div>

                    <!-- Players list -->
                    <div class="players-section">
                        <h6 class="section-title">
                            <i class="fas fa-users me-2"></i>
                            Players ({{ playersCount }}/{{ roomData.maxPlayers }})
                        </h6>
                        <div class="players-list">
                            <div
                                v-for="player in roomData.players"
                                :key="player"
                                class="player-item"
                                :class="{ 'is-current': player === authState.login }"
                            >
                                <div class="player-info">
                                    <i class="fas fa-user-circle me-2"></i>
                                    <span class="player-name">{{ player }}</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Game controls -->
                    <div class="game-controls" v-if="isOwner && !gameStarted">
                        <button
                            @click="startGame"
                            class="btn btn-success btn-block"
                            :disabled="!canStartGame"
                        >
                            <i class="fas fa-play me-2"></i>
                            Start Game
                        </button>
                        <small class="text-muted mt-2 d-block" v-if="!canStartGame">
                            Need at least 2 players to start
                        </small>
                    </div>

                    <!-- Leave room button -->
                    <div class="room-actions mt-3">
                        <button @click="leaveRoom" class="btn btn-outline-danger btn-sm">
                            <i class="fas fa-sign-out-alt me-2"></i>
                            Leave Room
                        </button>
                    </div>
                </div>

                <!-- Chat panel -->
                <div class="chat-panel">
                    <div class="chat-header">
                        <h6 class="mb-0">
                            <i class="fas fa-comments me-2"></i>
                            Chat
                        </h6>
                    </div>

                    <div class="chat-messages" ref="chatMessagesContainer">
                        <div
                            v-for="message in chatMessages"
                            :key="message.id"
                            class="chat-message"
                            :class="{ 'own-message': message.senderId === currentUserLogin }"
                        >
                            <div class="message-header">
                                <span class="sender-name">{{ message.senderName }}</span>
                                <span class="message-time">{{
                                    formatTime(message.timestamp)
                                }}</span>
                            </div>
                            <div class="message-content">{{ message.content }}</div>
                        </div>
                    </div>

                    <div class="chat-input mt-auto">
                        <form @submit.prevent="sendMessage">
                            <div class="input-group">
                                <input
                                    v-model="newMessage"
                                    type="text"
                                    class="form-control"
                                    placeholder="Type a message..."
                                    maxlength="200"
                                    :disabled="!isConnected"
                                />
                                <button
                                    type="submit"
                                    class="btn btn-primary"
                                    :disabled="!newMessage.trim() || !isConnected"
                                >
                                    <i class="fas fa-paper-plane"></i>
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { GAME_TYPES } from '@/constants/constants'
import { authState } from '@/auth/auth'
import axios from '@/services/axios'
import { gameService } from '@/api/endpoints'

const route = useRoute()
const router = useRouter()

const roomId = computed(() => route.params.roomId)
const loading = ref(true)
const error = ref(null)

// Room data
const roomData = ref({
    roomId: null,
    name: '',
    gameType: '',
    maxPlayers: 0,
    ownerLogin: null,
    players: [],
    isPublic: true,
    joinCode: '',
})

// Game state
const gameStarted = ref(false)
const currentPot = ref(0)

// Chat
const chatMessages = ref([
    {
        id: 1,
        senderId: 'Player1',
        senderName: 'Player 1',
        content: 'Hello everyone!',
        timestamp: new Date(),
    },
    {
        id: 2,
        senderId: 'Player2',
        senderName: 'Player 2',
        content: 'Ready to play!',
        timestamp: new Date(),
    },
])
const newMessage = ref('')
const chatMessagesContainer = ref(null) // Zmiana nazwy ref

// Connection state
const isConnected = ref(true)
const currentUserLogin = ref(authState.login)

// Computed properties
const gameTypeName = computed(() => {
    const gameType = GAME_TYPES.find((type) => type.value === roomData.value.gameType)
    return gameType ? gameType.label : 'Unknown'
})

const isOwner = computed(() => {
    return roomData.value.ownerLogin === currentUserLogin.value
})

const playersCount = computed(() => {
    if (Array.isArray(roomData.value.players)) {
        return roomData.value.players.length
    } else if (typeof roomData.value.players === 'number') {
        return roomData.value.players
    }
    return 0
})

const canStartGame = computed(() => {
    return playersCount.value >= 2
})

const fetchRoomData = async () => {
    console.log('Fetching room data for:', roomId.value)
    try {
        loading.value = true
        error.value = null
        console.log('Current state:', history.state)
        const stateData = history.state?.roomData
        if (stateData) {
            console.log('Room data from state:', roomData.value)
            roomData.value = stateData
            loading.value = false
            return
        }
        const response = await axios.get(gameService.fetchRoomById(roomId.value))
        console.log('Room data fetched:', response.data)
        roomData.value = response.data
        if (!roomData.value.roomId) {
            console.error('Invalid room data:', roomData.value)
            error.value = 'Invalid room data'
            return
        }
        console.log('Room data successfully loaded:', roomData.value)
        // Set current user ID
        currentUserLogin.value = authState.login
    } catch (err) {
        console.error('Error fetching room data:', err)
        error.value = 'Failed to load room data'
    } finally {
        loading.value = false
    }
}

// Methods
const startGame = () => {
    if (!canStartGame.value) return

    gameStarted.value = true
    // TODO: Send start game signal to backend
    console.log('Starting game...')
}

const leaveRoom = () => {
    if (confirm('Are you sure you want to leave the room?')) {
        //todo: try-catch block
        axios.post(gameService.leaveRoom(roomId.value))
        router.push('/')
    }
}

const sendMessage = () => {
    if (!newMessage.value.trim()) return

    const message = {
        id: Date.now(),
        senderId: currentUserLogin.value,
        senderName: authState.login,
        content: newMessage.value.trim(),
        timestamp: new Date(),
    }

    chatMessages.value.push(message)
    newMessage.value = ''

    // Scroll to bottom
    nextTick(() => {
        if (chatMessagesContainer.value) {
            chatMessagesContainer.value.scrollTop = chatMessagesContainer.value.scrollHeight
        }
    })

    // TODO: Send message to backend
}

const formatTime = (timestamp) => {
    return new Date(timestamp).toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit',
    })
}

// Lifecycle
onMounted(async () => {
    // TODO: Initialize room data from route params
    // Validate roomId
    if (!roomId.value) {
        console.error('Invalid room ID:', route.params.id)
        error.value = 'Invalid room ID'
        return
    }

    // Fetch room data
    await fetchRoomData()

    // TODO: Connect to WebSocket
    console.log('TODO: Connect to WebSocket of room:', route.params.roomId)
})

onUnmounted(() => {
    // TODO: Disconnect from WebSocket
    console.log('Disconnecting from room')
})
</script>

<style scoped>
.game-room-container {
    height: calc(100vh - 85px); /* Dostosuj 60px do wysokości twojego navbara */
    padding: 1rem;
    overflow: hidden;
}

.game-room-layout {
    display: flex;
    height: 100%;
    gap: 1rem;
    max-width: 1400px;
    margin: 0 auto;
}

/* Game Area */
.game-area {
    flex: 1;
    background: rgba(21, 95, 3, 0.15);
    border-radius: 1rem;
    padding: 1.5rem;
    backdrop-filter: blur(10px);
}

.game-content {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.waiting-area {
    text-align: center;
    color: white;
}

.poker-table {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background: #0d5f3a;
    border: 8px solid #8b4513;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    max-width: 600px;
    max-height: 400px;
    margin: 0 auto;
}

.table-center {
    text-align: center;
    color: white;
}

.community-cards {
    margin-bottom: 1rem;
}

.pot-info h5 {
    color: #ffd700;
    margin: 0;
}

/* Sidebar */
.sidebar {
    width: 350px;
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

/* Room Info Panel */
.room-info-panel {
    background: white;
    border-radius: 1rem;
    padding: 1.5rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.room-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #dee2e6;
}

.room-title {
    margin: 0;
    color: #2c3e50;
}

.badge-info {
    background-color: #17a2b8;
    color: white;
    padding: 0.25rem 0.5rem;
    border-radius: 0.25rem;
    font-size: 0.75rem;
}

.section-title {
    color: #495057;
    margin-bottom: 1rem;
    font-weight: 600;
}

.players-list {
    max-height: 200px;
    overflow-y: auto;
}

.player-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.75rem;
    margin-bottom: 0.5rem;
    background: #f8f9fa;
    border-radius: 0.5rem;
    transition: all 0.2s;
}

.player-item:hover {
    background: #e9ecef;
}

.player-item.is-owner {
    background: linear-gradient(45deg, #fff3cd, #ffeaa7);
    border: 1px solid #ffeaa7;
}

.player-item.is-current {
    background: linear-gradient(45deg, #d4edda, #c3e6cb);
    border: 1px solid #c3e6cb;
}

.player-info {
    display: flex;
    align-items: center;
}

.player-name {
    font-weight: 500;
}

.owner-badge {
    color: #f39c12;
    margin-left: 0.5rem;
}

.btn-block {
    width: 100%;
}

/* Chat Panel */
.chat-panel {
    flex: 1;
    background: white;
    border-radius: 1rem;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.chat-header {
    padding: 1rem 1.5rem;
    background: #f8f9fa;
    border-bottom: 1px solid #dee2e6;
}

.chat-messages {
    flex: 1;
    padding: 1rem;
    overflow-y: auto;
    max-height: 400px;
}

.chat-message {
    margin-bottom: 1rem;
}

.chat-message.own-message .message-content {
    background: #007bff;
    color: white;
    margin-left: auto;
}

.message-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 0.25rem;
}

.sender-name {
    font-weight: 600;
    font-size: 0.875rem;
    color: #495057;
}

.message-time {
    font-size: 0.75rem;
    color: #6c757d;
}

.message-content {
    background: #f8f9fa;
    padding: 0.5rem 0.75rem;
    border-radius: 0.5rem;
    max-width: 80%;
    word-wrap: break-word;
}

.chat-input {
    padding: 1rem;
    border-top: 1px solid #dee2e6;
    background: #f8f9fa;
}

.input-group {
    display: flex;
}

.input-group .form-control {
    flex: 1;
    border-top-right-radius: 0;
    border-bottom-right-radius: 0;
}

.input-group .btn {
    border-top-left-radius: 0;
    border-bottom-left-radius: 0;
}

/* Badges */
.badge-success {
    background-color: #28a745;
}
.badge-warning {
    background-color: #ffc107;
    color: #212529;
}
.badge-info {
    background-color: #17a2b8;
}
.badge-secondary {
    background-color: #6c757d;
}
.badge-light {
    background-color: #f8f9fa;
    color: #212529;
}

/* Responsive */
@media (max-width: 768px) {
    .game-room-layout {
        flex-direction: column;
    }

    .sidebar {
        width: 100%;
        order: -1;
    }

    .chat-panel {
        max-height: 300px;
    }
}
</style>
