package mage.cards.t;

import mage.abilities.CompoundAbility;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.effects.common.ExileSpellEffect;
import mage.abilities.effects.common.PreventDamageToControllerEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.effects.common.continuous.GainAbilityControllerEffect;
import mage.abilities.keyword.HexproofAbility;
import mage.abilities.keyword.IndestructibleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author muz
 */
public final class TheNeedsOfTheMany extends CardImpl {

    public TheNeedsOfTheMany(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{W}");

        // As an additional cost to cast this spell, sacrifice a creature.
        this.getSpellAbility().addCost(new SacrificeTargetCost(StaticFilters.FILTER_PERMANENT_CREATURE));

        // Creatures you control gain hexproof and indestructible until end of turn.
        this.getSpellAbility().addEffect(new GainAbilityControlledEffect(
            new CompoundAbility(HexproofAbility.getInstance(), IndestructibleAbility.getInstance()),
            Duration.EndOfTurn, StaticFilters.FILTER_PERMANENT_CREATURES
        ));

        // You gain hexproof until end of turn.
        this.getSpellAbility().addEffect(new GainAbilityControllerEffect(HexproofAbility.getInstance(), Duration.EndOfTurn));

        // Prevent all damage that would be dealt to you this turn.
        this.getSpellAbility().addEffect(new PreventDamageToControllerEffect(Duration.EndOfTurn));

        // Exile The Needs of the Many.
        this.getSpellAbility().addEffect(new ExileSpellEffect());
    }

    private TheNeedsOfTheMany(final TheNeedsOfTheMany card) {
        super(card);
    }

    @Override
    public TheNeedsOfTheMany copy() {
        return new TheNeedsOfTheMany(this);
    }
}
