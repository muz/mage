package mage.abilities.effects.common;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.constants.Outcome;
import mage.game.Game;

/**
 * @author muz
 */
public class EndCombatPhaseEffect extends OneShotEffect {

    public EndCombatPhaseEffect() {
        super(Outcome.Benefit);
        staticText = "end the combat phase";
    }

    protected EndCombatPhaseEffect(final EndCombatPhaseEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return game.getTurn().endCombat(game, source);
    }

    @Override
    public EndCombatPhaseEffect copy() {
        return new EndCombatPhaseEffect(this);
    }
}
