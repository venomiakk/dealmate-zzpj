export const GAME_TYPES = [
    { value: 'TEXAS_HOLDEM', label: 'Texas Holdem' },
    { value: 'FIVE_CARDS_DRAW', label: 'Five Cards Draw' },
]

export const MAX_PLAYERS_OPTIONS = [
    { value: 2, label: '2 Players' },
    { value: 3, label: '3 Players' },
    { value: 4, label: '4 Players' },
    { value: 6, label: '6 Players' },
    { value: 8, label: '8 Players' },
]

export const VISIBILITY_OPTIONS = [
    {
        value: true,
        label: 'Public - Anyone can join',
        icon: 'fas fa-globe',
    },
    {
        value: false,
        label: 'Private - Invite only',
        icon: 'fas fa-lock',
    },
]

export const DEFAULT_ROOM_CONFIG = {
    name: '',
    gameType: '',
    maxPlayers: 4,
    isPublic: true,
}
