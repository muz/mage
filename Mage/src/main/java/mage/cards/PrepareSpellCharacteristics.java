package mage.cards;

import mage.ObjectColor;
import mage.abilities.SpellAbility;
import mage.constants.CardType;
import mage.constants.Rarity;
import mage.constants.SuperType;
import mage.util.Copyable;
import mage.util.SubTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class PrepareSpellCharacteristics implements Serializable, Copyable<PrepareSpellCharacteristics> {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String manaCost;
    private final List<CardType> cardTypes;
    private final List<SuperType> superTypes;
    private final SubTypes subTypes;
    private final ObjectColor color;
    private final ObjectColor frameColor;
    private final FrameStyle frameStyle;
    private final Rarity rarity;
    private final String expansionSetCode;
    private final String cardNumber;
    private final boolean usesVariousArt;
    private final String imageFileName;
    private final int imageNumber;
    private final SpellAbility spellAbility;

    public PrepareSpellCharacteristics(PrepareSpellCard spellCard) {
        SpellAbility sourceSpellAbility = spellCard.getSpellAbility();
        if (sourceSpellAbility == null) {
            throw new IllegalArgumentException("Prepare spell card must have a spell ability: " + spellCard.getName());
        }
        // CR 722.3c: snapshot only the prepare spell characteristics for the exiled copy.
        this.name = spellCard.getName();
        this.manaCost = spellCard.getManaCost().getText();
        this.cardTypes = new ArrayList<>(spellCard.getCardType());
        this.superTypes = new ArrayList<>(spellCard.getSuperType());
        this.subTypes = spellCard.getSubtype().copy();
        this.color = spellCard.getColor().copy();
        this.frameColor = spellCard.getFrameColor().copy();
        this.frameStyle = spellCard.getFrameStyle();
        this.rarity = spellCard.getRarity();
        this.expansionSetCode = spellCard.getExpansionSetCode();
        this.cardNumber = spellCard.getCardNumber();
        this.usesVariousArt = spellCard.getUsesVariousArt();
        this.imageFileName = spellCard.getImageFileName();
        this.imageNumber = spellCard.getImageNumber();
        this.spellAbility = sourceSpellAbility.copy();
    }

    private PrepareSpellCharacteristics(final PrepareSpellCharacteristics characteristics) {
        this.name = characteristics.name;
        this.manaCost = characteristics.manaCost;
        this.cardTypes = new ArrayList<>(characteristics.cardTypes);
        this.superTypes = new ArrayList<>(characteristics.superTypes);
        this.subTypes = characteristics.subTypes.copy();
        this.color = characteristics.color.copy();
        this.frameColor = characteristics.frameColor.copy();
        this.frameStyle = characteristics.frameStyle;
        this.rarity = characteristics.rarity;
        this.expansionSetCode = characteristics.expansionSetCode;
        this.cardNumber = characteristics.cardNumber;
        this.usesVariousArt = characteristics.usesVariousArt;
        this.imageFileName = characteristics.imageFileName;
        this.imageNumber = characteristics.imageNumber;
        this.spellAbility = characteristics.spellAbility.copy();
    }

    @Override
    public PrepareSpellCharacteristics copy() {
        return new PrepareSpellCharacteristics(this);
    }

    public String getName() {
        return name;
    }

    public String getManaCost() {
        return manaCost;
    }

    public CardType[] getCardTypes() {
        return cardTypes.toArray(new CardType[0]);
    }

    public List<SuperType> getSuperTypes() {
        return new ArrayList<>(superTypes);
    }

    public SubTypes getSubTypes() {
        return subTypes.copy();
    }

    public ObjectColor getColor() {
        return color.copy();
    }

    public ObjectColor getFrameColor() {
        return frameColor.copy();
    }

    public FrameStyle getFrameStyle() {
        return frameStyle;
    }

    public CardSetInfo getCardSetInfo() {
        return new CardSetInfo(name, expansionSetCode, cardNumber, rarity);
    }

    public boolean getUsesVariousArt() {
        return usesVariousArt;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public int getImageNumber() {
        return imageNumber;
    }

    public SpellAbility getSpellAbility() {
        return spellAbility.copy();
    }
}
