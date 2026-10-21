package mage.cards.t;

import java.util.HashSet;
import java.util.Set;
import mage.ApprovingObject;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.PlayLandAbility;
import mage.abilities.SpellAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.hint.common.OpenSideboardHint;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AsThoughEffectType;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SuperType;
import mage.constants.WatcherScope;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.watchers.Watcher;

import java.util.UUID;

/**
 * @author muz
 */
public final class TheHolodeck extends CardImpl {

    public TheHolodeck(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{7}");

        this.supertype.add(SuperType.LEGENDARY);

        // Once during each of your turns, you may play a permanent card you own from outside the game or from your graveyard.
        this.addAbility(new SimpleStaticAbility(new TheHolodeckEffect()).addHint(OpenSideboardHint.instance), new TheHolodeckWatcher());
    }

    private TheHolodeck(final TheHolodeck card) {
        super(card);
    }

    @Override
    public TheHolodeck copy() {
        return new TheHolodeck(this);
    }
}

class TheHolodeckEffect extends AsThoughEffectImpl {

    TheHolodeckEffect() {
        super(AsThoughEffectType.PLAY_FROM_NOT_OWN_HAND_ZONE, Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "Once during each of your turns, you may play a permanent card you own from outside the game or from your graveyard";
    }

    private TheHolodeckEffect(final TheHolodeckEffect effect) {
        super(effect);
    }

    @Override
    public TheHolodeckEffect copy() {
        return new TheHolodeckEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID playerId, Game game) {
        return false;
    }

    @Override
    public boolean applies(UUID objectId, Ability affectedAbility, Ability source, Game game, UUID playerId) {
        Card card = game.getCard(objectId);
        Player player = game.getPlayer(playerId);
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        TheHolodeckWatcher watcher = game.getState().getWatcher(TheHolodeckWatcher.class);
        if (card == null || player == null || permanent == null || watcher == null
            || !source.isControlledBy(playerId) || !game.isActivePlayer(playerId) || !card.isOwnedBy(playerId)
            || watcher.isUsed(new MageObjectReference(permanent, game))) {
            return false;
        }
        UUID mainCardId = card.getMainCard().getId();
        if (!player.getGraveyard().contains(mainCardId) && !player.getSideboard().contains(mainCardId)) {
            return false;
        }
        if (affectedAbility instanceof SpellAbility) {
            SpellAbility spellAbility = (SpellAbility) affectedAbility;
            return !spellAbility.getManaCosts().isEmpty() && spellAbility.getCharacteristics(game).isPermanent(game);
        }
        return affectedAbility instanceof PlayLandAbility && card.isLand(game);
    }
}

class TheHolodeckWatcher extends Watcher {

    private final Set<MageObjectReference> used = new HashSet<>();

    TheHolodeckWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() != GameEvent.EventType.SPELL_CAST && event.getType() != GameEvent.EventType.LAND_PLAYED) {
            return;
        }
        ApprovingObject approver = event.getApprovingObject();
        if (approver != null && approver.getApprovingAbility().getEffects().stream().anyMatch(TheHolodeckEffect.class::isInstance)) {
            used.add(approver.getApprovingMageObjectReference());
        }
    }

    @Override
    public void reset() {
        super.reset();
        used.clear();
    }

    boolean isUsed(MageObjectReference permanent) {
        return used.contains(permanent);
    }
}
