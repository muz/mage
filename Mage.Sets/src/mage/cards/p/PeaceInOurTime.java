package mage.cards.p;

import mage.abilities.Ability;
import mage.abilities.condition.Condition;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.WinGameSourceControllerEffect;
import mage.abilities.effects.common.combat.CantAttackAllEffect;
import mage.abilities.triggers.BeginningOfCombatTriggeredAbility;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.TargetController;
import mage.counters.CounterType;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.permanent.ControllerIdPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.UUID;

/**
 * @author muz
 */
public final class PeaceInOurTime extends CardImpl {

    public PeaceInOurTime(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{W}");

        // At the beginning of combat on each player's turn, that player may draw a card. If they do, they can't attack this turn and you put a peace counter on this enchantment.
        this.addAbility(new BeginningOfCombatTriggeredAbility(TargetController.EACH_PLAYER, new PeaceInOurTimeEffect(), false));

        // At the beginning of your upkeep, if this enchantment has twelve or more peace counters on it, you win the game.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(new WinGameSourceControllerEffect())
            .withInterveningIf(PeaceInOurTimeCondition.instance));
    }

    private PeaceInOurTime(final PeaceInOurTime card) {
        super(card);
    }

    @Override
    public PeaceInOurTime copy() {
        return new PeaceInOurTime(this);
    }
}

class PeaceInOurTimeEffect extends OneShotEffect {

    PeaceInOurTimeEffect() {
        super(Outcome.Benefit);
        staticText = "that player may draw a card. If they do, they can't attack this turn and you put a peace counter on {this}";
    }

    private PeaceInOurTimeEffect(final PeaceInOurTimeEffect effect) {
        super(effect);
    }

    @Override
    public PeaceInOurTimeEffect copy() {
        return new PeaceInOurTimeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (player == null) {
            return false;
        }
        if (!player.chooseUse(outcome, "Draw a card and be unable to attack this turn?", source, game)) {
            return true;
        }
        player.drawCards(1, source, game);
        FilterCreaturePermanent filter = new FilterCreaturePermanent();
        filter.add(new ControllerIdPredicate(player.getId()));
        game.addEffect(new CantAttackAllEffect(Duration.EndOfTurn, filter), source);
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent != null) {
            permanent.addCounters(CounterType.PEACE.createInstance(), source, game);
        }
        return true;
    }
}

enum PeaceInOurTimeCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = source.getSourcePermanentOrLKI(game);
        return permanent != null && permanent.getCounters(game).getCount(CounterType.PEACE) >= 12;
    }

    @Override
    public String toString() {
        return "{this} has twelve or more peace counters on it";
    }
}
