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
                            :class="{
                                'own-message': message.senderId === currentUserLogin,
                                'system-message': message.isSystem
                            }"
                        >
                            <div class="message-wrapper">
                                <!-- Pokaż header tylko dla wiadomości nie-systemowych -->
                                <div v-if="!message.isSystem" class="message-header">
                                    <span class="sender-name">{{ message.senderName }}</span>
                                    <span class="message-time">{{ formatTime(message.timestamp) }}</span>
                                </div>
                                <div class="message-content">{{ message.content }}</div>
                            </div>
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
// POPRAWIONY IMPORT: importujemy zarówno 'auth' jak i 'authState'
import { auth, authState } from '@/auth/auth'
import axios from '@/services/axios'
import { gameService } from '@/api/endpoints'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const route = useRoute()
const router = useRouter()

const roomId = computed(() => route.params.roomId)
const loading = ref(true)
const error = ref(null)

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

const gameStarted = ref(false)
const currentPot = ref(0)

const chatMessages = ref([])
const newMessage = ref('')
const chatMessagesContainer = ref(null)

const isConnected = ref(false)
const stompClient = ref(null)
// POPRAWKA: odwołujemy się do zaimportowanego authState
const currentUserLogin = ref(authState.login)

const gameTypeName = computed(() => {
    const gameType = GAME_TYPES.find((type) => type.value === roomData.value.gameType)
    return gameType ? gameType.label : 'Unknown'
})

const isOwner = computed(() => {
    return roomData.value.ownerLogin === currentUserLogin.value
})

const playersCount = computed(() => {
    return Array.isArray(roomData.value.players) ? roomData.value.players.length : 0
})

const canStartGame = computed(() => {
    return playersCount.value >= 2
})

const fetchRoomData = async () => {
    console.log('Fetching room data for:', roomId.value)
    try {
        loading.value = true
        error.value = null
        const response = await axios.get(gameService.fetchRoomById(roomId.value))
        console.log('Room data fetched:', response.data)
        roomData.value = response.data
    } catch (err) {
        console.error('Error fetching room data:', err)
        error.value = 'Failed to load room data'
    } finally {
        loading.value = false
    }
}

const leaveRoom = () => {
    if (confirm('Are you sure you want to leave the room?')) {
        axios
            .post(gameService.leaveRoom(roomId.value))
            .then(() => router.push('/'))
            .catch((err) => console.error('Failed to leave room', err))
    }
}

