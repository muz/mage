package mage.cards.d;

import mage.abilities.dynamicvalue.common.DifferentlyNamedPermanentCount;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.continuous.MaximumHandSizeControllerEffect;
import mage.abilities.effects.common.continuous.MaximumHandSizeControllerEffect.HandSizeModification;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author muz
 */
public final class DeepSpaceScan extends CardImpl {

    private static final DifferentlyNamedPermanentCount landCount =
        new DifferentlyNamedPermanentCount(StaticFilters.FILTER_CONTROLLED_PERMANENT_LANDS);

    public DeepSpaceScan(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{4}{U}{U}");

        // Strange new worlds -- Draw X cards, where X is the number of differently named lands you control. You have no maximum hand size until your next turn.
        this.getSpellAbility().addEffect(new DrawCardSourceControllerEffect(landCount));
        this.getSpellAbility().addEffect(new MaximumHandSizeControllerEffect(
            Integer.MAX_VALUE, Duration.UntilYourNextTurn, HandSizeModification.SET
        ).setText("you have no maximum hand size until your next turn"));
        this.getSpellAbility().withFlavorWord("Strange new worlds").addHint(landCount.getHint());
    }

    private DeepSpaceScan(final DeepSpaceScan card) {
        super(card);
    }

    @Override
    public DeepSpaceScan copy() {
        return new DeepSpaceScan(this);
    }
}
