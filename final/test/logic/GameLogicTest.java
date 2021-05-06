package logic;

import gui.JavaFXGUI;
import logic.exceptions.CluedoException;
import logic.json.InitialGameDataJSON;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Testklasse für die Spiellogik
 *
 * @author Michael Smirnov
 */
public class GameLogicTest {

    InitialGameDataJSON initialGameDataJSON;

    public GameLogicTest() throws CluedoException {
        this.initialGameDataJSON = JavaFXGUI.loadInitialGameData("/logic/config/InitialGameDataCluedo.json");
    }

    @Test
    public void gameFieldFromString_3x4_NoRoomsSymmetrical() {
        String[] gameFieldStr = new String[4];
        gameFieldStr[0] = "   ";
        gameFieldStr[1] = "###";
        gameFieldStr[2] = "###";
        gameFieldStr[3] = "   ";
        GameCell[][] res = GameLogic.gameFieldFromString(4, 3, gameFieldStr, null);
        GameCell[][] actual = new GameCell[4][];
        actual[0] = new GameCell[]{GameCell.CORRIDOR, GameCell.CORRIDOR, GameCell.CORRIDOR};
        actual[1] = new GameCell[]{GameCell.WALL, GameCell.WALL, GameCell.WALL};
        actual[2] = new GameCell[]{GameCell.WALL, GameCell.WALL, GameCell.WALL};
        actual[3] = new GameCell[]{GameCell.CORRIDOR, GameCell.CORRIDOR, GameCell.CORRIDOR};
        Assert.assertTrue(Arrays.deepEquals(res, actual));
    }

    @Test
    public void gameFieldFromString_3x4_NoRooms3RowsWall() {
        String[] gameFieldStr = new String[4];
        gameFieldStr[0] = "###";
        gameFieldStr[1] = "###";
        gameFieldStr[2] = "###";
        gameFieldStr[3] = "   ";
        GameCell[][] res = GameLogic.gameFieldFromString(4, 3, gameFieldStr, null);
        GameCell[][] actual = new GameCell[4][];
        actual[0] = new GameCell[]{GameCell.WALL, GameCell.WALL, GameCell.WALL};
        actual[1] = new GameCell[]{GameCell.WALL, GameCell.WALL, GameCell.WALL};
        actual[2] = new GameCell[]{GameCell.WALL, GameCell.WALL, GameCell.WALL};
        actual[3] = new GameCell[]{GameCell.CORRIDOR, GameCell.CORRIDOR, GameCell.CORRIDOR};
        Assert.assertTrue(Arrays.deepEquals(res, actual));
    }


    @Test
    public void getRoomByPosition_InsideRoom_3x3() {
        Room testRoom = new Room("Testkammer des Schreckens", new Position(0, 0), new Position[]{new Position(2, 1)});
        GameCell[] avalibleGameCells = new GameCell[]{new GameCell(testRoom)};
        String[] gameFieldStr = new String[3];
        gameFieldStr[0] = "00 ";
        gameFieldStr[1] = "00 ";
        gameFieldStr[2] = "###";
        GameCell[][] gameField = GameLogic.gameFieldFromString(3, 3, gameFieldStr, avalibleGameCells);
        Character[] characters = new Character[]{new Character("Test", new Position(0, 0))};
        AIDifficulty[] difficulties = new AIDifficulty[]{AIDifficulty.STUPID};
        GameLogic logic = new GameLogic(gameField, characters, difficulties);
        Room res = logic.getGameCell(new Position(0, 1)).getRoom();
        Assert.assertEquals(testRoom, res);
    }

