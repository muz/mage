package mage.cards.o;

import mage.MageIdentifier;
import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.counter.AddCounterEnteringCreatureEffect;
import mage.abilities.effects.common.ruleModifying.PlayFromGraveyardControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.WatcherScope;
import mage.counters.CounterType;
import mage.filter.common.FilterArtifactCard;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.watchers.Watcher;
import mage.watchers.common.OnceEachTurnCastWatcher;

import java.util.UUID;

/**
 * @author muz
 */
public final class OverflowSubmatrix extends CardImpl {

    public OverflowSubmatrix(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{2}{B}");

        this.subtype.add(SubType.BORG);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Once during each of your turns, you may cast an artifact creature spell from your graveyard.
        Ability ability = new SimpleStaticAbility(new OverflowSubmatrixEffect()).setIdentifier(MageIdentifier.OnceEachTurnCastWatcher)
            .addHint(OnceEachTurnCastWatcher.getHint());
        ability.addWatcher(new OverflowSubmatrixWatcher());
        this.addAbility(ability, new OnceEachTurnCastWatcher());
    }

    private OverflowSubmatrix(final OverflowSubmatrix card) {
        super(card);
    }

    @Override
    public OverflowSubmatrix copy() {
        return new OverflowSubmatrix(this);
    }
}

class OverflowSubmatrixEffect extends PlayFromGraveyardControllerEffect {

    private static final FilterArtifactCard filter = new FilterArtifactCard("cast artifact creature spells");

    static {
        filter.add(CardType.CREATURE.getPredicate());
    }

    OverflowSubmatrixEffect() {
        super(filter);
        staticText = "once during each of your turns, you may cast an artifact creature spell from your graveyard"
            + ". If you cast a spell this way, that artifact creature enters with a finality counter on it";
    }

    private OverflowSubmatrixEffect(final OverflowSubmatrixEffect effect) {
        super(effect);
    }

    @Override
    public OverflowSubmatrixEffect copy() {
        return new OverflowSubmatrixEffect(this);
    }

    @Override
    public boolean applies(UUID objectId, Ability affectedAbility, Ability source, Game game, UUID playerId) {
        OnceEachTurnCastWatcher watcher = game.getState().getWatcher(OnceEachTurnCastWatcher.class);
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        return affectedAbility instanceof SpellAbility && game.isActivePlayer(playerId) && permanent != null && watcher != null
            && !watcher.isAbilityUsed(playerId, new MageObjectReference(permanent, game))
            && super.applies(objectId, affectedAbility, source, game, playerId);
    }
}

class OverflowSubmatrixWatcher extends Watcher {

    OverflowSubmatrixWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() != GameEvent.EventType.SPELL_CAST || event.getApprovingObject() == null
            || event.getApprovingObject().getApprovingAbility().getEffects().stream().noneMatch(OverflowSubmatrixEffect.class::isInstance)) {
            return;
        }
        Spell spell = game.getSpell(event.getTargetId());
        if (spell != null) {
            game.getState().addEffect(new AddCounterEnteringCreatureEffect(new MageObjectReference(spell.getCard(), game),
                CounterType.FINALITY.createInstance(), Outcome.BoostCreature), spell.getSpellAbility());
        }
    }
}
