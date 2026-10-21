package mage.cards.q;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.condition.common.MyTurnCondition;
import mage.abilities.costs.common.ExileFromHandCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.MayCastTargetCardEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetCardInHand;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author muz
 */
public final class QStackingTheDeck extends CardImpl {

    public QStackingTheDeck(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.Q);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // When Q enters, shuffle your library, then exile cards from the top of your library until you exile a nonland card. That nonland card gains "Exile a card from your hand: Create a copy of this card. You may cast the copy. Activate only during your turn and only if this card is exiled."
        this.addAbility(new EntersBattlefieldTriggeredAbility(new QStackingTheDeckEffect()));
    }

    private QStackingTheDeck(final QStackingTheDeck card) {
        super(card);
    }

    @Override
    public QStackingTheDeck copy() {
        return new QStackingTheDeck(this);
    }
}

class QStackingTheDeckEffect extends OneShotEffect {

    QStackingTheDeckEffect() {
        super(Outcome.Benefit);
        staticText = "shuffle your library, then exile cards from the top of your library until you exile a nonland card. "
            + "That nonland card gains \"Exile a card from your hand: Create a copy of this card. You may cast the copy. "
            + "Activate only during your turn and only if this card is exiled.\"";
    }

    private QStackingTheDeckEffect(final QStackingTheDeckEffect effect) {
        super(effect);
    }

    @Override
    public QStackingTheDeckEffect copy() {
        return new QStackingTheDeckEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        controller.shuffleLibrary(source, game);
        while (controller.getLibrary().hasCards()) {
            Card card = controller.getLibrary().getFromTop(game);
            controller.moveCards(card, Zone.EXILED, source, game);
            if (game.getState().getZone(card.getId()) == Zone.LIBRARY) {
                return false;
            }
            if (card.isLand(game) || game.getState().getZone(card.getId()) != Zone.EXILED) {
                continue;
            }
            Ability ability = new SimpleActivatedAbility(Zone.EXILED, new QStackingTheDeckCopyEffect(),
                new ExileFromHandCost(new TargetCardInHand())).setCondition(MyTurnCondition.instance);
            game.addEffect(new GainAbilityTargetEffect(ability, Duration.Custom, null, true)
                .setTargetPointer(new FixedTarget(card, game)), source);
            return true;
        }
        return true;
    }
}

class QStackingTheDeckCopyEffect extends OneShotEffect {

    QStackingTheDeckCopyEffect() {
        super(Outcome.Copy);
        staticText = "create a copy of this card. You may cast the copy. Activate only during your turn and only if this card is exiled";
    }

    private QStackingTheDeckCopyEffect(final QStackingTheDeckCopyEffect effect) {
        super(effect);
    }

    @Override
    public QStackingTheDeckCopyEffect copy() {
        return new QStackingTheDeckCopyEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Card card = game.getCard(source.getSourceId());
        if (card == null) {
            return false;
        }
        Card copy = game.copyCard(card, source, source.getControllerId());
        return new MayCastTargetCardEffect(false).setTargetPointer(new FixedTarget(copy, game)).apply(game, source);
    }
}
