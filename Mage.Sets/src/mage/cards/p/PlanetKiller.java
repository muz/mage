package mage.cards.p;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.AttacksEachCombatStaticAbility;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.IndestructibleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;

import java.util.UUID;

/**
 * @author muz
 */
public final class PlanetKiller extends CardImpl {

    public PlanetKiller(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{8}");

        this.subtype.add(SubType.CONSTRUCT);
        this.power = new MageInt(9);
        this.toughness = new MageInt(9);

        // Flying, indestructible
        this.addAbility(FlyingAbility.getInstance());
        this.addAbility(IndestructibleAbility.getInstance());

        // This creature attacks each combat if able.
        this.addAbility(new AttacksEachCombatStaticAbility());

        // Whenever this creature deals combat damage to a player, put a +1/+1 counter on it for each land that player controls.
        this.addAbility(new DealsCombatDamageToAPlayerTriggeredAbility(
            new AddCountersSourceEffect(CounterType.P1P1.createInstance(), PlanetKillerCount.instance), false, true));
    }

    private PlanetKiller(final PlanetKiller card) {
        super(card);
    }

    @Override
    public PlanetKiller copy() {
        return new PlanetKiller(this);
    }
}

enum PlanetKillerCount implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability source, Effect effect) {
        return game.getBattlefield().countAll(StaticFilters.FILTER_LANDS,
            effect.getTargetPointer().getFirst(game, source), game);
    }

    @Override
    public PlanetKillerCount copy() {
        return this;
    }

    @Override
    public String toString() {
        return "1";
    }

    @Override
    public String getMessage() {
        return "land that player controls";
    }
}
