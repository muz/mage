package mage.game.permanent.token;

import mage.MageInt;
import mage.constants.CardType;
import mage.constants.SubType;

/**
 * @author muz
 */
public final class HoloEngineerToken extends TokenImpl {

    public HoloEngineerToken() {
        super("Illusion Token", "3/3 colorless Illusion artifact creature token");
        cardType.add(CardType.ARTIFACT);
        cardType.add(CardType.CREATURE);
        subtype.add(SubType.ILLUSION);
        power = new MageInt(3);
        toughness = new MageInt(3);
    }

    private HoloEngineerToken(final HoloEngineerToken token) {
        super(token);
    }

    @Override
    public HoloEngineerToken copy() {
        return new HoloEngineerToken(this);
    }
}
