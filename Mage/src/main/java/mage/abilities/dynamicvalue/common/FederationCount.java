package mage.abilities.dynamicvalue.common;

import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.hint.Hint;
import mage.abilities.hint.ValueHint;
import mage.constants.SubType;
import mage.constants.SubTypeSet;
import mage.filter.StaticFilters;
import mage.game.Game;

import java.util.Collection;

/**
 * @author muz
 */
public enum FederationCount implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability source, Effect effect) {
        return (int) game.getBattlefield()
            .getActivePermanents(StaticFilters.FILTER_CONTROLLED_CREATURES, source.getControllerId(), source, game)
            .stream()
            .filter(permanent -> !permanent.hasSubtype(SubType.BORG, game))
            .map(permanent -> permanent.getSubtype(game))
            .flatMap(Collection::stream)
            .filter(subType -> subType.getSubTypeSet() == SubTypeSet.CreatureType)
            .distinct()
            .count();
    }

    @Override
    public FederationCount copy() {
        return this;
    }

    @Override
    public String toString() {
        return "X";
    }

    @Override
    public String getMessage() {
        return "the number of creature types among non-Borg creatures you control";
    }

    public Hint getHint() {
        return new ValueHint("Creature types among non-Borg creatures you control", this);
    }
}
