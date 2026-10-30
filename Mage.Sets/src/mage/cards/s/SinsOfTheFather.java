package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.common.SagaAbility;
import mage.abilities.dynamicvalue.common.OpponentsCount;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.LoseLifeOpponentsEffect;
import mage.abilities.effects.common.SacrificeOpponentsEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SagaChapter;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.FilterPermanent;
import mage.filter.StaticFilters;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.Game;
import mage.game.permanent.token.KlingonWarriorToken;
import mage.players.Player;
import mage.target.TargetPlayer;

import java.util.UUID;

/**
 * @author muz
 */
public final class SinsOfTheFather extends CardImpl {

    private static final FilterPermanent filter = new FilterCreaturePermanent("nontoken creature");

    static {
        filter.add(TokenPredicate.FALSE);
    }

    public SinsOfTheFather(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{B}{R}");

        this.subtype.add(SubType.SAGA);

        // (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
        SagaAbility sagaAbility = new SagaAbility(this);

        // I -- Each opponent sacrifices a nontoken creature of their choice.
        sagaAbility.addChapterEffect(this, SagaChapter.CHAPTER_I, new SacrificeOpponentsEffect(filter));

        // II -- For each opponent, you create a 2/1 red Klingon Warrior creature token with haste.
        sagaAbility.addChapterEffect(this, SagaChapter.CHAPTER_II,
            new CreateTokenEffect(new KlingonWarriorToken(), OpponentsCount.instance)
            .setText("for each opponent, you create a 2/1 red Klingon Warrior creature token with haste"));

        // III -- Exile target player's graveyard. Each opponent loses life equal to the number of creature cards exiled this way.
        sagaAbility.addChapterEffect(this, SagaChapter.CHAPTER_III, new SinsOfTheFatherEffect(), new TargetPlayer());
        this.addAbility(sagaAbility);
    }

    private SinsOfTheFather(final SinsOfTheFather card) {
        super(card);
    }

    @Override
    public SinsOfTheFather copy() {
        return new SinsOfTheFather(this);
    }
}

class SinsOfTheFatherEffect extends OneShotEffect {

    SinsOfTheFatherEffect() {
        super(Outcome.Benefit);
        staticText = "exile target player's graveyard. Each opponent loses life "
            + "equal to the number of creature cards exiled this way";
    }

    private SinsOfTheFatherEffect(final SinsOfTheFatherEffect effect) {
        super(effect);
    }

    @Override
    public SinsOfTheFatherEffect copy() {
        return new SinsOfTheFatherEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player player = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null || player == null) {
            return false;
        }
        Cards cards = new CardsImpl(player.getGraveyard());
        controller.moveCards(cards, Zone.EXILED, source, game);
        cards.retainZone(Zone.EXILED, game);
        return new LoseLifeOpponentsEffect(cards.count(StaticFilters.FILTER_CARD_CREATURE, game)).apply(game, source);
    }
}
