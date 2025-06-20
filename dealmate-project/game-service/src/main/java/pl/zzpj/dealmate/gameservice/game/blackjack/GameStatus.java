package pl.zzpj.dealmate.gameservice.game.blackjack;

public enum GameStatus {
    WAITING_FOR_PLAYERS,
    STARTING,
    PLAYER_TURN,
    DEALER_TURN,
    ROUND_FINISHED,
    GAME_OVER
}