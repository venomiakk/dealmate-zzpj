<template>
    <div v-if="show" class="modal-overlay" @click="closeModal">
        <div class="modal-content" @click.stop>
            <div class="modal-header">
                <h4>Create New Room</h4>
                <button @click="closeModal" class="btn-close" aria-label="Close">
                    <i class="fas fa-times"></i>
                </button>
            </div>

            <div class="modal-body">
                <form @submit.prevent="handleSubmit">
                    <div class="mb-3">
                        <label for="roomName" class="form-label">Room Name</label>
                        <input
                            v-model="roomConfig.name"
                            type="text"
                            id="roomName"
                            class="form-control"
                            placeholder="Enter room name"
                            required
                            maxlength="50"
                        />
                    </div>

                    <div class="mb-3">
                        <label for="gameType" class="form-label">Game Type</label>
                        <select
                            v-model="roomConfig.gameType"
                            id="gameType"
                            class="form-control"
                            required
                        >
                            <option
                                v-for="gameType in gameTypes"
                                :key="gameType.value"
                                :value="gameType.value"
                            >
                                {{ gameType.label }}
                            </option>
                        </select>
                    </div>

                    <div class="mb-3">
                        <label for="maxPlayers" class="form-label">Maximum Players</label>
                        <select
                            v-model="roomConfig.maxPlayers"
                            id="maxPlayers"
                            class="form-control"
                            required
                        >
                            <option
                                v-for="playerOption in maxPlayersOptions"
                                :key="playerOption.value"
                                :value="playerOption.value"
                            >
                                {{ playerOption.label }}
                            </option>
                        </select>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Room Visibility</label>
                        <div
                            v-for="visibility in visibilityOptions"
                            :key="visibility.value"
                            class="form-check"
                        >
                            <input
                                v-model="roomConfig.isPublic"
                                type="radio"
                                :id="visibility.value ? 'public' : 'private'"
                                name="visibility"
                                :value="visibility.value"
                                class="form-check-input"
                            />
                            <label
                                :for="visibility.value ? 'public' : 'private'"
                                class="form-check-label"
                            >
                                <i :class="visibility.icon" class="me-2"></i>{{ visibility.label }}
                            </label>
                        </div>
                    </div>
                </form>
            </div>

            <div class="modal-footer">
                <button @click="closeModal" type="button" class="btn btn-secondary">Cancel</button>
                <button
                    @click="handleSubmit"
                    type="button"
                    class="btn btn-primary"
                    :disabled="!isFormValid"
                >
                    Create Room
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { authState } from '@/auth/auth'
import { GAME_TYPES, MAX_PLAYERS_OPTIONS, VISIBILITY_OPTIONS } from '@/constants/constants'

const props = defineProps({
    show: {
        type: Boolean,
        default: false,
    },
})

const emit = defineEmits(['close', 'create'])

// Configuration options
const gameTypes = ref(GAME_TYPES)

const maxPlayersOptions = ref(MAX_PLAYERS_OPTIONS)

const visibilityOptions = ref(VISIBILITY_OPTIONS)

const roomConfig = ref({
    ownerLogin: authState.login,
    name: '',
    gameType: '',
    maxPlayers: 4,
    isPublic: true,
})

const isFormValid = computed(() => {
    return roomConfig.value.name.trim() !== '' && roomConfig.value.gameType !== ''
})

const closeModal = () => {
    emit('close')
    resetForm()
}

const handleSubmit = () => {
    if (!isFormValid.value) return
    emit('create', { ...roomConfig.value })
    closeModal()
}

const resetForm = () => {
    roomConfig.value = {
        ownerLogin: authState.login,
        name: authState.login ? `${authState.login}'s Room` : 'New Room',
        gameType: gameTypes.value[0].value,
        maxPlayers: 4,
        isPublic: true,
    }
}

// Reset form when modal is opened
watch(
    () => props.show,
    (newVal) => {
        if (newVal) {
            resetForm()
        }
    },
)
</script>

<style scoped>
/* Modal Styles */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1050;
}

.modal-content {
    background: white;
    border-radius: 0.5rem;
    width: 90%;
    max-width: 500px;
    max-height: 90vh;
    overflow-y: auto;
    box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem 1.5rem;
    border-bottom: 1px solid #dee2e6;
}

.modal-header h4 {
    margin: 0;
}

.btn-close {
    background: none;
    border: none;
    font-size: 1.25rem;
    cursor: pointer;
    color: #6c757d;
    padding: 0;
}

.btn-close:hover {
    color: #000;
}

.modal-body {
    padding: 1.5rem;
}

.modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 0.5rem;
    padding: 1rem 1.5rem;
    border-top: 1px solid #dee2e6;
}

.form-label {
    font-weight: 600;
    margin-bottom: 0.5rem;
    display: block;
}

.form-control {
    width: 100%;
    padding: 0.375rem 0.75rem;
    border: 1px solid #ced4da;
    border-radius: 0.375rem;
    font-size: 1rem;
}

.form-control:focus {
    border-color: #86b7fe;
    outline: 0;
    box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
}

.form-check {
    margin-bottom: 0.5rem;
}

.form-check-input {
    margin-right: 0.5rem;
}

.form-check-label {
    cursor: pointer;
    display: flex;
    align-items: center;
}

.btn {
    padding: 0.375rem 0.75rem;
    border: 1px solid transparent;
    border-radius: 0.375rem;
    cursor: pointer;
    font-size: 1rem;
    text-decoration: none;
    display: inline-block;
}

.btn-primary {
    background-color: #0d6efd;
    border-color: #0d6efd;
    color: white;
}

.btn-primary:hover:not(:disabled) {
    background-color: #0b5ed7;
    border-color: #0a58ca;
}

.btn-primary:disabled {
    background-color: #6c757d;
    border-color: #6c757d;
    opacity: 0.65;
    cursor: not-allowed;
}

.btn-secondary {
    background-color: #6c757d;
    border-color: #6c757d;
    color: white;
}

.btn-secondary:hover {
    background-color: #5c636a;
    border-color: #565e64;
}
</style>
