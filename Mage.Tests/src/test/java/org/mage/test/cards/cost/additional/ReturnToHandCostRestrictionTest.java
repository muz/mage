package org.mage.test.cards.cost.additional;

import mage.abilities.keyword.BeamMeUpAbility;
import mage.cards.Card;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Before;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author muz
 */
public class ReturnToHandCostRestrictionTest extends CardTestPlayerBase {

    // Marooned
    // Enchant artifact or creature
    // When this Aura enters, you may pay {3}. If you do, tap enchanted permanent.
    // Enchanted permanent doesn't untap during its controller's untap step and can't be beamed up.
    private static final String aura = "Marooned";

    // Open Communications
    // Draw a card.
    // Beam me up {2}{U}
    private static final String spell = "Open Communications";
    private static final String beamMeUp = "Beam me up {2}{U}";

    // Grizzly Bears — 2/2 creature with no abilities.
    private static final String creature = "Grizzly Bears";

    // Memnite — {0}, 1/1 artifact creature with no abilities.
    private static final String otherCreature = "Memnite";

    // Island — {T}: Add {U}.
    private static final String island = "Island";

    // Familiar's Ruse
    // As an additional cost to cast this spell, return a creature you control to its owner's hand.
    // Counter target spell.
    private static final String counterspell = "Familiar's Ruse";

    @Before
    public void enableStrictMode() {
        setStrictChooseMode(true);
    }

    @Test
    public void testOnlyCreatureCannotPayBeamMeUp() {
        addCard(Zone.BATTLEFIELD, playerA, island, 4);
        addCard(Zone.BATTLEFIELD, playerA, creature);
        addCard(Zone.HAND, playerA, aura);
        addCard(Zone.GRAVEYARD, playerA, spell);

        checkPlayableAbility("before Marooned", 1, PhaseStep.PRECOMBAT_MAIN, playerA, beamMeUp, true);
        enchantCreature();
        checkPlayableAbility("only creature is Marooned", 1, PhaseStep.PRECOMBAT_MAIN, playerA, beamMeUp, false);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, aura, 1);
        assertPermanentCount(playerA, creature, 1);
        assertGraveyardCount(playerA, spell, 1);
    }

    @Test
    public void testAnotherCreatureCanPayBeamMeUp() {
        addCard(Zone.BATTLEFIELD, playerA, island, 4);
        addCard(Zone.BATTLEFIELD, playerA, creature);
        addCard(Zone.BATTLEFIELD, playerA, otherCreature);
        addCard(Zone.HAND, playerA, aura);
        addCard(Zone.GRAVEYARD, playerA, spell);

        enchantCreature();
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, beamMeUp);
        setChoice(playerA, otherCreature);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, aura, 1);
        assertPermanentCount(playerA, creature, 1);
        assertPermanentCount(playerA, otherCreature, 0);
        assertHandCount(playerA, otherCreature, 1);
        assertExileCount(playerA, spell, 1);
    }

    @Test
    public void testRemovingMaroonedRestoresPayment() {
        addCard(Zone.BATTLEFIELD, playerA, island, 4);
        // Plains — {T}: Add {W}.
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.BATTLEFIELD, playerA, creature);
        addCard(Zone.HAND, playerA, aura);
        // Disenchant — Destroy target artifact or enchantment.
        addCard(Zone.HAND, playerA, "Disenchant");
        addCard(Zone.GRAVEYARD, playerA, spell);

        enchantCreature();
        checkPlayableAbility("while Marooned", 1, PhaseStep.PRECOMBAT_MAIN, playerA, beamMeUp, false);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Disenchant", aura);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("after Marooned", 1, PhaseStep.PRECOMBAT_MAIN, playerA, beamMeUp, true);
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, beamMeUp);
        setChoice(playerA, creature);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertGraveyardCount(playerA, aura, 1);
        assertPermanentCount(playerA, creature, 0);
        assertHandCount(playerA, creature, 1);
        assertExileCount(playerA, spell, 1);
    }

    @Test
    public void testOrdinaryReturnCostStillWorks() {
        checkSeparateReturnCost(false);
    }

    @Test
    public void testSeparateReturnCostOnBeamMeUpSpellStillWorks() {
        checkSeparateReturnCost(true);
    }

    private void checkSeparateReturnCost(boolean useBeamMeUp) {
        addCard(Zone.BATTLEFIELD, playerA, island, 3);
        addCard(Zone.BATTLEFIELD, playerA, creature);
        addCard(Zone.HAND, playerA, aura);
        addCard(useBeamMeUp ? Zone.GRAVEYARD : Zone.HAND, playerA, counterspell);
        if (useBeamMeUp) {
            // Supply the granted keyword directly so this cost regression does not depend on Scotty.
            Card card = getGraveCards(playerA).get(0);
            card.addAbility(new BeamMeUpAbility(card, "{U}{U}"));
            addCard(Zone.BATTLEFIELD, playerA, otherCreature);
        }
        // Mountain — {T}: Add {R}.
        addCard(Zone.BATTLEFIELD, playerB, "Mountain");
        // Lightning Bolt — Lightning Bolt deals 3 damage to any target.
        addCard(Zone.HAND, playerB, "Lightning Bolt");

        enchantCreature();
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerB, "Lightning Bolt", playerA);
        if (useBeamMeUp) {
            activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Beam me up {U}{U}", "Lightning Bolt");
        } else {
            castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, counterspell, "Lightning Bolt");
        }
        setChoice(playerA, creature); // Familiar's Ruse's own additional cost can return the enchanted creature.
        if (useBeamMeUp) {
            setChoice(playerA, otherCreature); // Only this payment is beaming up a creature.
        }

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertLife(playerA, 20);
        assertHandCount(playerA, creature, 1);
        assertPermanentCount(playerA, creature, 0);
        assertGraveyardCount(playerA, aura, 1);
        assertGraveyardCount(playerB, "Lightning Bolt", 1);
        assertGraveyardCount(playerA, counterspell, useBeamMeUp ? 0 : 1);
        assertExileCount(playerA, counterspell, useBeamMeUp ? 1 : 0);
        if (useBeamMeUp) {
            assertHandCount(playerA, otherCreature, 1);
        }
    }

    private void enchantCreature() {
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, aura, creature);
        setChoice(playerA, false);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
    }
}
