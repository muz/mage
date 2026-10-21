package mage.cards.b;

import mage.MageInt;
import mage.abilities.common.BecomesTappedSourceTriggeredAbility;
import mage.abilities.effects.common.replacement.ModifyCountersAddedEffect;
import mage.abilities.keyword.LifelinkAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author muz
 */
public final class BradBoimlerEagerEnsign extends CardImpl {

    public BradBoimlerEagerEnsign(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Lifelink
        this.addAbility(LifelinkAbility.getInstance());

        // Whenever Brad Boimler becomes tapped, until end of turn, if one or more counters would be put on a permanent you control, that many plus one of each of those kinds of counters are put on that permanent instead.
        this.addAbility(new BecomesTappedSourceTriggeredAbility(new ModifyCountersAddedEffect(
            StaticFilters.FILTER_CONTROLLED_PERMANENT, null
        ).setDuration(Duration.EndOfTurn).setText("until end of turn, if one or more counters would be put on "
            + "a permanent you control, that many plus one of each of those kinds of counters are put on that permanent instead")));
    }

    private BradBoimlerEagerEnsign(final BradBoimlerEagerEnsign card) {
        super(card);
    }

    @Override
    public BradBoimlerEagerEnsign copy() {
        return new BradBoimlerEagerEnsign(this);
    }
}
