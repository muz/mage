package org.mage.test.cards.single.mbc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * Chira, All In {3}{R}
 * Legendary Creature - Human Rogue 3/3
 * Whenever you discard one or more cards at random, select one or more targets at random,
 * choose one or more modes at random, flip one or more coins, or roll one or more dice,
 * put a +1/+1 counter on Chira and create a tapped Treasure token.
 */
public class ChiraAllInTest extends CardTestPlayerBase {

    private static final String chira = "Chira, All In";

    private void assertTriggeredOnce() {
        assertCounterCount(playerA, chira, CounterType.P1P1, 1);
        assertPermanentCount(playerA, "Treasure Token", 1);
        assertTapped("Treasure Token", true);
    }

    private void assertNotTriggered() {
        assertCounterCount(playerA, chira, CounterType.P1P1, 0);
        assertPermanentCount(playerA, "Treasure Token", 0);
    }

    @Test
    public void test_RandomDiscard() {
        addCard(Zone.BATTLEFIELD, playerA, chira);
        // {T}, Discard a card at random: Mage il-Vec deals 1 damage to any target.
        addCard(Zone.BATTLEFIELD, playerA, "Mage il-Vec");
        addCard(Zone.HAND, playerA, "Island");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}, Discard a card at random", playerB);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertGraveyardCount(playerA, "Island", 1);
        assertTriggeredOnce();
    }

    @Test
    public void test_ChosenDiscardDoesNotTrigger() {
        addCard(Zone.BATTLEFIELD, playerA, chira);
        // Target player discards two cards.
        addCard(Zone.HAND, playerA, "Mind Rot"); // {2}{B}
        addCard(Zone.HAND, playerA, "Island", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Mind Rot", playerA);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertGraveyardCount(playerA, "Island", 2);
        assertNotTriggered();
    }

    @Test
    public void test_RandomTarget() {
        addCard(Zone.BATTLEFIELD, playerA, chira);
        // Flying
        // {T}: Goblin Test Pilot deals 2 damage to any target chosen at random.
        addCard(Zone.BATTLEFIELD, playerA, "Goblin Test Pilot");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: {this} deals 2 damage");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertTriggeredOnce();
    }

    @Test
    public void test_ChosenTargetDoesNotTrigger() {
        addCard(Zone.BATTLEFIELD, playerA, chira);
        // Lightning Bolt deals 3 damage to any target.
        addCard(Zone.HAND, playerA, "Lightning Bolt");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Lightning Bolt", playerB);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertLife(playerB, 20 - 3);
        assertNotTriggered();
    }

    @Test
    public void test_RandomMode() {
        addCard(Zone.BATTLEFIELD, playerA, chira);
        // Whenever Cult of Skaro attacks, choose one at random --
        // * Thay -- Put a +1/+1 counter on each artifact creature you control.
        // * Caan -- Draw two cards.
        // * Sec -- Create a 3/3 black Dalek artifact creature token with menace.
        // * Jast -- Each opponent loses 4 life.
        addCard(Zone.BATTLEFIELD, playerA, "Cult of Skaro");

        attack(1, playerA, "Cult of Skaro", playerB);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.DECLARE_ATTACKERS);
        execute();

        assertTriggeredOnce();
    }

    @Test
    public void test_FlipCoin() {
        addCard(Zone.BATTLEFIELD, playerA, chira);
        // Flying
        // {0}: Flip a coin. If you win the flip, Frenetic Efreet phases out.
        // If you lose the flip, sacrifice Frenetic Efreet.
        addCard(Zone.BATTLEFIELD, playerA, "Frenetic Efreet");
        setFlipCoinResult(playerA, true);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{0}: Flip a coin");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertTriggeredOnce();
    }

    @Test
    public void test_RollDie() {
        addCard(Zone.BATTLEFIELD, playerA, chira);
        // When this creature enters the battlefield from anywhere other than a graveyard or exile,
        // if it's on the battlefield and you control 9 or fewer creatures named "Name Sticker" Goblin,
        // roll a 20-sided die.
        // 1-6 | Add {R}{R}{R}{R}.
        // 7-14 | Add {R}{R}{R}{R}{R}.
        // 15-20 | Add {R}{R}{R}{R}{R}{R}.
        addCard(Zone.HAND, playerA, "\"Name Sticker\" Goblin"); // {2}{R}
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 3);
        setDieRollResult(playerA, 1);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "\"Name Sticker\" Goblin", true);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertTriggeredOnce();
    }
}
