package mage.cards.w;

import java.util.UUID;
import mage.constants.SuperType;
import mage.abilities.common.ActivatePlaneswalkerLoyaltyAbilityTriggeredAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.keyword.EmpowerJaceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SetTargetPointer;

/**
 *
 * @author muz
 */
public final class WayOfTheMindSculptor extends CardImpl {

    public WayOfTheMindSculptor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{4}{U}");
        
        this.supertype.add(SuperType.LEGENDARY);

        // When Way of the Mind Sculptor enters, empower Jace 5.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new EmpowerJaceEffect(5)));

        // Whenever you activate a loyalty ability, if you removed two or more loyalty counters to activate it, draw a card.
        this.addAbility(new ActivatePlaneswalkerLoyaltyAbilityTriggeredAbility(
            new DrawCardSourceControllerEffect(1),
            SetTargetPointer.NONE
        ).withInterveningIf(null)); // TODO: Need a custom condition to check if 2 or more loyalty counters were removed.
    }

    private WayOfTheMindSculptor(final WayOfTheMindSculptor card) {
        super(card);
    }

    @Override
    public WayOfTheMindSculptor copy() {
        return new WayOfTheMindSculptor(this);
    }
}
