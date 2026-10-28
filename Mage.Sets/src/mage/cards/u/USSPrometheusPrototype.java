package mage.cards.u;

import java.util.UUID;
import mage.abilities.common.DealsDamageToOpponentTriggeredAbility;
import mage.abilities.effects.common.DamageEachOtherOpponentThatMuchEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.StationAbility;
import mage.abilities.keyword.StationLevelAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;

/**
 * @author muz
 */
public final class USSPrometheusPrototype extends CardImpl {

    public USSPrometheusPrototype(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{5}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SPACECRAFT);

        // Station
        this.addAbility(new StationAbility());

        // STATION 4+
        // Flying
        this.addAbility(new StationLevelAbility(4).withLevelAbility(FlyingAbility.getInstance()));

        // STATION 8+
        // Multivector Assault Mode -- Whenever U.S.S. Prometheus deals combat damage to an opponent, it deals that much damage to each other opponent.
        // 6/6
        this.addAbility(new StationLevelAbility(8).withLevelAbility(
            new DealsDamageToOpponentTriggeredAbility(new DamageEachOtherOpponentThatMuchEffect(), false, true, true)
                .withFlavorWord("Multivector Assault Mode")).withPT(6, 6));
    }

    private USSPrometheusPrototype(final USSPrometheusPrototype card) {
        super(card);
    }

    @Override
    public USSPrometheusPrototype copy() {
        return new USSPrometheusPrototype(this);
    }
}
