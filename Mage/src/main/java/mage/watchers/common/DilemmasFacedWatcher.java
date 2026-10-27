package mage.watchers.common;

import mage.constants.WatcherScope;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.watchers.Watcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author muz
 */
public class DilemmasFacedWatcher extends Watcher {

    private final Map<UUID, Integer> counts = new HashMap<>();

    public DilemmasFacedWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.FACED_DILEMMA) {
            counts.merge(event.getPlayerId(), 1, Integer::sum);
        }
    }

    public int getCount(UUID playerId) {
        return counts.getOrDefault(playerId, 0);
    }

    @Override
    public void reset() {
        super.reset();
        counts.clear();
    }
}
