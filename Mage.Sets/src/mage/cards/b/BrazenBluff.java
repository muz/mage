package mage.cards.b;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.cost.CastFromHandForFreeEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.TargetController;
import mage.filter.FilterSpell;
import mage.filter.StaticFilters;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.target.TargetSpell;

import java.util.UUID;

/**
 * @author muz
 */
public final class BrazenBluff extends CardImpl {

    private static final FilterSpell filter = new FilterSpell("noncreature spell an opponent controls");

    static {
        filter.add(Predicates.not(CardType.CREATURE.getPredicate()));
        filter.add(TargetController.OPPONENT.getControllerPredicate());
    }

    public BrazenBluff(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{R}{R}");

        // Choose target noncreature spell an opponent controls. That player may exile it.
        // If they don't, you may cast a noncreature spell from your hand without paying its mana cost.
        this.getSpellAbility().addEffect(new BrazenBluffEffect());
        this.getSpellAbility().addTarget(new TargetSpell(filter));
    }

    private BrazenBluff(final BrazenBluff card) {
        super(card);
    }

    @Override
    public BrazenBluff copy() {
        return new BrazenBluff(this);
    }
}

class BrazenBluffEffect extends OneShotEffect {

    BrazenBluffEffect() {
        super(Outcome.Benefit);
        staticText = "choose target noncreature spell an opponent controls. That player may exile it. "
            + "If they don't, you may cast a noncreature spell from your hand without paying its mana cost";
    }

    private BrazenBluffEffect(final BrazenBluffEffect effect) {
        super(effect);
    }

    @Override
    public BrazenBluffEffect copy() {
        return new BrazenBluffEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Spell spell = game.getStack().getSpell(getTargetPointer().getFirst(game, source));
        if (spell == null) {
            return false;
        }
        Player opponent = game.getPlayer(spell.getControllerId());
        if (opponent == null) {
            return false;
        }
        if (opponent.chooseUse(Outcome.Exile, "Exile " + spell.getName() + '?', source, game)
            && spell.moveToExile(null, "", source, game)) {
            return true;
        }
        return new CastFromHandForFreeEffect(StaticFilters.FILTER_CARD_NON_CREATURE).apply(game, source);
    }
}
