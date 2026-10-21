package mage.abilities.common;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.Effect;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;

/**
 * @author muz
 */
public class FaceDilemmaTriggeredAbility extends TriggeredAbilityImpl {

    public FaceDilemmaTriggeredAbility(Effect effect) {
        super(Zone.BATTLEFIELD, effect);
        setTriggerPhrase("Whenever you face a dilemma, ");
    }

    private FaceDilemmaTriggeredAbility(final FaceDilemmaTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public FaceDilemmaTriggeredAbility copy() {
        return new FaceDilemmaTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.FACED_DILEMMA;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return isControlledBy(event.getPlayerId());
    }
}
