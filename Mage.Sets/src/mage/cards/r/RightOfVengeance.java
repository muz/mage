package mage.cards.r;

import java.util.UUID;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.effects.common.CreateDelayedTriggeredAbilityEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.DestroyTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.game.Game;
import mage.game.events.DamagedPlayerEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.KlingonWarriorToken;
import mage.target.targetpointer.FixedTarget;

/**
 * @author muz
 */
public final class RightOfVengeance extends CardImpl {

    public RightOfVengeance(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{3}{W}");


        // Until end of turn, whenever a creature deals combat damage to you, destroy that creature and create a 2/1 red Klingon Warrior creature token with haste.
        this.getSpellAbility().addEffect(new CreateDelayedTriggeredAbilityEffect(new RightOfVengeanceTriggeredAbility()));
    }

    private RightOfVengeance(final RightOfVengeance card) {
        super(card);
    }

    @Override
    public RightOfVengeance copy() {
        return new RightOfVengeance(this);
    }
}

class RightOfVengeanceTriggeredAbility extends DelayedTriggeredAbility {

    RightOfVengeanceTriggeredAbility() {
        super(new DestroyTargetEffect().setText("destroy that creature"), Duration.EndOfTurn, false);
        setTriggerPhrase("Until end of turn, whenever a creature deals combat damage to you, ");
        addEffect(new CreateTokenEffect(new KlingonWarriorToken()).concatBy("and"));
    }

    private RightOfVengeanceTriggeredAbility(final RightOfVengeanceTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public RightOfVengeanceTriggeredAbility copy() {
        return new RightOfVengeanceTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGED_PLAYER;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!event.getTargetId().equals(getControllerId()) || !((DamagedPlayerEvent) event).isCombatDamage()) {
            return false;
        }
        Permanent permanent = game.getPermanent(event.getSourceId());
        if (permanent == null || !permanent.isCreature(game)) {
            return false;
        }
        getEffects().setTargetPointer(new FixedTarget(permanent, game));
        return true;
    }
}
