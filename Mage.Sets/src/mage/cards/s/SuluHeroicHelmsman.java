package mage.cards.s;

import mage.MageInt;
import mage.abilities.common.CrewStationTripledPowerAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;

import java.util.UUID;

/**
 * @author muz
 */
public final class SuluHeroicHelmsman extends CardImpl {

    public SuluHeroicHelmsman(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.PILOT);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Sulu stations Spacecraft and crews Vehicles as though his power were tripled.
        this.addAbility(new CrewStationTripledPowerAbility());
    }

    private SuluHeroicHelmsman(final SuluHeroicHelmsman card) {
        super(card);
    }

    @Override
    public SuluHeroicHelmsman copy() {
        return new SuluHeroicHelmsman(this);
    }
}
