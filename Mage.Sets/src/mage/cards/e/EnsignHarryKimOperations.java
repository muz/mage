package mage.cards.e;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldAllTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.RemoveCounterCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetControlledPermanent;
import mage.util.CardUtil;

/**
 * @author muz
 */
public final class EnsignHarryKimOperations extends CardImpl {

    private static final FilterControlledCreaturePermanent filter =
        new FilterControlledCreaturePermanent("among creatures you control");

    static {
        filter.add(CounterType.P1P1.getPredicate());
    }

    public EnsignHarryKimOperations(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(1);
        this.toughness = new MageInt(2);

        // Whenever another creature you control enters, if it has greater power than Ensign Kim, put a +1/+1 counter on Ensign Kim.
        this.addAbility(new EnsignHarryKimOperationsTriggeredAbility());

        // {2}, Remove two +1/+1 counters from among creatures you control: Draw a card.
        Ability ability = new SimpleActivatedAbility(new DrawCardSourceControllerEffect(1), new GenericManaCost(2));
        ability.addCost(new RemoveCounterCost(
            new TargetControlledPermanent(1, 2, filter, true), CounterType.P1P1, 2));
        this.addAbility(ability);
    }

    private EnsignHarryKimOperations(final EnsignHarryKimOperations card) {
        super(card);
    }

    @Override
    public EnsignHarryKimOperations copy() {
        return new EnsignHarryKimOperations(this);
    }
}

class EnsignHarryKimOperationsTriggeredAbility extends EntersBattlefieldAllTriggeredAbility {

    EnsignHarryKimOperationsTriggeredAbility() {
        super(new AddCountersSourceEffect(CounterType.P1P1.createInstance()),
            StaticFilters.FILTER_ANOTHER_CREATURE_YOU_CONTROL);
        setTriggerPhrase("Whenever another creature you control enters, if it has greater power than {this}, ");
    }

    private EnsignHarryKimOperationsTriggeredAbility(final EnsignHarryKimOperationsTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public EnsignHarryKimOperationsTriggeredAbility copy() {
        return new EnsignHarryKimOperationsTriggeredAbility(this);
    }

    @Override
    public boolean checkInterveningIfClause(Game game) {
        Permanent permanent = getSourcePermanentOrLKI(game);
        return permanent != null
            && CardUtil.getEffectValueFromAbility(this, "permanentEnteringBattlefield", Permanent.class)
                .filter(entering -> entering.getPower().getValue() > permanent.getPower().getValue())
                .isPresent();
    }
}
