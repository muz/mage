package mage.cards.d;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.common.delayed.WhenTargetDiesDelayedTriggeredAbility;
import mage.abilities.effects.common.CreateDelayedTriggeredAbilityEffect;
import mage.abilities.effects.common.ReturnFromGraveyardToHandTargetEffect;
import mage.abilities.effects.common.ReturnToBattlefieldUnderOwnerControlWithCounterTargetEffect;
import mage.abilities.keyword.FlashAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.cards.MeldCard;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.filter.common.FilterPermanentCard;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.target.TargetPermanent;
import mage.target.common.TargetCardInYourGraveyard;
import mage.target.targetpointer.FixedTargets;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author muz
 */
public final class DrLeonardBonesMcCoy extends CardImpl {

    private static final FilterPermanentCard filter = new FilterPermanentCard("non-Doctor permanent card from your graveyard");

    static {
        filter.add(Predicates.not(SubType.DOCTOR.getPredicate()));
    }

    public DrLeonardBonesMcCoy(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}{G}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.DOCTOR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // Flash
        this.addAbility(FlashAbility.getInstance());

        // When Dr. McCoy enters, choose one --
        // * Return target non-Doctor permanent card from your graveyard to your hand.
        Ability ability = new EntersBattlefieldTriggeredAbility(new ReturnFromGraveyardToHandTargetEffect());
        ability.addTarget(new TargetCardInYourGraveyard(filter));

        // * Choose up to one other target creature. When it dies this turn, return that card to the battlefield under its owner's control with a +1/+1 counter on it at the beginning of the next end step.
        Mode mode = new Mode(new CreateDelayedTriggeredAbilityEffect(
            new DrLeonardBonesMcCoyDelayedTriggeredAbility()
        ).setText("choose up to one other target creature. When it dies this turn, return that card to the battlefield "
            + "under its owner's control with a +1/+1 counter on it at the beginning of the next end step"));
        mode.addTarget(new TargetPermanent(0, 1, StaticFilters.FILTER_ANOTHER_CREATURE));
        ability.addMode(mode);
        this.addAbility(ability);
    }

    private DrLeonardBonesMcCoy(final DrLeonardBonesMcCoy card) {
        super(card);
    }

    @Override
    public DrLeonardBonesMcCoy copy() {
        return new DrLeonardBonesMcCoy(this);
    }
}

class DrLeonardBonesMcCoyDelayedTriggeredAbility extends WhenTargetDiesDelayedTriggeredAbility {

    DrLeonardBonesMcCoyDelayedTriggeredAbility() {
        super(new CreateDelayedTriggeredAbilityEffect(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(
            new ReturnToBattlefieldUnderOwnerControlWithCounterTargetEffect(CounterType.P1P1.createInstance())
        )));
    }

    private DrLeonardBonesMcCoyDelayedTriggeredAbility(final DrLeonardBonesMcCoyDelayedTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public DrLeonardBonesMcCoyDelayedTriggeredAbility copy() {
        return new DrLeonardBonesMcCoyDelayedTriggeredAbility(this);
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!super.checkTrigger(event, game)) {
            return false;
        }
        Cards cards = new CardsImpl();
        for (Card card : CardUtil.getAllCardsFromPermanentLeftBattlefield(((ZoneChangeEvent) event).getTarget(), game)) {
            if (card instanceof MeldCard) {
                cards.addAll(((MeldCard) card).getHalves());
            } else {
                cards.add(card);
            }
        }
        getEffects().setTargetPointer(new FixedTargets(cards, game));
        return true;
    }
}
