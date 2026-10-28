package mage.abilities.common;

import mage.abilities.StaticAbility;
import mage.abilities.effects.common.InfoEffect;
import mage.constants.Zone;

/**
 * @author muz
 */
public class CrewStationTripledPowerAbility extends StaticAbility {

    public CrewStationTripledPowerAbility() {
        super(Zone.BATTLEFIELD, new InfoEffect(
            "{this} stations Spacecraft and crews Vehicles as though its power were tripled."
        ));
    }

    private CrewStationTripledPowerAbility(final CrewStationTripledPowerAbility ability) {
        super(ability);
    }

    @Override
    public CrewStationTripledPowerAbility copy() {
        return new CrewStationTripledPowerAbility(this);
    }
}
