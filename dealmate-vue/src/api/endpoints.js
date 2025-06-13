const API_GATEWAY_URL = 'http://localhost:8100'
const API_USERSERVICE_URL = 'http://localhost:8102'
export const API_LOGIN_URL = 'http://127.0.0.1:9000/login'

// TODO: to use this, api gateway should have cors configured properly
export const gateway = {
    register: `${API_GATEWAY_URL}/userservice/user/register`,
}

export const userService = {
    register: `${API_USERSERVICE_URL}/user/register`,
}
