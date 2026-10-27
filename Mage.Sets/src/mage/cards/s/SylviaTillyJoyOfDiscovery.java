package mage.cards.s;

import mage.MageInt;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.delayed.ReflexiveTriggeredAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.Costs;
import mage.abilities.costs.CostsImpl;
import mage.abilities.costs.common.ExileFromGraveCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.DoWhenCostPaid;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.effects.keyword.SurveilEffect;
import mage.abilities.triggers.BeginningOfCombatTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.target.common.TargetCardInYourGraveyard;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * @author muz
 */
public final class SylviaTillyJoyOfDiscovery extends CardImpl {

    public SylviaTillyJoyOfDiscovery(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SCIENTIST);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // When Sylvia Tilly enters, surveil 2.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new SurveilEffect(2)));

        // At the beginning of combat on your turn, you may pay {1} and exile a card from your graveyard. When you do, put a +1/+1 counter on target creature.
        ReflexiveTriggeredAbility ability = new ReflexiveTriggeredAbility(new AddCountersTargetEffect(CounterType.P1P1.createInstance()), false);
        ability.addTarget(new TargetCreaturePermanent());
        Costs<Cost> costs = new CostsImpl<>();
        costs.add(new GenericManaCost(1));
        costs.add(new ExileFromGraveCost(new TargetCardInYourGraveyard()));
        this.addAbility(new BeginningOfCombatTriggeredAbility(new DoWhenCostPaid(ability, costs,
            "Pay {1} and exile a card from your graveyard?"
        ).setText("you may pay {1} and exile a card from your graveyard. When you do, put a +1/+1 counter on target creature")));
    }

    private SylviaTillyJoyOfDiscovery(final SylviaTillyJoyOfDiscovery card) {
        super(card);
    }

    @Override
    public SylviaTillyJoyOfDiscovery copy() {
        return new SylviaTillyJoyOfDiscovery(this);
    }
}
