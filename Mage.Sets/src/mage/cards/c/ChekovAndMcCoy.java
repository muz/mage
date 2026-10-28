package mage.cards.c;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.common.PutCounterOnPermanentTriggeredAbility;
import mage.abilities.dynamicvalue.common.EffectKeyValue;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.AnotherPredicate;

/**
 * @author muz
 */
public final class ChekovAndMcCoy extends CardImpl {

    private static final FilterPermanent filter = new FilterPermanent("{this}");

    static {
        filter.add(Predicates.not(AnotherPredicate.instance));
    }

    public ChekovAndMcCoy(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.PILOT);
        this.subtype.add(SubType.DOCTOR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Whenever you put one or more +1/+1 counters on Chekov and McCoy, you may draw that many cards. Do this only once each turn.
        this.addAbility(new PutCounterOnPermanentTriggeredAbility(
            new DrawCardSourceControllerEffect(new EffectKeyValue("countersAdded", "that many")),
            CounterType.P1P1, filter).setDoOnlyOnceEachTurn(true));
    }

    private ChekovAndMcCoy(final ChekovAndMcCoy card) {
        super(card);
    }

    @Override
    public ChekovAndMcCoy copy() {
        return new ChekovAndMcCoy(this);
    }
}
