<template>
    <div class="card room-card" :class="{ unavailable: isRoomFull }">
        <div class="card-body">
            <div class="d-flex justify-content-between align-items-start">
                <div class="flex-grow-1">
                    <h5 class="card-title mb-2">
                        {{ room.name }}
                    </h5>

                    <p class="card-text text-muted mb-2">{{ gameTypeName }}</p>

                    <div class="room-details">
                        <small class="text-muted">
                            <i class="fas fa-users me-1"></i>
                            Players: {{ playersCount }} / {{ room.maxPlayers }}
                        </small>
                    </div>
                </div>

                <div>
                    <div class="room-actions">
                        <button
                            @click="handleJoinRoom"
                            class="btn btn-sm ms-auto"
                            :class="buttonClass"
                            :disabled="isJoinDisabled"
                            :title="buttonTooltip"
                        >
                            {{ buttonText }}
                        </button>
                    </div>
                    <div>
                        <small class="text-muted">
                            <i class="fas fa-users me-1"></i>
                            Owner: {{ room.ownerLogin ? room.ownerLogin : 'Unknown' }}
                        </small>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import { GAME_TYPES } from '@/constants/constants'

const props = defineProps({
    room: {
        type: Object,
        required: true,
    },
})

const emit = defineEmits(['join'])

const playersCount = computed(() => {
    if (Array.isArray(props.room.players)) {
        return props.room.players.length
    } else if (typeof props.room.players === 'number') {
        return props.room.players
    }
    return 0
})

const isRoomFull = computed(() => {
    return playersCount.value >= props.room.maxPlayers
})

const gameTypeName = computed(() => {
    const gameType = GAME_TYPES.find((type) => type.value === props.room.gameType)
    return gameType ? gameType.label : 'Unknown Game Type'
})

const isJoinDisabled = computed(() => {
    return isRoomFull.value
})

const buttonText = computed(() => {
    if (isRoomFull.value) return 'Full'
    return 'Join Room'
})

const buttonClass = computed(() => {
    if (isJoinDisabled.value) return 'btn-secondary'
    return 'btn-primary'
})

const buttonTooltip = computed(() => {
    if (isRoomFull.value) return 'Room is full'
    return 'Click to join this room'
})

const handleJoinRoom = () => {
    if (!isJoinDisabled.value) {
        emit('join', props.room)
        // console.log(`Joining room: ${props.room.name}`)
    }
}
</script>

<style scoped>
.room-card {
    transition: all 0.3s ease;
    border-left: 4px solid #28a745;
}

.room-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.room-card.unavailable {
    border-left-color: #dc3545;
}

.badge-success {
    background-color: #28a745;
}

.badge-danger {
    background-color: #dc3545;
}

.badge-warning {
    background-color: #ffc107;
}

.room-actions {
    display: flex;
    gap: 0.25rem;
}

.btn-sm {
    padding: 0.25rem 0.5rem;
}

.btn:disabled {
    cursor: not-allowed;
    opacity: 0.6;
}
</style>
