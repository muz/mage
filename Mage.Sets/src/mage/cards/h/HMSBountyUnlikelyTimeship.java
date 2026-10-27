package mage.cards.h;

import java.util.HashSet;
import java.util.Set;
import mage.abilities.Ability;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.condition.Condition;
import mage.abilities.decorator.ConditionalOneShotEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.search.SearchLibraryPutInHandEffect;
import mage.abilities.effects.common.turn.AddExtraTurnControllerEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.StationAbility;
import mage.abilities.keyword.StationLevelAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.WatcherScope;
import mage.filter.FilterCard;
import mage.filter.common.FilterControlledPermanent;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.target.common.TargetCardInLibrary;
import mage.watchers.Watcher;

import java.util.UUID;

/**
 * @author muz
 */
public final class HMSBountyUnlikelyTimeship extends CardImpl {

    private static final FilterCard filter = new FilterCard("Whale card");

    static {
        filter.add(SubType.WHALE.getPredicate());
    }

    public HMSBountyUnlikelyTimeship(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{4}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SPACECRAFT);

        // When H.M.S. Bounty enters, you may search your library for a Whale card, reveal it, put it into your hand, then shuffle.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new SearchLibraryPutInHandEffect(new TargetCardInLibrary(filter), true), true));

        // Station
        this.addAbility(new StationAbility());

        // STATION 8+
        // Flying
        // Whenever H.M.S. Bounty deals combat damage to a player, draw a card. If you control a Whale and haven't begun an extra turn this game, take an extra turn after this one.
        // 3/4
        Ability ability = new DealsCombatDamageToAPlayerTriggeredAbility(new DrawCardSourceControllerEffect(1));
        ability.addEffect(new ConditionalOneShotEffect(new AddExtraTurnControllerEffect(), HMSBountyUnlikelyTimeshipCondition.instance,
            "if you control a Whale and haven't begun an extra turn this game, take an extra turn after this one"));
        this.addAbility(new StationLevelAbility(8).withLevelAbility(FlyingAbility.getInstance()).withLevelAbility(ability).withPT(3, 4),
            new HMSBountyUnlikelyTimeshipWatcher());
    }

    private HMSBountyUnlikelyTimeship(final HMSBountyUnlikelyTimeship card) {
        super(card);
    }

    @Override
    public HMSBountyUnlikelyTimeship copy() {
        return new HMSBountyUnlikelyTimeship(this);
    }
}

enum HMSBountyUnlikelyTimeshipCondition implements Condition {
    instance;

    private static final FilterControlledPermanent filter = new FilterControlledPermanent("a Whale");

    static {
        filter.add(SubType.WHALE.getPredicate());
    }

    @Override
    public boolean apply(Game game, Ability source) {
        HMSBountyUnlikelyTimeshipWatcher watcher = game.getState().getWatcher(HMSBountyUnlikelyTimeshipWatcher.class);
        return watcher != null && !watcher.hasBegunExtraTurn(source.getControllerId())
            && game.getBattlefield().contains(filter, source.getControllerId(), source, game, 1);
    }
}

class HMSBountyUnlikelyTimeshipWatcher extends Watcher {

    private final Set<UUID> players = new HashSet<>();

    HMSBountyUnlikelyTimeshipWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.BEGIN_TURN && game.getState().isExtraTurn()) {
            players.add(event.getPlayerId());
        }
    }

    boolean hasBegunExtraTurn(UUID playerId) {
        return players.contains(playerId);
    }
}
