package mage.cards.t;

import mage.MageObjectReference;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.common.SagaAbility;
import mage.abilities.dynamicvalue.common.GreatestAmongPermanentsValue;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.GainLifeEffect;
import mage.abilities.effects.common.ReturnFromGraveyardToBattlefieldTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SagaChapter;
import mage.constants.SubType;
import mage.filter.common.FilterCreatureCard;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.ColorPredicate;
import mage.game.Game;
import mage.game.events.DamagedEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.TreasureToken;
import mage.target.common.TargetCardInYourGraveyard;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * @author muz
 */
public final class TheInnerLight extends CardImpl {

    private static final FilterCreatureCard filter = new FilterCreatureCard("green or white creature card from your graveyard");

    static {
        filter.add(Predicates.or(new ColorPredicate(ObjectColor.GREEN), new ColorPredicate(ObjectColor.WHITE)));
    }

    public TheInnerLight(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{G}{W}");

        this.subtype.add(SubType.SAGA);

        SagaAbility saga = new SagaAbility(this);

        // I -- Return target green or white creature card from your graveyard to the battlefield.
        saga.addChapterEffect(this, SagaChapter.CHAPTER_I, new ReturnFromGraveyardToBattlefieldTargetEffect(), new TargetCardInYourGraveyard(filter));

        // II -- Choose target creature. Whenever it deals combat damage to a player this turn, you draw that many cards.
        saga.addChapterEffect(this, SagaChapter.CHAPTER_II, new TheInnerLightEffect(), new TargetCreaturePermanent());

        // III -- You gain life equal to the greatest power among creatures you control. Create a Treasure token.
        saga.addChapterEffect(this, SagaChapter.CHAPTER_III,
            new GainLifeEffect(GreatestAmongPermanentsValue.POWER_CONTROLLED_CREATURES,
            "you gain life equal to the greatest power among creatures you control"), new CreateTokenEffect(new TreasureToken()));
        this.addAbility(saga);
    }

    private TheInnerLight(final TheInnerLight card) {
        super(card);
    }

    @Override
    public TheInnerLight copy() {
        return new TheInnerLight(this);
    }
}

class TheInnerLightEffect extends OneShotEffect {

    TheInnerLightEffect() {
        super(Outcome.Benefit);
        staticText = "choose target creature. Whenever it deals combat damage to a player this turn, you draw that many cards";
    }

    private TheInnerLightEffect(final TheInnerLightEffect effect) {
        super(effect);
    }

    @Override
    public TheInnerLightEffect copy() {
        return new TheInnerLightEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null) {
            return false;
        }
        game.addDelayedTriggeredAbility(new TheInnerLightTriggeredAbility(new MageObjectReference(permanent, game)), source);
        return true;
    }
}

class TheInnerLightTriggeredAbility extends DelayedTriggeredAbility {

    private final MageObjectReference creature;

    TheInnerLightTriggeredAbility(MageObjectReference creature) {
        super(null, Duration.EndOfTurn, false);
        this.creature = creature;
        setTriggerPhrase("Whenever that creature deals combat damage to a player this turn, ");
    }

    private TheInnerLightTriggeredAbility(final TheInnerLightTriggeredAbility ability) {
        super(ability);
        this.creature = ability.creature;
    }

    @Override
    public TheInnerLightTriggeredAbility copy() {
        return new TheInnerLightTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGED_PLAYER;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!creature.refersTo(event.getSourceId(), game) || !((DamagedEvent) event).isCombatDamage()) {
            return false;
        }
        getEffects().clear();
        addEffect(new DrawCardSourceControllerEffect(event.getAmount(), true));
        return true;
    }
}
