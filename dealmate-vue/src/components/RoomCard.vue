<template>
    <div class="card room-card" :class="{ unavailable: isRoomFull }">
        <div class="card-body">
            <div class="d-flex justify-content-between align-items-start">
                <div class="flex-grow-1">
                    <h5 class="card-title mb-2">
                        {{ room.name }}
                        <!-- Badge dla entry fee -->
                        <span v-if="room.entryFee > 0" class="badge badge-warning ms-2">
                            {{ room.entryFee }}
                        </span>
                        <span v-else class="badge badge-success ms-2"> Free </span>
                    </h5>

                    <p class="card-text text-muted mb-2">{{ gameTypeName }}</p>

                    <div class="room-details">
                        <small class="text-muted d-block mb-1">
                            <i class="fas fa-users me-1"></i>
                            Players: {{ playersCount }} / {{ room.maxPlayers }}
                        </small>
                        <small class="text-muted d-block">
                            <i class="fas fa-dollar-sign me-1"></i>
                            Entry Fee: {{ room.entryFee > 0 ? `${room.entryFee}` : 'Free' }}
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
                    <div class="mt-2">
                        <small class="text-muted">
                            <i class="fas fa-crown me-1"></i>
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
    return props.room.entryFee > 0 ? `Join - ${props.room.entryFee}` : 'Join Room'
})

const buttonClass = computed(() => {
    if (isJoinDisabled.value) return 'btn-secondary'
    return props.room.entryFee > 0 ? 'btn-warning' : 'btn-primary'
})

const buttonTooltip = computed(() => {
    if (isRoomFull.value) return 'Room is full'
    if (props.room.entryFee > 0)
        return `Click to join this room (Entry fee: ${props.room.entryFee})`
    return 'Click to join this room'
})

const handleJoinRoom = () => {
    if (!isJoinDisabled.value) {
        // Optional: Show confirmation for paid rooms
        if (props.room.entryFee > 0) {
            const confirmed = confirm(`Join room "${props.room.name}" for ${props.room.entryFee}?`)
            if (!confirmed) return
        }

        emit('join', props.room)
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

/* Badge styles */
.badge-success {
    background-color: #28a745;
    color: white;
    font-size: 0.75rem;
    padding: 0.25rem 0.5rem;
    border-radius: 0.375rem;
}

.badge-warning {
    background-color: #ffc107;
    color: #212529;
    font-size: 0.75rem;
    padding: 0.25rem 0.5rem;
    border-radius: 0.375rem;
}

.badge-danger {
    background-color: #dc3545;
    color: white;
}

/* Button styles */
.room-actions {
    display: flex;
    gap: 0.25rem;
}

.btn-sm {
    padding: 0.25rem 0.5rem;
    font-size: 0.875rem;
}

.btn-primary {
    background-color: #0d6efd;
    border-color: #0d6efd;
    color: white;
}

.btn-warning {
    background-color: #ffc107;
    border-color: #ffc107;
    color: #212529;
}

.btn-secondary {
    background-color: #6c757d;
    border-color: #6c757d;
    color: white;
}

.btn:disabled {
    cursor: not-allowed;
    opacity: 0.6;
}

.btn:hover:not(:disabled) {
    transform: translateY(-1px);
}

/* Icon colors */
.fa-dollar-sign {
    color: #28a745;
}

.fa-crown {
    color: #ffc107;
}

/* Layout improvements */
.room-details small {
    line-height: 1.4;
}
</style>
