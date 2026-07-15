package org.mage.test.commander;

import mage.cards.Card;
import mage.cards.CardSetInfo;
import mage.cards.PrepareCard;
import mage.cards.repository.CardInfo;
import mage.cards.repository.CardRepository;
import mage.constants.CardType;
import mage.constants.Rarity;
import mage.filter.FilterMana;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestCommander3PlayersFFA;

import java.util.UUID;

/**
 * @author LevelX2
 */
public class CommanderColorIdentityTest extends CardTestCommander3PlayersFFA {

    @Test
    public void TestColors() {
        // Basic Colors
        Assert.assertEquals("{B}", getColorIdentityString("Sengir Vampire"));
        Assert.assertEquals("{G}", getColorIdentityString("Elvish Visionary"));
        Assert.assertEquals("{R}", getColorIdentityString("Demolish"));
        Assert.assertEquals("{U}", getColorIdentityString("Negate"));
        Assert.assertEquals("{W}", getColorIdentityString("Silvercoat Lion"));

        // Multicolor
        Assert.assertEquals("{W}{G}", getColorIdentityString("Veteran Warleader"));
        Assert.assertEquals("{B}{R}", getColorIdentityString("Forerunner of Slaughter"));
        Assert.assertEquals("{U}{R}", getColorIdentityString("Brutal Expulsion"));
        Assert.assertEquals("{B}{G}", getColorIdentityString("Catacomb Sifter"));
        Assert.assertEquals("{U}{B}", getColorIdentityString("Fathom Feeder"));

        Assert.assertEquals("{W}{R}", getColorIdentityString("Angelic Captain"));
        Assert.assertEquals("{U}{G}", getColorIdentityString("Bring to Light"));
        Assert.assertEquals("{W}{B}", getColorIdentityString("Drana's Emissary"));
        Assert.assertEquals("{R}{G}", getColorIdentityString("Grove Rumbler"));
        Assert.assertEquals("{W}{U}", getColorIdentityString("Roil Spout"));

        // Cards with colors in the rule text
        Assert.assertEquals("{B}{R}", getColorIdentityString("Fires of Undeath"));

        // Cards without casting costs
        Assert.assertEquals("{B}", getColorIdentityString("Living End"));
        Assert.assertEquals("{G}", getColorIdentityString("Hypergenesis"));

        // Phyrexian Mana
        Assert.assertEquals("{B}", getColorIdentityString("Dismember"));

        // Hybrid mana
        Assert.assertEquals("{W}{G}", getColorIdentityString("Kitchen Finks"));

        // Lands with colored activation costs
        Assert.assertEquals("{G}", getColorIdentityString("Treetop Village"));

        // Cards with extort don't use extort to determine color identity
        Assert.assertEquals("{W}", getColorIdentityString("Knight of Obligation"));

        // Two face cards
        Assert.assertEquals("{R}{G}", getColorIdentityString("Daybreak Ranger"));
        Assert.assertEquals("{W}{R}", getColorIdentityString("Archangel Avacyn"));
        Assert.assertEquals("{U}{R}", getColorIdentityString("Civilized Scholar"));

        // Split cards
        Assert.assertEquals("{U}{R}", getColorIdentityString("Fire // Ice"));

        // MDF cards
        Assert.assertEquals("{W}{U}{B}{R}{G}", getColorIdentityString("Esika, God of the Tree"));

        // Adventure cards
        Assert.assertEquals("{G}", getColorIdentityString("Rosethorn Acolyte"));

        // Prepare spell alternative characteristics
        Assert.assertEquals(
                "{W}{G}",
                new TestPrepareColorIdentityCard(playerA.getId()).getColorIdentity().toString()
        );
    }

    private String getColorIdentityString(String cardName) {
        CardInfo cardInfo = CardRepository.instance.findCard(cardName);
        if (cardInfo == null) {
            throw new IllegalArgumentException("Couldn't find the card " + cardName + " in the DB.");
        }
        Card card = cardInfo.createCard();
        FilterMana filterMana = card.getColorIdentity();
        return filterMana.toString();
    }

    private static final class TestPrepareColorIdentityCard extends PrepareCard {

        private TestPrepareColorIdentityCard(UUID ownerId) {
            super(
                    ownerId,
                    new CardSetInfo("Test Prepare Color Identity", "TST", "1", Rarity.RARE),
                    new CardType[]{CardType.CREATURE},
                    "{1}{W}",
                    "Green Prepare Spell",
                    new CardType[]{CardType.SORCERY},
                    "{G}"
            );
        }

        private TestPrepareColorIdentityCard(final TestPrepareColorIdentityCard card) {
            super(card);
        }

        @Override
        public TestPrepareColorIdentityCard copy() {
            return new TestPrepareColorIdentityCard(this);
        }
    }
}
