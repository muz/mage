package mage.game.combat;

import mage.MageObjectReference;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Describes an attacking entry before entry replacements are applied.
 * A replacement that adds attacking must preserve any specified defender.
 *
 * @author muz
 */
public final class AttackingCreatureEntry implements Serializable {

    private final MageObjectReference defender;
    private final UUID defendingPlayer;
    private final Set<UUID> possibleDefenders;

    public AttackingCreatureEntry() {
        this(null, null, null);
    }

    public AttackingCreatureEntry(MageObjectReference defender) {
        this(defender, null, null);
    }

    private AttackingCreatureEntry(MageObjectReference defender, UUID defendingPlayer, Set<UUID> possibleDefenders) {
        this.defender = defender;
        this.defendingPlayer = defendingPlayer;
        this.possibleDefenders = possibleDefenders == null
            ? null : Collections.unmodifiableSet(new LinkedHashSet<>(possibleDefenders));
    }

    public static AttackingCreatureEntry attackingPlayer(UUID playerId) {
        return new AttackingCreatureEntry(null, playerId, null);
    }

    public static AttackingCreatureEntry attackingDefenders(Set<UUID> defenderIds) {
        return new AttackingCreatureEntry(null, null, defenderIds);
    }

    public Set<UUID> getPossibleDefenders() {
        return possibleDefenders;
    }

    public MageObjectReference getDefender() {
        return defender;
    }

    public UUID getDefendingPlayer() {
        return defendingPlayer;
    }
}
