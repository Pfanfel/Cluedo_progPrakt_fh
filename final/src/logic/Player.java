package logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Repräsentiert einen Spieler der an dem Cluedo Spiel teilnimmt.
 *
 * @author Michael Smirnov
 */
public class Player {
    //Die Karten auf der Hand
    private List<Card> cards;
    //Die KI welche den Spieler steuert. (Mensch == null)
    private AI ai;
    //Die Spielfigur die der Spieler steuert.
    private final Character character;
    //Die Notizen zu den eigenen Karten.
    private NoteSelf[] noteSelf;
    //Notizen über die anderen Spieler im Spiel
    private NoteOthers[][] noteOthers;
    //Status, ob der Spieler gerade in einen Raum gewünscht wurde
    private boolean requested;

    /**
     * Konstruktor eines Spielers.
     *
     * @param character            die Spielfigur die der Spieler steuert.
     * @param aiDifficulty         die KI-Stärke die der Spieler haben soll (Mensch == null)
     * @param characterInGameCount die Anzahl der gesamten Spielfiguren im Spiel.
     * @param cardsInGameCount     die Anzahl der gesamten Karten im Spiel.
     */
    public Player(Character character, AIDifficulty aiDifficulty, int characterInGameCount, int cardsInGameCount) {

        this.character = character;
        this.ai = initAIByDifficulty(aiDifficulty, characterInGameCount, cardsInGameCount);
        this.cards = new ArrayList<>();
        this.noteSelf = new NoteSelf[cardsInGameCount];
        Arrays.fill(noteSelf, NoteSelf.NOTHING);
        this.requested = false;
        //Ming zählt nicht zu den anderen Notizen
        this.noteOthers = new NoteOthers[characterInGameCount - 1][cardsInGameCount];
        for (NoteOthers[] noteOther : noteOthers) {
            Arrays.fill(noteOther, NoteOthers.NOTHING);
        }
    }

    /**
     * Konstruktor zum Testen.
     *
     * @param character die Spielfigur die der Spieler steuert.
     */
    public Player(Character character) {
        this.character = character;
    }

    /**
     * Konstruktor der beim laden aus einem Spielstand genutzt wird.
     *
     * @param cards        die Karten des Spielers.
     * @param pos          die Spielerposition.
     * @param aiDifficulty die KI-Stärke.
     * @param character    die Spielfigur die der Spieler steuert.
     * @param noteSelf     Notizen über die anderen Spieler im Spiel
     * @param noteOthers   Notizen über die anderen Spieler im Spiel
     */
    public Player(List<Card> cards, Position pos, AIDifficulty aiDifficulty, Character character, NoteSelf[] noteSelf, NoteOthers[][] noteOthers) {
        this.cards = cards;
        character.setPosition(pos);
        //+1 da in den Notizen über andere die eigene Person fehlt
        this.ai = initAIByDifficulty(aiDifficulty, noteOthers.length + 1, noteSelf.length);
        this.character = character;
        this.noteSelf = noteSelf;
        this.noteOthers = noteOthers;
    }

    /**
     * Konstruktor zum laden aus einem Spielstand.
     *
     * @param cards        die Karten des Spielers.
     * @param pos          die Spielerposition.
     * @param aiDifficulty die KI-Stärke.
     * @param character    die Spielfigur die der Spieler steuert.
     * @param noteSelf     Notizen über die anderen Spieler im Spiel
     * @param requested    ob der Spieler in einen Raum gewünscht wurde.
     * @param noteOthers   Notizen über die anderen Spieler im Spiel
     */
    public Player(List<Card> cards, Position pos, AIDifficulty aiDifficulty, Character character, NoteSelf[] noteSelf, boolean requested, NoteOthers[][] noteOthers) {
        this(cards, pos, aiDifficulty, character, noteSelf, noteOthers);
        this.requested = requested;
    }

    /**
     * Liefert den Status, ob der Spieler in einen Raum gewünscht wurde.
     *
     * @return der Status, ob der Spieler in einen Raum gewünscht wurde.
     */
    public boolean getRequested() {
        return requested;
    }

