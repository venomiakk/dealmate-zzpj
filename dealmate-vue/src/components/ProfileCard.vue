<template>
    <div class="container mt-4">
        <div v-if="isLoading" class="text-center py-5">
            <div class="spinner-border" role="status">
                <span class="visually-hidden">Loading profile...</span>
            </div>
            <p class="mt-2">Loading {{ isPublicProfile ? 'user' : 'your' }} profile...</p>
        </div>

        <div v-else-if="error" class="alert alert-danger">
            <h4>Error loading profile</h4>
            <p>{{ error }}</p>
            <button @click="fetchUserProfile" class="btn btn-outline-danger">Try Again</button>
        </div>

        <div v-else-if="userProfile" class="row">
            <div class="col-md-8 mx-auto">
                <div class="card">
                    <div class="card-header">
                        <h2 class="mb-0">
                            <i class="fas fa-user me-2"></i>
                            {{
                                isPublicProfile
                                    ? `${userProfile.username}'s Profile`
                                    : 'Your Profile'
                            }}
                        </h2>
                    </div>

                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-4 text-center mb-4">
                                <div class="avatar-container">
                                    <img
                                        :src="userProfile.avatar || defaultAvatar"
                                        :alt="userProfile.username"
                                        class="rounded-circle avatar-img"
                                    />
                                </div>
                                <h4 class="mt-3">{{ userProfile.username }}</h4>
                            </div>

                            <div class="col-md-8">
                                <div class="row mb-3">
                                    <div class="col-sm-4">
                                        <strong>Full Name:</strong>
                                    </div>
                                    <div class="col-sm-8">
                                        {{
                                            userProfile.firstName && userProfile.lastName
                                                ? `${userProfile.firstName} ${userProfile.lastName}`
                                                : 'Not provided'
                                        }}
                                    </div>
                                </div>

                                <div class="row mb-3">
                                    <div class="col-sm-4">
                                        <strong>Member Since:</strong>
                                    </div>
                                    <div class="col-sm-8 d-flex align-items-center">
                                        {{ new Date(userProfile.createdAt).toLocaleDateString() }}
                                    </div>
                                </div>

                                <div class="row mb-3">
                                    <div class="col-sm-4">
                                        <strong>Credits:</strong>
                                    </div>
                                    <div class="col-sm-8">
                                        {{ userProfile.credits != null ? userProfile.credits : '0' }}
                                    </div>
                                </div>

                                <div class="row mb-3">
                                    <div class="col-sm-4 d-flex align-items-center">
                                        <strong>Nationality:</strong>
                                    </div>
                                    <div class="col-sm-8">
                                        <div v-if="countryInfo" class="d-flex align-items-center">
                                            <a
                                                :href="countryInfo.wikiUrl"
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                class="text-decoration-none d-flex align-items-center"
                                                :title="`Learn more about ${countryInfo.name}`"
                                            >
                                                <img
                                                    :src="`https://flagsapi.com/${userProfile.countryCode}/flat/64.png`"
                                                    :alt="`${countryInfo.name} flag`"
                                                    class="me-2 flag-img"
                                                />
                                                <span class="country-name">{{
                                                    countryInfo.name
                                                }}</span>
                                                <i
                                                    class="fas fa-external-link-alt ms-2 text-muted small"
                                                ></i>
                                            </a>
                                        </div>
                                        <p
                                            v-else
                                            class="d-flex align-items-center h-100 mb-0"
                                            style="min-height: 64px"
                                        >
                                            Not provided
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div v-if="!isPublicProfile" class="text-center mt-4">
                            <button @click="editProfile" class="btn btn-primary me-2">
                                <i class="fas fa-edit me-1"></i>
                                Edit Profile
                            </button>
                        </div>
                    </div>
                </div>

                <div class="card mt-4">
                    <div class="card-header">
                        <h3 class="mb-0"><i class="fas fa-history me-2"></i>Game History</h3>
                    </div>
                    <div class="card-body p-0">
                        <div v-if="historyLoading" class="text-center p-4">
                            <div class="spinner-border spinner-border-sm" role="status"></div>
                            <span class="ms-2">Loading history...</span>
                        </div>
                        <div v-else-if="historyError" class="alert alert-warning m-3">{{ historyError }}</div>
                        <div v-else-if="gameHistory.length === 0" class="text-center p-4 text-muted">No game history found.</div>
                        <div v-else class="table-responsive">
                            <table class="table table-striped table-hover mb-0">
                                <thead>
                                    <tr>
                                        <th>Game</th>
                                        <th>Result</th>
                                        <th>Amount</th>
                                        <th>Date</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr v-for="entry in gameHistory" :key="entry.id">
                                        <td>{{ formatResult(entry.gameType) }}</td>
                                        <td>
                                            <span class="badge" :class="getResultClass(entry.result)">
                                                {{ formatResult(entry.result) }}
                                            </span>
                                        </td>
                                        <td :class="getAmountClass(entry.amount)">
                                            {{ entry.amount >= 0 ? '+' : '' }}{{ entry.amount }}
                                        </td>
                                        <td>{{ new Date(entry.timestamp).toLocaleString() }}</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { authState, auth } from '../auth/auth.js'
