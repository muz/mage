package mage.cards.d;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CopyPermanentEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.Counter;
import mage.counters.Counters;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.targetpointer.FixedTarget;
import mage.util.functions.CopyApplier;

import java.util.UUID;

/**
 * @author muz
 */
public final class DominionSaboteur extends CardImpl {

    public DominionSaboteur(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{U}");

        this.subtype.add(SubType.SHAPESHIFTER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // You may have this creature enter as a copy of any artifact or creature on the battlefield, except it isn't legendary. If you do, it enters with additional counters on it equal to the same number and kinds of counters the copied permanent has on it.
        this.addAbility(new EntersBattlefieldAbility(new DominionSaboteurEffect(), true));
    }

    private DominionSaboteur(final DominionSaboteur card) {
        super(card);
    }

    @Override
    public DominionSaboteur copy() {
        return new DominionSaboteur(this);
    }
}

class DominionSaboteurEffect extends OneShotEffect {

    DominionSaboteurEffect() {
        super(Outcome.Copy);
        staticText = "as a copy of any artifact or creature on the battlefield, except it isn't legendary. "
            + "If you do, it enters with additional counters on it equal to the same number and kinds of counters the copied permanent has on it";
    }

    private DominionSaboteurEffect(final DominionSaboteurEffect effect) {
        super(effect);
    }

    @Override
    public DominionSaboteurEffect copy() {
        return new DominionSaboteurEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetPermanent target = new TargetPermanent(StaticFilters.FILTER_PERMANENT_ARTIFACT_OR_CREATURE);
        target.withNotTarget(true);
        if (!controller.choose(outcome, target, source, game)) {
            return false;
        }
        Permanent original = game.getPermanent(target.getFirstTarget());
        if (original == null) {
            return false;
        }
        Counters counters = original.getCounters(game).copy();
        CopyPermanentEffect copyEffect = new CopyPermanentEffect(
            StaticFilters.FILTER_PERMANENT_ARTIFACT_OR_CREATURE, new DominionSaboteurCopyApplier(), true
        );
        copyEffect.setTargetPointer(new FixedTarget(original, game));
        if (!copyEffect.apply(game, source)) {
            return false;
        }
        game.applyEffects();
        Permanent entering = game.getPermanentEntering(source.getSourceId());
        if (entering != null) {
            for (Counter counter : counters.values()) {
                entering.addCounters(counter.copy(), source, game);
            }
        }
        return true;
    }
}

class DominionSaboteurCopyApplier extends CopyApplier {

    @Override
    public boolean apply(Game game, MageObject blueprint, Ability source, UUID copyToObjectId) {
        blueprint.removeSuperType(SuperType.LEGENDARY);
        return true;
    }
}
