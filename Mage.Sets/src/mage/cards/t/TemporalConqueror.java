package mage.cards.t;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.MillCardsControllerEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.token.TreasureToken;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 * @author muz
 */
public final class TemporalConqueror extends CardImpl {

    public TemporalConqueror(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}");

        this.subtype.add(SubType.NAKUHL);
        this.subtype.add(SubType.WARRIOR);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // When this creature enters, mill three cards. Then return a land card from your graveyard to your hand. If you can't, create a Treasure token.
        Ability ability = new EntersBattlefieldTriggeredAbility(new MillCardsControllerEffect(3));
        ability.addEffect(new TemporalConquerorEffect());
        this.addAbility(ability);
    }

    private TemporalConqueror(final TemporalConqueror card) {
        super(card);
    }

    @Override
    public TemporalConqueror copy() {
        return new TemporalConqueror(this);
    }
}

class TemporalConquerorEffect extends OneShotEffect {

    TemporalConquerorEffect() {
        super(Outcome.ReturnToHand);
        staticText = "then return a land card from your graveyard to your hand. If you can't, create a Treasure token";
    }

    private TemporalConquerorEffect(final TemporalConquerorEffect effect) {
        super(effect);
    }

    @Override
    public TemporalConquerorEffect copy() {
        return new TemporalConquerorEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        TargetCardInYourGraveyard target = new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_LAND);
        target.withNotTarget(true);
        if (player.choose(outcome, target, source, game)) {
            Card card = game.getCard(target.getFirstTarget());
            if (card != null && player.moveCards(card, Zone.HAND, source, game)) {
                return true;
            }
        }
        return new CreateTokenEffect(new TreasureToken()).apply(game, source);
    }
}
