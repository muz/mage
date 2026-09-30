package mage.cards.v;

import mage.abilities.Ability;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ExileTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCreatureOrPlaneswalker;
import mage.target.targetpointer.FixedTargets;
import mage.util.CardUtil;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author muz
 */
public final class VindictiveTriumph extends CardImpl {

    public VindictiveTriumph(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{W}{B}{B}");

        // Exile target creature or planeswalker. If that permanent's mana value was 3 or less, return it to the battlefield tapped under your control. Exile it at the beginning of the next end step.
        this.getSpellAbility().addEffect(new VindictiveTriumphEffect());
        this.getSpellAbility().addTarget(new TargetCreatureOrPlaneswalker());
    }

    private VindictiveTriumph(final VindictiveTriumph card) {
        super(card);
    }

    @Override
    public VindictiveTriumph copy() {
        return new VindictiveTriumph(this);
    }
}

class VindictiveTriumphEffect extends OneShotEffect {

    VindictiveTriumphEffect() {
        super(Outcome.Exile);
        staticText = "exile target creature or planeswalker. If that permanent's mana value was 3 or less, "
                + "return it to the battlefield tapped under your control. Exile it at the beginning of the next end step";
    }

    private VindictiveTriumphEffect(final VindictiveTriumphEffect effect) {
        super(effect);
    }

    @Override
    public VindictiveTriumphEffect copy() {
        return new VindictiveTriumphEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (player == null || permanent == null) {
            return false;
        }
        int manaValue = permanent.getManaValue();
        Cards toReturn = new CardsImpl(CardUtil.getAllCardsFromPermanentLeftBattlefield(permanent, game));
        if (permanent.isToken()) {
            toReturn.remove(permanent.getId());
        }
        if (!player.moveCards(permanent, Zone.EXILED, source, game)) {
            return false;
        }
        game.processAction();
        toReturn.retainZone(Zone.EXILED, game);
        if (manaValue > 3 || toReturn.isEmpty()) {
            return true;
        }
        player.moveCards(toReturn, Zone.BATTLEFIELD, source, game, true, false, false, null);
        List<Permanent> returned = toReturn.getCards(game).stream()
                .map(card -> CardUtil.getPermanentFromCardPutToBattlefield(card, game))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!returned.isEmpty()) {
            game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(
                    new ExileTargetEffect("exile it").setTargetPointer(new FixedTargets(returned, game))
            ), source);
        }
        return true;
    }
}