const formatTime = (timestamp) => {
    return new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

// --- Logika WebSocket ---
// POPRAWKA: Funkcja jest teraz asynchroniczna
const connectToChat = async () => {
    const CHAT_SERVICE_URL = 'http://localhost:8100/ws-chat'

    // POPRAWKA: Najpierw pobieramy token i czekamy na niego
    console.log('Attempting to get auth token...')
    const token = await auth.getToken()
    console.log('Token received:', token ? 'OK' : 'null or undefined')

    if (!token) {
        console.error('FATAL: Auth token is not available. Cannot connect to WebSocket.')
        return
    }
    try {
        console.log('Checking chat service availability...')
        const response = await axios.get(`http://localhost:8100/chatservice/api/chat/test`)
        if (response.status === 200) {
            console.log('Chat service is available: ', response.data)
        } else {
            console.error('Chat service is not available:', response.statusText)
            return
        }
    } catch (error) {
        console.error('Error checking chat service availability:', error)
        return
    }

    console.log("Token", token)
    const client = new Client({
        webSocketFactory: () => new SockJS(CHAT_SERVICE_URL),
        connectHeaders: {
            // POPRAWKA: Używamy pobranego tokenu
            Authorization: `Bearer ${token}`,
        },
        debug: (str) => {
            console.log(`[WebSocket] ${new Date().toLocaleTimeString()}: ${str}`)
        },
        reconnectDelay: 5000,
        onConnect: () => {
            isConnected.value = true
            console.log('Successfully connected to Chat Service!')

            client.subscribe(`/topic/room/${roomId.value}/chat`, (message) => {
                const chatMsg = JSON.parse(message.body)
                if (chatMsg.sender === 'System') {
                    addSystemMessage(chatMsg.content)
                    return
                }

                chatMessages.value.push({
                    id: chatMsg.timestamp,
                    senderId: chatMsg.sender,
                    senderName: chatMsg.sender,
                    content: chatMsg.content,
                    timestamp: chatMsg.timestamp,
                })
                scrollToBottom()
            })

            client.subscribe(`/topic/room/${roomId.value}/players`, (message) => {
                const playersUpdate = JSON.parse(message.body)
                console.log('Received players update:', playersUpdate)
                roomData.value.players = playersUpdate.players
            })
        },
        onStompError: (frame) => {
            console.error('Broker reported error: ' + frame.headers['message'])
            console.error('Additional details: ' + frame.body)
            isConnected.value = false
        },
        onWebSocketClose: () => {
            isConnected.value = false
            console.log('WebSocket connection closed.')
        },
    })

    client.activate()
    stompClient.value = client
}

const disconnectFromChat = () => {
    if (stompClient.value) {
        stompClient.value.deactivate()
    }
}

const sendMessage = () => {
    if (!newMessage.value.trim() || !isConnected.value) return

    const chatMessage = {
        content: newMessage.value.trim(),
        sender: authState.login // Dodaj nadawcę
    }

    stompClient.value.publish({
        destination: `/app/room/${roomId.value}/chat.sendMessage`,
        body: JSON.stringify(chatMessage),
        headers: {
            sender: authState.login // Dodaj nadawcę w nagłówkach
        }
    })

    newMessage.value = ''
}

const addSystemMessage = (content) => {
    const message = {
        id: Date.now(),
        senderId: 'system',
        senderName: 'System',
        content: content,
        timestamp: new Date(),
        isSystem: true // Dodaj flagę systemową
    }
    chatMessages.value.push(message)
    scrollToBottom()
}

const scrollToBottom = () => {
    nextTick(() => {
        if (chatMessagesContainer.value) {
            chatMessagesContainer.value.scrollTop = chatMessagesContainer.value.scrollHeight
        }
    })
}

// --- Cykl Życia Komponentu ---
onMounted(async () => {
    if (!roomId.value) {
        error.value = 'Invalid room ID'
        return
    }
    await fetchRoomData()
    if (!error.value) {
        // Wywołujemy teraz naszą nową, asynchroniczną funkcję
        connectToChat()
    }
})

onUnmounted(() => {
    disconnectFromChat()
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
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
}

.chat-message {
    display: flex;
    width: 100%;
}

.chat-message:not(.own-message) {
    justify-content: flex-start;
}

.chat-message.own-message {
    justify-content: flex-end;
}

.message-wrapper {
    max-width: 70%;
    min-width: 120px;
}

.message-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.25rem;
    padding: 0 0.5rem;
}

.chat-message.own-message .message-header {
    flex-direction: row-reverse;
}

.sender-name {
    font-weight: 600;
    font-size: 0.875rem;
    color: #495057;
}

.chat-message.own-message .sender-name {
    color: #0d6efd;
}

.message-time {
    font-size: 0.75rem;
    color: #6c757d;
    margin-left: 0.5rem;
}

.chat-message.own-message .message-time {
    margin-left: 0;
    margin-right: 0.5rem;
}

.message-content {
    padding: 0.75rem 1rem;
    border-radius: 1rem;
    word-wrap: break-word;
    position: relative;
}

/* Wiadomości innych użytkowników - po lewej, szare */
.chat-message:not(.own-message) .message-content {
    background: #f8f9fa;
    color: #333;
    border-bottom-left-radius: 0.25rem;
    border: 1px solid #e9ecef;
}

/* Własne wiadomości - po prawej, niebieskie */
.chat-message.own-message .message-content {
    background: #0d6efd;
    color: white;
    border-bottom-right-radius: 0.25rem;
    box-shadow: 0 2px 4px rgba(13, 110, 253, 0.2);
}

/* Efekt hover */
.message-content:hover {
    transform: translateY(-1px);
    transition: transform 0.2s ease;
}

.chat-message:not(.own-message) .message-content:hover {
    background: #e9ecef;
}

.chat-message.own-message .message-content:hover {
    background: #0b5ed7;
}

/* Scrollbar dla chat messages */
.chat-messages::-webkit-scrollbar {
    width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}

/* Wiadomości systemowe (opcjonalnie) */
.chat-message.system-message {
    justify-content: center;
    margin: 1rem 0; /* Więcej marginesu dla lepszego oddzielenia */
}

.chat-message.system-message .message-wrapper {
    max-width: 90%;
    text-align: center;
}

.chat-message.system-message .message-content {
    /* background: #fff3cd; */
    /* color: #856404; */
    /* border: 1px solid #ffeaa7; */
    font-style: italic;
    border-radius: 1rem;
    padding: 0.5rem 1rem;
    font-size: 0.875rem;
    box-shadow: none;
}



/* ...rest of existing styles remain the same... */
</style>
