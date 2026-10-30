package mage.cards.p;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.RevealCardsFromLibraryUntilEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.PutCards;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.FilterCard;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;

import java.util.UUID;

/**
 * @author muz
 */
public final class PavelChekovFirstPosting extends CardImpl {

    public PavelChekovFirstPosting(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.PILOT);
        this.power = new MageInt(1);
        this.toughness = new MageInt(4);

        // If you would draw a card, you may instead choose land or nonland and reveal cards from the top of your library until you reveal a card of the chosen kind. Put that card into your hand and put all other cards revealed this way on the bottom of your library in a random order.
        this.addAbility(new SimpleStaticAbility(new PavelChekovFirstPostingEffect()));
    }

    private PavelChekovFirstPosting(final PavelChekovFirstPosting card) {
        super(card);
    }

    @Override
    public PavelChekovFirstPosting copy() {
        return new PavelChekovFirstPosting(this);
    }
}

class PavelChekovFirstPostingEffect extends ReplacementEffectImpl {

    PavelChekovFirstPostingEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "If you would draw a card, you may instead choose land or nonland "
            + "and reveal cards from the top of your library until you reveal a card of the chosen kind. "
            + "Put that card into your hand and put all other cards revealed this way "
            + "on the bottom of your library in a random order";
    }

    private PavelChekovFirstPostingEffect(final PavelChekovFirstPostingEffect effect) {
        super(effect);
    }

    @Override
    public PavelChekovFirstPostingEffect copy() {
        return new PavelChekovFirstPostingEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DRAW_CARD;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return event.getPlayerId().equals(source.getControllerId());
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Player player = game.getPlayer(event.getPlayerId());
        if (player == null || !player.chooseUse(Outcome.Benefit,
            "Choose land or nonland and reveal cards instead of drawing?", source, game)) {
            return false;
        }
        boolean land = player.chooseUse(Outcome.Neutral, "Choose land or nonland", null,
            "Land", "Nonland", source, game);
        FilterCard filter = new FilterCard();
        filter.add(land ? CardType.LAND.getPredicate() : Predicates.not(CardType.LAND.getPredicate()));
        new RevealCardsFromLibraryUntilEffect(filter, PutCards.HAND, PutCards.BOTTOM_RANDOM).apply(game, source);
        return true;
    }
}
