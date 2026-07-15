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
import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.game.GameState;
import mage.game.PrepareCopyInfo;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.permanent.PermanentCard;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Collection;
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

    @Test
    public void testEntersPreparedCreatesTrackedExiledCopy() {
        setStrictChooseMode(true);

        addCard(Zone.BATTLEFIELD, playerA, "Plains");
        addCard(Zone.HAND, playerA, "Elite Interceptor");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Elite Interceptor");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        Permanent permanent = getPermanent("Elite Interceptor", playerA);
        PrepareCopyInfo info = getOnlyPrepareCopyInfo();
        Card copy = currentGame.getCard(info.getCopyId());

        Assert.assertTrue("source permanent must be prepared", permanent.isPrepared());
        Assert.assertEquals("tracking must point at the source permanent", permanent.getId(), info.getSourcePermanentId());
        Assert.assertTrue("tracked card must be a Prepare spell copy", copy instanceof PrepareSpellCopyCard);
        Assert.assertEquals("copy must use the prepare spell name", "Rejoinder", copy.getName());
        Assert.assertEquals("copy must be in exile", Zone.EXILED, currentGame.getState().getZone(info.getCopyId()));
        Assert.assertSame("copy must resolve through exile", copy, currentGame.getExile().getCard(info.getCopyId(), currentGame));
        Assert.assertTrue("copy must be live under CR 722.3c", currentGame.getState().isLivePrepareSpellCopy(info.getCopyId(), currentGame));
        Assert.assertNull(
                "Prepare copies must not use the generic copied-card LKI key",
                currentGame.getState().getValue(GameState.COPIED_CARD_KEY + info.getCopyId())
        );
        assertExileCount(playerA, "Rejoinder", 1);
    }

    @Test
    public void testRepeatedPrepareDoesNotCreateSecondCopy() {
        setStrictChooseMode(true);

        addCard(Zone.BATTLEFIELD, playerA, "Plains");
        addCard(Zone.HAND, playerA, "Elite Interceptor");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Elite Interceptor");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        Permanent permanent = getPermanent("Elite Interceptor", playerA);
        PrepareCopyInfo firstInfo = getOnlyPrepareCopyInfo();
        Ability sourceAbility = permanent.getAbilities().iterator().next();

        Assert.assertTrue("repeated prepare effect should resolve", PrepareUtil.setPrepared(permanent, true, sourceAbility, currentGame));

        PrepareCopyInfo secondInfo = getOnlyPrepareCopyInfo();
        Assert.assertEquals("repeated prepare must keep the existing copy", firstInfo.getCopyId(), secondInfo.getCopyId());
        assertExileCount(playerA, "Rejoinder", 1);
    }

    @Test
    public void testCastingPrepareCopyFromExileConsumesPreparedState() {
        setStrictChooseMode(true);

        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);
        addCard(Zone.HAND, playerA, "Elite Interceptor");
        addCard(Zone.BATTLEFIELD, playerB, "Silvercoat Lion");
        addCard(Zone.LIBRARY, playerA, "Island");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Elite Interceptor", true);
        checkPlayableAbility("prepare copy is castable from exile", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast Rejoinder", true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Rejoinder", "Silvercoat Lion", true);
        setChoice(playerA, true);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        Permanent permanent = getPermanent("Elite Interceptor", playerA);

        Assert.assertFalse("source permanent must lose prepared after casting the copy", permanent.isPrepared());
        Assert.assertEquals("successful cast must clear Prepare tracking", 0, currentGame.getState().getPrepareCopyInfos().size());
        assertExileCount(playerA, "Rejoinder", 0);
        assertTapped("Silvercoat Lion", true);
        assertHandCount(playerA, "Island", 1);
    }

    @Test
    public void testSourceLeavingBattlefieldBeforeCastRemovesPrepareCopy() {
        setStrictChooseMode(true);

        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);
        addCard(Zone.HAND, playerA, "Elite Interceptor");
        addCard(Zone.BATTLEFIELD, playerB, "Island");
        addCard(Zone.HAND, playerB, "Unsummon");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Elite Interceptor", true);
        checkPlayableAbility("prepare copy exists before source leaves", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast Rejoinder", true);
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Unsummon", "Elite Interceptor", true);

        setStopAt(2, PhaseStep.BEGIN_COMBAT);
        execute();

        Assert.assertEquals("source leaving must clear Prepare tracking", 0, currentGame.getState().getPrepareCopyInfos().size());
        assertPermanentCount(playerA, "Elite Interceptor", 0);
        assertHandCount(playerA, "Elite Interceptor", 1);
        assertExileCount(playerA, "Rejoinder", 0);
    }

    @Test
    public void testUnprepareRemovesTrackedExiledCopy() {
        setStrictChooseMode(true);

        addCard(Zone.BATTLEFIELD, playerA, "Plains");
        addCard(Zone.HAND, playerA, "Elite Interceptor");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Elite Interceptor");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        Permanent permanent = getPermanent("Elite Interceptor", playerA);
        PrepareCopyInfo info = getOnlyPrepareCopyInfo();
        UUID copyId = info.getCopyId();
        Ability sourceAbility = permanent.getAbilities().iterator().next();

        Assert.assertTrue("unprepare effect should resolve", PrepareUtil.setPrepared(permanent, false, sourceAbility, currentGame));

        Assert.assertFalse("source permanent must lose prepared", permanent.isPrepared());
        Assert.assertEquals("unprepare must clear Prepare tracking", 0, currentGame.getState().getPrepareCopyInfos().size());
        Assert.assertNull("unprepare must remove copied-card registration", currentGame.getState().getCopiedCard(copyId));
        Assert.assertEquals("unprepare must put the copy outside the game", Zone.OUTSIDE, currentGame.getState().getZone(copyId));
        assertExileCount(playerA, "Rejoinder", 0);
    }

    @Test
    public void testPrepareCastPermissionFollowsCurrentController() {
        setStrictChooseMode(true);

        addCard(Zone.BATTLEFIELD, playerA, "Island", 4);
        addCard(Zone.BATTLEFIELD, playerA, "Silvercoat Lion");
        addCard(Zone.HAND, playerA, "Skycoach Conductor");

        addCard(Zone.BATTLEFIELD, playerB, "Mountain", 3);
        addCard(Zone.BATTLEFIELD, playerB, "Island");
        addCard(Zone.BATTLEFIELD, playerB, "Pillarfield Ox");
        addCard(Zone.HAND, playerB, "Act of Treason");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Skycoach Conductor", true);
        checkPlayableAbility("old controller can cast before control changes", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast All Aboard", true);

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Act of Treason", "Skycoach Conductor", true);
        checkPlayableAbility("old controller loses Prepare permission", 2, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast All Aboard", false);
        checkPlayableAbility("new controller gains Prepare permission", 2, PhaseStep.PRECOMBAT_MAIN, playerB, "Cast All Aboard", true);

        setStopAt(2, PhaseStep.BEGIN_COMBAT);
        execute();
    }

    @Test
    public void testPhasingRemovesAndRecreatesPrepareCopy() {
        setStrictChooseMode(true);

        addCard(Zone.BATTLEFIELD, playerA, "Plains");
        addCard(Zone.HAND, playerA, "Elite Interceptor");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Elite Interceptor");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        Permanent permanent = getPermanent("Elite Interceptor", playerA);
        UUID firstCopyId = getOnlyPrepareCopyInfo().getCopyId();

        Assert.assertTrue("prepared source should phase out", permanent.phaseOut(currentGame));
        Assert.assertTrue("phased-out source must keep prepared marker", permanent.isPrepared());
        Assert.assertFalse("source must be phased out", permanent.isPhasedIn());
        Assert.assertEquals("phase-out must remove Prepare tracking", 0, currentGame.getState().getPrepareCopyInfos().size());
        Assert.assertNull("phase-out must remove old copied-card registration", currentGame.getState().getCopiedCard(firstCopyId));
        Assert.assertEquals("phase-out must put the old copy outside the game", Zone.OUTSIDE, currentGame.getState().getZone(firstCopyId));
        assertExileCount(playerA, "Rejoinder", 0);

        Assert.assertTrue("prepared source should phase in", permanent.phaseIn(currentGame));
        currentGame.getState().handleSimultaneousEvent(currentGame);

        PrepareCopyInfo secondInfo = getOnlyPrepareCopyInfo();
        Assert.assertTrue("phased-in source must remain prepared", permanent.isPrepared());
        Assert.assertTrue("source must be phased in", permanent.isPhasedIn());
        Assert.assertNotEquals("phase-in must create a fresh copy id", firstCopyId, secondInfo.getCopyId());
        assertExileCount(playerA, "Rejoinder", 1);

        currentGame.fireEvent(GameEvent.getEvent(
                GameEvent.EventType.PHASED_IN,
                permanent.getId(),
                null,
                permanent.getControllerId()
        ));
        PrepareCopyInfo duplicatePhaseInInfo = getOnlyPrepareCopyInfo();
        Assert.assertEquals(
                "duplicate phase-in handling must not replace the live copy",
                secondInfo.getCopyId(),
                duplicatePhaseInInfo.getCopyId()
        );
        assertExileCount(playerA, "Rejoinder", 1);
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

    private PrepareCopyInfo getOnlyPrepareCopyInfo() {
        Collection<PrepareCopyInfo> infos = currentGame.getState().getPrepareCopyInfos();
        Assert.assertEquals("there must be exactly one tracked Prepare copy", 1, infos.size());
        return infos.iterator().next();
    }
}
