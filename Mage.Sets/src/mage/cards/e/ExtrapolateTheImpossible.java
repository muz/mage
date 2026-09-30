package mage.cards.e;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.hint.common.OpenSideboardHint;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.common.TargetOpponent;
import mage.util.CardUtil;

import java.util.Objects;
import java.util.UUID;

/**
 * @author muz
 */
public final class ExtrapolateTheImpossible extends CardImpl {

    public ExtrapolateTheImpossible(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{1}{B}");

        // You may reveal exactly two cards you own with different names from outside the game. An opponent chooses one of them. You put that card into your hand.
        this.getSpellAbility().addEffect(new ExtrapolateTheImpossibleEffect());
        this.getSpellAbility().addHint(OpenSideboardHint.instance);
    }

    private ExtrapolateTheImpossible(final ExtrapolateTheImpossible card) {
        super(card);
    }

    @Override
    public ExtrapolateTheImpossible copy() {
        return new ExtrapolateTheImpossible(this);
    }
}

class ExtrapolateTheImpossibleEffect extends OneShotEffect {

    ExtrapolateTheImpossibleEffect() {
        super(Outcome.DrawCard);
        staticText = "you may reveal exactly two cards you own with different names from outside the game. "
                + "An opponent chooses one of them. You put that card into your hand";
    }

    private ExtrapolateTheImpossibleEffect(final ExtrapolateTheImpossibleEffect effect) {
        super(effect);
    }

    @Override
    public ExtrapolateTheImpossibleEffect copy() {
        return new ExtrapolateTheImpossibleEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        Cards sideboard = player.getSideboard();
        boolean hasPair = sideboard.getCards(game).stream().anyMatch(first ->
                sideboard.getCards(game).stream().anyMatch(second -> !CardUtil.haveSameNames(first, second)));
        if (!hasPair || !player.chooseUse(outcome,
                "Reveal exactly two cards with different names from outside the game?", source, game)) {
            return true;
        }
        TargetCard pair = new ExtrapolateTheImpossibleTarget();
        if (!player.choose(outcome, sideboard, pair, source, game) || pair.getTargets().size() != 2) {
            return true;
        }
        Cards revealed = new CardsImpl(pair.getTargets());
        player.revealCards(source, revealed, game);

        TargetOpponent opponentChoice = new TargetOpponent();
        opponentChoice.withNotTarget(true);
        player.choose(outcome, opponentChoice, source, game);
        Player opponent = game.getPlayer(opponentChoice.getFirstTarget());
        if (opponent != null) {
            TargetCard chosen = new TargetCard(Zone.ALL, StaticFilters.FILTER_CARD).withNotTarget(true);
            chosen.withChooseHint("to put into " + player.getName() + "'s hand");
            if (opponent.choose(outcome, revealed, chosen, source, game)) {
                player.moveCards(new CardsImpl(chosen.getTargets()), Zone.HAND, source, game);
            }
        }
        return true;
    }
}

class ExtrapolateTheImpossibleTarget extends TargetCard {

    ExtrapolateTheImpossibleTarget() {
        super(2, Zone.ALL, StaticFilters.FILTER_CARD);
        withNotTarget(true);
        withChooseHint("exactly two cards with different names");
    }

    private ExtrapolateTheImpossibleTarget(final ExtrapolateTheImpossibleTarget target) {
        super(target);
    }

    @Override
    public ExtrapolateTheImpossibleTarget copy() {
        return new ExtrapolateTheImpossibleTarget(this);
    }

    @Override
    public boolean canTarget(UUID playerId, UUID id, Ability source, Game game) {
        Card card = game.getCard(id);
        Player player = game.getPlayer(source.getControllerId());
        return super.canTarget(playerId, id, source, game)
                && player != null && player.getSideboard().contains(id)
                && card.isOwnedBy(source.getControllerId())
                && getTargets().stream().map(game::getCard).filter(Objects::nonNull)
                .noneMatch(chosen -> CardUtil.haveSameNames(chosen, card));
    }
}
