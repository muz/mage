package mage.cards.u;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.ReturnFromGraveyardToBattlefieldWithCounterTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.StationAbility;
import mage.abilities.keyword.StationLevelAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ComparisonType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.common.FilterCreatureCard;
import mage.filter.predicate.mageobject.ManaValuePredicate;
import mage.game.Game;
import mage.game.events.DamagedEvent;
import mage.game.events.GameEvent;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 * @author muz
 */
public final class USSReliantFromHellsHeart extends CardImpl {

    public USSReliantFromHellsHeart(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}{B}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SPACECRAFT);

        // Whenever U.S.S. Reliant enters or deals combat damage to a player, return target creature card with mana value 4 or less from your graveyard to the battlefield with a finality counter on it.
        this.addAbility(new USSReliantFromHellsHeartTriggeredAbility());

        // Station
        this.addAbility(new StationAbility());

        // STATION 10+
        // Flying
        // 4/3
        this.addAbility(new StationLevelAbility(10).withLevelAbility(FlyingAbility.getInstance()).withPT(4, 3));
    }

    private USSReliantFromHellsHeart(final USSReliantFromHellsHeart card) {
        super(card);
    }

    @Override
    public USSReliantFromHellsHeart copy() {
        return new USSReliantFromHellsHeart(this);
    }
}

class USSReliantFromHellsHeartTriggeredAbility extends TriggeredAbilityImpl {

    private static final FilterCreatureCard filter = new FilterCreatureCard("creature card with mana value 4 or less from your graveyard");

    static {
        filter.add(new ManaValuePredicate(ComparisonType.FEWER_THAN, 5));
    }

    USSReliantFromHellsHeartTriggeredAbility() {
        super(Zone.BATTLEFIELD, new ReturnFromGraveyardToBattlefieldWithCounterTargetEffect(CounterType.FINALITY.createInstance()));
        addTarget(new TargetCardInYourGraveyard(filter));
        setTriggerPhrase("Whenever {this} enters or deals combat damage to a player, ");
    }

    private USSReliantFromHellsHeartTriggeredAbility(final USSReliantFromHellsHeartTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public USSReliantFromHellsHeartTriggeredAbility copy() {
        return new USSReliantFromHellsHeartTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD
            || event.getType() == GameEvent.EventType.DAMAGED_PLAYER;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD) {
            return event.getTargetId().equals(getSourceId());
        }
        return event.getSourceId().equals(getSourceId()) && ((DamagedEvent) event).isCombatDamage();
    }
}
