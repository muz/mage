package mage.cards.u;

import java.util.UUID;
import mage.abilities.Ability;
import mage.abilities.common.SagaAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.PutCardFromHandOrGraveyardOntoBattlefieldEffect;
import mage.abilities.effects.common.ShuffleIntoLibraryTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SagaChapter;
import mage.constants.SubType;
import mage.filter.FilterCard;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.players.PlayerList;
import mage.target.common.TargetCreaturePermanent;
import mage.target.common.TargetOpponent;
import mage.target.targetpointer.FixedTarget;

/**
 * @author muz
 */
public final class Unification extends CardImpl {

    private static final FilterCard filter = new FilterCard("Spacecraft card");

    static {
        filter.add(SubType.SPACECRAFT.getPredicate());
    }

    public Unification(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{W}{U}");

        this.subtype.add(SubType.SAGA);

        // (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
        SagaAbility sagaAbility = new SagaAbility(this);

        // I -- The owner of up to one target creature draws two cards, then shuffles that creature into their library.
        sagaAbility.addChapterEffect(this, SagaChapter.CHAPTER_I,
            new UnificationEffect(), new TargetCreaturePermanent(0, 1));

        // II -- You may put a Spacecraft card from your hand or graveyard onto the battlefield.
        sagaAbility.addChapterEffect(this, SagaChapter.CHAPTER_II,
            new PutCardFromHandOrGraveyardOntoBattlefieldEffect(filter, false));

        // III -- You and target opponent each draw two cards.
        sagaAbility.addChapterEffect(this, SagaChapter.CHAPTER_III,
            new UnificationDrawEffect(), new TargetOpponent());
        this.addAbility(sagaAbility);
    }

    private Unification(final Unification card) {
        super(card);
    }

    @Override
    public Unification copy() {
        return new Unification(this);
    }
}

class UnificationEffect extends OneShotEffect {

    UnificationEffect() {
        super(Outcome.ReturnToHand);
        staticText = "the owner of up to one target creature draws two cards, "
            + "then shuffles that creature into their library";
    }

    private UnificationEffect(final UnificationEffect effect) {
        super(effect);
    }

    @Override
    public UnificationEffect copy() {
        return new UnificationEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null) {
            return false;
        }
        Player owner = game.getPlayer(permanent.getOwnerId());
        if (owner == null) {
            return false;
        }
        Effect effect = new ShuffleIntoLibraryTargetEffect().setTargetPointer(new FixedTarget(permanent, game));
        owner.drawCards(2, source, game);
        return effect.apply(game, source);
    }
}

class UnificationDrawEffect extends OneShotEffect {

    UnificationDrawEffect() {
        super(Outcome.DrawCard);
        staticText = "you and target opponent each draw two cards";
    }

    private UnificationDrawEffect(final UnificationDrawEffect effect) {
        super(effect);
    }

    @Override
    public UnificationDrawEffect copy() {
        return new UnificationDrawEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        UUID targetId = getTargetPointer().getFirst(game, source);
        PlayerList players = game.getPlayerList().copy();
        players.setCurrent(game.getActivePlayerId());
        for (UUID playerId : players) {
            if (!playerId.equals(source.getControllerId()) && !playerId.equals(targetId)) {
                continue;
            }
            Player player = game.getPlayer(playerId);
            if (player != null) {
                player.drawCards(2, source, game);
            }
        }
        return true;
    }
}
