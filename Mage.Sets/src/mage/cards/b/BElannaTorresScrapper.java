package mage.cards.b;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.game.Game;
import mage.players.Player;

/**
 * @author muz
 */
public final class BElannaTorresScrapper extends CardImpl {

    public BElannaTorresScrapper(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.KLINGON);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Vigilance, trample
        this.addAbility(VigilanceAbility.getInstance());
        this.addAbility(TrampleAbility.getInstance());

        // {2}, {T}: Surveil 2, then you may reveal the top card of your library. Draw a card if it's an artifact card. If you don't draw a card this way, put two +1/+1 counters on B'Elanna Torres.
        Ability ability = new SimpleActivatedAbility(new BElannaTorresScrapperEffect(), new GenericManaCost(2));
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);
    }

    private BElannaTorresScrapper(final BElannaTorresScrapper card) {
        super(card);
    }

    @Override
    public BElannaTorresScrapper copy() {
        return new BElannaTorresScrapper(this);
    }
}

class BElannaTorresScrapperEffect extends OneShotEffect {

    BElannaTorresScrapperEffect() {
        super(Outcome.Benefit);
        staticText = "surveil 2, then you may reveal the top card of your library. "
            + "Draw a card if it's an artifact card. If you don't draw a card this way, "
            + "put two +1/+1 counters on {this}";
    }

    private BElannaTorresScrapperEffect(final BElannaTorresScrapperEffect effect) {
        super(effect);
    }

    @Override
    public BElannaTorresScrapperEffect copy() {
        return new BElannaTorresScrapperEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        controller.surveil(2, source, game);
        Card card = controller.getLibrary().getFromTop(game);
        if (card != null && controller.chooseUse(outcome, "Reveal the top card of your library?", source, game)) {
            controller.revealCards(source, new CardsImpl(card), game);
            if (card.isArtifact(game) && controller.drawCards(1, source, game) > 0) {
                return true;
            }
        }
        new AddCountersSourceEffect(CounterType.P1P1.createInstance(2)).apply(game, source);
        return true;
    }
}
