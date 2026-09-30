package mage.cards.l;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.ActivateIfConditionActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.ThresholdCondition;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.effects.common.continuous.BoostSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.AsThoughEffectType;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 * @author muz
 */
public final class LootTheAnomaly extends CardImpl {

    private static final FilterControlledPermanent filter = new FilterControlledPermanent("another creature or planeswalker");

    static {
        filter.add(Predicates.or(CardType.CREATURE.getPredicate(), CardType.PLANESWALKER.getPredicate()));
        filter.add(AnotherPredicate.instance);
    }

    public LootTheAnomaly(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.BEAST, SubType.HORROR);
        this.power = new MageInt(-2);
        this.toughness = new MageInt(4);

        // If Loot's power is negative, he assigns combat damage as though his power were positive.
        this.addAbility(new SimpleStaticAbility(new LootTheAnomalyEffect()));

        // Threshold -- Sacrifice another creature or planeswalker: Loot gets -2/-0 until end of turn. Activate only if there are seven or more cards in your graveyard.
        this.addAbility(new ActivateIfConditionActivatedAbility(
                new BoostSourceEffect(-2, 0, Duration.EndOfTurn).setText("Loot gets -2/-0 until end of turn"),
                new SacrificeTargetCost(filter), ThresholdCondition.instance
        ).withConditionText("Activate only if there are seven or more cards in your graveyard")
                .setAbilityWord(AbilityWord.THRESHOLD));
    }

    private LootTheAnomaly(final LootTheAnomaly card) {
        super(card);
    }

    @Override
    public LootTheAnomaly copy() {
        return new LootTheAnomaly(this);
    }
}

class LootTheAnomalyEffect extends AsThoughEffectImpl {

    LootTheAnomalyEffect() {
        super(AsThoughEffectType.COMBAT_DAMAGE_WITH_POSITIVE_POWER, Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "if Loot's power is negative, he assigns combat damage as though his power were positive";
    }

    private LootTheAnomalyEffect(final LootTheAnomalyEffect effect) {
        super(effect);
    }

    @Override
    public LootTheAnomalyEffect copy() {
        return new LootTheAnomalyEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID playerId, Game game) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        return permanent != null && permanent.getId().equals(objectId) && permanent.getPower().getValue() < 0;
    }
}
