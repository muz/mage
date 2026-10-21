package mage.cards.d;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BoostControlledEffect;
import mage.abilities.keyword.StationAbility;
import mage.abilities.keyword.StationLevelAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;

import java.util.UUID;

/**
 * @author muz
 */
public final class DeepSpace9 extends CardImpl {

    public DeepSpace9(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{9}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SPACECRAFT);

        // When Deep Space 9 enters and at the beginning of your upkeep, exile the top four cards of your library. Put all permanent cards exiled this way onto the battlefield.
        this.addAbility(new DeepSpace9TriggeredAbility());

        // Station
        this.addAbility(new StationAbility());

        // STATION 9+
        // Creatures you control get +3/+3.
        this.addAbility(new StationLevelAbility(9).withLevelAbility(
            new SimpleStaticAbility(new BoostControlledEffect(3, 3, Duration.WhileOnBattlefield))
        ));
    }

    private DeepSpace9(final DeepSpace9 card) {
        super(card);
    }

    @Override
    public DeepSpace9 copy() {
        return new DeepSpace9(this);
    }
}

class DeepSpace9TriggeredAbility extends TriggeredAbilityImpl {

    DeepSpace9TriggeredAbility() {
        super(Zone.BATTLEFIELD, new DeepSpace9Effect());
        setTriggerPhrase("When {this} enters and at the beginning of your upkeep, ");
    }

    private DeepSpace9TriggeredAbility(final DeepSpace9TriggeredAbility ability) {
        super(ability);
    }

    @Override
    public DeepSpace9TriggeredAbility copy() {
        return new DeepSpace9TriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD
            || event.getType() == GameEvent.EventType.UPKEEP_STEP_PRE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD
            ? event.getTargetId().equals(getSourceId()) : isControlledBy(event.getPlayerId());
    }
}

class DeepSpace9Effect extends OneShotEffect {

    DeepSpace9Effect() {
        super(Outcome.PutCardInPlay);
        staticText = "exile the top four cards of your library. Put all permanent cards exiled this way onto the battlefield";
    }

    private DeepSpace9Effect(final DeepSpace9Effect effect) {
        super(effect);
    }

    @Override
    public DeepSpace9Effect copy() {
        return new DeepSpace9Effect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        UUID exileId = UUID.randomUUID();
        controller.moveCardsToExile(controller.getLibrary().getTopCards(game, 4), source, game, true, exileId, "Deep Space 9");
        ExileZone exileZone = game.getExile().getExileZone(exileId);
        if (exileZone == null) {
            return true;
        }
        Cards cards = new CardsImpl();
        for (Card card : exileZone.getCards(game)) {
            if (card.isPermanent(game)) {
                cards.add(card);
            }
        }
        controller.moveCards(cards, Zone.BATTLEFIELD, source, game);
        return true;
    }
}
