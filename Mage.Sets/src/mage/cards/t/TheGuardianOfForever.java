package mage.cards.t;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ReturnToBattlefieldUnderOwnerControlTargetEffect;
import mage.abilities.keyword.DefenderAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.MeldCard;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.targetpointer.FixedTargets;
import mage.util.CardUtil;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author muz
 */
public final class TheGuardianOfForever extends CardImpl {

    private static final FilterPermanent filter = new FilterCreaturePermanent("another nontoken creature");

    static {
        filter.add(AnotherPredicate.instance);
        filter.add(TokenPredicate.FALSE);
    }

    public TheGuardianOfForever(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{3}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.CONSTRUCT);
        this.power = new MageInt(0);
        this.toughness = new MageInt(6);

        // Defender
        this.addAbility(DefenderAbility.getInstance());

        // {4}, Sacrifice another nontoken creature: Return that card to the battlefield under its owner's control at the beginning of the next end step.
        Ability ability = new SimpleActivatedAbility(new TheGuardianOfForeverEffect(), new GenericManaCost(4));
        ability.addCost(new TheGuardianOfForeverCost(filter));
        this.addAbility(ability);
    }

    private TheGuardianOfForever(final TheGuardianOfForever card) {
        super(card);
    }

    @Override
    public TheGuardianOfForever copy() {
        return new TheGuardianOfForever(this);
    }
}

class TheGuardianOfForeverCost extends SacrificeTargetCost {

    private final Set<MageObjectReference> sacrificedCards = new LinkedHashSet<>();

    TheGuardianOfForeverCost(FilterPermanent filter) {
        super(filter);
    }

    private TheGuardianOfForeverCost(final TheGuardianOfForeverCost cost) {
        super(cost);
        sacrificedCards.addAll(cost.sacrificedCards);
    }

    @Override
    public TheGuardianOfForeverCost copy() {
        return new TheGuardianOfForeverCost(this);
    }

    @Override
    protected void addSacrificeTarget(Game game, Permanent permanent) {
        super.addSacrificeTarget(game, permanent);
        for (Card card : CardUtil.getAllCardsFromPermanentLeftBattlefield(permanent, game)) {
            if (card instanceof MeldCard) {
                for (Card half : ((MeldCard) card).getHalves().getCards(game)) {
                    sacrificedCards.add(new MageObjectReference(half, game, 1));
                }
            } else {
                sacrificedCards.add(new MageObjectReference(card, game, 1));
            }
        }
    }

    Set<MageObjectReference> getSacrificedCards() {
        return sacrificedCards;
    }
}

class TheGuardianOfForeverEffect extends OneShotEffect {

    TheGuardianOfForeverEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "return that card to the battlefield under its owner's control at the beginning of the next end step";
    }

    private TheGuardianOfForeverEffect(final TheGuardianOfForeverEffect effect) {
        super(effect);
    }

    @Override
    public TheGuardianOfForeverEffect copy() {
        return new TheGuardianOfForeverEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Set<MageObjectReference> cards = new LinkedHashSet<>();
        for (Cost cost : source.getCosts()) {
            if (!(cost instanceof TheGuardianOfForeverCost)) {
                continue;
            }
            for (MageObjectReference card : ((TheGuardianOfForeverCost) cost).getSacrificedCards()) {
                if (!card.zoneCounterIsCurrent(game)) {
                    continue;
                }
                Zone zone = game.getState().getZone(card.getSourceId());
                if (zone == null || !zone.isPublicZone()) {
                    continue;
                }
                cards.add(card);
            }
        }
        if (!cards.isEmpty()) {
            Effect effect = new ReturnToBattlefieldUnderOwnerControlTargetEffect(false, false)
                .setTargetPointer(new FixedTargets(cards));
            game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(effect), source);
        }
        return true;
    }
}
