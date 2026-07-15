package mage.cards;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.PrepareCopyInfo;
import mage.game.permanent.Permanent;
import mage.game.permanent.PermanentCard;

import java.util.Optional;
import java.util.UUID;

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

        removePrepareCopy(sourcePermanentReference, game);
        permanent.setPrepared(true, game);
        if (!permanent.isPrepared()) {
            return true;
        }

        UUID copyCreatorId = permanent.getControllerId();
        PrepareSpellCopyCard copyCard = createPrepareSpellCopy(characteristics.get(), copyCreatorId);
        PrepareCopyInfo info = new PrepareCopyInfo(
                sourcePermanentReference,
                copyCard.getId(),
                game.getExile().getPermanentExile().getId(),
                copyCreatorId
        );
        game.getState().registerPrepareSpellCopy(copyCard, info);
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

    private static void removePrepareCopy(MageObjectReference sourcePermanentReference, Game game) {
        PrepareCopyInfo info = game.getState().removePrepareCopyInfo(sourcePermanentReference);
        if (info == null) {
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
}
