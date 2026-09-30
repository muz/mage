package mage.cards.t;

import mage.MageInt;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.DamageTargetEffect;
import mage.abilities.keyword.DoubleStrikeAbility;
import mage.abilities.keyword.ProwessAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author muz
 */
public final class TetsukoUmezawaPursuer extends CardImpl {

    public TetsukoUmezawaPursuer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN, SubType.MERCENARY);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Double strike
        this.addAbility(DoubleStrikeAbility.getInstance());

        // Prowess
        this.addAbility(new ProwessAbility());

        // Whenever a creature an opponent controls with power or toughness 1 or less blocks, Tetsuko Umezawa deals 1 damage to that creature's controller.
        this.addAbility(new TetsukoUmezawaPursuerTriggeredAbility());
    }

    private TetsukoUmezawaPursuer(final TetsukoUmezawaPursuer card) {
        super(card);
    }

    @Override
    public TetsukoUmezawaPursuer copy() {
        return new TetsukoUmezawaPursuer(this);
    }
}

class TetsukoUmezawaPursuerTriggeredAbility extends TriggeredAbilityImpl {

    TetsukoUmezawaPursuerTriggeredAbility() {
        super(Zone.BATTLEFIELD, new DamageTargetEffect(1));
        setTriggerPhrase("Whenever a creature an opponent controls with power or toughness 1 or less blocks, ");
        getEffects().get(0).setText("{this} deals 1 damage to that creature's controller");
    }

    private TetsukoUmezawaPursuerTriggeredAbility(final TetsukoUmezawaPursuerTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public TetsukoUmezawaPursuerTriggeredAbility copy() {
        return new TetsukoUmezawaPursuerTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.CREATURE_BLOCKS;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Permanent blocker = game.getPermanent(event.getTargetId());
        if (blocker == null || !blocker.isCreature(game)
                || !game.getOpponents(getControllerId()).contains(blocker.getControllerId())
                || (blocker.getPower().getValue() > 1 && blocker.getToughness().getValue() > 1)) {
            return false;
        }
        getEffects().setTargetPointer(new FixedTarget(blocker.getControllerId()));
        return true;
    }
}
