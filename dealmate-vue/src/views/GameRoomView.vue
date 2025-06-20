<template>
    <div class="game-room-container">
        <div class="game-room-layout">
            <div class="game-area">
                <div class="game-content">
                    <div v-if="!gameHasStarted" class="waiting-area">
                        <div class="text-center">
                            <i class="fas fa-clock fa-3x mb-3 text-muted"></i>
                            <h4>Waiting for game to start...</h4>
                            <p class="text-muted">
                                {{ isOwner ? 'Click "Start Game" when ready' : 'Waiting for room owner to start' }}
                            </p>
                        </div>
                    </div>

                    <div v-else class="blackjack-board">
                        <div v-if="isSpectating" class="spectator-view text-center">
                            <i class="fas fa-eye fa-3x mb-3 text-muted"></i>
                            <h4>Game in progress...</h4>
                            <p class="text-muted">You are spectating. Waiting for the next round to begin.</p>
                        </div>
                        
                        <template v-else>
                            <div class="hand-area dealer-hand">
                                <h5 class="area-title">Dealer's Hand ({{ dealerHandValue }})</h5>
                                <div class="cards-container">
                                    <div v-for="(card, index) in gameState.dealerHand?.cards" :key="'dealer-' + index" class="card" :class="getCardClass(card)">
                                        {{ card.code !== 'BK' ? getCardSymbol(card) : '' }}
                                    </div>
                                </div>
                            </div>

                            <div class="game-info text-center my-3">
                                <h4 v-if="gameState.message" class="game-message">{{ gameState.message }}</h4>
                                <h4 v-if="gameState.nextRoundCountdown > 0" class="countdown-message">
                                    Next round in {{ gameState.nextRoundCountdown }}s...
                                </h4>
                                <h5 class="pot-info">Pot: {{ gameState.pot || 0 }}</h5>
                            </div>
                            <div class="hand-area player-hand">
                                <h5 class="area-title">Your Hand ({{ myHandValue }}) - {{ myStatus }}</h5>
                                <div class="cards-container">
                                    <div v-for="(card, index) in myHand?.cards" :key="'player-' + index" class="card" :class="getCardClass(card)">
                                        {{ getCardSymbol(card) }}
                                    </div>
                                </div>
                            </div>

                            <div v-if="isMyTurn" class="player-actions text-center mt-3">
                                <button @click="performAction('HIT')" class="btn btn-lg btn-success mx-2" :disabled="!canHit">
                                    <i class="fas fa-plus-circle me-2"></i>Hit
                                </button>
                                <button @click="performAction('STAND')" class="btn btn-lg btn-warning mx-2">
                                    <i class="fas fa-hand-paper me-2"></i>Stand
                                </button>
                            </div>
                        </template>
                        
                        <div class="other-players-container">
                             <div v-for="player in otherPlayers" :key="player.playerId" class="other-player-hand">
                                 <h6>{{ player.playerId }} ({{ player.value }}) - {{player.status}}</h6>
                                 <div class="cards-container-small">
                                      <div v-for="(card, index) in player.cards" :key="index" class="card-small" :class="getCardClass(card)">
                                          {{ getCardSymbol(card) }}
                                      </div>
                                 </div>
                             </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="sidebar">
                <div class="room-info-panel">
                    <div class="room-header">
                        <h4 class="room-title">{{ roomData.name }}</h4>
                    </div>
                    <div class="players-section">
                        <h6 class="section-title">
                            <i class="fas fa-users me-2"></i>
                            Players ({{ playersCount }}/{{ roomData.maxPlayers }})
                        </h6>
                        <div class="players-list">
                             <div v-for="player in roomData.players" :key="player.login" 
                                 class="player-item"
                                 :class="{ 'winner': gameState.winners && gameState.winners.includes(player.login) }">
                                <div class="player-details">
                                    <i class="fas fa-user-circle me-2"></i>
                                    <span>{{ player.login }}</span>
                                    <span v-if="player.login === gameState.currentPlayerId" class="badge bg-success ms-2">Turn</span>
                                </div>
                                <div class="player-credits">
                                    <span class="badge rounded-pill bg-primary">
                                        <i class="fas fa-coins me-1"></i>
                                        {{ player.credits }}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="game-controls" v-if="isOwner && !gameHasStarted">
                        <button @click="startGame" class="btn btn-success btn-block" :disabled="!canStartGame">
                            <i class="fas fa-play me-2"></i>Start Game
                        </button>
                    </div>
                    <div class="room-actions mt-3">
                        <button @click="leaveRoom" class="btn btn-outline-danger btn-sm">Leave Room</button>
                    </div>
                </div>
                <div class="chat-panel">
                    </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { auth, authState } from '@/auth/auth';
