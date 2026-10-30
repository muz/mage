package mage.cards.s;

import java.util.LinkedHashMap;
import java.util.Map;
import mage.abilities.Ability;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.StationAbility;
import mage.abilities.keyword.StationLevelAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.ExileZone;
import mage.game.Game;
import mage.players.Player;
import mage.players.PlayerList;
import mage.target.common.TargetCardInGraveyard;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author muz
 */
public final class SSBotanyBay extends CardImpl {

    public SSBotanyBay(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{3}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SPACECRAFT);

        // {T}: Each opponent exiles a card from their graveyard. You may exile a card from your graveyard.
        this.addAbility(new SimpleActivatedAbility(new SSBotanyBayExileEffect(), new TapSourceCost()));

        // Station
        this.addAbility(new StationAbility());

        // STATION 8+
        // Flying
        // Whenever S.S. Botany Bay deals combat damage to a player, return each creature card you own exiled with it to the battlefield.
        // 3/5
        this.addAbility(new StationLevelAbility(8)
            .withLevelAbility(FlyingAbility.getInstance())
            .withLevelAbility(new DealsCombatDamageToAPlayerTriggeredAbility(new SSBotanyBayReturnEffect()))
            .withPT(3, 5));
    }

    private SSBotanyBay(final SSBotanyBay card) {
        super(card);
    }

    @Override
    public SSBotanyBay copy() {
        return new SSBotanyBay(this);
    }
}

class SSBotanyBayExileEffect extends OneShotEffect {

    SSBotanyBayExileEffect() {
        super(Outcome.Benefit);
        staticText = "each opponent exiles a card from their graveyard. You may exile a card from your graveyard";
    }

    private SSBotanyBayExileEffect(final SSBotanyBayExileEffect effect) {
        super(effect);
    }

    @Override
    public SSBotanyBayExileEffect copy() {
        return new SSBotanyBayExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        UUID exileId = CardUtil.getExileZoneId(game, source);
        String exileName = CardUtil.getSourceName(game, source);
        Map<UUID, Card> choices = new LinkedHashMap<>();
        PlayerList players = game.getPlayerList().copy();
        players.setCurrent(game.getActivePlayerId());
        for (UUID playerId : players) {
            Player player = game.getPlayer(playerId);
            if (player == null || !player.isInGame() || !controller.hasOpponent(playerId, game)
                || player.getGraveyard().isEmpty()) {
                continue;
            }
            TargetCardInGraveyard target = new TargetCardInGraveyard(1, 1, StaticFilters.FILTER_CARD, true);
            if (player.choose(Outcome.Exile, player.getGraveyard(), target, source, game)) {
                Card card = game.getCard(target.getFirstTarget());
                if (card != null) {
                    choices.put(playerId, card);
                }
            }
        }
        for (Map.Entry<UUID, Card> choice : choices.entrySet()) {
            game.getPlayer(choice.getKey()).moveCardToExileWithInfo(
                choice.getValue(), exileId, exileName, source, game, Zone.GRAVEYARD, true);
        }
        game.processAction();
        TargetCardInGraveyard target = new TargetCardInGraveyard(0, 1, StaticFilters.FILTER_CARD, true);
        if (!controller.getGraveyard().isEmpty()
            && controller.choose(Outcome.Exile, controller.getGraveyard(), target, source, game)) {
            Card card = game.getCard(target.getFirstTarget());
            if (card != null) {
                controller.moveCardToExileWithInfo(card, exileId, exileName, source, game, Zone.GRAVEYARD, true);
            }
        }
        return true;
    }
}

class SSBotanyBayReturnEffect extends OneShotEffect {

    SSBotanyBayReturnEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "return each creature card you own exiled with it to the battlefield";
    }

    private SSBotanyBayReturnEffect(final SSBotanyBayReturnEffect effect) {
        super(effect);
    }

    @Override
    public SSBotanyBayReturnEffect copy() {
        return new SSBotanyBayReturnEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        ExileZone exile = game.getExile().getExileZone(CardUtil.getExileZoneId(game, source));
        if (exile == null) {
            return false;
        }
        Cards cards = new CardsImpl();
        for (Card card : exile.getCards(game)) {
            if (card.isOwnedBy(player.getId()) && card.isCreature(game)) {
                cards.add(card);
            }
        }
        return player.moveCards(cards, Zone.BATTLEFIELD, source, game);
    }
}
