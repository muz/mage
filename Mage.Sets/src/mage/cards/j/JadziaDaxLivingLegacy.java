package mage.cards.j;

import mage.MageInt;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.DiscardSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.effects.mana.DynamicManaEffect;
import mage.abilities.triggers.BeginningOfFirstMainTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * @author muz
 */
public final class JadziaDaxLivingLegacy extends CardImpl {

    public JadziaDaxLivingLegacy(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.TRILL);
        this.subtype.add(SubType.SCIENTIST);
        this.power = new MageInt(0);
        this.toughness = new MageInt(1);

        // At the beginning of your first main phase, put a +1/+1 counter on Jadzia Dax, then add an amount of {G} equal to the number of counters on her.
        Ability ability = new BeginningOfFirstMainTriggeredAbility(new AddCountersSourceEffect(CounterType.P1P1.createInstance()));
        ability.addEffect(new DynamicManaEffect(Mana.GreenMana(1), CountersSourceCount.ANY,
            "then add an amount of {G} equal to the number of counters on {this}"));
        this.addAbility(ability);

        // Transfer Symbiont -- {2}{G}, Discard this card: Put two +1/+1 counters on target creature.
        ability = new SimpleActivatedAbility(Zone.HAND, new AddCountersTargetEffect(CounterType.P1P1.createInstance(2)), new ManaCostsImpl<>("{2}{G}"));
        ability.addCost(new DiscardSourceCost());
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability.withFlavorWord("Transfer Symbiont"));
    }

    private JadziaDaxLivingLegacy(final JadziaDaxLivingLegacy card) {
        super(card);
    }

    @Override
    public JadziaDaxLivingLegacy copy() {
        return new JadziaDaxLivingLegacy(this);
    }
}