    @Test
    public void getRoomByPosition_OutsideHouse_Default() {
        GameLogic defaultLogic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, 2, new AIDifficulty[]{null, AIDifficulty.NORMAL});
        GameCell resCell = defaultLogic.getGameCell(new Position(0, 0));
        Room resRoom = resCell.getRoom();
        Assert.assertEquals(GameCell.WALL, resCell);
        Assert.assertNull(resRoom);
    }

    @Test
    public void getRoomByPosition_InsideStaircase_Default() {
        GameLogic defaultLogic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, 2, new AIDifficulty[]{null, AIDifficulty.NORMAL});
        GameCell resCell = defaultLogic.getGameCell(new Position(12, 10));
        Room resRoom = resCell.getRoom();
        Assert.assertEquals(GameCell.WALL, resCell);
        Assert.assertNull(resRoom);
    }

    @Test
    public void getRoomByPosition_Corridor_Default() {
        GameLogic defaultLogic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, 2, new AIDifficulty[]{null, AIDifficulty.NORMAL});
        GameCell resCell = defaultLogic.getGameCell(new Position(6, 3));
        Room resRoom = resCell.getRoom();
        Assert.assertEquals(GameCell.CORRIDOR, resCell);
        Assert.assertNull(resRoom);
    }


    @Test
    public void validateMove_StraightPossible_Default() {
        GameLogic defaultLogic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, 2, new AIDifficulty[]{null, AIDifficulty.NORMAL});
        boolean res = defaultLogic.isCorridorReachable(new Position(7, 20), 4, true);
        Assert.assertTrue(res);
    }

    @Test
    public void validateMove_StraightImpossible_Default() {
        GameLogic defaultLogic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, 2, new AIDifficulty[]{null, AIDifficulty.NORMAL});
        boolean res = defaultLogic.isCorridorReachable(new Position(7, 20), 5, true);
        Assert.assertFalse(res);
    }

    @Test
    public void validateMove_AroundCornerPossible_2x2() {
        Room testRoom = new Room("Testkammer des Schreckens", new Position(0, 0), new Position[]{});
        Character character = new Character("Susi", new Position(1, 1));
        Player player = new Player(character);
        GameCell[] avalibleGameCells = new GameCell[]{new GameCell(testRoom)};
        String[] gameFieldStr = new String[2];
        gameFieldStr[0] = "  ";
        gameFieldStr[1] = "0 ";
        GameCell[][] gameField = GameLogic.gameFieldFromString(2, 2, gameFieldStr, avalibleGameCells);
        Character[] characters = new Character[]{new Character("Test", new Position(0, 0))};
        AIDifficulty[] difficulties = new AIDifficulty[]{AIDifficulty.STUPID};
        GameLogic logic = new GameLogic(gameField, new Player[]{player}, characters, difficulties);
        boolean res = logic.isCorridorReachable(new Position(0, 0), 2, true);
        Assert.assertTrue(res);
    }

    @Test
    public void validateMove_AroundCornerNotPossibleStepsTooFew_2x3() {

        Room testRoom = new Room("Testkammer des Schreckens", new Position(0, 1), new Position[]{});
        Character character = new Character("Susi", new Position(2, 1));
        Player player = new Player(character);
        GameCell[] avalibleGameCells = new GameCell[]{new GameCell(testRoom)};
        String[] gameFieldStr = new String[2];
        gameFieldStr[0] = "   ";
        gameFieldStr[1] = "00 ";
        GameCell[][] gameField = GameLogic.gameFieldFromString(2, 3, gameFieldStr, avalibleGameCells);
        Character[] characters = new Character[]{new Character("Test", new Position(0, 0))};
        AIDifficulty[] difficulties = new AIDifficulty[]{AIDifficulty.STUPID};
        GameLogic logic = new GameLogic(gameField, new Player[]{player}, characters, difficulties);
        boolean res = logic.isCorridorReachable(new Position(0, 0), 2, true);
        Assert.assertFalse(res);
    }

    @Test
    public void validateMove_AroundPlayerNotPossibleStepsTooFew_3x3() {

        Room testRoom = new Room("Testkammer des Schreckens", new Position(0, 2), new Position[]{});
        Character characterSusi = new Character("Susi", new Position(1, 2));
        Player playerSusi = new Player(characterSusi);
        Character characterHans = new Character("Hans", new Position(1, 1));
        Player playerHans = new Player(characterHans);
        GameCell[] avalibleGameCells = new GameCell[]{new GameCell(testRoom)};
        String[] gameFieldStr = new String[3];
        gameFieldStr[0] = "   ";
        gameFieldStr[1] = "0  ";
        gameFieldStr[2] = "0  ";
        GameCell[][] gameField = GameLogic.gameFieldFromString(3, 3, gameFieldStr, avalibleGameCells);

        Character[] characters = new Character[]{new Character("Test", new Position(0, 0)), new Character("Test2", new Position(0, 0))};
        AIDifficulty[] difficulties = new AIDifficulty[]{AIDifficulty.STUPID, AIDifficulty.STUPID};
        GameLogic logic = new GameLogic(gameField, new Player[]{playerSusi, playerHans}, characters, difficulties);
        boolean res = logic.isCorridorReachable(new Position(1, 0), 3, true);
        Assert.assertFalse(res);
    }

    @Test
    public void validateMove_AroundPlayerPossible_3x3() {

        Room testRoom = new Room("Testkammer des Schreckens", new Position(0, 2), new Position[]{});
        Character characterSusi = new Character("Susi", new Position(1, 2));
        Player playerSusi = new Player(characterSusi);
        Character characterHans = new Character("Hans", new Position(1, 1));
        Player playerHans = new Player(characterHans);
        GameCell[] avalibleGameCells = new GameCell[]{new GameCell(testRoom)};
        String[] gameFieldStr = new String[3];
        gameFieldStr[0] = "   ";
        gameFieldStr[1] = "0  ";
        gameFieldStr[2] = "0  ";
        GameCell[][] gameField = GameLogic.gameFieldFromString(3, 3, gameFieldStr, avalibleGameCells);
        Character[] characters = new Character[]{new Character("Test", new Position(0, 0)), new Character("Test2", new Position(0, 0))};
        AIDifficulty[] difficulties = new AIDifficulty[]{AIDifficulty.STUPID, AIDifficulty.STUPID};
        GameLogic logic = new GameLogic(gameField, new Player[]{playerSusi, playerHans}, characters, difficulties);
        boolean res = logic.isCorridorReachable(new Position(1, 0), 4, true);
        Assert.assertTrue(res);
    }

    @Test
    public void validateMove_ThroughWallNotPossible_3x3() {

        Character characterSusi = new Character("Susi", new Position(0, 0));
        Player playerSusi = new Player(characterSusi);
        GameCell[] avalibleGameCells = new GameCell[]{};
        String[] gameFieldStr = new String[3];
        gameFieldStr[0] = "   ";
        gameFieldStr[1] = "###";
        gameFieldStr[2] = "   ";
        GameCell[][] gameField = GameLogic.gameFieldFromString(3, 3, gameFieldStr, avalibleGameCells);
        Character[] characters = new Character[]{new Character("Test", new Position(0, 0))};
        AIDifficulty[] difficulties = new AIDifficulty[]{AIDifficulty.STUPID};
        GameLogic logic = new GameLogic(gameField, new Player[]{playerSusi}, characters, difficulties);
        boolean res = logic.isCorridorReachable(new Position(0, 2), 2, true);
        Assert.assertFalse(res);
    }

    @Test
    public void validateMove_StepsTooMutch_2x2() {

        Character characterSusi = new Character("Susi", new Position(0, 0));
        Player playerSusi = new Player(characterSusi);
        GameCell[] avalibleGameCells = new GameCell[]{};
        String[] gameFieldStr = new String[2];
        gameFieldStr[0] = "  ";
        gameFieldStr[1] = "# ";
        GameCell[][] gameField = GameLogic.gameFieldFromString(2, 2, gameFieldStr, avalibleGameCells);
        Character[] characters = new Character[]{new Character("Test", new Position(0, 0))};
        AIDifficulty[] difficulties = new AIDifficulty[]{AIDifficulty.STUPID};
        GameLogic logic = new GameLogic(gameField, new Player[]{playerSusi}, characters, difficulties);
        boolean res = logic.isCorridorReachable(new Position(1, 1), 3, true);
        Assert.assertFalse(res);
    }

    @Test
    public void roomIsReachable_KitchenIsPossible_Default() {
        GameLogic defaultLogic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, 2, new AIDifficulty[]{null, AIDifficulty.NORMAL});
        defaultLogic.setCurrentPlayerPosition(new Position(6, 6));
        Room dest = defaultLogic.getGameCell(new Position(1, 1)).getRoom(); //Küche
        Assert.assertTrue(defaultLogic.roomIsReachable(dest, 4));
    }

    @Test
    public void roomIsReachable_KitchenIsPossible_1_Step_Default() {
        GameLogic defaultLogic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, 2, new AIDifficulty[]{null, AIDifficulty.NORMAL});
        defaultLogic.setCurrentPlayerPosition(new Position(4, 7));
        Room dest = defaultLogic.getGameCell(new Position(1, 1)).getRoom(); //Küche
        Assert.assertTrue(defaultLogic.roomIsReachable(dest, 1));
    }

    @Test
    public void roomIsReachable_SalonIsPossible_Default() {
        GameLogic defaultLogic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, 2, new AIDifficulty[]{null, AIDifficulty.NORMAL});
        defaultLogic.setCurrentPlayerPosition(new Position(11, 9));
        Room dest = defaultLogic.getGameCell(new Position(11, 1)).getRoom(); //Salon
        Assert.assertTrue(defaultLogic.roomIsReachable(dest, 4));
    }

    @Test
    public void roomIsReachable_LibraryIsPossible_Default() {
        GameLogic defaultLogic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, 2, new AIDifficulty[]{null, AIDifficulty.NORMAL});
        defaultLogic.setCurrentPlayerPosition(new Position(11, 17));
        Room dest = defaultLogic.getGameCell(new Position(20, 16)).getRoom(); //Bib.
        Assert.assertTrue(defaultLogic.roomIsReachable(dest, 7));
    }

    @Test
    public void roomIsReachable_VerandaIsPossible_MoreSteps_Default() {
        GameLogic defaultLogic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, 2, new AIDifficulty[]{null, AIDifficulty.NORMAL});
        defaultLogic.setCurrentPlayerPosition(new Position(5, 18));
        Room dest = defaultLogic.getGameCell(new Position(5, 20)).getRoom(); //Veranda
        Assert.assertTrue(defaultLogic.roomIsReachable(dest, 4));
    }

    @Test
    public void roomIsReachable_VerandaNotPossible_MoreSteps_Default() {
        GameLogic defaultLogic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, 2, new AIDifficulty[]{null, AIDifficulty.NORMAL});
        defaultLogic.setCurrentPlayerPosition(new Position(5, 18));
        Room dest = defaultLogic.getGameCell(new Position(5, 20)).getRoom(); //Veranda
        Assert.assertFalse(defaultLogic.roomIsReachable(dest, 1));
    }

    @Test
    public void roomIsReachable_MusicRoomFromVerandaIsPossible_Secret_Default() {
        GameLogic defaultLogic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, 2, new AIDifficulty[]{null, AIDifficulty.NORMAL});
        defaultLogic.setCurrentPlayerPosition(new Position(5, 20));//In Veranda
        Room dest = defaultLogic.getGameCell(new Position(20, 3)).getRoom(); //Musikzimmer
        Assert.assertTrue(defaultLogic.roomIsReachable(dest, 1));
    }

    @Test
    public void roomIsReachable_RoomWithoutDoorsNotPossible_3x2() {
        Room testRoom = new Room("Testkammer des Schreckens", new Position(1, 2), new Position[]{});
        Character susi = new Character("Susi", new Position(0, 0));
        Player playerSusi = new Player(susi);
        GameCell[] avalibleGameCells = new GameCell[]{new GameCell(testRoom)};
        String[] gameFieldStr = new String[3];
        gameFieldStr[0] = "  ";
        gameFieldStr[1] = "00";
        gameFieldStr[2] = "00";
        GameCell[][] gameField = GameLogic.gameFieldFromString(3, 2, gameFieldStr, avalibleGameCells);
        Character[] characters = new Character[]{new Character("Test", new Position(0, 0))};
        AIDifficulty[] difficulties = new AIDifficulty[]{AIDifficulty.STUPID};
        GameLogic logic = new GameLogic(gameField, new Player[]{playerSusi}, characters, difficulties);
        Assert.assertFalse(logic.roomIsReachable(testRoom, 1));
    }

    @Test
    public void roomIsInInReach_Kitchen_Default() {
        GameLogic defaultLogic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, 2, new AIDifficulty[]{null, AIDifficulty.NORMAL});
        defaultLogic.setCurrentPlayerPosition(new Position(6, 6));
        Assert.assertTrue(defaultLogic.roomIsInReach(4));  //Salon mit 3 Küche mit 4 schritten
    }

    @Test
    public void roomIsInInReach_Salon_Default() {
        GameLogic defaultLogic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, 2, new AIDifficulty[]{null, AIDifficulty.NORMAL});
        defaultLogic.setCurrentPlayerPosition(new Position(6, 6));
        Assert.assertTrue(defaultLogic.roomIsInReach(3));  //Salon mit 3 Küche mit 4 schritten
    }

    @Test
    public void roomIsInReach_LibraryIsPossible_Default() {
        GameLogic defaultLogic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, 2, new AIDifficulty[]{null, AIDifficulty.NORMAL});
        defaultLogic.setCurrentPlayerPosition(new Position(15, 17));
        Assert.assertTrue(defaultLogic.roomIsInReach(3));
    }

    @Test
    public void roomIsInReach_LibraryIsNotPossible_Default() {
        GameLogic defaultLogic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, 2, new AIDifficulty[]{null, AIDifficulty.NORMAL});
        defaultLogic.setCurrentPlayerPosition(new Position(15, 17));
        Assert.assertFalse(defaultLogic.roomIsInReach(2));
    }

    @Test
    public void roomIsInReach_EntranceHallIsPossibleLibraryBlocked_Default() {
        GameLogic defaultLogic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, 2, new AIDifficulty[]{null, AIDifficulty.NORMAL});
        defaultLogic.setCurrentPlayerPosition(new Position(15, 17));
        defaultLogic.setCurrentPlayerIndex(1);
        defaultLogic.setCurrentPlayerPosition(new Position(16, 16)); //Vor Bib.
        defaultLogic.setCurrentPlayerIndex(0);
        Assert.assertTrue(defaultLogic.roomIsInReach(4));
    }

    @Test
    public void roomIsInReach_EntranceHallIsNotPossibleLibraryBlocked_Default() {
        GameLogic defaultLogic = GameLogic.createInitialGameLogicFromJSON(initialGameDataJSON, 2, new AIDifficulty[]{null, AIDifficulty.NORMAL});
        defaultLogic.setCurrentPlayerPosition(new Position(15, 17));
        defaultLogic.setCurrentPlayerIndex(1);
        defaultLogic.setCurrentPlayerPosition(new Position(16, 16)); //Vor Bib.
        defaultLogic.setCurrentPlayerIndex(0);
        Assert.assertFalse(defaultLogic.roomIsInReach(3));
    }

    @Test
    public void roomIsInReach_RoomWithoutDoorsNotPossible_3x2() {
        Room testRoom = new Room("Testkammer des Schreckens", new Position(1, 2), new Position[]{});
        Character susi = new Character("Susi", new Position(0, 0));
        Player playerSusi = new Player(susi);
        GameCell[] avalibleGameCells = new GameCell[]{new GameCell(testRoom)};
        String[] gameFieldStr = new String[3];
        gameFieldStr[0] = "  ";
        gameFieldStr[1] = "00";
        gameFieldStr[2] = "00";
        GameCell[][] gameField = GameLogic.gameFieldFromString(3, 2, gameFieldStr, avalibleGameCells);
        Character[] characters = new Character[]{new Character("Test", new Position(0, 0))};
        AIDifficulty[] difficulties = new AIDifficulty[]{AIDifficulty.STUPID};
        GameLogic logic = new GameLogic(gameField, new Player[]{playerSusi}, characters, difficulties);
        Assert.assertFalse(logic.roomIsInReach(2)); //Keine Türen vorhanden
    }

    @Test
    public void getShortestPath_Simple3x3_Possible() {
//        Player playerSusi = new Player(susi);
//        GameCell[] avalibleGameCells = new GameCell[]{new GameCell(testRoom)};
//        String[] gameFieldStr = new String[3];
//        gameFieldStr[0] = "  ";
//        gameFieldStr[1] = "00";
//        gameFieldStr[2] = "00";
    }
}
