package mage.cards.i;

import mage.abilities.Ability;
import mage.abilities.dynamicvalue.common.FederationCount;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ExileSpellEffect;
import mage.abilities.effects.common.MayCastTargetCardEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.CastManaAdjustment;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author muz
 */
public final class InfiniteDiversity extends CardImpl {

    public InfiniteDiversity(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{7}{R}{R}");

        // Federation -- Exile target instant or sorcery card from your graveyard. Copy that card X times, where X is the number of creature types among non-Borg creatures you control. You may cast the copies without paying their mana costs. Exile Infinite Diversity.
        this.getSpellAbility().addEffect(new InfiniteDiversityEffect());
        this.getSpellAbility().addEffect(new ExileSpellEffect());
        this.getSpellAbility().addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_INSTANT_OR_SORCERY));
        this.getSpellAbility().setAbilityWord(AbilityWord.FEDERATION);
        this.getSpellAbility().addHint(FederationCount.instance.getHint());
    }

    private InfiniteDiversity(final InfiniteDiversity card) {
        super(card);
    }

    @Override
    public InfiniteDiversity copy() {
        return new InfiniteDiversity(this);
    }
}

class InfiniteDiversityEffect extends OneShotEffect {

    InfiniteDiversityEffect() {
        super(Outcome.Benefit);
        staticText = "exile target instant or sorcery card from your graveyard. Copy that card X times, where X is the number of creature types among non-Borg creatures you control. You may cast the copies without paying their mana costs";
    }

    private InfiniteDiversityEffect(final InfiniteDiversityEffect effect) {
        super(effect);
    }

    @Override
    public InfiniteDiversityEffect copy() {
        return new InfiniteDiversityEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (player == null || card == null) {
            return false;
        }
        player.moveCards(card, Zone.EXILED, source, game);
        int amount = FederationCount.instance.calculate(game, source, this);
        Cards copies = new CardsImpl();
        for (int i = 0; i < amount; i++) {
            copies.add(game.copyCard(card, source, player.getId()));
        }
        for (Card copy : copies.getCards(game)) {
            new MayCastTargetCardEffect(CastManaAdjustment.WITHOUT_PAYING_MANA_COST)
                .setTargetPointer(new FixedTarget(copy, game)).apply(game, source);
        }
        return true;
    }
}