import axios from '@/services/axios';
import { gameService } from '@/api/endpoints';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const route = useRoute();
const router = useRouter();
const roomId = computed(() => route.params.roomId);

const roomData = ref({ players: [] });
const currentUserLogin = ref(authState.login);
const isOwner = computed(() => roomData.value.ownerLogin === currentUserLogin.value);
const playersCount = computed(() => Array.isArray(roomData.value.players) ? roomData.value.players.length : 0);
const canStartGame = computed(() => playersCount.value >= 1 && !gameHasStarted.value);

const gameHasStarted = ref(false);
const gameState = ref({});
const stompClient = ref(null);
const isGameConnected = ref(false);

const myHand = computed(() => gameState.value.playerHands?.[currentUserLogin.value]);
const myHandValue = computed(() => myHand.value?.value || 0);
const myStatus = computed(() => myHand.value?.status || 'WAITING');
const dealerHandValue = computed(() => {
    if (gameState.value.dealerHand?.cards?.some(c => c.value === 'HIDDEN')) return '?';
    return gameState.value.dealerHand?.value || 0;
});
const isMyTurn = computed(() => gameState.value.currentPlayerId === currentUserLogin.value && myStatus.value === 'PLAYING');
const canHit = computed(() => myStatus.value === 'PLAYING');
const otherPlayers = computed(() => {
    if (!gameState.value.playerHands) return [];
    return Object.values(gameState.value.playerHands).filter(p => p.playerId !== currentUserLogin.value);
});

const isSpectating = computed(() => {
    return gameHasStarted.value && (!gameState.value.playerHands || !gameState.value.playerHands[currentUserLogin.value]);
});

const chatMessages = ref([]);
const newMessage = ref('');
const chatMessagesContainer = ref(null);
const isChatConnected = ref(false);

const fetchRoomData = async () => {
    try {
        const response = await axios.get(gameService.fetchRoomById(roomId.value));
        roomData.value = response.data;
    } catch (err) {
        console.error('Error fetching room data:', err);
        router.push('/');
    }
};

const startGame = async () => {
    try {
        await axios.post(gameService.startGame(roomId.value)); 
        console.log('Start game request sent.');
    } catch (error) {
        console.error('Failed to start game:', error);
        if (error instanceof TypeError) {
             console.error("Potential issue: Check if 'gameService.startGame' is correctly defined in your endpoints.js file.");
        }
        alert(error.response?.data?.message || 'Could not start the game.');
    }
};

const leaveRoom = () => {
    axios.post(gameService.leaveRoom(roomId.value))
        .then(() => router.push('/'))
        .catch((err) => console.error('Failed to leave room', err));
};

