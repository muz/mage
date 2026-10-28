package mage.cards.t;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import mage.abilities.Ability;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.common.FilterCreatureCard;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.players.Player;
import mage.util.CardUtil;

/**
 * @author muz
 */
public final class ThePromiseOfKahless extends CardImpl {

    public ThePromiseOfKahless(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{4}{B}");


        // You may sacrifice a Klingon or Warrior. If you do, return all other Klingon and Warrior creature cards from your graveyard to the battlefield.
        this.getSpellAbility().addEffect(new ThePromiseOfKahlessEffect());
    }

    private ThePromiseOfKahless(final ThePromiseOfKahless card) {
        super(card);
    }

    @Override
    public ThePromiseOfKahless copy() {
        return new ThePromiseOfKahless(this);
    }
}

class ThePromiseOfKahlessEffect extends OneShotEffect {

    private static final FilterControlledPermanent sacrificeFilter = new FilterControlledPermanent("Klingon or Warrior");
    private static final FilterCreatureCard returnFilter = new FilterCreatureCard();

    static {
        sacrificeFilter.add(Predicates.or(SubType.KLINGON.getPredicate(), SubType.WARRIOR.getPredicate()));
        returnFilter.add(Predicates.or(SubType.KLINGON.getPredicate(), SubType.WARRIOR.getPredicate()));
    }

    ThePromiseOfKahlessEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "you may sacrifice a Klingon or Warrior. If you do, return all other Klingon and Warrior "
            + "creature cards from your graveyard to the battlefield";
    }

    private ThePromiseOfKahlessEffect(final ThePromiseOfKahlessEffect effect) {
        super(effect);
    }

    @Override
    public ThePromiseOfKahlessEffect copy() {
        return new ThePromiseOfKahlessEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        SacrificeTargetCost cost = new SacrificeTargetCost(sacrificeFilter);
        if (!cost.canPay(source, source, player.getId(), game)
            || !player.chooseUse(outcome, "Sacrifice a Klingon or Warrior?", source, game)
            || !cost.pay(source, game, source, player.getId(), false)) {
            return false;
        }
        Set<UUID> sacrificedCards = CardUtil.getAllCardsFromPermanentsLeftBattlefield(cost.getPermanents(), game)
            .stream().map(Card::getId).collect(Collectors.toSet());
        Set<Card> cards = player.getGraveyard().getCards(game).stream()
            .filter(card -> returnFilter.match(card, game))
            .filter(card -> !sacrificedCards.contains(card.getId()))
            .collect(Collectors.toSet());
        return player.moveCards(cards, Zone.BATTLEFIELD, source, game);
    }
}
