package org.mage.test.cards.abilities.keywords;

import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author muz
 */
public class SneakTest extends CardTestPlayerBase {

    private static final String attacker = "Grizzly Bears";
    private static final String ninjas = "Foot Ninjas"; // 5/5, sneak {3}{W/B}, enters: gain 3 life
    private static final String planeswalker = "Ugin, the Spirit Dragon"; // 7 loyalty

    private void setupSneak() {
        setupSneak("Swamp", 4);
    }

    private void setupSneak(String land, int amount) {
        addCard(Zone.BATTLEFIELD, playerA, land, amount);
        addCard(Zone.BATTLEFIELD, playerA, attacker);
        addCard(Zone.HAND, playerA, ninjas);
    }

    private void castWithSneak() {
        castSpell(1, PhaseStep.DECLARE_BLOCKERS, playerA, ninjas + " with Sneak");
        setChoice(playerA, attacker);
    }

    private void finishCombatDamage() {
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.COMBAT_DAMAGE);
        execute();
    }

    @Test
    public void entersTappedAndAttackingWithoutAnAttackTrigger() {
        setupSneak();
        addCard(Zone.BATTLEFIELD, playerA, "Beastmaster Ascension");

        attack(1, playerA, attacker, playerB);
        setChoice(playerA, true); // put a quest counter on Beastmaster Ascension
        castWithSneak();
        finishCombatDamage();

        assertHandCount(playerA, attacker, 1);
        assertPermanentCount(playerA, ninjas, 1);
        assertTapped(ninjas, true);
        assertAttacking(ninjas, true);
        assertLife(playerA, 23);
        assertLife(playerB, 15);
        assertCounterCount("Beastmaster Ascension", CounterType.QUEST, 1);
    }

    @Test
    public void normalCastDoesNotEnterTappedOrAttacking() {
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 6);
        addCard(Zone.HAND, playerA, ninjas);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, ninjas);
        finishCombatDamage();

        assertTapped(ninjas, false);
        assertAttacking(ninjas, false);
        assertLife(playerA, 23);
        assertLife(playerB, 20);
    }

    @Test
    public void attacksTheSamePlaneswalker() {
        setupSneak();
        addCard(Zone.BATTLEFIELD, playerB, planeswalker);

        attack(1, playerA, attacker, planeswalker);
        castWithSneak();
        finishCombatDamage();

        assertTapped(ninjas, true);
        assertAttacking(ninjas, true);
        assertCounterCount(planeswalker, CounterType.LOYALTY, 2);
        assertLife(playerB, 20);
        Assert.assertEquals(getPermanent(planeswalker).getId(), getPermanent(ninjas).getAttacking().getSourceId());
    }

    @Test
    public void attacksTheSameBattle() {
        String battle = "Invasion of Belenon"; // 5 defense
        String splinter = "Splinter, Hamato Yoshi"; // 1/3, sneak {B}
        addCard(Zone.BATTLEFIELD, playerA, "Scrubland", 4);
        addCard(Zone.BATTLEFIELD, playerA, attacker);
        addCard(Zone.HAND, playerA, battle);
        addCard(Zone.HAND, playerA, splinter);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, battle);
        attack(1, playerA, attacker, battle);
        castSpell(1, PhaseStep.DECLARE_BLOCKERS, playerA, splinter + " with Sneak");
        setChoice(playerA, attacker);
        finishCombatDamage();

        assertTapped(splinter, true);
        assertAttacking(splinter, true);
        assertCounterCount(battle, CounterType.DEFENSE, 4);
        assertLife(playerB, 20);
        Assert.assertEquals(getPermanent(battle).getId(), getPermanent(splinter).getAttacking().getSourceId());
    }

    @Test
    public void removedDefenderDoesNotRedirectToItsController() {
        setupSneak();
        addCard(Zone.BATTLEFIELD, playerB, planeswalker);
        addCard(Zone.BATTLEFIELD, playerB, "Island", 2);
        addCard(Zone.HAND, playerB, "Boomerang");

        attack(1, playerA, attacker, planeswalker);
        castWithSneak();
        castSpell(1, PhaseStep.DECLARE_BLOCKERS, playerB, "Boomerang", planeswalker, ninjas, StackClause.WHILE_ON_STACK);
        finishCombatDamage();

        assertHandCount(playerB, planeswalker, 1);
        assertTapped(ninjas, true);
        assertAttacking(ninjas, false);
        assertLife(playerB, 20);
    }

    @Test
    public void blinkedDefenderIsANewObject() {
        setupSneak();
        addCard(Zone.BATTLEFIELD, playerB, planeswalker);
        addCard(Zone.BATTLEFIELD, playerB, "Leyline of Anticipation");
        addCard(Zone.BATTLEFIELD, playerB, "Plains", 2);
        addCard(Zone.HAND, playerB, "Flicker");

        attack(1, playerA, attacker, planeswalker);
        castWithSneak();
        castSpell(1, PhaseStep.DECLARE_BLOCKERS, playerB, "Flicker", planeswalker, ninjas, StackClause.WHILE_ON_STACK);
        finishCombatDamage();

        assertGraveyardCount(playerB, "Flicker", 1);
        assertCounterCount(planeswalker, CounterType.LOYALTY, 7);
        assertTapped(ninjas, true);
        assertAttacking(ninjas, false);
        assertLife(playerB, 20);
    }

    @Test
    public void copiedSpellRetainsSneakAndDefender() {
        setupSneak("Zagoth Triome", 6);
        addCard(Zone.HAND, playerA, "Double Major");

        attack(1, playerA, attacker, playerB);
        castWithSneak();
        castSpell(1, PhaseStep.DECLARE_BLOCKERS, playerA, "Double Major", ninjas, ninjas, StackClause.WHILE_ON_STACK);
        finishCombatDamage();

        assertHandCount(playerA, attacker, 1);
        assertPermanentCount(playerA, ninjas, 2);
        assertTappedCount(ninjas, true, 2);
        assertLife(playerA, 26);
        assertLife(playerB, 10);
    }

    @Test
    public void stolenSpellEntersTappedButNotAttacking() {
        setupSneak();
        addCard(Zone.BATTLEFIELD, playerB, "Island", 6);
        addCard(Zone.HAND, playerB, "Aethersnatch");

        attack(1, playerA, attacker, playerB);
        castWithSneak();
        castSpell(1, PhaseStep.DECLARE_BLOCKERS, playerB, "Aethersnatch", ninjas, ninjas, StackClause.WHILE_ON_STACK);
        finishCombatDamage();

        assertPermanentCount(playerB, ninjas, 1);
        assertTapped(ninjas, true);
        assertAttacking(ninjas, false);
        assertLife(playerB, 23);
    }

    @Test
    public void counteringTheSpellDoesNotRefundTheReturnedAttacker() {
        setupSneak();
        addCard(Zone.BATTLEFIELD, playerB, "Island", 2);
        addCard(Zone.HAND, playerB, "Counterspell");

        attack(1, playerA, attacker, playerB);
        castWithSneak();
        castSpell(1, PhaseStep.DECLARE_BLOCKERS, playerB, "Counterspell", ninjas, ninjas, StackClause.WHILE_ON_STACK);
        finishCombatDamage();

        assertHandCount(playerA, attacker, 1);
        assertGraveyardCount(playerA, ninjas, 1);
        assertPermanentCount(playerA, ninjas, 0);
        assertLife(playerA, 20);
        assertLife(playerB, 20);
    }

    @Test
    public void blinkingTheCreatureDoesNotRepeatSneak() {
        setupSneak("Scrubland", 5);
        addCard(Zone.HAND, playerA, "Cloudshift");

        attack(1, playerA, attacker, playerB);
        castWithSneak();
        waitStackResolved(1, PhaseStep.DECLARE_BLOCKERS);
        castSpell(1, PhaseStep.DECLARE_BLOCKERS, playerA, "Cloudshift", ninjas);
        finishCombatDamage();

        assertTapped(ninjas, false);
        assertAttacking(ninjas, false);
        assertLife(playerA, 26);
        assertLife(playerB, 20);
    }

    @Test
    public void worksWhenTheEnteringCreatureLosesItsAbilities() {
        setupSneak();
        addCard(Zone.BATTLEFIELD, playerB, "Humility");

        attack(1, playerA, attacker, playerB);
        castWithSneak();
        finishCombatDamage();

        assertTapped(ninjas, true);
        assertAttacking(ninjas, true);
        assertPowerToughness(playerA, ninjas, 1, 1);
        assertLife(playerA, 20);
        assertLife(playerB, 19);
    }

    @Test
    public void enterUntappedReplacementStillApplies() {
        setupSneak();
        addCard(Zone.BATTLEFIELD, playerB, "Archelos, Lagoon Mystic");

        attack(1, playerA, attacker, playerB);
        castWithSneak();
        finishCombatDamage();

        assertTapped(ninjas, false);
        assertAttacking(ninjas, true);
        assertLife(playerB, 15);
    }

    @Test
    public void enteringAttackingTriggersArni() {
        setupSneak("Badlands", 6);
        addCard(Zone.BATTLEFIELD, playerA, "Arni Metalbrow");

        attack(1, playerA, attacker, playerB);
        setChoice(playerA, false); // Arni: the declared attacker
        castWithSneak();
        setChoice(playerA, ninjas); // order the life-gain and Arni entry triggers
        setChoice(playerA, true); // Arni: Foot Ninjas enters attacking
        setChoice(playerA, attacker); // put the returned Bear onto the battlefield
        setChoice(playerA, false); // Arni: the Bear enters attacking
        finishCombatDamage();

        assertPermanentCount(playerA, attacker, 1);
        assertTapped(attacker, true);
        assertAttacking(attacker, true);
        assertLife(playerB, 13);
    }

    @Test
    public void enteringAttackingIgnoresDeclarationRestrictions() {
        setupSneak();
        addCard(Zone.BATTLEFIELD, playerA, "Silvercoat Lion");
        addCard(Zone.BATTLEFIELD, playerB, "Crawlspace");

        attack(1, playerA, attacker, playerB);
        attack(1, playerA, "Silvercoat Lion", playerB);
        castWithSneak();
        finishCombatDamage();

        // Two creatures were declared; Sneak must not count as a third declaration.
        assertAttacking(ninjas, true);
        assertLife(playerB, 13);
    }

    private void setupGrantedSneak(String cardName) {
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 7);
        addCard(Zone.BATTLEFIELD, playerA, "Ninja Teen");
        addCard(Zone.BATTLEFIELD, playerA, attacker);
        addCard(Zone.GRAVEYARD, playerA, cardName);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{1}{B}: Level 2");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{B}: Level 3");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        attack(1, playerA, attacker, playerB);
        castSpell(1, PhaseStep.DECLARE_BLOCKERS, playerA, cardName + " with Sneak");
        setChoice(playerA, attacker);
    }

    @Test
    public void grantedSneakWorksFromTheGraveyard() {
        setupGrantedSneak("Runeclaw Bear");
        finishCombatDamage();

        assertGraveyardCount(playerA, "Runeclaw Bear", 0);
        assertTapped("Runeclaw Bear", true);
        assertAttacking("Runeclaw Bear", true);
        assertLife(playerB, 16); // Ninja Teen's leaves trigger, then the boosted Bear
    }

    @Test
    public void noncreaturePermanentEntersTappedButNotAttacking() {
        String god = "Purphoros, God of the Forge";
        setupGrantedSneak(god);
        finishCombatDamage();

        assertPermanentCount(playerA, god, 1);
        assertType(god, CardType.CREATURE, false);
        assertTapped(god, true);
        assertAttacking(god, false);
        assertLife(playerB, 19); // only Ninja Teen's leaves trigger
    }

    @Test
    public void sneakWorksFromTheCommandZone() {
        String splinter = "Splinter, Hamato Yoshi";
        addCard(Zone.BATTLEFIELD, playerA, "Swamp");
        addCard(Zone.BATTLEFIELD, playerA, attacker);
        addCard(Zone.COMMAND, playerA, splinter);

        attack(1, playerA, attacker, playerB);
        castSpell(1, PhaseStep.DECLARE_BLOCKERS, playerA, splinter + " with Sneak");
        setChoice(playerA, attacker);
        finishCombatDamage();

        assertCommandZoneCount(playerA, splinter, 0);
        assertHandCount(playerA, attacker, 1);
        assertTapped(splinter, true);
        assertAttacking(splinter, true);
        assertLife(playerB, 19);
    }

    @Test
    public void canReturnAnAttackerWhoseDefenderAlreadyLeftCombat() {
        setupSneak("Scrubland", 6);
        addCard(Zone.BATTLEFIELD, playerB, planeswalker);
        addCard(Zone.BATTLEFIELD, playerA, "Leyline of Anticipation");
        addCard(Zone.HAND, playerA, "Flicker");

        attack(1, playerA, attacker, planeswalker);
        castSpell(1, PhaseStep.DECLARE_BLOCKERS, playerA, "Flicker", planeswalker);
        waitStackResolved(1, PhaseStep.DECLARE_BLOCKERS);
        castWithSneak();
        finishCombatDamage();

        assertHandCount(playerA, attacker, 1);
        assertCounterCount(planeswalker, CounterType.LOYALTY, 7);
        assertTapped(ninjas, true);
        assertAttacking(ninjas, false);
        assertLife(playerB, 20);
    }
}