    /**
     * Initialisiert die KI ausgehend von der dem Konstruktor übergebenen Stärke.
     *
     * @param aiDifficulty         die KI-Stärke
     * @param characterInGameCount Anzahl der Spielfiguren im Spiel.
     * @param cardsInGameCount     Anzahl der Karten im Spiel.
     * @return die Initialisierte KI.
     */
    private static AI initAIByDifficulty(AIDifficulty aiDifficulty, int characterInGameCount, int cardsInGameCount) {
        AI ret = null;
        if (aiDifficulty != null) {
            switch (aiDifficulty) {
                case STUPID:
                    ret = new AIStupid();
                    break;
                case NORMAL:
                    ret = new AINormal();
                    break;
                case SMART:
                    ret = new AISmart(characterInGameCount, cardsInGameCount);
                    break;
            }
        }
        return ret;
    }

    /**
     * Setzt den Status ob der Spieler in einen Raum gewünscht wurde.
     *
     * @param requested ob der Spieler in einen Raum gewünscht wurde.
     */
    public void setRequested(boolean requested) {
        this.requested = requested;
    }

    /**
     * Liefert die Karten des Spielers.
     *
     * @return die Karten des Spielers.
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    /**
     * Liefert die aktuelle Position der Spielfigur des Spielers.
     *
     * @return die aktuelle Position der Spielfigur des Spielers.
     */
    public Position getPos() {
        return this.character.getPosition();
    }

    /**
     * Liefert die KI des Spielers.
     *
     * @return die KI des Spielers.
     */
    public AI getAi() {
        return ai;
    }

    /**
     * Liefert die Spielfigur des Spielers.
     *
     * @return die Spielfigur des Spielers.
     */
    public Character getCharacter() {
        return character;
    }

    /**
     * Prüft, ob es sich bei dem Spieler um eine KI handelt.
     *
     * @return ob es sich bei dem Spieler um eine KI handelt.
     */
    public boolean isAI() {
        return ai != null;
    }

    /**
     * Liefert die eigenen Notizen.
     *
     * @return die eigenen Notizen.
     */
    public NoteSelf[] getNoteSelf() {
        return this.noteSelf;
    }

    /**
     * Setzt die aktuelle Position der Spielfigur des Spielers.
     *
     * @param newPos die zu setzende Position der Spielfigur des Spielers.
     */
    public void setPos(Position newPos) {
        this.character.setPosition(newPos);
    }

    /**
     * Liefert die Notizen über die anderen Spieler im Spiel.
     *
     * @return die Notizen über die anderen Spieler im Spiel.
     */
    public NoteOthers[][] getNoteOthers() {
        return noteOthers;
    }

    /**
     * Wird nur beim Initialen verteilen der Karten benutzt.
     * Fügt Karten zu den Handkarten des Spielers hinzu.
     *
     * @param card die hinzuzufügende Karte.
     */
    public void addCard(Card card) {
        this.cards.add(card);
    }

    /**
     * Liefert die Karten, die der Spieler gegeben einer Verdächtigung zeigen kann.
     *
     * @param suspicion die Verdächtigung auf die der Spieler reagieren soll.
     * @return die Karten, die der Spieler gegeben einer Verdächtigung zeigen kann.
     */
    public CardTriple possibleCardsToShow(CardTriple suspicion) {
        CardTriple showableCards = new CardTriple();
        for (Card card : this.cards) {
            if (card.equals(suspicion.getCharacter())) {
                showableCards.setCharacter(card);
            }
            if (card.equals(suspicion.getWeapon())) {
                showableCards.setWeapon(card);
            }
            if (card.equals(suspicion.getRoom())) {
                showableCards.setRoom(card);
            }
        }
        return showableCards;
    }

    /**
     * Prüft, ob der Spieler sich innerhalb des übergebenen Raumes befindet.
     *
     * @param room der Raum in dem der Spieler sich befinden soll.
     * @return ob der Spieler sich innerhalb des übergebenen Raumes befindet.
     */
    public boolean isInside(Room room) {
        return this.character.getPosition().equals(room.getMidPoint());
    }

    /**
     * Setzt die eigenen Notizen bei der Erstellung eines neuen Spiels.
     *
     * @param allCards alle im Spiel befindlichen Karten
     */
    public void initNoteSelf(Card[] allCards) {
        for (int i = 0; i < allCards.length; i++) {
            if (this.cards.contains(allCards[i])) {
                this.noteSelf[i] = NoteSelf.OWN;
            } else {
                this.noteSelf[i] = NoteSelf.NOTHING;
            }
        }
    }

}
