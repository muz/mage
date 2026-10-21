package mage.cards.t;

import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.common.SagaAbility;
import mage.abilities.common.delayed.AtTheBeginOfCombatDelayedTriggeredAbility;
import mage.abilities.costs.common.ExileFromHandCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DamageTargetEffect;
import mage.abilities.effects.common.DoIfCostPaid;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.SacrificeOpponentsEffect;
import mage.abilities.effects.common.UntapTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.effects.common.continuous.GainControlTargetEffect;
import mage.abilities.keyword.HasteAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SagaChapter;
import mage.constants.SubType;
import mage.filter.FilterPermanent;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCardInHand;
import mage.target.common.TargetOpponentsCreaturePermanent;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author muz
 */
public final class TheWrathOfKhan extends CardImpl {

    private static final FilterPermanent filter = new FilterPermanent("nontoken permanent");

    static {
        filter.add(TokenPredicate.FALSE);
    }

    public TheWrathOfKhan(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{R}");

        this.subtype.add(SubType.SAGA);

        // (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
        SagaAbility sagaAbility = new SagaAbility(this);

        // I -- This Saga deals 4 damage to target creature an opponent controls. At the beginning of the next combat this turn, gain control of that creature until end of turn. Untap it. It gains haste until end of turn.
        sagaAbility.addChapterEffect(this, SagaChapter.CHAPTER_I, ability -> {
            ability.addEffect(new DamageTargetEffect(4));
            ability.addEffect(new TheWrathOfKhanEffect());
            ability.addTarget(new TargetOpponentsCreaturePermanent());
        });

        // II -- You may exile a card from your hand. If you do, draw a card.
        sagaAbility.addChapterEffect(this, SagaChapter.CHAPTER_II, new DoIfCostPaid(
            new DrawCardSourceControllerEffect(1), new ExileFromHandCost(new TargetCardInHand())
        ));

        // III -- Each opponent sacrifices a nontoken permanent of their choice.
        sagaAbility.addChapterEffect(this, SagaChapter.CHAPTER_III, new SacrificeOpponentsEffect(filter));
        this.addAbility(sagaAbility);
    }

    private TheWrathOfKhan(final TheWrathOfKhan card) {
        super(card);
    }

    @Override
    public TheWrathOfKhan copy() {
        return new TheWrathOfKhan(this);
    }
}

class TheWrathOfKhanEffect extends OneShotEffect {

    TheWrathOfKhanEffect() {
        super(Outcome.GainControl);
        staticText = "At the beginning of the next combat this turn, gain control of that creature until end of turn. "
            + "Untap it. It gains haste until end of turn";
    }

    private TheWrathOfKhanEffect(final TheWrathOfKhanEffect effect) {
        super(effect);
    }

    @Override
    public TheWrathOfKhanEffect copy() {
        return new TheWrathOfKhanEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null) {
            return false;
        }
        DelayedTriggeredAbility ability = new AtTheBeginOfCombatDelayedTriggeredAbility(new GainControlTargetEffect(Duration.EndOfTurn));
        ability.setDuration(Duration.EndOfTurn);
        ability.addEffect(new UntapTargetEffect());
        ability.addEffect(new GainAbilityTargetEffect(HasteAbility.getInstance()));
        ability.getEffects().setTargetPointer(new FixedTarget(permanent, game));
        game.addDelayedTriggeredAbility(ability, source);
        return true;
    }
}
