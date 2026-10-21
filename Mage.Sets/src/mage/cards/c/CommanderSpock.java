package mage.cards.c;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldOrAttacksSourceTriggeredAbility;
import mage.abilities.dynamicvalue.common.DifferentlyNamedPermanentCount;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.ComparisonType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.filter.StaticFilters;
import mage.filter.predicate.mageobject.ManaValuePredicate;
import mage.game.Game;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author muz
 */
public final class CommanderSpock extends CardImpl {

    public CommanderSpock(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}{W}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.VULCAN);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SCIENTIST);
        this.power = new MageInt(3);
        this.toughness = new MageInt(5);

        // Strange new worlds -- Whenever Commander Spock enters or attacks, reveal the top card of your library. You may cast that card without paying its mana cost if that spell's mana value is equal to or less than the number of differently named lands you control. If it's a land card, you may put it onto the battlefield tapped. If that card is still on top of your library, draw a card.
        this.addAbility(new EntersBattlefieldOrAttacksSourceTriggeredAbility(new CommanderSpockEffect())
            .withFlavorWord("Strange new worlds").addHint(CommanderSpockEffect.landCount.getHint()));
    }

    private CommanderSpock(final CommanderSpock card) {
        super(card);
    }

    @Override
    public CommanderSpock copy() {
        return new CommanderSpock(this);
    }
}

class CommanderSpockEffect extends OneShotEffect {

    static final DifferentlyNamedPermanentCount landCount =
        new DifferentlyNamedPermanentCount(StaticFilters.FILTER_CONTROLLED_PERMANENT_LANDS);

    CommanderSpockEffect() {
        super(Outcome.PlayForFree);
        staticText = "reveal the top card of your library. You may cast that card without paying its mana cost "
            + "if that spell's mana value is equal to or less than the number of differently named lands you control. "
            + "If it's a land card, you may put it onto the battlefield tapped. "
            + "If that card is still on top of your library, draw a card";
    }

    private CommanderSpockEffect(final CommanderSpockEffect effect) {
        super(effect);
    }

    @Override
    public CommanderSpockEffect copy() {
        return new CommanderSpockEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Card card = controller.getLibrary().getFromTop(game);
        if (card == null) {
            return true;
        }
        MageObjectReference revealedCard = new MageObjectReference(card, game);
        controller.revealCards(source, new CardsImpl(card), game);

        FilterCard filter = new FilterCard();
        filter.add(new ManaValuePredicate(ComparisonType.OR_LESS, landCount.calculate(game, source, this)));
        CardUtil.castSpellWithAttributesForFree(controller, source, game, card, filter);

        if (revealedCard.refersTo(card, game) && card.isLand(game) && controller.chooseUse(
            Outcome.PutLandInPlay, "Put " + card.getIdName() + " onto the battlefield tapped?", source, game
        )) {
            controller.moveCards(card, Zone.BATTLEFIELD, source, game, true, false, false, null);
        }
        if (revealedCard.refersTo(controller.getLibrary().getFromTop(game), game)) {
            controller.drawCards(1, source, game);
        }
        return true;
    }
}
