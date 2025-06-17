<template>
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-md-6 col-lg-5">
                <div class="card shadow">
                    <div class="card-body p-4">
                        <h2 class="card-title text-center mb-4">Create Account</h2>

                        <form @submit.prevent="handleSubmit">
                            <!-- Username Field -->
                            <div class="mb-3">
                                <label for="username" class="form-label">Username</label>
                                <input
                                    id="username"
                                    v-model="form.username"
                                    type="text"
                                    class="form-control"
                                    :class="{ 'is-invalid': errors.username }"
                                    placeholder="Enter your username"
                                    required
                                />
                                <div v-if="errors.username" class="invalid-feedback">
                                    {{ errors.username }}
                                </div>
                            </div>

                            <!-- Email Field -->
                            <div class="mb-3">
                                <label for="email" class="form-label">Email</label>
                                <input
                                    id="email"
                                    v-model="form.email"
                                    type="email"
                                    class="form-control"
                                    :class="{ 'is-invalid': errors.email }"
                                    placeholder="Enter your email"
                                    required
                                />
                                <div v-if="errors.email" class="invalid-feedback">
                                    {{ errors.email }}
                                </div>
                            </div>

                            <!-- Password Field -->
                            <div class="mb-3">
                                <label for="password" class="form-label">Password</label>
                                <div class="input-group">
                                    <input
                                        id="password"
                                        v-model="form.password"
                                        :type="showPassword ? 'text' : 'password'"
                                        class="form-control"
                                        :class="{ 'is-invalid': errors.password }"
                                        placeholder="Enter your password"
                                        required
                                    />
                                    <button
                                        type="button"
                                        class="btn btn-outline-secondary"
                                        @click="togglePasswordVisibility"
                                    >
                                        <i
                                            :class="
                                                showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'
                                            "
                                        ></i>
                                    </button>
                                </div>
                                <div v-if="errors.password" class="invalid-feedback">
                                    {{ errors.password }}
                                </div>
                                <div class="form-text">
                                    Password must be at least 6 characters long
                                </div>
                            </div>

                            <!-- Submit Button -->
                            <button
                                type="submit"
                                class="btn btn-primary w-100"
                                :disabled="isLoading"
                            >
                                <span
                                    v-if="isLoading"
                                    class="spinner-border spinner-border-sm me-2"
                                ></span>
                                {{ isLoading ? 'Creating Account...' : 'Create Account' }}
                            </button>
                        </form>

                        <!-- Success/Error Messages -->
                        <div v-if="successMessage" class="alert alert-success mt-3">
                            {{ successMessage }}
                        </div>

                        <div v-if="errorMessage" class="alert alert-danger mt-3">
                            {{ errorMessage }}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { userService, API_LOGIN_URL } from '@/api/endpoints'
import axios from 'axios'

// Form data
const form = reactive({
    username: '',
    email: '',
    password: '',
})

// Form state
const errors = ref({})
const isLoading = ref(false)
const showPassword = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

// Toggle password visibility
const togglePasswordVisibility = () => {
    showPassword.value = !showPassword.value
}

// Validation functions
const validateForm = () => {
    const newErrors = {}

    // Username validation
    if (!form.username) {
        newErrors.username = 'Username is required'
    } else if (form.username.length < 3) {
        newErrors.username = 'Username must be at least 3 characters long'
    } else if (!/^[a-zA-Z0-9_]+$/.test(form.username)) {
        newErrors.username = 'Username can only contain letters, numbers, and underscores'
    }

    // Email validation
    if (!form.email) {
        newErrors.email = 'Email is required'
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
        newErrors.email = 'Please enter a valid email address'
    }

    // Password validation
    if (!form.password) {
        newErrors.password = 'Password is required'
    } else if (form.password.length < 6) {
        newErrors.password = 'Password must be at least 6 characters long'
    }
    errors.value = newErrors
    return Object.keys(newErrors).length === 0
}

// Form submission
const handleSubmit = async () => {
    // Clear previous messages
    successMessage.value = ''
    errorMessage.value = ''
    errors.value = {}
    // Validate form
    if (!validateForm()) {
        return
    }

    isLoading.value = true

    try {
        const registrationData = {
            username: form.username,
            email: form.email,
            password: form.password,
        }

        const response = await axios.post(userService.register, registrationData, {
            timeout: 5000, // 5 seconds timeout
        })
        console.log('Registration response:', response)
        if (response.status === 201) {
            successMessage.value = 'Account created successfully! Redirecting to login...'
            setTimeout(() => {
                window.open(API_LOGIN_URL, '_self')
            }, 1000)
        }
    } catch (error) {
        errorMessage.value = 'Registration failed: ' + (error.response?.data || 'Unknown error')
        console.error('Registration error:', error)
    } finally {
        isLoading.value = false
    }
}
</script>

<style scoped>
.card {
    margin-top: 2rem;
    margin-bottom: 2rem;
}

.input-group .btn {
    border-left: none;
}

.form-check-input.is-invalid {
    border-color: #dc3545;
}

.spinner-border-sm {
    width: 1rem;
    height: 1rem;
}

.alert {
    border-radius: 0.375rem;
}

@media (max-width: 576px) {
    .card-body {
        padding: 1.5rem !important;
    }
}
</style>
