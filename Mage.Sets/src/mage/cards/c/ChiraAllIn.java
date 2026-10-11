package mage.cards.c;

import mage.MageInt;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.DiscardedCardsEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.token.TreasureToken;

import java.util.UUID;

/**
 *
 * @author muz
 */
public final class ChiraAllIn extends CardImpl {

    public ChiraAllIn(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ROGUE);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Whenever you discard one or more cards at random, select one or more targets at random, choose one or more modes at random, flip one or more coins, or roll one or more dice, put a +1/+1 counter on Chira and create a tapped Treasure token.
        this.addAbility(new ChiraAllInTriggeredAbility());
    }

    private ChiraAllIn(final ChiraAllIn card) {
        super(card);
    }

    @Override
    public ChiraAllIn copy() {
        return new ChiraAllIn(this);
    }
}

class ChiraAllInTriggeredAbility extends TriggeredAbilityImpl {

    ChiraAllInTriggeredAbility() {
        super(Zone.BATTLEFIELD, new AddCountersSourceEffect(CounterType.P1P1.createInstance()));
        this.addEffect(new CreateTokenEffect(new TreasureToken(), 1, true).concatBy("and"));
        setTriggerPhrase("Whenever you discard one or more cards at random, select one or more targets at random, "
            + "choose one or more modes at random, flip one or more coins, or roll one or more dice, ");
    }

    private ChiraAllInTriggeredAbility(final ChiraAllInTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public ChiraAllInTriggeredAbility copy() {
        return new ChiraAllInTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        switch (event.getType()) {
            case DISCARDED_CARDS:
            case TARGETS_SELECTED_AT_RANDOM:
            case MODES_CHOSEN_AT_RANDOM:
            case COINS_FLIPPED:
            case DICE_ROLLED:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        switch (event.getType()) {
            case DISCARDED_CARDS:
                return isControlledBy(event.getPlayerId()) && ((DiscardedCardsEvent) event).isRandom();
            case DICE_ROLLED:
                return isControlledBy(event.getTargetId());
            default:
                return isControlledBy(event.getPlayerId());
        }
    }
}
