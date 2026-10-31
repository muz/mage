package org.mage.test.cards.modal;

import mage.abilities.SpellAbility;
import mage.cards.Card;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Before;
import org.junit.Test;
import org.mage.test.player.TestPlayer;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author muz
 */
public class FaceDilemmaTest extends CardTestPlayerBase {

    // Seven of Nine — Whenever you face a dilemma, draw a card.
    private static final String SEVEN = "Seven of Nine";
    // Melkotian Adjudicator — Flying, vigilance
    // Whenever you face a dilemma, put a +1/+1 counter on this creature.
    private static final String MELKOTIAN = "Melkotian Adjudicator";

    // Basic lands: Island, Swamp, Forest, Mountain, and Plains have their respective
    // intrinsic mana abilities: {T}: Add {U}, {B}, {G}, {R}, or {W}.
    private static final String ISLAND = "Island";
    private static final String SWAMP = "Swamp";
    private static final String FOREST = "Forest";
    private static final String MOUNTAIN = "Mountain";
    private static final String PLAINS = "Plains";
    // Grizzly Bears — 2/2 creature with no abilities.
    private static final String BEARS = "Grizzly Bears";

    // Funeral Charm — Choose one —
    // * Target player discards a card.
    // * Target creature gets +2/-1 until end of turn.
    // * Target creature gains swampwalk until end of turn.
    private static final String FUNERAL_CHARM = "Funeral Charm";
    // Boros Charm — Choose one — [relevant mode]
    // * Boros Charm deals 4 damage to target player or planeswalker.
    private static final String BOROS_CHARM = "Boros Charm";
    // Call Damage Control — Choose up to two. Return those cards from your graveyard to your hand.
    // * Target artifact card. * Target creature card. * Target enchantment card. * Target land card.
    private static final String CALL_DAMAGE_CONTROL = "Call Damage Control";

    @Before
    public void enableStrictChoices() {
        setStrictChooseMode(true);
    }

    private void runToCombat() {
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();
    }

    @Test
    public void drawResolvesBeforeTheModalSpell() {
        skipInitShuffling();
        addCard(Zone.BATTLEFIELD, playerA, SEVEN);
        addCard(Zone.BATTLEFIELD, playerA, SWAMP);
        addCard(Zone.LIBRARY, playerA, FOREST);
        addCard(Zone.HAND, playerA, FUNERAL_CHARM);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, FUNERAL_CHARM, playerA);
        setModeChoice(playerA, "1");
        setChoice(playerA, FOREST);

        runToCombat();

