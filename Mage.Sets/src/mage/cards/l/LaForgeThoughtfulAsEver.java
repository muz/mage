package mage.cards.l;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.WatcherScope;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.permanent.PermanentToken;
import mage.game.permanent.token.FaerieToken;
import mage.watchers.Watcher;

/**
 * @author muz
 */
public final class LaForgeThoughtfulAsEver extends CardImpl {

    public LaForgeThoughtfulAsEver(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // {T}: For each permanent you control that you've put a counter on this turn, create a 1/1 blue Faerie creature token with flying.
        this.addAbility(new SimpleActivatedAbility(new LaForgeThoughtfulAsEverEffect(), new TapSourceCost()),
            new LaForgeThoughtfulAsEverWatcher());
    }

    private LaForgeThoughtfulAsEver(final LaForgeThoughtfulAsEver card) {
        super(card);
    }

    @Override
    public LaForgeThoughtfulAsEver copy() {
        return new LaForgeThoughtfulAsEver(this);
    }
}

class LaForgeThoughtfulAsEverEffect extends OneShotEffect {

    LaForgeThoughtfulAsEverEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "for each permanent you control that you've put a counter on this turn, "
            + "create a 1/1 blue Faerie creature token with flying";
    }

    private LaForgeThoughtfulAsEverEffect(final LaForgeThoughtfulAsEverEffect effect) {
        super(effect);
    }

    @Override
    public LaForgeThoughtfulAsEverEffect copy() {
        return new LaForgeThoughtfulAsEverEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        LaForgeThoughtfulAsEverWatcher watcher = game.getState().getWatcher(LaForgeThoughtfulAsEverWatcher.class);
        int count = watcher == null ? 0 : watcher.getCount(source.getControllerId(), game);
        return new CreateTokenEffect(new FaerieToken(), count).apply(game, source);
    }
}

class LaForgeThoughtfulAsEverWatcher extends Watcher {

    private final Map<UUID, Set<MageObjectReference>> permanents = new HashMap<>();

    LaForgeThoughtfulAsEverWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() != GameEvent.EventType.COUNTERS_ADDED || event.getAmount() <= 0) {
            return;
        }
        Permanent permanent = game.getPermanent(event.getTargetId());
        int offset = 0;
        if (permanent == null) {
            permanent = game.getPermanentEntering(event.getTargetId());
            offset = permanent instanceof PermanentToken ? 0 : 1;
        }
        if (permanent != null) {
            permanents.computeIfAbsent(event.getPlayerId(), key -> new HashSet<>())
                .add(new MageObjectReference(permanent, game, offset));
        }
    }

    int getCount(UUID playerId, Game game) {
        return (int) permanents.getOrDefault(playerId, Collections.emptySet()).stream()
            .map(reference -> reference.getPermanent(game))
            .filter(Objects::nonNull)
            .filter(permanent -> permanent.isControlledBy(playerId))
            .count();
    }

    @Override
    public void reset() {
        super.reset();
        permanents.clear();
    }
}
