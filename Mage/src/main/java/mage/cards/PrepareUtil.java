package mage.cards;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.AsThoughEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.effects.common.asthought.PrepareCastFromExileEffect;
import mage.constants.AsThoughEffectType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.PrepareCopyInfo;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.permanent.Permanent;
import mage.game.permanent.PermanentCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PrepareUtil {

    private PrepareUtil() {
    }

    public static Optional<PrepareSpellCharacteristics> getPrepareSpellCharacteristics(Card card) {
        if (card instanceof PrepareCard) {
            return Optional.of(new PrepareSpellCharacteristics(((PrepareCard) card).getSpellCard()));
        }
        return Optional.empty();
    }

    public static Optional<PrepareSpellCharacteristics> getPrepareSpellCharacteristics(Permanent permanent, Game game) {
        if (permanent instanceof PermanentCard) {
            return getPrepareSpellCharacteristics(((PermanentCard) permanent).getCard());
        }
        return Optional.empty();
    }

    public static PrepareSpellCopyCard createPrepareSpellCopy(PrepareSpellCharacteristics characteristics, UUID copyCreatorId) {
        // CR 722.3c: Prepare creates a copy with only the prepare spell characteristics.
        return new PrepareSpellCopyCard(copyCreatorId, characteristics);
    }

    public static boolean setPrepared(Permanent permanent, boolean prepared, Ability source, Game game) {
        if (permanent == null || game == null) {
            return false;
        }
        if (!prepared) {
            clearPrepared(permanent, game);
            return true;
        }
        if (source == null) {
            return false;
        }

        Optional<PrepareSpellCharacteristics> characteristics = getPrepareSpellCharacteristics(permanent, game);
        if (!characteristics.isPresent()) {
            return true;
        }

        MageObjectReference sourcePermanentReference = new MageObjectReference(permanent, game);
        if (permanent.isPrepared()) {
            removeStalePrepareCopy(sourcePermanentReference, game);
            return true;
        }

        permanent.setPrepared(true, game);
        if (!permanent.isPrepared()) {
            return true;
        }

        createPrepareCopy(permanent, sourcePermanentReference, characteristics.get(), source, game);
        return true;
    }

    private static void createPrepareCopy(Permanent permanent, MageObjectReference sourcePermanentReference,
                                          PrepareSpellCharacteristics characteristics, Ability source, Game game) {
        removePrepareCopy(sourcePermanentReference, game);
        UUID copyCreatorId = permanent.getControllerId();
        PrepareSpellCopyCard copyCard = createPrepareSpellCopy(characteristics, copyCreatorId);
        PrepareCopyInfo info = new PrepareCopyInfo(
                sourcePermanentReference,
                copyCard.getId(),
                game.getExile().getPermanentExile().getId(),
                copyCreatorId
        );
        game.getState().registerPrepareSpellCopy(copyCard, info);
        // CR 722.3c: the source permanent's controller may cast the live copy from exile.
        game.addEffect(new PrepareCastFromExileEffect(sourcePermanentReference, copyCard.getId()), source);
        recordPrepareCastPermissionEffectId(info, game);
    }

    public static boolean consumePrepareSpellCast(GameEvent event, Game game) {
        if (event == null
                || game == null
                || !GameEvent.EventType.SPELL_CAST.equals(event.getType())
                || event.getSourceId() == null) {
            return false;
        }

        UUID copyId = event.getSourceId();
        if (!(game.getState().getCopiedCard(copyId) instanceof PrepareSpellCopyCard)) {
            return false;
        }
        PrepareCopyInfo info = game.getState().findPrepareCopyInfoByCopyId(copyId).orElse(null);
        if (info == null) {
            return false;
        }

        Permanent permanent = info.getSourcePermanentReference().getPermanent(game);
        if (permanent != null && permanent.isPrepared()) {
            // CR 722.3c / 601.2i: once the prepare copy becomes cast, the source loses prepared.
            permanent.setPrepared(false, game);
        }
        game.getState().removePrepareCopyInfo(info.getSourcePermanentReference());
        discardPrepareCastPermission(copyId, game);
        return true;
    }

    public static boolean handlePrepareZoneChange(GameEvent event, Game game) {
        if (!(event instanceof ZoneChangeEvent) || game == null) {
            return false;
        }
        ZoneChangeEvent zoneChangeEvent = (ZoneChangeEvent) event;
        if (!Zone.BATTLEFIELD.match(zoneChangeEvent.getFromZone())
                || Zone.BATTLEFIELD.match(zoneChangeEvent.getToZone())) {
            return false;
        }

        boolean removed = false;
        for (PrepareCopyInfo info : new ArrayList<>(game.getState().getPrepareCopyInfos())) {
            if (zoneChangeEvent.getTarget() == null
                    || !info.getSourcePermanentReference().refersTo(zoneChangeEvent.getTarget(), game)) {
                continue;
            }
            // CR 722.3c: the exile copy exists only while the linked source remains on the battlefield.
            removePrepareCopy(info, game);
            removed = true;
        }
        return removed;
    }

    public static boolean handlePreparePhaseChange(GameEvent event, Game game) {
        if (event == null || game == null || event.getTargetId() == null) {
            return false;
        }
        if (!GameEvent.EventType.PHASED_OUT.equals(event.getType())
                && !GameEvent.EventType.PHASED_IN.equals(event.getType())) {
            return false;
        }

        Permanent permanent = game.getPermanent(event.getTargetId());
        if (permanent == null) {
            return false;
        }
        MageObjectReference sourcePermanentReference = new MageObjectReference(permanent, game);
        if (GameEvent.EventType.PHASED_OUT.equals(event.getType())) {
            // CR 722.3c: phased-out permanents are not available to keep the exile copy alive.
            return removePrepareCopy(sourcePermanentReference, game);
        }
        if (!permanent.isPrepared()) {
            return false;
        }
        PrepareCopyInfo existingInfo = game.getState().getPrepareCopyInfo(sourcePermanentReference);
        if (existingInfo != null && game.getState().isLivePrepareSpellCopy(existingInfo.getCopyId(), game)) {
            return false;
        }
        Optional<PrepareSpellCharacteristics> characteristics = getPrepareSpellCharacteristics(permanent, game);
        if (!characteristics.isPresent()) {
            return false;
        }
        // CR 722.3c: when a prepared permanent phases in, its controller creates a fresh copy.
        createPrepareCopy(permanent, sourcePermanentReference, characteristics.get(), createPreparePermissionSource(permanent), game);
        return true;
    }

    private static void clearPrepared(Permanent permanent, Game game) {
        MageObjectReference sourcePermanentReference = new MageObjectReference(permanent, game);
        permanent.setPrepared(false, game);
        removePrepareCopy(sourcePermanentReference, game);
    }

    private static void removeStalePrepareCopy(MageObjectReference sourcePermanentReference, Game game) {
        PrepareCopyInfo info = game.getState().getPrepareCopyInfo(sourcePermanentReference);
        if (info != null && !game.getState().isLivePrepareSpellCopy(info.getCopyId(), game)) {
            removePrepareCopy(sourcePermanentReference, game);
        }
    }

    private static boolean removePrepareCopy(MageObjectReference sourcePermanentReference, Game game) {
        PrepareCopyInfo info = game.getState().removePrepareCopyInfo(sourcePermanentReference);
        if (info == null) {
            return false;
        }
        removePrepareCopy(info, game);
        return true;
    }

    private static void removePrepareCopy(PrepareCopyInfo info, Game game) {
        game.getState().removePrepareCopyInfo(info.getSourcePermanentReference());
        discardPrepareCastPermission(info.getCopyId(), game);

        Zone zone = game.getState().getZone(info.getCopyId());
        if (Zone.STACK.equals(zone)) {
            return;
        }
        if (zone != null && !Zone.EXILED.equals(zone) && !Zone.OUTSIDE.equals(zone)) {
            return;
        }

        Card copyCard = game.getState().removePrepareSpellCopy(info.getCopyId());
        if (copyCard != null) {
            game.getExile().removeCard(copyCard);
            copyCard.setZone(Zone.OUTSIDE, game);
        } else {
            game.getState().setZone(info.getCopyId(), Zone.OUTSIDE);
        }
    }

    private static void recordPrepareCastPermissionEffectId(PrepareCopyInfo info, Game game) {
        getPrepareCastPermissionEffects(info.getCopyId(), game)
                .stream()
                .findFirst()
                .ifPresent(effect -> info.setPermissionEffectId(effect.getId()));
    }

    private static void discardPrepareCastPermission(UUID copyId, Game game) {
        getPrepareCastPermissionEffects(copyId, game).forEach(AsThoughEffect::discard);
    }

    private static List<AsThoughEffect> getPrepareCastPermissionEffects(UUID copyId, Game game) {
        return game
                .getContinuousEffects()
                .getApplicableAsThoughEffects(AsThoughEffectType.CAST_FROM_NOT_OWN_HAND_ZONE, game)
                .stream()
                .filter(effect -> effect instanceof PrepareCastFromExileEffect)
                .filter(effect -> ((PrepareCastFromExileEffect) effect).getCopyId().equals(copyId))
                .collect(Collectors.toList());
    }

    private static Ability createPreparePermissionSource(Permanent permanent) {
        Ability source = new SimpleStaticAbility(Zone.ALL, new InfoEffect(""));
        source.setSourceId(permanent.getId());
        source.setControllerId(permanent.getControllerId());
        return source;
    }
}
