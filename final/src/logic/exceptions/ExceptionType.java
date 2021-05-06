package logic.exceptions;

/**
 * Definiert die verschieden Exceptiontypen, die während des Cluedo Programms auftreten könnten.
 * Werden benutzt, um auf der GUI sprachunabhängige Fehlermeldungen zu generieren.
 */
public enum ExceptionType {
    IllegalNoteOthersInSavedGame,
    IllegalNoteSelfInSavedGame,
    InitialGameDataNotFound,
    CanceledAtGameStart,
    WeaponsInRoomsLength,
    WeaponNameNotFound,
    RoomNameNotFound,
    CharacterNameNotFound,
    AIDifficultyNotFound,
    PlayerToLoadInWrongRoom,
    PlayerToLoadInWall,
    CardNameNotFound,
    NoteSelfLength,
    NoteOthersLength,
    NoteOthersWrongFormat,
    FileNotFound,
    InvalidJSON,
    WritingError,
    PlayerNameNotFound,
    PlayerNotFound,
    WeaponNotFound,
    CharacterNotFound,
    RequestedButNotInRoom,
    InitialGameDataIOException,
    InitialGameDataSyntaxException,
    RestartGame,
    PlayerToLoadNotInRoomCenter,
    NullInField,
    PlayerToLoadRequestedButNotInRoom,
}
