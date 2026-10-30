package mage.cards.d;

import mage.abilities.Ability;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenCopyTargetEffect;
import mage.abilities.effects.common.ExileTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.ChangelingAbility;
import mage.abilities.keyword.HasteAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.target.TargetPermanent;
import mage.target.targetadjustment.XTargetsCountAdjuster;
import mage.target.targetpointer.FixedTargets;

import java.util.UUID;

/**
 * @author muz
 */
public final class DominionInfiltration extends CardImpl {

    public DominionInfiltration(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{X}{R}{R}{R}");

        // Choose X target creatures your opponents control. For each of them, create a token that's a copy of that creature,
        // except it has changeling. Those tokens gain haste. Exile those tokens at the beginning of the next end step.
        this.getSpellAbility().addEffect(new DominionInfiltrationEffect());
        this.getSpellAbility().addTarget(new TargetPermanent(StaticFilters.FILTER_OPPONENTS_PERMANENT_CREATURE));
        this.getSpellAbility().setTargetAdjuster(new XTargetsCountAdjuster());
    }

    private DominionInfiltration(final DominionInfiltration card) {
        super(card);
    }

    @Override
    public DominionInfiltration copy() {
        return new DominionInfiltration(this);
    }
}

class DominionInfiltrationEffect extends OneShotEffect {

    DominionInfiltrationEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "choose X target creatures your opponents control. For each of them, create a token that's a copy "
            + "of that creature, except it has changeling. Those tokens gain haste. "
            + "Exile those tokens at the beginning of the next end step";
    }

    private DominionInfiltrationEffect(final DominionInfiltrationEffect effect) {
        super(effect);
    }

    @Override
    public DominionInfiltrationEffect copy() {
        return new DominionInfiltrationEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        CreateTokenCopyTargetEffect copyEffect = new CreateTokenCopyTargetEffect()
            .addAdditionalAbilities(new ChangelingAbility());
        copyEffect.setTargetPointer(getTargetPointer().copy());
        copyEffect.apply(game, source);
        if (copyEffect.getAddedPermanents().isEmpty()) {
            return true;
        }
        game.addEffect(new GainAbilityTargetEffect(HasteAbility.getInstance(), Duration.Custom)
            .setTargetPointer(new FixedTargets(copyEffect.getAddedPermanents(), game)), source);
        game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(
            new ExileTargetEffect().setTargetPointer(new FixedTargets(copyEffect.getAddedPermanents(), game))
        ), source);
        return true;
    }
}
