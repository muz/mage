package mage.cards.e;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.dynamicvalue.common.StrangeNewWorldsValue;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.asthought.PlayFromNotOwnHandZoneTargetEffect;
import mage.abilities.hint.common.StrangeNewWorldsHint;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.common.TargetCardInExile;
import mage.target.targetpointer.FixedTargets;
import mage.util.CardUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author muz
 */
public final class EnsignPavelChekov extends CardImpl {

    public EnsignPavelChekov(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.PILOT);
        this.power = new MageInt(1);
        this.toughness = new MageInt(5);

        // Strange new worlds -- When Ensign Chekov enters, each opponent exiles the top X cards of their library, where X is the number of differently named lands you control. For each opponent, choose a land card that player owns exiled this way. You may play the chosen cards for as long as they remain exiled.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new EnsignPavelChekovEffect())
            .setAbilityWord(AbilityWord.STRANGE_NEW_WORLDS).addHint(StrangeNewWorldsHint.instance));
    }

    private EnsignPavelChekov(final EnsignPavelChekov card) {
        super(card);
    }

    @Override
    public EnsignPavelChekov copy() {
        return new EnsignPavelChekov(this);
    }
}

class EnsignPavelChekovEffect extends OneShotEffect {

    EnsignPavelChekovEffect() {
        super(Outcome.Benefit);
        staticText = "each opponent exiles the top X cards of their library, "
            + "where X is the number of differently named lands you control. "
            + "For each opponent, choose a land card that player owns exiled this way. "
            + "You may play the chosen cards for as long as they remain exiled";
    }

    private EnsignPavelChekovEffect(final EnsignPavelChekovEffect effect) {
        super(effect);
    }

    @Override
    public EnsignPavelChekovEffect copy() {
        return new EnsignPavelChekovEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        int amount = StrangeNewWorldsValue.instance.calculate(game, source, this);
        Map<UUID, Cards> exiledCards = new LinkedHashMap<>();
        for (UUID opponentId : game.getOpponents(controller.getId())) {
            Player opponent = game.getPlayer(opponentId);
            if (opponent == null) {
                continue;
            }
            Cards cards = new CardsImpl(opponent.getLibrary().getTopCards(game, amount));
            opponent.moveCardsToExile(cards.getCards(game), source, game, true,
                CardUtil.getExileZoneId(game, source), CardUtil.getSourceIdName(game, source));
            exiledCards.put(opponentId, cards);
        }
        Cards chosenCards = new CardsImpl();
        for (Map.Entry<UUID, Cards> entry : exiledCards.entrySet()) {
            Cards cards = entry.getValue();
            cards.retainZone(Zone.EXILED, game);
            cards.removeIf(cardId -> !game.getCard(cardId).isOwnedBy(entry.getKey()));
            TargetCard target = new TargetCardInExile(StaticFilters.FILTER_CARD_LAND);
            target.withNotTarget(true);
            controller.choose(outcome, cards, target, source, game);
            chosenCards.addAll(target.getTargets());
        }
        if (!chosenCards.isEmpty()) {
            game.addEffect(new PlayFromNotOwnHandZoneTargetEffect(Zone.EXILED, Duration.Custom)
                .setTargetPointer(new FixedTargets(chosenCards, game)), source);
        }
        return true;
    }
}
