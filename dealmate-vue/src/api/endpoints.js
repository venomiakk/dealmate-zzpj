const API_GATEWAY_URL = 'http://localhost:8100'
// const API_USERSERVICE_URL = 'http://localhost:8102'
export const API_LOGIN_URL = 'http://127.0.0.1:9000/login'

export const userService = {
    register: `${API_GATEWAY_URL}/userservice/user/register`,
    fetchUserDataByLogin: (login) =>
        `${API_GATEWAY_URL}/userservice/user/getuser/username/${login}`,
    updateUserData: (login) => `${API_GATEWAY_URL}/userservice/user/update/${login}`,
}

export const gameService = {
    fetchAllRooms: `${API_GATEWAY_URL}/gameservice/game`,
    createRoom: `${API_GATEWAY_URL}/gameservice/game/create`,
    joinRoom: (roomId) => `${API_GATEWAY_URL}/gameservice/game/${roomId}/join`,
    fetchRoomById: (roomId) => `${API_GATEWAY_URL}/gameservice/game/get/${roomId}`,
    leaveRoom: (roomId) => `${API_GATEWAY_URL}/gameservice/game/${roomId}/leave`,
}
