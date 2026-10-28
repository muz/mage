package mage.cards.a;

import java.util.Set;
import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.game.Game;
import mage.players.Player;
import mage.players.PlayerList;
import mage.util.RandomUtil;

/**
 * @author muz
 */
public final class AlexanderShipsFool extends CardImpl {

    public AlexanderShipsFool(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.KLINGON);
        this.subtype.add(SubType.HUMAN);
        this.power = new MageInt(3);
        this.toughness = new MageInt(5);

        // Vigilance, haste
        this.addAbility(VigilanceAbility.getInstance());
        this.addAbility(HasteAbility.getInstance());

        // {T}: Each player reveals the top card of their library, then draws a card. Alexander deals X damage to a random opponent, where X is the greatest mana value among cards revealed this way.
        this.addAbility(new SimpleActivatedAbility(new AlexanderShipsFoolEffect(), new TapSourceCost()));
    }

    private AlexanderShipsFool(final AlexanderShipsFool card) {
        super(card);
    }

    @Override
    public AlexanderShipsFool copy() {
        return new AlexanderShipsFool(this);
    }
}

class AlexanderShipsFoolEffect extends OneShotEffect {

    AlexanderShipsFoolEffect() {
        super(Outcome.Benefit);
        staticText = "each player reveals the top card of their library, then draws a card. "
            + "{this} deals X damage to a random opponent, where X is the greatest mana value among cards revealed this way";
    }

    private AlexanderShipsFoolEffect(final AlexanderShipsFoolEffect effect) {
        super(effect);
    }

    @Override
    public AlexanderShipsFoolEffect copy() {
        return new AlexanderShipsFoolEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        PlayerList players = game.getPlayerList().copy();
        players.setCurrent(game.getActivePlayerId());
        int damage = 0;
        for (UUID playerId : players) {
            Player player = game.getPlayer(playerId);
            if (player == null || !player.isInGame()
                || !game.getState().getPlayersInRange(source.getControllerId(), game).contains(playerId)) {
                continue;
            }
            Card card = player.getLibrary().getFromTop(game);
            if (card != null) {
                damage = Math.max(damage, card.getManaValue());
                player.revealCards(source, new CardsImpl(card), game);
            }
        }
        for (UUID playerId : players) {
            Player player = game.getPlayer(playerId);
            if (player != null && player.isInGame()
                && game.getState().getPlayersInRange(source.getControllerId(), game).contains(playerId)) {
                player.drawCards(1, source, game);
            }
        }
        Set<UUID> opponents = game.getOpponents(source.getControllerId(), true);
        if (!opponents.isEmpty()) {
            Player opponent = game.getPlayer(RandomUtil.randomFromCollection(opponents));
            if (opponent != null) {
                opponent.damage(damage, source, game);
            }
        }
        return true;
    }
}