const connectToWebsockets = async () => {
    const token = await auth.getToken();
    if (!token) {
        console.error('Auth token not available.');
        return;
    }
    const headers = { Authorization: `Bearer ${token}` };

    const gameSocket = new SockJS('http://localhost:8104/ws');
    const gameClient = new Client({
        webSocketFactory: () => gameSocket,
        connectHeaders: headers,
        onConnect: () => {
            isGameConnected.value = true;
            console.log('Connected to Game Service WebSocket!');
            gameClient.subscribe(`/topic/room/${roomId.value}/players`, (message) => {
                const updatedPlayers = JSON.parse(message.body).players;
                console.log('Received players update:', updatedPlayers);
                if (roomData.value) {
                    roomData.value.players = updatedPlayers;
                }
            });
            gameClient.subscribe(`/topic/game/${roomId.value}`, (message) => {
                gameState.value = JSON.parse(message.body);
                if (!gameHasStarted.value && gameState.value.gameStatus && gameState.value.gameStatus !== 'WAITING_FOR_PLAYERS') {
                    gameHasStarted.value = true;
                }
                console.log("Game state update:", gameState.value);
            });
        },
        onDisconnect: () => isGameConnected.value = false,
    });
    gameClient.activate();

    const chatSocket = new SockJS('http://localhost:8100/ws-chat');
    const chatClient = new Client({
        webSocketFactory: () => chatSocket,
        connectHeaders: headers,
        onConnect: () => {
            isChatConnected.value = true;
            console.log('Connected to Chat Service WebSocket!');
            chatClient.subscribe(`/topic/room/${roomId.value}/chat`, (message) => {
                const chatMsg = JSON.parse(message.body);
                if (chatMsg.sender === 'System') {
                    addSystemMessage(chatMsg.content);
                    return;
                }
                chatMessages.value.push({
                    id: chatMsg.timestamp,
                    senderId: chatMsg.sender,
                    senderName: chatMsg.sender,
                    content: chatMsg.content,
                    timestamp: chatMsg.timestamp,
                    isSystem: false,
                });
                scrollToBottom();
            });
            chatClient.subscribe(`/topic/room/${roomId.value}/players`, (message) => {
                const update = JSON.parse(message.body);
                if (update.systemMessage) {
                    addSystemMessage(update.systemMessage);
                }
            });
        },
        onDisconnect: () => isChatConnected.value = false,
    });
    chatClient.activate();
    stompClient.value = { game: gameClient, chat: chatClient };
};

const performAction = (actionType) => {
    if (!isGameConnected.value || !stompClient.value.game.active) return;
    
    const playerAction = { action: actionType };
    const requestBody = {
        action: playerAction,
        playerId: currentUserLogin.value
    };

    stompClient.value.game.publish({
        destination: `/app/game/${roomId.value}/action`,
        body: JSON.stringify(requestBody)
    });
};

const sendMessage = () => {
    if (!newMessage.value.trim() || !isChatConnected.value || !stompClient.value.chat.active) return;
    stompClient.value.chat.publish({
        destination: `/app/room/${roomId.value}/chat.sendMessage`,
        body: JSON.stringify({ content: newMessage.value.trim(), sender: authState.login })
    });
    newMessage.value = '';
};

const addSystemMessage = (content) => {
    chatMessages.value.push({
        id: Date.now(),
        senderId: 'system',
        senderName: 'System',
        content: content,
        timestamp: new Date(),
        isSystem: true
    });
    scrollToBottom();
};

const getCardSymbol = (card) => {
    if (!card || !card.code) return '';
    const suitSymbols = { 'S': '♠', 'H': '♥', 'D': '♦', 'C': '♣' };
    const value = card.code.slice(0, -1);
    const suit = card.code.slice(-1);
    return `${value}${suitSymbols[suit] || ''}`;
};
const getCardClass = (card) => {
    if (!card || !card.code) return 'card-back';
    if(card.code === 'BK') return 'card-back';
    const suit = card.code.slice(-1);
    return (suit === 'H' || suit === 'D') ? 'red-card' : 'black-card';
};
const formatTime = (timestamp) => new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
const scrollToBottom = () => nextTick(() => {
    if (chatMessagesContainer.value) chatMessagesContainer.value.scrollTop = chatMessagesContainer.value.scrollHeight;
});


onMounted(async () => {
    await fetchRoomData();
    await connectToWebsockets();
});

onUnmounted(() => {
    if (stompClient.value?.game) stompClient.value.game.deactivate();
    if (stompClient.value?.chat) stompClient.value.chat.deactivate();
});
</script>

<style scoped>
.player-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.player-details {
    display: flex;
    align-items: center;
}
.player-item.winner {
    background-color: #fff3cd;
    border: 1px solid #ffeeba;
    font-weight: bold;
}

