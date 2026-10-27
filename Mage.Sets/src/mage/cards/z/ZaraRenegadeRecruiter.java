package mage.cards.z;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ReturnToHandTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.ZonesHandler;
import mage.game.combat.AttackingCreatureEntry;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class ZaraRenegadeRecruiter extends CardImpl {

    public ZaraRenegadeRecruiter(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.PIRATE);
        this.power = new MageInt(4);
        this.toughness = new MageInt(3);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Whenever Zara, Renegade Recruiter attacks, look at defending player's hand. You may put a creature card from it onto the battlefield under your control tapped and attacking that player or a planeswalker they control. Return that creature to its owner's hand at the beginning of the next end step.
        this.addAbility(new AttacksTriggeredAbility(
                new ZaraRenegadeRecruiterEffect(), false, null, SetTargetPointer.PLAYER
        ));
    }

    private ZaraRenegadeRecruiter(final ZaraRenegadeRecruiter card) {
        super(card);
    }

    @Override
    public ZaraRenegadeRecruiter copy() {
        return new ZaraRenegadeRecruiter(this);
    }
}

class ZaraRenegadeRecruiterEffect extends OneShotEffect {

    ZaraRenegadeRecruiterEffect() {
        super(Outcome.Benefit);
        staticText = "look at defending player's hand. You may put a creature card from it onto the battlefield " +
                "under your control tapped and attacking that player or a planeswalker they control. " +
                "Return that creature to its owner's hand at the beginning of the next end step";
    }

    private ZaraRenegadeRecruiterEffect(final ZaraRenegadeRecruiterEffect effect) {
        super(effect);
    }

    @Override
    public ZaraRenegadeRecruiterEffect copy() {
        return new ZaraRenegadeRecruiterEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player player = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null || player == null || player.getHand().isEmpty()) {
            return false;
        }
        TargetCard targetCard = new TargetCard(0, 1, Zone.HAND, StaticFilters.FILTER_CARD_CREATURE);
        controller.choose(Outcome.Detriment, player.getHand(), targetCard, source, game);
        Card card = game.getCard(targetCard.getFirstTarget());
        if (card == null) {
            return false;
        }
        Permanent permanent = ZonesHandler.putOntoBattlefieldAttacking(card, controller.getId(), source, game,
            AttackingCreatureEntry.attackingPlayer(player.getId()));
        if (permanent == null) {
            return false;
        }
        game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(
                new ReturnToHandTargetEffect().setTargetPointer(new FixedTarget(permanent, game))
        ), source);
        return true;
    }
}