        // The hand was empty after casting; Seven must draw before the discard resolves.
        assertGraveyardCount(playerA, FOREST, 1);
        assertGraveyardCount(playerA, FUNERAL_CHARM, 1);
        assertHandCount(playerA, 0);
    }

    @Test
    public void repeatedModesCountAsOneDilemma() {
        addCard(Zone.BATTLEFIELD, playerA, MELKOTIAN);
        addCard(Zone.BATTLEFIELD, playerA, SWAMP, 5);
        // Wretched Confluence — Choose three. You may choose the same mode more than once.
        // * Target player draws a card and loses 1 life. [relevant mode]
        addCard(Zone.HAND, playerA, "Wretched Confluence");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Wretched Confluence");
        setModeChoice(playerA, "1");
        setModeChoice(playerA, "1");
        setModeChoice(playerA, "1");
        addTarget(playerA, playerA);
        addTarget(playerA, playerA);
        addTarget(playerA, playerA);

        runToCombat();

        assertCounterCount(playerA, MELKOTIAN, CounterType.P1P1, 1);
        assertHandCount(playerA, 3);
        assertLife(playerA, 17);
    }

    @Test
    public void automaticEntwineSelectionCountsOnce() {
        addCard(Zone.BATTLEFIELD, playerA, MELKOTIAN);
        addCard(Zone.BATTLEFIELD, playerA, MOUNTAIN, 5);
        addCard(Zone.BATTLEFIELD, playerB, BEARS);
        // Barbed Lightning — Choose one —
        // * Barbed Lightning deals 3 damage to target creature.
        // * Barbed Lightning deals 3 damage to target player or planeswalker.
        // Entwine {2} (Choose both if you pay the entwine cost.)
        addCard(Zone.HAND, playerA, "Barbed Lightning");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Barbed Lightning");
        setChoice(playerA, true);
        addTarget(playerA, BEARS);
        addTarget(playerA, playerB);

        runToCombat();

        assertCounterCount(playerA, MELKOTIAN, CounterType.P1P1, 1);
        assertGraveyardCount(playerB, BEARS, 1);
        assertLife(playerB, 17);
    }

    @Test
    public void opponentChoosingModesFacesTheDilemma() {
        addCard(Zone.BATTLEFIELD, playerA, SEVEN);
        addCard(Zone.BATTLEFIELD, playerB, SEVEN);
        addCard(Zone.BATTLEFIELD, playerA, ISLAND, 5);
        // Library of Lat-Nam — An opponent chooses one — [relevant mode]
        // * You draw three cards at the beginning of the next turn's upkeep.
        addCard(Zone.HAND, playerA, "Library of Lat-Nam");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Library of Lat-Nam");
        setChoice(playerA, playerB.getName());
        setModeChoice(playerB, "1");

        runToCombat();

        assertHandCount(playerA, 0);
        assertHandCount(playerB, 1);
    }

    @Test
    public void controllerPreselectionCannotReplaceTheOpponentsChoice() {
        addCard(Zone.BATTLEFIELD, playerA, SEVEN);
        addCard(Zone.BATTLEFIELD, playerB, SEVEN);
        addCard(Zone.BATTLEFIELD, playerA, ISLAND, 5);
        addCard(Zone.HAND, playerA, "Library of Lat-Nam");

        runCode("cast with the controller's preselected mode", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            Card library = player.getHand().getCards(game).iterator().next();
            UUID searchMode = library.getSpellAbility().getModes().getModeId(2);
            SpellAbility option = (SpellAbility) player.getPlayableOptions(library.getSpellAbility(), game).stream()
                .filter(ability -> ability.getModes().getSelectedModes().contains(searchMode))
                .findFirst().orElseThrow(() -> new AssertionError("Missing search option"));
            assertTrue(player.cast(option, game, false, null));
        });
        setChoice(playerA, playerB.getName());
        setModeChoice(playerB, "1");

        runToCombat();

        assertHandCount(playerA, 0);
        assertHandCount(playerB, 1);
    }

    @Test
    public void choosingARandomModeFacesOneDilemma() {
        addCard(Zone.BATTLEFIELD, playerA, MELKOTIAN);
        // Cult of Skaro — Whenever Cult of Skaro attacks, choose one at random —
        // * Thay — Put a +1/+1 counter on each artifact creature you control.
        // * Caan — Draw two cards.
        // * Sec — Create a 3/3 black Dalek artifact creature token with menace.
        // * Jast — Each opponent loses 4 life.
        addCard(Zone.BATTLEFIELD, playerA, "Cult of Skaro");

        attack(1, playerA, "Cult of Skaro", playerB);

        setStopAt(1, PhaseStep.END_COMBAT);
        execute();

        // None of the random modes puts a counter on the nonartifact Adjudicator.
        assertCounterCount(playerA, MELKOTIAN, CounterType.P1P1, 1);
    }

    @Test
    public void modalTriggeredAbilityFacesADilemma() {
        addCard(Zone.BATTLEFIELD, playerA, MELKOTIAN);
        addCard(Zone.BATTLEFIELD, playerA, FOREST, 2);
        addCard(Zone.BATTLEFIELD, playerA, PLAINS);
        // Knight of Autumn — When Knight of Autumn enters, choose one — [relevant mode]
        // * Put two +1/+1 counters on Knight of Autumn.
        addCard(Zone.HAND, playerA, "Knight of Autumn");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Knight of Autumn");
        setModeChoice(playerA, "1");

        runToCombat();

        assertCounterCount(playerA, MELKOTIAN, CounterType.P1P1, 1);
        assertCounterCount(playerA, "Knight of Autumn", CounterType.P1P1, 2);
    }

    @Test
    public void modalActivatedAbilityWithOnlyOneLegalModeFacesADilemma() {
        addCard(Zone.BATTLEFIELD, playerA, MELKOTIAN);
        addCard(Zone.BATTLEFIELD, playerA, FOREST);
        addCard(Zone.BATTLEFIELD, playerB, BEARS);
        // Goblin Cratermaker — {1}, Sacrifice Goblin Cratermaker: Choose one —
        // * Goblin Cratermaker deals 2 damage to target creature.
        // * Destroy target colorless nonland permanent.
        addCard(Zone.BATTLEFIELD, playerA, "Goblin Cratermaker");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{1}, Sacrifice Goblin Cratermaker", BEARS);
        setModeChoice(playerA, "1");

        runToCombat();

        assertCounterCount(playerA, MELKOTIAN, CounterType.P1P1, 1);
        assertGraveyardCount(playerA, "Goblin Cratermaker", 1);
        assertGraveyardCount(playerB, BEARS, 1);
    }

    @Test
    public void decliningOptionalModesDoesNotFaceADilemma() {
        addCard(Zone.BATTLEFIELD, playerA, MELKOTIAN);
        addCard(Zone.BATTLEFIELD, playerA, FOREST, 2);
        addCard(Zone.GRAVEYARD, playerA, BEARS);
        addCard(Zone.HAND, playerA, CALL_DAMAGE_CONTROL);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, CALL_DAMAGE_CONTROL);
        setModeChoice(playerA, TestPlayer.MODE_SKIP);

        runToCombat();

        assertCounterCount(playerA, MELKOTIAN, CounterType.P1P1, 0);
        assertGraveyardCount(playerA, BEARS, 1);
        assertGraveyardCount(playerA, CALL_DAMAGE_CONTROL, 1);
    }

    @Test
    public void noAvailableModesDoesNotFaceADilemma() {
        addCard(Zone.BATTLEFIELD, playerA, MELKOTIAN);
        addCard(Zone.BATTLEFIELD, playerA, FOREST, 2);
        addCard(Zone.HAND, playerA, CALL_DAMAGE_CONTROL);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, CALL_DAMAGE_CONTROL);

        runToCombat();

        assertCounterCount(playerA, MELKOTIAN, CounterType.P1P1, 0);
        assertGraveyardCount(playerA, CALL_DAMAGE_CONTROL, 1);
    }

    @Test
    public void triggeredAbilityWithNoLegalModesDoesNotFaceADilemma() {
        addCard(Zone.BATTLEFIELD, playerA, MELKOTIAN);
        addCard(Zone.BATTLEFIELD, playerA, SWAMP, 2);
        // Satsuki, the Living Lore — When Satsuki dies, choose up to one —
        // * Return target Saga or enchantment creature you control to its owner's hand.
        // * Return target Saga card from your graveyard to your hand.
        addCard(Zone.BATTLEFIELD, playerA, "Satsuki, the Living Lore");
        // Doom Blade — Destroy target nonblack creature.
        addCard(Zone.HAND, playerA, "Doom Blade");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Doom Blade", "Satsuki, the Living Lore");

        runToCombat();

        assertCounterCount(playerA, MELKOTIAN, CounterType.P1P1, 0);
        assertGraveyardCount(playerA, "Satsuki, the Living Lore", 1);
    }

    @Test
    public void choosingAColorIsNotChoosingAMode() {
        addCard(Zone.BATTLEFIELD, playerA, MELKOTIAN);
        addCard(Zone.BATTLEFIELD, playerA, PLAINS);
        // Brave the Elements — Choose a color. White creatures you control gain protection
        // from the chosen color until end of turn.
        addCard(Zone.HAND, playerA, "Brave the Elements");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Brave the Elements");
        setChoice(playerA, "Blue");

        runToCombat();

        assertCounterCount(playerA, MELKOTIAN, CounterType.P1P1, 0);
    }

    @Test
    public void copyingAModalSpellDoesNotFaceAnotherDilemma() {
        addCard(Zone.BATTLEFIELD, playerA, MELKOTIAN);
        addCard(Zone.BATTLEFIELD, playerA, MOUNTAIN, 3);
        addCard(Zone.BATTLEFIELD, playerA, PLAINS);
        addCard(Zone.HAND, playerA, BOROS_CHARM);
        // Reverberate — Copy target instant or sorcery spell. You may choose new targets for the copy.
        addCard(Zone.HAND, playerA, "Reverberate");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, BOROS_CHARM, playerB);
        setModeChoice(playerA, "1");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Reverberate", BOROS_CHARM, BOROS_CHARM);
        setChoice(playerA, false);

        runToCombat();

        assertCounterCount(playerA, MELKOTIAN, CounterType.P1P1, 1);
        assertLife(playerB, 12);
    }

    @Test
    public void counteringTheModalSpellDoesNotUndoTheDilemma() {
        addCard(Zone.BATTLEFIELD, playerA, MELKOTIAN);
        addCard(Zone.BATTLEFIELD, playerA, MOUNTAIN);
        addCard(Zone.BATTLEFIELD, playerA, PLAINS);
        addCard(Zone.BATTLEFIELD, playerB, ISLAND, 2);
        addCard(Zone.HAND, playerA, BOROS_CHARM);
        // Counterspell — Counter target spell.
        addCard(Zone.HAND, playerB, "Counterspell");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, BOROS_CHARM, playerB);
        setModeChoice(playerA, "1");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerB, "Counterspell", BOROS_CHARM, BOROS_CHARM);

        runToCombat();

        assertCounterCount(playerA, MELKOTIAN, CounterType.P1P1, 1);
        assertLife(playerB, 20);
        assertGraveyardCount(playerA, BOROS_CHARM, 1);
    }

    @Test
    public void aiPreselectedModesFaceOneDilemma() {
        addCard(Zone.BATTLEFIELD, playerA, MELKOTIAN);
        addCard(Zone.BATTLEFIELD, playerA, SWAMP);
        addCard(Zone.HAND, playerA, FUNERAL_CHARM);

        runCode("cast with AI-preselected modes and targets", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            Card charm = player.getHand().getCards(game).iterator().next();
            UUID swampwalkMode = charm.getSpellAbility().getModes().getModeId(3);
            SpellAbility option = (SpellAbility) player.getPlayableOptions(charm.getSpellAbility(), game).stream()
                .filter(ability -> ability.getModes().getSelectedModes().contains(swampwalkMode))
                .findFirst().orElseThrow(() -> new AssertionError("Missing swampwalk option"));
            assertTrue(player.cast(option, game, false, null));
        });

        runToCombat();

        assertCounterCount(playerA, MELKOTIAN, CounterType.P1P1, 1);
        assertGraveyardCount(playerA, FUNERAL_CHARM, 1);
    }

    @Test
    public void failedCastingRollsBackTheDilemma() {
        addCard(Zone.BATTLEFIELD, playerA, MELKOTIAN);
        addCard(Zone.HAND, playerA, FUNERAL_CHARM);

        runCode("attempt to cast without mana", 1, PhaseStep.PRECOMBAT_MAIN, playerA, (info, player, game) -> {
            Card charm = player.getHand().getCards(game).iterator().next();
            assertFalse(player.cast(charm.getSpellAbility(), game, false, null));
        });
        setModeChoice(playerA, "3");
        addTarget(playerA, MELKOTIAN);

        runToCombat();

        assertCounterCount(playerA, MELKOTIAN, CounterType.P1P1, 0);
        assertHandCount(playerA, FUNERAL_CHARM, 1);
    }
}
