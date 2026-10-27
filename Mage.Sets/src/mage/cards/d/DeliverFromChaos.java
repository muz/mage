package mage.cards.d;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.BorgToken;
import mage.players.Player;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author muz
 */
public final class DeliverFromChaos extends CardImpl {

    public DeliverFromChaos(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{3}{W}{B}");

        // Exile target artifact or creature. Create X 1/1 colorless Borg artifact creature tokens, where X is that permanent's mana value.
        this.getSpellAbility().addEffect(new DeliverFromChaosEffect());
        this.getSpellAbility().addTarget(new TargetPermanent(StaticFilters.FILTER_PERMANENT_ARTIFACT_OR_CREATURE));
    }

    private DeliverFromChaos(final DeliverFromChaos card) {
        super(card);
    }

    @Override
    public DeliverFromChaos copy() {
        return new DeliverFromChaos(this);
    }
}

class DeliverFromChaosEffect extends OneShotEffect {

    DeliverFromChaosEffect() {
        super(Outcome.Exile);
        staticText = "exile target artifact or creature. Create X 1/1 colorless Borg artifact creature tokens, where X is that permanent's mana value";
    }

    private DeliverFromChaosEffect(final DeliverFromChaosEffect effect) {
        super(effect);
    }

    @Override
    public DeliverFromChaosEffect copy() {
        return new DeliverFromChaosEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        Player controller = game.getPlayer(source.getControllerId());
        if (permanent == null || controller == null) {
            return false;
        }
        int amount = permanent.getManaValue();
        controller.moveCards(permanent, Zone.EXILED, source, game);
        return new CreateTokenEffect(new BorgToken(), amount).apply(game, source);
    }
}
