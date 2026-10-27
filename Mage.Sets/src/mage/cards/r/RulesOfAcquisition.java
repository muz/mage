package mage.cards.r;

import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.GainControlTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.TargetController;
import mage.filter.common.FilterNonlandPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.common.TargetOpponent;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author muz
 */
public final class RulesOfAcquisition extends CardImpl {

    private static final FilterNonlandPermanent opponentFilter = new FilterNonlandPermanent("nonland permanent an opponent controls");
    private static final FilterNonlandPermanent controlledFilter = new FilterNonlandPermanent("nonland permanent you control");

    static {
        opponentFilter.add(TargetController.OPPONENT.getControllerPredicate());
        controlledFilter.add(TargetController.YOU.getControllerPredicate());
    }

    public RulesOfAcquisition(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{3}{U}{U}");

        // Choose one --
        // Follow the 3rd Rule -- Gain control of target nonland permanent an opponent controls. That opponent draws three cards.
        this.getSpellAbility().addEffect(new RulesOfAcquisitionTakeEffect());
        this.getSpellAbility().addTarget(new TargetPermanent(opponentFilter));
        this.getSpellAbility().getModes().getMode().withFlavorWord("Follow the 3rd Rule");

        // Follow the 98th Rule -- Target opponent gains control of target nonland permanent you control. If they do, you draw three cards.
        Mode mode = new Mode(new RulesOfAcquisitionGiveEffect());
        mode.addTarget(new TargetOpponent());
        mode.addTarget(new TargetPermanent(controlledFilter));
        mode.withFlavorWord("Follow the 98th Rule");
        this.getSpellAbility().addMode(mode);
    }

    private RulesOfAcquisition(final RulesOfAcquisition card) {
        super(card);
    }

    @Override
    public RulesOfAcquisition copy() {
        return new RulesOfAcquisition(this);
    }
}

class RulesOfAcquisitionTakeEffect extends OneShotEffect {

    RulesOfAcquisitionTakeEffect() {
        super(Outcome.Benefit);
        staticText = "gain control of target nonland permanent an opponent controls. That opponent draws three cards";
    }

    private RulesOfAcquisitionTakeEffect(final RulesOfAcquisitionTakeEffect effect) {
        super(effect);
    }

    @Override
    public RulesOfAcquisitionTakeEffect copy() {
        return new RulesOfAcquisitionTakeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null) {
            return false;
        }
        Player opponent = game.getPlayer(permanent.getControllerId());
        game.addEffect(new GainControlTargetEffect(Duration.Custom, source.getControllerId())
            .setTargetPointer(new FixedTarget(permanent, game)), source);
        game.applyEffects();
        if (opponent != null) {
            opponent.drawCards(3, source, game);
        }
        return true;
    }
}

class RulesOfAcquisitionGiveEffect extends OneShotEffect {

    RulesOfAcquisitionGiveEffect() {
        super(Outcome.Benefit);
        staticText = "target opponent gains control of target nonland permanent you control. If they do, you draw three cards";
    }

    private RulesOfAcquisitionGiveEffect(final RulesOfAcquisitionGiveEffect effect) {
        super(effect);
    }

    @Override
    public RulesOfAcquisitionGiveEffect copy() {
        return new RulesOfAcquisitionGiveEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player opponent = game.getPlayer(source.getFirstTarget());
        Permanent permanent = game.getPermanent(source.getTargets().get(1).getFirstTarget());
        if (opponent == null || permanent == null) {
            return false;
        }
        game.addEffect(new GainControlTargetEffect(Duration.Custom, opponent.getId())
            .setTargetPointer(new FixedTarget(permanent, game)), source);
        game.applyEffects();
        Player player = game.getPlayer(source.getControllerId());
        if (player != null && permanent.isControlledBy(opponent.getId())) {
            player.drawCards(3, source, game);
        }
        return true;
    }
}