import { userService, gameService } from '@/api/endpoints' // ZMIANA: Dodano gameService
import axios from 'axios'
import { countries } from 'countries-list'

// Props
const props = defineProps({
    userLogin: {
        type: String,
        required: true,
    },
    isPublicProfile: {
        type: Boolean,
        default: false,
    },
})

const router = useRouter()

// Reactive data
const userProfile = ref(null)
const isLoading = ref(true)
const error = ref(null)

// ZMIANA: Nowe zmienne dla historii gier
const gameHistory = ref([]);
const historyLoading = ref(true);
const historyError = ref(null);

// Default avatar
const defaultAvatar = 'https://placehold.co/600x400?text=Profile+Picture'

// Computed property for country info
const countryInfo = computed(() => {
    if (!userProfile.value?.countryCode) return null
    const countryData = countries[userProfile.value.countryCode]
    if (!countryData) return null
    return {
        name: countryData.name,
        wikiUrl: `https://en.wikipedia.org/wiki/${countryData.name.replace(/\s+/g, '_')}`,
    }
})

// Methods
const fetchUserProfile = async () => {
    isLoading.value = true
    error.value = null
    try {
        if (!props.userLogin) throw new Error('No user login provided')
        if (!props.isPublicProfile) {
            if (!authState.isAuthenticated) throw new Error('User not authenticated')
            const token = await auth.getToken()
            if (!token) throw new Error('No access token available')
        }
        const response = await axios.get(userService.fetchUserDataByLogin(props.userLogin), {
            timeout: 10000,
        })
        console.log('Profile data received:', response.data)
        userProfile.value = response.data
    } catch (err) {
        console.error('Error fetching user profile:', err)
        if (err.response) {
            const status = err.response.status
            if (status === 401) {
                error.value = 'Session expired. Please log in again.'
                if (!props.isPublicProfile) setTimeout(() => auth.login(), 2000)
            } else if (status === 403) {
                error.value = 'Access denied. You do not have permission to view this profile.'
            } else if (status === 404) {
                error.value = 'Profile not found.'
            } else {
                error.value = `Server error: ${err.response.data?.message || 'Unknown error'}`
            }
        } else if (err.code === 'ERR_NETWORK') {
            error.value = 'Network error. Please check your connection and try again.'
        } else {
            error.value = err.message || 'Failed to load profile'
        }
    } finally {
        isLoading.value = false
    }
}

// ZMIANA: Nowa metoda do pobierania historii gier
const fetchGameHistory = async () => {
    if (!props.userLogin) return;
    historyLoading.value = true;
    historyError.value = null;
    try {
        const response = await axios.get(gameService.fetchHistory(props.userLogin));
        gameHistory.value = response.data;
    } catch (err) {
        console.error("Failed to fetch game history:", err);
        historyError.value = "Could not load game history.";
    } finally {
        historyLoading.value = false;
    }
};

// ZMIANA: Nowe metody pomocnicze do formatowania danych w tabeli
const formatResult = (result) => {
    if (!result) return '';
    return result.replace('_', ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
};

const getResultClass = (result) => {
    if (result.includes('WIN')) return 'bg-success';
    if (result === 'LOSS') return 'bg-danger';
    return 'bg-secondary';
};

const getAmountClass = (amount) => {
    if (amount > 0) return 'text-success fw-bold';
    if (amount < 0) return 'text-danger fw-bold';
    return 'text-muted';
};

const editProfile = () => {
    router.push('/profile/edit')
}

// ZMIANA: watch i onMounted teraz pobierają również historię
watch(() => props.userLogin, (newLogin) => {
    if (newLogin) {
        fetchUserProfile()
        fetchGameHistory()
    }
}, { immediate: false })

onMounted(async () => {
    if (!props.isPublicProfile && authState.isLoading) {
        await auth.init()
    }
    if (!props.isPublicProfile && !authState.isAuthenticated) {
        error.value = 'Please log in to view your profile'
        isLoading.value = false
        return
    }
    if (props.userLogin) {
        await fetchUserProfile()
        await fetchGameHistory()
    }
})
</script>

<style scoped>
.avatar-img {
    width: 150px;
    height: 150px;
    object-fit: cover;
    border: 3px solid #dee2e6;
}
.avatar-container {
    position: relative;
    display: inline-block;
}
.card {
    box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
    border: 1px solid rgba(0, 0, 0, 0.125);
}
.card-header {
    background-color: #f8f9fa;
    border-bottom: 1px solid rgba(0, 0, 0, 0.125);
}
.badge {
    font-size: 0.875em;
}
.flag-img {
    width: 48px;
    height: auto;
    border-radius: 4px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    transition: transform 0.2s ease;
}
.flag-img:hover {
    transform: scale(1.05);
}
.country-name {
    color: #495057;
    font-weight: 500;
    transition: color 0.2s ease;
}
a:hover .country-name {
    color: #007bff;
}
a:hover .flag-img {
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}
/* ZMIANA: Dodano style dla tabeli historii */
.table-responsive {
    max-height: 400px;
    overflow-y: auto;
}
@media (max-width: 768px) {
    .avatar-img {
        width: 100px;
        height: 100px;
    }
}
</style>