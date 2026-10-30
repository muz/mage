
package mage.abilities.costs.common;

import mage.abilities.Ability;
import mage.abilities.costs.Cost;
import mage.abilities.costs.CostImpl;
import mage.cards.Card;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.FilterPermanent;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.ObjectSourcePlayerPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetControlledPermanent;
import mage.util.CardUtil;

import java.util.*;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class ReturnToHandChosenControlledPermanentCost extends CostImpl {

    private final List<Permanent> permanents = new ArrayList<>();

    public ReturnToHandChosenControlledPermanentCost(TargetControlledPermanent target) {
        FilterPermanent filter = target.getFilter().copy();
        filter.add(new ReturnToHandForCostPredicate(getId()));
        target.replaceFilter(filter);
        target.withNotTarget(true);
        this.addTarget(target);
        if (target.getMaxNumberOfTargets() > 1 && target.getMaxNumberOfTargets() == target.getMinNumberOfTargets()) {
            this.text = "return " + CardUtil.numberToText(target.getMaxNumberOfTargets()) + ' '
                + target.getTargetName()
                + (target.getTargetName().endsWith(" you control") ? "" : " you control")
                + " to their owner's hand";
        } else {
            this.text = "return " + CardUtil.addArticle(target.getTargetName())
                + (target.getTargetName().endsWith(" you control") ? "" : " you control")
                + " to its owner's hand";
        }
    }

    protected ReturnToHandChosenControlledPermanentCost(final ReturnToHandChosenControlledPermanentCost cost) {
        super(cost);
        for (Permanent permanent : cost.permanents) {
            this.permanents.add(permanent.copy());
        }
    }

    @Override
    public boolean pay(Ability ability, Game game, Ability source, UUID controllerId, boolean noMana, Cost costToPay) {
        Player controller = game.getPlayer(controllerId);
        if (controller != null) {
            if (this.getTargets().choose(Outcome.ReturnToHand, controllerId, source.getSourceId(), source, game)) {
                Set<Card> permanentsToReturn = new HashSet<>();
                for (UUID targetId : this.getTargets().get(0).getTargets()) {
                    Permanent permanent = game.getPermanent(targetId);
                    if (permanent == null) {
                        return false;
                    }
                    addReturnTarget(game, permanent);
                    permanentsToReturn.add(permanent);
                }
                controller.moveCards(permanentsToReturn, Zone.HAND, ability, game);
                paid = true;
            }
        }
        return paid;
    }

    protected void addReturnTarget(Game game, Permanent permanent) {
        permanents.add(permanent.copy());
    }

    @Override
    public boolean canPay(Ability ability, Ability source, UUID controllerId, Game game) {
        return canChooseOrAlreadyChosen(ability, source, controllerId, game);
    }

    @Override
    public ReturnToHandChosenControlledPermanentCost copy() {
        return new ReturnToHandChosenControlledPermanentCost(this);
    }

    public List<Permanent> getPermanents() {
        return permanents;
    }

}

class ReturnToHandForCostPredicate implements ObjectSourcePlayerPredicate<Permanent> {

    private final UUID costId;

    ReturnToHandForCostPredicate(UUID costId) {
        this.costId = costId;
    }

    @Override
    public boolean apply(ObjectSourcePlayer<Permanent> input, Game game) {
        if (input.getSource() == null) {
            return true;
        }
        GameEvent event = GameEvent.getEvent(GameEvent.EventType.PAY_RETURN_TO_HAND_COST,
            input.getObject().getId(), input.getSource(), input.getPlayerId());
        event.setData(costId.toString());
        return !game.getContinuousEffects().preventedByRuleModification(event, input.getSource(), game, true);
    }
}
