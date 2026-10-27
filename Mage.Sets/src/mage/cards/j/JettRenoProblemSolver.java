package mage.cards.j;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.BecomesTappedSourceTriggeredAbility;
import mage.abilities.common.FaceDilemmaTriggeredAbility;
import mage.abilities.condition.Condition;
import mage.abilities.decorator.ConditionalOneShotEffect;
import mage.abilities.dynamicvalue.common.SourcePermanentPowerValue;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.effects.keyword.SurveilEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.game.Game;

import java.util.UUID;

/**
 * @author muz
 */
public final class JettRenoProblemSolver extends CardImpl {

    public JettRenoProblemSolver(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(1);
        this.toughness = new MageInt(2);

        // Whenever Jett Reno becomes tapped, surveil 1. If her power is 4 or greater, instead draw a card.
        this.addAbility(new BecomesTappedSourceTriggeredAbility(new ConditionalOneShotEffect(
            new DrawCardSourceControllerEffect(1), new SurveilEffect(1), JettRenoProblemSolverCondition.instance,
            "surveil 1. If {this}'s power is 4 or greater, instead draw a card"
        )));

        // Whenever you face a dilemma, put a +1/+1 counter on Jett Reno.
        this.addAbility(new FaceDilemmaTriggeredAbility(new AddCountersSourceEffect(CounterType.P1P1.createInstance())));
    }

    private JettRenoProblemSolver(final JettRenoProblemSolver card) {
        super(card);
    }

    @Override
    public JettRenoProblemSolver copy() {
        return new JettRenoProblemSolver(this);
    }
}

enum JettRenoProblemSolverCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        return SourcePermanentPowerValue.NOT_NEGATIVE.calculate(game, source, null) >= 4;
    }
}
