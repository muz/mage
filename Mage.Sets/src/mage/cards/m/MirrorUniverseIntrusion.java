package mage.cards.m;

import mage.abilities.Ability;
import mage.abilities.condition.common.KickedCondition;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenCopyTargetEffect;
import mage.abilities.keyword.KickerAbility;
import mage.abilities.keyword.MenaceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.common.TargetCreatureOrPlayer;
import mage.target.common.TargetOpponent;
import mage.target.targetadjustment.ConditionalTargetAdjuster;
import mage.target.targetpointer.FixedTargets;

import java.util.UUID;

/**
 * @author muz
 */
public final class MirrorUniverseIntrusion extends CardImpl {

    public MirrorUniverseIntrusion(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{1}{U}{B}");

        // Kicker {4}{U}{B}
        this.addAbility(new KickerAbility("{4}{U}{B}"));

        // Create a token that's a copy of target creature an opponent controls, except it has menace. If this spell was kicked, instead for each creature target opponent controls, create a token that's a copy of that creature, except it has menace.
        this.getSpellAbility().addEffect(new MirrorUniverseIntrusionEffect());
        this.getSpellAbility().addTarget(new TargetCreatureOrPlayer());
        this.getSpellAbility().setTargetAdjuster(new ConditionalTargetAdjuster(KickedCondition.ONCE,
            new TargetPermanent(StaticFilters.FILTER_OPPONENTS_PERMANENT_CREATURE), new TargetOpponent()));
    }

    private MirrorUniverseIntrusion(final MirrorUniverseIntrusion card) {
        super(card);
    }

    @Override
    public MirrorUniverseIntrusion copy() {
        return new MirrorUniverseIntrusion(this);
    }
}

class MirrorUniverseIntrusionEffect extends OneShotEffect {

    MirrorUniverseIntrusionEffect() {
        super(Outcome.Benefit);
        staticText = "create a token that's a copy of target creature an opponent controls, except it has menace. If this spell was kicked, instead for each creature target opponent controls, create a token that's a copy of that creature, except it has menace";
    }

    private MirrorUniverseIntrusionEffect(final MirrorUniverseIntrusionEffect effect) {
        super(effect);
    }

    @Override
    public MirrorUniverseIntrusionEffect copy() {
        return new MirrorUniverseIntrusionEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        CreateTokenCopyTargetEffect effect = new CreateTokenCopyTargetEffect().addAdditionalAbilities(MenaceAbility.getInstance());
        if (KickedCondition.ONCE.apply(game, source)) {
            Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
            if (opponent == null) {
                return false;
            }
            effect.setTargetPointer(new FixedTargets(game.getBattlefield().getAllActivePermanents(
                StaticFilters.FILTER_PERMANENT_CREATURE, opponent.getId(), game), game));
        } else {
            effect.setTargetPointer(getTargetPointer().copy());
        }
        return effect.apply(game, source);
    }
}
