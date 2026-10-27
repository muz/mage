package mage.cards.m;

import java.util.HashSet;
import java.util.Set;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.condition.Condition;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.ReturnToHandTargetEffect;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.TargetController;
import mage.constants.WatcherScope;
import mage.constants.Zone;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.target.TargetPermanent;
import mage.watchers.Watcher;

import java.util.UUID;

/**
 * @author muz
 */
public final class MilesOBrienTransporterOps extends CardImpl {

    private static final FilterControlledPermanent filter = new FilterControlledPermanent("other creature or artifact you control");

    static {
        filter.add(Predicates.or(CardType.CREATURE.getPredicate(), CardType.ARTIFACT.getPredicate()));
        filter.add(AnotherPredicate.instance);
    }

    public MilesOBrienTransporterOps(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // When Miles O'Brien enters, return up to one other target creature or artifact you control to its owner's hand.
        Ability ability = new EntersBattlefieldTriggeredAbility(new ReturnToHandTargetEffect());
        ability.addTarget(new TargetPermanent(0, 1, filter));
        this.addAbility(ability);

        // At the beginning of each end step, if a permanent was returned to your hand from the battlefield this turn, draw a card.
        this.addAbility(new BeginningOfEndStepTriggeredAbility(TargetController.ANY, new DrawCardSourceControllerEffect(1), false)
            .withInterveningIf(MilesOBrienTransporterOpsCondition.instance), new MilesOBrienTransporterOpsWatcher());
    }

    private MilesOBrienTransporterOps(final MilesOBrienTransporterOps card) {
        super(card);
    }

    @Override
    public MilesOBrienTransporterOps copy() {
        return new MilesOBrienTransporterOps(this);
    }
}

enum MilesOBrienTransporterOpsCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        MilesOBrienTransporterOpsWatcher watcher = game.getState().getWatcher(MilesOBrienTransporterOpsWatcher.class);
        return watcher != null && watcher.hasReturnedPermanent(source.getControllerId());
    }

    @Override
    public String toString() {
        return "a permanent was returned to your hand from the battlefield this turn";
    }
}

class MilesOBrienTransporterOpsWatcher extends Watcher {

    private final Set<UUID> players = new HashSet<>();

    MilesOBrienTransporterOpsWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() != GameEvent.EventType.ZONE_CHANGE) {
            return;
        }
        ZoneChangeEvent zEvent = (ZoneChangeEvent) event;
        if (zEvent.getFromZone() == Zone.BATTLEFIELD && zEvent.getToZone() == Zone.HAND && zEvent.isPermanentMoved()) {
            players.add(zEvent.getTarget().getOwnerId());
        }
    }

    boolean hasReturnedPermanent(UUID playerId) {
        return players.contains(playerId);
    }

    @Override
    public void reset() {
        super.reset();
        players.clear();
    }
}
