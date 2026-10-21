package mage.cards.d;

import mage.abilities.Ability;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.StationAbility;
import mage.abilities.keyword.StationLevelAbility;
import mage.abilities.triggers.BeginningOfCombatTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.game.Game;
import mage.game.permanent.token.KlingonWarriorToken;

import java.util.UUID;

/**
 * @author muz
 */
public final class D7BattleCruiser extends CardImpl {

    public D7BattleCruiser(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{1}{R}");

        this.subtype.add(SubType.SPACECRAFT);

        // Station
        this.addAbility(new StationAbility());

        // STATION 3+
        // At the beginning of combat on your turn, create a 2/1 red Klingon Warrior creature token with haste. Sacrifice it at the beginning of the next end step.
        this.addAbility(new StationLevelAbility(3).withLevelAbility(
            new BeginningOfCombatTriggeredAbility(new D7BattleCruiserEffect())
        ));

        // STATION 8+
        // Flying
        // 3/3
        this.addAbility(new StationLevelAbility(8).withLevelAbility(FlyingAbility.getInstance()).withPT(3, 3));
    }

    private D7BattleCruiser(final D7BattleCruiser card) {
        super(card);
    }

    @Override
    public D7BattleCruiser copy() {
        return new D7BattleCruiser(this);
    }
}

class D7BattleCruiserEffect extends CreateTokenEffect {

    D7BattleCruiserEffect() {
        super(new KlingonWarriorToken());
        this.withAdditionalRules("Sacrifice it at the beginning of the next end step.");
    }

    private D7BattleCruiserEffect(final D7BattleCruiserEffect effect) {
        super(effect);
    }

    @Override
    public D7BattleCruiserEffect copy() {
        return new D7BattleCruiserEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        if (!super.apply(game, source)) {
            return false;
        }
        this.removeTokensCreatedAt(game, source, false, PhaseStep.END_TURN, TargetController.ANY);
        return true;
    }
}
