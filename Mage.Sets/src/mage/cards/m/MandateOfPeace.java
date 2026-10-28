package mage.cards.m;

import java.util.UUID;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.CastOnlyDuringPhaseStepSourceAbility;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.abilities.effects.common.EndCombatPhaseEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.TurnPhase;
import mage.game.Game;
import mage.game.events.GameEvent;

/**
 *
 * @author goesta
 */
public final class MandateOfPeace extends CardImpl {

    public MandateOfPeace(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{W}");

        // Cast this spell only during combat.
        this.addAbility(new CastOnlyDuringPhaseStepSourceAbility(TurnPhase.COMBAT));

        // Your opponents can't cast spells this turn.
        this.getSpellAbility().addEffect(new MandateOfPeaceOpponentsCantCastSpellsEffect());

        // End the combat phase.
        this.getSpellAbility().addEffect(new EndCombatPhaseEffect().concatBy("<br>"));
    }

    private MandateOfPeace(final MandateOfPeace card) {
        super(card);
    }

    @Override
    public MandateOfPeace copy() {
        return new MandateOfPeace(this);
    }
}

class MandateOfPeaceOpponentsCantCastSpellsEffect extends ContinuousRuleModifyingEffectImpl {

    MandateOfPeaceOpponentsCantCastSpellsEffect() {
        super(Duration.EndOfTurn, Outcome.Benefit);
        staticText = "Your opponents can't cast spells this turn";
    }

    private MandateOfPeaceOpponentsCantCastSpellsEffect(final MandateOfPeaceOpponentsCantCastSpellsEffect effect) {
        super(effect);
    }

    @Override
    public MandateOfPeaceOpponentsCantCastSpellsEffect copy() {
        return new MandateOfPeaceOpponentsCantCastSpellsEffect(this);
    }

    @Override
    public String getInfoMessage(Ability source, GameEvent event, Game game) {
        MageObject mageObject = game.getObject(source);
        if (mageObject != null) {
            return "You can't cast spells this turn (" + mageObject.getIdName() + ").";
        }
        return null;
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.CAST_SPELL;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return game.getOpponents(source.getControllerId()).contains(event.getPlayerId());
    }

}
