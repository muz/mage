package org.mage.test.cards.abilities.keywords;

import mage.abilities.Ability;
import mage.cards.Card;
import mage.cards.CardWithSpellOption;
import mage.cards.PrepareCard;
import mage.cards.PrepareSpellCharacteristics;
import mage.cards.PrepareSpellCopyCard;
import mage.cards.PrepareUtil;
import mage.cards.repository.CardInfo;
import mage.cards.repository.CardRepository;
import mage.constants.CardType;
import mage.game.permanent.PermanentCard;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Optional;
import java.util.UUID;

public class PrepareTest extends CardTestPlayerBase {

    @Test
    public void testPrepareCardCopyOwnsIndependentSpellBlueprint() {
        PrepareCard original = createPrepareCard("Elite Interceptor");
        PrepareCard copied = (PrepareCard) original.copy();

        Assert.assertNotSame("copy must be a different card object", original, copied);
        Assert.assertNotSame("copy must own an independent prepare spell blueprint", original.getSpellCard(), copied.getSpellCard());
        Assert.assertSame("original spell parent must remain original", original, original.getSpellCard().getParentCard());
        Assert.assertSame("copied spell parent must be copied card", copied, copied.getSpellCard().getParentCard());
        Assert.assertEquals("copied spell rules must match original spell rules", original.getSpellCard().getRules(), copied.getSpellCard().getRules());

        UUID copiedMainId = copied.getId();
        UUID copiedSpellId = copied.getSpellCard().getId();
        copied.assignNewId();
        Assert.assertNotEquals("copy main id must change on assignNewId", copiedMainId, copied.getId());
        Assert.assertNotEquals("copy spell id must change on assignNewId", copiedSpellId, copied.getSpellCard().getId());
        assertAllAbilitySources(copied.getSpellCard(), copied.getSpellCard().getId());
    }

    @Test
    public void testPhysicalPrepareCharacteristicsCreateStandaloneSpellCopy() {
        PrepareCard source = createPrepareCard("Elite Interceptor");
        PrepareSpellCharacteristics characteristics = PrepareUtil
                .getPrepareSpellCharacteristics(source)
                .orElseThrow(AssertionError::new);

        UUID copyOwnerId = playerA.getId();
        PrepareSpellCopyCard copy = PrepareUtil.createPrepareSpellCopy(characteristics, copyOwnerId);

        Assert.assertEquals("copy must use prepare spell name", "Rejoinder", copy.getName());
        Assert.assertEquals("copy must use prepare spell mana cost", "{1}{W}", copy.getManaCost().getText());
        Assert.assertTrue("copy must have sorcery card type", copy.getCardType().contains(CardType.SORCERY));
        Assert.assertFalse("copy must not have source permanent card type", copy.getCardType().contains(CardType.CREATURE));
        Assert.assertSame("copy main card must be itself", copy, copy.getMainCard());
        Assert.assertFalse("copy must not inherit CardWithSpellOption behavior", copy instanceof CardWithSpellOption);
        Assert.assertTrue("copy must identify as a Prepare spell copy", copy.isPrepareSpellCopy());
        Assert.assertTrue("copy must be marked as a copied object", copy.isCopy());
        Assert.assertEquals("copy owner must be the copy creator", copyOwnerId, copy.getOwnerId());
        Assert.assertNotSame("copy must own an independent spell ability", source.getSpellCard().getSpellAbility(), copy.getSpellAbility());
        Assert.assertEquals("spell ability source must be copied card", copy.getId(), copy.getSpellAbility().getSourceId());
        assertAllAbilitySources(copy, copy.getId());
    }

    @Test
    public void testPermanentCardPrepareCharacteristicsUsePhysicalPrepareBlueprint() {
        PrepareCard source = createPrepareCard("Elite Interceptor");
        source.setOwnerId(playerA.getId());
        PermanentCard permanent = new PermanentCard(source, playerA.getId(), currentGame);

        Optional<PrepareSpellCharacteristics> characteristics = PrepareUtil.getPrepareSpellCharacteristics(permanent, currentGame);

        Assert.assertTrue("physical Prepare permanent must expose prepare spell characteristics", characteristics.isPresent());
        Assert.assertEquals("permanent characteristics must use prepare spell name", "Rejoinder", characteristics.get().getName());
    }

    private PrepareCard createPrepareCard(String cardName) {
        CardInfo cardInfo = CardRepository.instance.findCard(cardName);
        Assert.assertNotNull("test fixture must exist: " + cardName, cardInfo);
        Card card = cardInfo.createCard();
        Assert.assertTrue("test fixture must be a PrepareCard: " + cardName, card instanceof PrepareCard);
        return (PrepareCard) card;
    }

    private void assertAllAbilitySources(Card card, UUID expectedSourceId) {
        for (Ability ability : card.getAbilities()) {
            Assert.assertEquals("ability source must be the expected card id: " + ability, expectedSourceId, ability.getSourceId());
        }
    }
}
