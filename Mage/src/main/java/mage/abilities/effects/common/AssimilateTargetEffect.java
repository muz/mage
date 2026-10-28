package mage.abilities.effects.common;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BecomesCreatureTargetEffect;
import mage.abilities.effects.common.continuous.GainControlTargetEffect;
import mage.cards.Card;
import mage.constants.DependencyType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.counters.Counters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.BorgToken;
import mage.players.Player;
import mage.target.targetpointer.FixedTarget;
import mage.target.targetpointer.FixedTargets;
import mage.util.CardUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author muz
 */
public class AssimilateTargetEffect extends OneShotEffect {

    public AssimilateTargetEffect() {
        super(Outcome.GainControl);
    }

    protected AssimilateTargetEffect(final AssimilateTargetEffect effect) {
        super(effect);
    }

    @Override
    public AssimilateTargetEffect copy() {
        return new AssimilateTargetEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return !assimilate(getTargetPointer().getTargets(game, source), source, game).isEmpty();
    }

    /**
     * Also supports cards chosen without targeting. Returned references identify
     * the resulting permanents, for effects that refer to them later.
     */
    public static Set<MageObjectReference> assimilate(Collection<UUID> objectIds, Ability source, Game game) {
        Set<MageObjectReference> assimilated = new LinkedHashSet<>();
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return assimilated;
        }

        List<MageObjectReference> permanents = new ArrayList<>();
        List<MageObjectReference> enteringCards = new ArrayList<>();
        List<MageObjectReference> affectedObjects = new ArrayList<>();
        Map<Zone, Set<Card>> cardsByZone = new EnumMap<>(Zone.class);
        for (UUID objectId : new LinkedHashSet<>(objectIds)) {
            Permanent permanent = game.getPermanent(objectId);
            if (permanent != null) {
                MageObjectReference reference = new MageObjectReference(permanent, game);
                permanents.add(reference);
                affectedObjects.add(reference);
                game.addEffect(new GainControlTargetEffect(Duration.Custom, true)
                    .setTargetPointer(new FixedTarget(reference)), source);
                continue;
            }
            Card card = game.getCard(objectId);
            if (card == null) {
                continue;
            }
            // A card returned by an effect enters on its front face, even when
            // the supplied ID belongs to the back face.
            card = card.getMainCard();
            Zone zone = game.getState().getZone(card.getId());
            if (zone == null || zone == Zone.BATTLEFIELD) {
                continue;
            }
            Card enteringCard = CardUtil.getDefaultCardSideForBattlefield(game, card);
            if (!enteringCard.isPermanent(game)
                || !cardsByZone.computeIfAbsent(zone, key -> new LinkedHashSet<>()).add(card)) {
                continue;
            }
            enteringCards.add(new MageObjectReference(enteringCard, game));
            affectedObjects.add(new MageObjectReference(enteringCard, game, 1));
        }
        if (affectedObjects.isEmpty()) {
            return assimilated;
        }

        // Existing creatures change controllers before receiving their counters.
        if (!permanents.isEmpty()) {
            game.applyEffects();
            for (MageObjectReference reference : permanents) {
                Permanent permanent = reference.getPermanent(game);
                if (permanent != null) {
                    permanent.addCounters(CounterType.P1P1.createInstance(), source, game);
                }
            }
        }

        AssimilateContinuousEffect effect = new AssimilateContinuousEffect(affectedObjects, enteringCards);
        Ability effectSource = source.copy();
        effectSource.initSourceObjectZoneChangeCounter(game, true);
        effect.init(effectSource, game);
        // Keep this initialized instance so pending entry references can be
        // cleared when the move attempt finishes, including prevented moves.
        game.getState().addEffect(effect, effectSource);
        try {
            for (MageObjectReference reference : enteringCards) {
                game.setEnterWithCounters(reference.getSourceId(), new Counters(CounterType.P1P1.createInstance()));
            }
            for (Set<Card> cards : cardsByZone.values()) {
                controller.moveCards(cards, Zone.BATTLEFIELD, source, game);
            }
        } finally {
            for (MageObjectReference reference : enteringCards) {
                game.setEnterWithCounters(reference.getSourceId(), null);
            }
            effect.finishEntering(game);
        }
        game.applyEffects();
        for (MageObjectReference reference : affectedObjects) {
            if (reference.getPermanent(game) != null) {
                assimilated.add(reference);
            }
        }
        return assimilated;
    }

    @Override
    public String getText(Mode mode) {
        if (staticText != null && !staticText.isEmpty()) {
            return staticText;
        }
        boolean plural = getTargetPointer().isPlural(mode.getTargets());
        boolean battlefield = !mode.getTargets().isEmpty() && mode.getTargets().get(0).getZone() == Zone.BATTLEFIELD;
        String action = battlefield
            ? "Gain control of " + (plural ? "them" : "it") + " and put a +1/+1 counter on "
            + (plural ? "each of them" : "it")
            : "Put " + (plural ? "them" : "it") + " onto the battlefield under your control with a +1/+1 counter"
            + (plural ? " on each of them" : "");
        return "assimilate " + getTargetPointer().describeTargets(mode.getTargets(), "that card")
            + ". <i>(" + action + ". " + (plural ? "They're Borg artifact creatures and lose" : "It's a Borg artifact creature and loses")
            + " all other creature types.)</i>";
    }
}

class AssimilateContinuousEffect extends BecomesCreatureTargetEffect {

    private final List<MageObjectReference> enteringCards = new ArrayList<>();

    AssimilateContinuousEffect(Collection<MageObjectReference> affectedObjects, Collection<MageObjectReference> enteringCards) {
        super(new BorgToken(), false, false, Duration.Custom);
        affectedObjectList.addAll(affectedObjects);
        this.enteringCards.addAll(enteringCards);
        setTargetPointer(new FixedTargets(affectedObjects));
        dependencyTypes.add(DependencyType.ArtifactAddingRemoving);
    }

    private AssimilateContinuousEffect(final AssimilateContinuousEffect effect) {
        super(effect);
        this.enteringCards.addAll(effect.enteringCards);
    }

    @Override
    public AssimilateContinuousEffect copy() {
        return new AssimilateContinuousEffect(this);
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.TypeChangingEffects_4;
    }

    @Override
    public boolean apply(Layer layer, SubLayer sublayer, Ability source, Game game) {
        boolean applied = false;
        for (MageObjectReference reference : affectedObjectList) {
            Permanent permanent = reference.getPermanent(game);
            if (permanent != null) {
                applyTypes(permanent, game);
                applied = true;
            }
        }
        if (!applied && enteringCards.isEmpty()) {
            discard();
        }
        return applied;
    }

    @Override
    public void applyToEnteringPermanent(Permanent permanent, Ability source, Game game) {
        if (enteringCards.stream().anyMatch(reference -> reference.refersTo(permanent, game))) {
            applyTypes(permanent, game);
        }
    }

    void finishEntering(Game game) {
        enteringCards.clear();
        affectedObjectList.removeIf(reference -> reference.getPermanent(game) == null);
    }
}
