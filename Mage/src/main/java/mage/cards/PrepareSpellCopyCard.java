package mage.cards;

import mage.abilities.SpellAbility;

import java.util.UUID;

public final class PrepareSpellCopyCard extends CardImpl {

    private static final long serialVersionUID = 1L;

    private final PrepareSpellCharacteristics characteristics;

    public PrepareSpellCopyCard(UUID ownerId, PrepareSpellCharacteristics characteristics) {
        super(ownerId, characteristics.getCardSetInfo(), characteristics.getCardTypes(), characteristics.getManaCost());
        this.characteristics = characteristics.copy();

        this.setName(characteristics.getName());
        this.supertype.clear();
        this.supertype.addAll(characteristics.getSuperTypes());
        this.subtype.copyFrom(characteristics.getSubTypes());
        this.color = characteristics.getColor();
        this.frameColor = characteristics.getFrameColor();
        this.frameStyle = characteristics.getFrameStyle();
        this.setUsesVariousArt(characteristics.getUsesVariousArt());
        this.setImageFileName(characteristics.getImageFileName());
        this.setImageNumber(characteristics.getImageNumber());

        SpellAbility copiedSpellAbility = characteristics.getSpellAbility();
        this.replaceSpellAbility(copiedSpellAbility);
        this.spellAbility = copiedSpellAbility;
        this.setOwnerId(ownerId);
        this.setCopy(true, null);
        repairPrepareSpellSourceIds();
    }

    private PrepareSpellCopyCard(final PrepareSpellCopyCard card) {
        super(card);
        this.characteristics = card.characteristics.copy();
        repairPrepareSpellSourceIds();
    }

    public PrepareSpellCharacteristics getPrepareSpellCharacteristics() {
        return characteristics.copy();
    }

    public boolean isPrepareSpellCopy() {
        return true;
    }

    @Override
    public void assignNewId() {
        super.assignNewId();
        repairPrepareSpellSourceIds();
    }

    private void repairPrepareSpellSourceIds() {
        // CR 722.3c: the exiled copy is the object cast, so copied abilities source from it.
        this.getAbilities().setSourceId(this.getId());
        if (this.getSpellAbility() != null) {
            this.getSpellAbility().setSourceId(this.getId());
        }
    }

    @Override
    public PrepareSpellCopyCard copy() {
        return new PrepareSpellCopyCard(this);
    }
}
