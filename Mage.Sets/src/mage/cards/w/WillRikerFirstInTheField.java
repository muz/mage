package mage.cards.w;

import java.util.ArrayList;
import java.util.List;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.SacrificeTargetEffect;
import mage.abilities.triggers.BeginningOfCombatTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.PutCards;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.targetpointer.FixedTargets;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author muz
 */
public final class WillRikerFirstInTheField extends CardImpl {

    public WillRikerFirstInTheField(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(4);
        this.toughness = new MageInt(3);

        // At the beginning of combat on your turn, reveal cards from the top of your library until you reveal two land cards. Put those land cards onto the battlefield and the rest on the bottom of your library in a random order. Sacrifice those lands at the beginning of your next end step.
        this.addAbility(new BeginningOfCombatTriggeredAbility(new WillRikerFirstInTheFieldEffect()));
    }

    private WillRikerFirstInTheField(final WillRikerFirstInTheField card) {
        super(card);
    }

    @Override
    public WillRikerFirstInTheField copy() {
        return new WillRikerFirstInTheField(this);
    }
}

class WillRikerFirstInTheFieldEffect extends OneShotEffect {

    WillRikerFirstInTheFieldEffect() {
        super(Outcome.Benefit);
        staticText = "reveal cards from the top of your library until you reveal two land cards. Put those land cards onto the battlefield and the rest on the bottom of your library in a random order. Sacrifice those lands at the beginning of your next end step";
    }

    private WillRikerFirstInTheFieldEffect(final WillRikerFirstInTheFieldEffect effect) {
        super(effect);
    }

    @Override
    public WillRikerFirstInTheFieldEffect copy() {
        return new WillRikerFirstInTheFieldEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        Cards revealed = new CardsImpl();
        Cards lands = new CardsImpl();
        for (Card card : player.getLibrary().getCards(game)) {
            revealed.add(card);
            if (card.isLand(game)) {
                lands.add(card);
                if (lands.size() == 2) {
                    break;
                }
            }
        }
        player.revealCards(source, revealed, game);
        player.moveCards(lands, Zone.BATTLEFIELD, source, game);
        List<Permanent> permanents = new ArrayList<>();
        for (Card card : lands.getCards(game)) {
            Permanent permanent = CardUtil.getPermanentFromCardPutToBattlefield(card, game);
            if (permanent != null) {
                permanents.add(permanent);
            }
        }
        revealed.removeAll(lands);
        PutCards.BOTTOM_RANDOM.moveCards(player, revealed, source, game);
        if (!permanents.isEmpty()) {
            game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(
                new SacrificeTargetEffect("sacrifice those lands", player.getId()).setTargetPointer(new FixedTargets(permanents, game)),
                TargetController.YOU
            ), source);
        }
        return true;
    }
}
