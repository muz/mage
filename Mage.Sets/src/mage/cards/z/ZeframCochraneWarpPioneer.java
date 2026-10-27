package mage.cards.z;

import mage.MageIdentifier;
import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.EntersWithCountersControlledEffect;
import mage.abilities.effects.common.continuous.LookAtTopCardOfLibraryAnyTimeEffect;
import mage.abilities.effects.common.continuous.PlayFromTopOfLibraryEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterArtifactCard;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.watchers.common.OnceEachTurnCastWatcher;

import java.util.UUID;

/**
 * @author muz
 */
public final class ZeframCochraneWarpPioneer extends CardImpl {

    private static final FilterPermanent filter = new FilterPermanent("Spacecraft");

    static {
        filter.add(SubType.SPACECRAFT.getPredicate());
    }

    public ZeframCochraneWarpPioneer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.PILOT);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // You may look at the top card of your library any time.
        this.addAbility(new SimpleStaticAbility(new LookAtTopCardOfLibraryAnyTimeEffect()));


        // Once during each of your turns, you may cast an artifact spell from the top of your library.
        Ability ability = new SimpleStaticAbility(new ZeframCochraneWarpPioneerEffect()).setIdentifier(MageIdentifier.OnceEachTurnCastWatcher)
            .addHint(OnceEachTurnCastWatcher.getHint());
        this.addAbility(ability, new OnceEachTurnCastWatcher());

        // Spacecraft you control enter with two additional charge counters on them.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersControlledEffect(filter, CounterType.CHARGE.createInstance(2), false)
            .setText("Spacecraft you control enter with two additional charge counters on them")));
    }

    private ZeframCochraneWarpPioneer(final ZeframCochraneWarpPioneer card) {
        super(card);
    }

    @Override
    public ZeframCochraneWarpPioneer copy() {
        return new ZeframCochraneWarpPioneer(this);
    }
}

class ZeframCochraneWarpPioneerEffect extends PlayFromTopOfLibraryEffect {

    private static final FilterArtifactCard filter = new FilterArtifactCard("cast artifact spells");

    ZeframCochraneWarpPioneerEffect() {
        super(filter);
        staticText = "once during each of your turns, you may cast an artifact spell from the top of your library";
    }

    private ZeframCochraneWarpPioneerEffect(final ZeframCochraneWarpPioneerEffect effect) {
        super(effect);
    }

    @Override
    public ZeframCochraneWarpPioneerEffect copy() {
        return new ZeframCochraneWarpPioneerEffect(this);
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
