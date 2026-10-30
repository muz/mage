package mage.cards.j;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldOrAttacksAllTriggeredAbility;
import mage.abilities.effects.common.RevealCardsFromLibraryUntilEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.PutCards;
import mage.constants.SetTargetPointer;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.mageobject.CommanderPredicate;

/**
 * @author muz
 */
public final class JaniceRandEssentialAide extends CardImpl {

    private static final FilterControlledPermanent filter = new FilterControlledPermanent("a commander you control");

    static {
        filter.add(CommanderPredicate.instance);
    }

    public JaniceRandEssentialAide(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Whenever a commander you control enters or attacks, put a +1/+1 counter on it, then reveal cards
        // from the top of your library until you reveal a land card. Put that card onto the battlefield tapped
        // and the rest on the bottom of your library in a random order.
        Ability ability = new EntersBattlefieldOrAttacksAllTriggeredAbility(Zone.BATTLEFIELD,
            new AddCountersTargetEffect(CounterType.P1P1.createInstance()).setText("put a +1/+1 counter on it"),
            filter, false, SetTargetPointer.PERMANENT, null);
        ability.addEffect(new RevealCardsFromLibraryUntilEffect(
            StaticFilters.FILTER_CARD_LAND_A, PutCards.BATTLEFIELD_TAPPED, PutCards.BOTTOM_RANDOM).concatBy(", then"));
        this.addAbility(ability);
    }

    private JaniceRandEssentialAide(final JaniceRandEssentialAide card) {
        super(card);
    }

    @Override
    public JaniceRandEssentialAide copy() {
        return new JaniceRandEssentialAide(this);
    }
}