.spectator-view, .waiting-area {
    color: white;
}
.countdown-message {
    color: #ffd700;
    animation: pulse 1.5s infinite;
}
@keyframes pulse {
    0% { transform: scale(1); opacity: 0.8; }
    50% { transform: scale(1.05); opacity: 1; }
    100% { transform: scale(1); opacity: 0.8; }
}
.player-item.winner {
    background-color: #fff3cd;
    border: 1px solid #ffeeba;
    font-weight: bold;
}
.game-room-container { height: calc(100vh - 85px); padding: 1rem; overflow: hidden; background: #2c3e50; }
.game-room-layout { display: flex; height: 100%; gap: 1rem; max-width: 1600px; margin: 0 auto; }
.game-area { flex: 1; background: rgba(0, 0, 0, 0.2); border-radius: 1rem; padding: 1.5rem; display: flex; align-items: center; justify-content: center; }
.game-content { width: 100%; height: 100%; }
.blackjack-board {
display: flex;
flex-direction: column;
justify-content: space-between;
height: 100%;
color: white;
}
.hand-area { padding: 1rem; background: rgba(0,0,0,0.2); border-radius: .5rem; }
.area-title { margin-bottom: 1rem; font-weight: bold; }
.cards-container { display: flex; gap: .5rem; min-height: 120px; }
.player-actions { padding: 1rem; }
.card {
width: 80px;
height: 120px;
border-radius: 8px;
background-color: white;
border: 1px solid #333;
display: flex;
align-items: center;
justify-content: center;
font-size: 2rem;
font-weight: bold;
box-shadow: 0 4px 8px rgba(0,0,0,0.2);
}
.card.red-card { color: #d63031; }
.card.black-card { color: #2d3436; }
.card.card-back {
background-image: linear-gradient(45deg, #2980b9, #3498db);
color: transparent;
}
.other-players-container { display: flex; justify-content: center; gap: 1rem; margin-top: 1rem; }
.other-player-hand { background: rgba(255,255,255,0.1); padding: .5rem; border-radius: .5rem; text-align: center; }
.cards-container-small { display: flex; gap: .25rem; min-height: 70px; }
.card-small {
width: 40px; height: 60px; border-radius: 4px; background-color: white; display: flex;
align-items: center; justify-content: center; font-size: 1rem; font-weight: bold;
}
.card-small.red-card { color: #d63031; }
.card-small.black-card { color: #2d3436; }
.sidebar { width: 350px; display: flex; flex-direction: column; gap: 1rem; }
.room-info-panel, .chat-panel { background: white; border-radius: 1rem; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
.room-info-panel { padding: 1.5rem; }
.chat-panel { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.room-header { padding-bottom: 1rem; border-bottom: 1px solid #dee2e6; }
.room-title { color: #2c3e50; }
.section-title { color: #495057; margin-top: 1.5rem; margin-bottom: 1rem; font-weight: 600; }
.players-list { max-height: 200px; overflow-y: auto; }
.player-item { padding: 0.75rem; background: #f8f9fa; border-radius: 0.5rem; margin-bottom: 0.5rem; display: flex; align-items: center; }
.chat-header { padding: 1rem 1.5rem; background: #f8f9fa; border-bottom: 1px solid #dee2e6; }
.chat-messages { flex: 1; padding: 1rem; overflow-y: auto; display: flex; flex-direction: column; gap: .75rem; }
.chat-input { padding: 1rem; }
.chat-message { display: flex; }
.chat-message:not(.own-message) { justify-content: flex-start; }
.chat-message.own-message { justify-content: flex-end; }
.message-wrapper { max-width: 80%; }
.message-header { display: flex; justify-content: space-between; margin-bottom: .25rem; }
.sender-name { font-weight: bold; font-size: .9em; }
.message-time { font-size: .8em; color: #6c757d; }
.message-content { padding: .5rem 1rem; border-radius: 1rem; word-wrap: break-word; }
.chat-message:not(.own-message) .message-content { background: #e9ecef; border-bottom-left-radius: .25rem; }
.chat-message.own-message .message-content { background: #0d6efd; color: white; border-bottom-right-radius: .25rem; }
.system-message .message-content { background: transparent; font-style: italic; color: #6c757d; text-align: center; width: 100%; }
.system-message .message-wrapper { width: 100%; max-width: 100%; text-align: center; }
</style>