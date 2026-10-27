package mage.cards.a;

import java.util.EnumSet;
import mage.abilities.Ability;
import mage.abilities.common.SagaAbility;
import mage.abilities.effects.Effects;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SagaChapter;
import mage.constants.SubType;
import mage.game.ExileZone;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetCardInHand;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author muz
 */
public final class AllGoodThings extends CardImpl {

    public AllGoodThings(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{G}{U}");

        this.subtype.add(SubType.SAGA);

        SagaAbility saga = new SagaAbility(this);

        // I, II, III -- Draw two cards, then exile a card from your hand.
        saga.addChapterEffect(this, SagaChapter.CHAPTER_I, SagaChapter.CHAPTER_III,
            new Effects(new DrawCardSourceControllerEffect(2), new AllGoodThingsExileEffect()));

        // IV -- If all cards exiled with this Saga share a card type, you gain 10 life.
        saga.addChapterEffect(this, SagaChapter.CHAPTER_IV, new AllGoodThingsLifeEffect());
        this.addAbility(saga);
    }

    private AllGoodThings(final AllGoodThings card) {
        super(card);
    }

    @Override
    public AllGoodThings copy() {
        return new AllGoodThings(this);
    }
}

class AllGoodThingsExileEffect extends OneShotEffect {

    AllGoodThingsExileEffect() {
        super(Outcome.Benefit);
        staticText = "then exile a card from your hand";
    }

    private AllGoodThingsExileEffect(final AllGoodThingsExileEffect effect) {
        super(effect);
    }

    @Override
    public AllGoodThingsExileEffect copy() {
        return new AllGoodThingsExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        TargetCardInHand target = new TargetCardInHand();
        target.withNotTarget(true);
        if (player.choose(outcome, target, source, game)) {
            Card card = game.getCard(target.getFirstTarget());
            if (card != null) {
                player.moveCardsToExile(card, source, game, true, CardUtil.getExileZoneId(game, source), CardUtil.getSourceIdName(game, source));
            }
        }
        return true;
    }
}

class AllGoodThingsLifeEffect extends OneShotEffect {

    AllGoodThingsLifeEffect() {
        super(Outcome.Benefit);
        staticText = "if all cards exiled with {this} share a card type, you gain 10 life";
    }

    private AllGoodThingsLifeEffect(final AllGoodThingsLifeEffect effect) {
        super(effect);
    }

    @Override
    public AllGoodThingsLifeEffect copy() {
        return new AllGoodThingsLifeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        ExileZone exile = game.getExile().getExileZone(CardUtil.getExileZoneId(game, source));
        if (player == null || exile == null || exile.isEmpty()) {
            return false;
        }
        EnumSet<CardType> types = EnumSet.allOf(CardType.class);
        for (Card card : exile.getCards(game)) {
            types.retainAll(card.getCardType(game));
        }
        if (!types.isEmpty()) {
            player.gainLife(10, game, source);
        }
        return true;
    }
}
