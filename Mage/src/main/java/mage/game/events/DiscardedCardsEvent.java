package mage.game.events;

import mage.abilities.Ability;
import mage.cards.Cards;
import mage.cards.CardsImpl;

import java.util.UUID;

/**
 *
 * @author htrajan
 */
public class DiscardedCardsEvent extends GameEvent {

    private final Cards discardedCards;
    private final boolean random;

    public DiscardedCardsEvent(Ability source, UUID playerId, int amount, Cards discardedCards) {
        this(source, playerId, amount, discardedCards, false);
    }

    public DiscardedCardsEvent(Ability source, UUID playerId, int amount, Cards discardedCards, boolean random) {
        super(EventType.DISCARDED_CARDS, null, source, playerId, amount, false);
        this.discardedCards = new CardsImpl(discardedCards);
        this.random = random;
    }

    public boolean isRandom() {
        return random;
    }

    public Cards getDiscardedCards() {
        return discardedCards;
    }
}
