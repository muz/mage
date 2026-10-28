package mage.cards.m;

import mage.abilities.decorator.ConditionalOneShotEffect;
import mage.abilities.decorator.OptionalOneShotEffect;
import mage.abilities.effects.common.EndCombatPhaseEffect;
import mage.abilities.effects.common.UntapAllControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.TurnPhase;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author muz
 */
public final class MercifulMandate extends CardImpl {

    public MercifulMandate(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{W}");

        // Untap all creatures you control. If it's a combat phase, you may end the combat phase.
        this.getSpellAbility().addEffect(new UntapAllControllerEffect(StaticFilters.FILTER_PERMANENT_CREATURES));
        this.getSpellAbility().addEffect(new ConditionalOneShotEffect(
            new OptionalOneShotEffect(new EndCombatPhaseEffect()),
            (game, source) -> game.getTurnPhaseType() == TurnPhase.COMBAT,
            "if it's a combat phase, you may end the combat phase"
        ));
    }

    private MercifulMandate(final MercifulMandate card) {
        super(card);
    }

    @Override
    public MercifulMandate copy() {
        return new MercifulMandate(this);
    }
}
