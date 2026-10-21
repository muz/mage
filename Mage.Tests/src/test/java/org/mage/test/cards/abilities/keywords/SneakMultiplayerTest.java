package org.mage.test.cards.abilities.keywords;

import mage.constants.MultiplayerAttackOption;
import mage.constants.PhaseStep;
import mage.constants.RangeOfInfluence;
import mage.constants.Zone;
import mage.game.FreeForAll;
import mage.game.Game;
import mage.game.GameException;
import mage.game.mulligan.MulliganType;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestMultiPlayerBase;

import java.io.FileNotFoundException;

/**
 * @author muz
 */
public class SneakMultiplayerTest extends CardTestMultiPlayerBase {

    @Override
    protected Game createNewGameAndPlayers() throws GameException, FileNotFoundException {
        Game game = new FreeForAll(
                MultiplayerAttackOption.MULTIPLE, RangeOfInfluence.ALL,
                MulliganType.GAME_DEFAULT.getMulligan(0), 20, 7
        );
        playerA = createPlayer(game, "PlayerA");
        playerB = createPlayer(game, "PlayerB");
        playerC = createPlayer(game, "PlayerC");
        playerD = createPlayer(game, "PlayerD");
        return game;
    }

    @Test
    public void usesTheReturnedAttackersDefender() {
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 4);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Silvercoat Lion");
        addCard(Zone.HAND, playerA, "Foot Ninjas");

        attack(1, playerA, "Grizzly Bears", playerC);
        attack(1, playerA, "Silvercoat Lion", playerB);
        castSpell(1, PhaseStep.DECLARE_BLOCKERS, playerA, "Foot Ninjas with Sneak");
        setChoice(playerA, "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.COMBAT_DAMAGE);
        execute();

        assertTapped("Foot Ninjas", true);
        assertAttacking("Foot Ninjas", true);
        Assert.assertEquals(playerC.getId(), getPermanent("Foot Ninjas").getAttacking().getSourceId());
        assertLife(playerB, 18);
        assertLife(playerC, 15);
        assertLife(playerD, 20);
    }

    @Test
    public void concurrentSneakSpellsKeepSeparateDefenders() {
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 5);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Silvercoat Lion");
        addCard(Zone.HAND, playerA, "Foot Ninjas");
        addCard(Zone.HAND, playerA, "Splinter, Hamato Yoshi");

        attack(1, playerA, "Grizzly Bears", playerC);
        attack(1, playerA, "Silvercoat Lion", playerB);
        castSpell(1, PhaseStep.DECLARE_BLOCKERS, playerA, "Foot Ninjas with Sneak");
        setChoice(playerA, "Grizzly Bears");
        castSpell(1, PhaseStep.DECLARE_BLOCKERS, playerA, "Splinter, Hamato Yoshi with Sneak");
        setChoice(playerA, "Silvercoat Lion");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.COMBAT_DAMAGE);
        execute();

        assertHandCount(playerA, "Grizzly Bears", 1);
        assertHandCount(playerA, "Silvercoat Lion", 1);
        assertTapped("Foot Ninjas", true);
        assertTapped("Splinter, Hamato Yoshi", true);
        Assert.assertEquals(playerC.getId(), getPermanent("Foot Ninjas").getAttacking().getSourceId());
        Assert.assertEquals(playerB.getId(), getPermanent("Splinter, Hamato Yoshi").getAttacking().getSourceId());
        assertLife(playerB, 19);
        assertLife(playerC, 14); // Splinter boosts Foot Ninjas to 6/6
        assertLife(playerD, 20);
    }
}
