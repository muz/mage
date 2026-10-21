package mage.cards.q;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.TargetController;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.TreasureToken;

import java.util.UUID;

/**
 * @author muz
 */
public final class QuarkHumbleProprietor extends CardImpl {

    public QuarkHumbleProprietor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.FERENGI);
        this.subtype.add(SubType.ROGUE);
        this.power = new MageInt(1);
        this.toughness = new MageInt(2);

        // You can't sacrifice Treasure tokens.
        this.addAbility(new SimpleStaticAbility(new QuarkHumbleProprietorEffect()));

        // At the beginning of each upkeep, create a Treasure token.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(TargetController.ANY, new CreateTokenEffect(new TreasureToken()), false));
    }

    private QuarkHumbleProprietor(final QuarkHumbleProprietor card) {
        super(card);
    }

    @Override
    public QuarkHumbleProprietor copy() {
        return new QuarkHumbleProprietor(this);
    }
}

class QuarkHumbleProprietorEffect extends ContinuousRuleModifyingEffectImpl {

    QuarkHumbleProprietorEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "you can't sacrifice Treasure tokens";
    }

    private QuarkHumbleProprietorEffect(final QuarkHumbleProprietorEffect effect) {
        super(effect);
    }

    @Override
    public QuarkHumbleProprietorEffect copy() {
        return new QuarkHumbleProprietorEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.SACRIFICE_PERMANENT;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        Permanent permanent = game.getPermanent(event.getTargetId());
        return source.isControlledBy(event.getPlayerId()) && permanent != null
            && permanent.isToken() && permanent.hasSubtype(SubType.TREASURE, game);
    }
}
