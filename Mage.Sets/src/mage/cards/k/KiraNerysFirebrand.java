package mage.cards.k;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldAllTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.continuous.BoostTargetEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SetTargetPointer;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.combat.AttackingCreatureEntry;
import mage.game.events.EntersTheBattlefieldEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 * @author muz
 */
public final class KiraNerysFirebrand extends CardImpl {

    public KiraNerysFirebrand(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{R}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.BAJORAN);
        this.subtype.add(SubType.SOLDIER);
        this.power = new MageInt(4);
        this.toughness = new MageInt(3);

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // If a creature you control would enter while you control an attacking creature, instead it enters tapped and attacking.
        this.addAbility(new SimpleStaticAbility(new KiraNerysFirebrandEffect()));

        // Whenever another creature you control enters, it gets +1/+1 until end of turn.
        this.addAbility(new EntersBattlefieldAllTriggeredAbility(Zone.BATTLEFIELD,
            new BoostTargetEffect(1, 1).setText("it gets +1/+1 until end of turn"),
            StaticFilters.FILTER_ANOTHER_CREATURE_YOU_CONTROL, false, SetTargetPointer.PERMANENT));
    }

    private KiraNerysFirebrand(final KiraNerysFirebrand card) {
        super(card);
    }

    @Override
    public KiraNerysFirebrand copy() {
        return new KiraNerysFirebrand(this);
    }
}

class KiraNerysFirebrandEffect extends ReplacementEffectImpl {

    KiraNerysFirebrandEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit, false);
        staticText = "if a creature you control would enter while you control an attacking creature, instead it enters tapped and attacking";
    }

    private KiraNerysFirebrandEffect(final KiraNerysFirebrandEffect effect) {
        super(effect);
    }

    @Override
    public KiraNerysFirebrandEffect copy() {
        return new KiraNerysFirebrandEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        Permanent entering = ((EntersTheBattlefieldEvent) event).getTarget();
        return entering != null && entering.isCreature(game) && entering.isControlledBy(source.getControllerId())
            && game.getBattlefield().getAllActivePermanents(StaticFilters.FILTER_PERMANENT_CREATURE, source.getControllerId(), game)
            .stream().anyMatch(Permanent::isAttacking);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        EntersTheBattlefieldEvent enterEvent = (EntersTheBattlefieldEvent) event;
        enterEvent.getTarget().setTapped(true);
        if (enterEvent.getAttackingEntry() == null) {
            enterEvent.setAttackingEntry(new AttackingCreatureEntry());
        }
        return false;
    }
}
