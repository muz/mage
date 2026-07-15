package mage.abilities.effects.common.asthought;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.cards.PrepareSpellCopyCard;
import mage.constants.AsThoughEffectType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.PrepareCopyInfo;
import mage.game.permanent.Permanent;

import java.util.Objects;
import java.util.UUID;

public class PrepareCastFromExileEffect extends AsThoughEffectImpl {

    private final MageObjectReference sourcePermanentReference;
    private final UUID copyId;

    public PrepareCastFromExileEffect(MageObjectReference sourcePermanentReference, UUID copyId) {
        super(AsThoughEffectType.CAST_FROM_NOT_OWN_HAND_ZONE, Duration.Custom, Outcome.Benefit);
        this.sourcePermanentReference = Objects.requireNonNull(sourcePermanentReference);
        this.copyId = Objects.requireNonNull(copyId);
        this.staticText = "You may cast the prepare spell copy from exile";
    }

    private PrepareCastFromExileEffect(final PrepareCastFromExileEffect effect) {
        super(effect);
        this.sourcePermanentReference = effect.sourcePermanentReference;
        this.copyId = effect.copyId;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public PrepareCastFromExileEffect copy() {
        return new PrepareCastFromExileEffect(this);
    }

    public UUID getCopyId() {
        return copyId;
    }

    @Override
    public boolean isInactive(Ability source, Game game) {
        return !hasLiveLinkedCopy(game);
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID affectedControllerId, Game game) {
        return false;
    }

    @Override
    public boolean applies(UUID objectId, Ability affectedAbility, Ability source, Game game, UUID playerId) {
        if (!copyId.equals(objectId)) {
            return false;
        }
        if (affectedAbility == null || !copyId.equals(affectedAbility.getSourceId())) {
            return false;
        }
        if (!hasLiveLinkedCopy(game)) {
            this.discard();
            return false;
        }

        Permanent sourcePermanent = sourcePermanentReference.getPermanent(game);
        return sourcePermanent != null
                && playerId != null
                && playerId.equals(sourcePermanent.getControllerId());
    }

    private boolean hasLiveLinkedCopy(Game game) {
        PrepareCopyInfo info = game.getState().findPrepareCopyInfoByCopyId(copyId).orElse(null);
        if (info == null || !sourcePermanentReference.equals(info.getSourcePermanentReference())) {
            return false;
        }
        if (!(game.getState().getCopiedCard(copyId) instanceof PrepareSpellCopyCard)) {
            return false;
        }
        if (game.getState().getZone(copyId) != Zone.EXILED) {
            return false;
        }
        ExileZone exileZone = game.getExile().getExileZone(info.getExileZoneId());
        if (exileZone == null || !exileZone.contains(copyId)) {
            return false;
        }
        Permanent sourcePermanent = sourcePermanentReference.getPermanent(game);
        // CR 722.3c: only the current controller of the phased-in prepared source may cast the live exile copy.
        return sourcePermanent != null
                && sourcePermanent.isPrepared()
                && sourcePermanent.isPhasedIn();
    }
}
