package mage.cards;

import mage.game.Game;
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
}
