package mage.cards.k;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.SacrificeSourceEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

/**
 * @author muz
 */
public final class KesEvolvingTelepath extends CardImpl {

    public KesEvolvingTelepath(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.OCAMPA);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(2);
        this.toughness = new MageInt(6);

        // At the beginning of your end step, put a time counter on Kes, then draw X cards, where X is the number of time counters on her. Then if she has nine or more time counters on her, sacrifice her.
        this.addAbility(new BeginningOfEndStepTriggeredAbility(new KesEvolvingTelepathEffect()));
    }

    private KesEvolvingTelepath(final KesEvolvingTelepath card) {
        super(card);
    }

    @Override
    public KesEvolvingTelepath copy() {
        return new KesEvolvingTelepath(this);
    }
}

class KesEvolvingTelepathEffect extends OneShotEffect {

    KesEvolvingTelepathEffect() {
        super(Outcome.DrawCard);
        staticText = "put a time counter on {this}, then draw X cards, where X is the number of time counters on her. "
            + "Then if she has nine or more time counters on her, sacrifice her";
    }

    private KesEvolvingTelepathEffect(final KesEvolvingTelepathEffect effect) {
        super(effect);
    }

    @Override
    public KesEvolvingTelepathEffect copy() {
        return new KesEvolvingTelepathEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        new AddCountersSourceEffect(CounterType.TIME.createInstance()).apply(game, source);
        controller.drawCards(new CountersSourceCount(CounterType.TIME).calculate(game, source, this), source, game);
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent != null && permanent.getCounters(game).getCount(CounterType.TIME) >= 9) {
            new SacrificeSourceEffect().apply(game, source);
        }
        return true;
    }
}
