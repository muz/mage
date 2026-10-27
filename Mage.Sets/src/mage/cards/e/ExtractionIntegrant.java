package mage.cards.e;

import mage.MageInt;
import mage.abilities.CompoundAbility;
import mage.abilities.common.BecomesBlockedByCreatureTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.BoostTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.keyword.MenaceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 * @author muz
 */
public final class ExtractionIntegrant extends CardImpl {

    public ExtractionIntegrant(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{1}{B}");

        this.subtype.add(SubType.BORG);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Artifact creatures you control have menace and "Whenever a creature blocks this creature, that creature gets -1/-1 until end of turn."
        this.addAbility(new SimpleStaticAbility(new GainAbilityControlledEffect(new CompoundAbility(
            MenaceAbility.getInstance(), new BecomesBlockedByCreatureTriggeredAbility(
            new BoostTargetEffect(-1, -1).setText("that creature gets -1/-1 until end of turn"), false
        ).setTriggerPhrase("Whenever a creature blocks {this}, ")
        ), Duration.WhileOnBattlefield, StaticFilters.FILTER_PERMANENTS_ARTIFACT_CREATURE)));
    }

    private ExtractionIntegrant(final ExtractionIntegrant card) {
        super(card);
    }

    @Override
    public ExtractionIntegrant copy() {
        return new ExtractionIntegrant(this);
    }
}
