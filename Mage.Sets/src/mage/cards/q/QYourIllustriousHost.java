package mage.cards.q;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapTargetCost;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.permanent.TappedPredicate;
import mage.game.Game;
import mage.players.Player;
import mage.util.CardUtil;

/**
 * @author muz
 */
public final class QYourIllustriousHost extends CardImpl {

    private static final FilterControlledPermanent filter = new FilterControlledPermanent("untapped Faeries you control");

    static {
        filter.add(SubType.FAERIE.getPredicate());
        filter.add(TappedPredicate.UNTAPPED);
    }

    public QYourIllustriousHost(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{W}{U}{B}{R}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.Q);
        this.power = new MageInt(5);
        this.toughness = new MageInt(5);

        // Tap three untapped Faeries you control: Exile cards from the top of your library until you exile a nonland card. You may cast that card without paying its mana cost.
        this.addAbility(new SimpleActivatedAbility(new QYourIllustriousHostEffect(), new TapTargetCost(3, filter)));
    }

    private QYourIllustriousHost(final QYourIllustriousHost card) {
        super(card);
    }

    @Override
    public QYourIllustriousHost copy() {
        return new QYourIllustriousHost(this);
    }
}

class QYourIllustriousHostEffect extends OneShotEffect {

    QYourIllustriousHostEffect() {
        super(Outcome.Benefit);
        staticText = "exile cards from the top of your library until you exile a nonland card. "
            + "You may cast that card without paying its mana cost";
    }

    private QYourIllustriousHostEffect(final QYourIllustriousHostEffect effect) {
        super(effect);
    }

    @Override
    public QYourIllustriousHostEffect copy() {
        return new QYourIllustriousHostEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        for (Card card : player.getLibrary().getCards(game)) {
            player.moveCards(card, Zone.EXILED, source, game);
            if (game.getState().getZone(card.getId()) == Zone.EXILED && !card.isLand(game)) {
                CardUtil.castSpellWithAttributesForFree(player, source, game, card);
                break;
            }
        }
        return true;
    }
}
