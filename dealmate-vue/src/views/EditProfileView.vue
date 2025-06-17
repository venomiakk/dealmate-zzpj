<template>
    <div>
        <div class="container mt-4">
            <div class="row">
                <div class="col-md-8 offset-md-2">
                    <!-- Alert messages -->
                    <div
                        v-if="successMessage"
                        class="alert alert-success alert-dismissible fade show"
                        role="alert"
                    >
                        <i class="bi bi-check-circle-fill me-2"></i>
                        {{ successMessage }}
                        <button
                            type="button"
                            class="btn-close"
                            @click="successMessage = ''"
                            aria-label="Close"
                        ></button>
                    </div>

                    <div
                        v-if="errorMessage"
                        class="alert alert-danger alert-dismissible fade show"
                        role="alert"
                    >
                        <i class="bi bi-exclamation-triangle-fill me-2"></i>
                        {{ errorMessage }}
                        <button
                            type="button"
                            class="btn-close"
                            @click="errorMessage = ''"
                            aria-label="Close"
                        ></button>
                    </div>

                    <div class="card">
                        <div class="card-body">
                            <h5 class="card-title">Edit Your Profile</h5>
                            <form @submit.prevent="saveChanges">
                                <div class="mb-3">
                                    <label for="firstName" class="form-label">First Name</label>
                                    <input
                                        type="text"
                                        class="form-control"
                                        id="firstName"
                                        v-model="firstName"
                                    />
                                </div>
                                <div class="mb-3">
                                    <label for="lastName" class="form-label">Last Name</label>
                                    <input
                                        type="text"
                                        class="form-control"
                                        id="lastName"
                                        v-model="lastName"
                                    />
                                </div>
                                <div class="mb-3">
                                    <label for="country" class="form-label">Country</label>
                                    <div>
                                        <multiselect
                                            id="single-select-search"
                                            v-model="selectedCountry"
                                            :options="countriesList"
                                            placeholder="Select one"
                                            label="name"
                                            track-by="name"
                                        ></multiselect>
                                    </div>
                                </div>
                                <div class="text-center">
                                    <button
                                        type="submit"
                                        class="btn btn-primary"
                                        :disabled="isLoading"
                                    >
                                        <span
                                            v-if="isLoading"
                                            class="spinner-border spinner-border-sm me-2"
                                            role="status"
                                            aria-hidden="true"
                                        ></span>
                                        {{ isLoading ? 'Saving...' : 'Save Changes' }}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { authState } from '@/auth/auth'
import Multiselect from 'vue-multiselect'
import { countries } from 'countries-list'
import { userService } from '@/api/endpoints'
import axios from '@/services/axios'

const firstName = ref('')
const lastName = ref('')
const selectedCountry = ref('')
const successMessage = ref('')
const errorMessage = ref('')
const isLoading = ref(false)

const countriesList = computed(() => {
    return Object.keys(countries)
        .map((key) => {
            return {
                code: key,
                name: countries[key].name,
            }
        })
        .sort((a, b) => a.name.localeCompare(b.name))
})

const saveChanges = async () => {
    // Clear previous messages
    successMessage.value = ''
    errorMessage.value = ''
    isLoading.value = true

    try {
        const newUserData = {
            username: authState.user.profile.sub,
            firstName: firstName.value ? firstName.value : '',
            lastName: lastName.value ? lastName.value : '',
            countryCode: selectedCountry.value['code'] ? selectedCountry.value['code'] : null,
        }
        console.log('New user data:', newUserData)

        const response = await axios.patch(
            userService.updateUserData(newUserData.username),
            newUserData,
        )

        console.log('Profile updated successfully:', response.data)
        successMessage.value = 'Profile updated successfully!'

        // Auto-hide success message after 5 seconds
        setTimeout(() => {
            successMessage.value = ''
        }, 5000)
    } catch (error) {
        console.error('Error updating profile:', error)

        // Show specific error message based on response
        if (error.response?.data?.message) {
            errorMessage.value = `Failed to update profile: ${error.response.data.message}`
        } else if (error.response?.status === 404) {
            errorMessage.value = 'User not found. Please try again.'
        } else if (error.response?.status >= 500) {
            errorMessage.value = 'Server error. Please try again later.'
        } else {
            errorMessage.value =
                'Failed to update profile. Please check your connection and try again.'
        }
    } finally {
        isLoading.value = false
    }
}
</script>

<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>
