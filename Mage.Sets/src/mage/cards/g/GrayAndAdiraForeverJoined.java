package mage.cards.g;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.condition.common.CastNoncreatureSpellThisTurnCondition;
import mage.abilities.effects.common.CreateTokenCopyTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.triggers.BeginningOfCombatTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.game.Game;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.target.targetpointer.FixedTargets;
import mage.watchers.common.SpellsCastWatcher;

import java.util.UUID;

/**
 * @author muz
 */
public final class GrayAndAdiraForeverJoined extends CardImpl {

    public GrayAndAdiraForeverJoined(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U/R}{U/R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.TRILL);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // At the beginning of combat on your turn, if you've cast a noncreature spell this turn, create a token that's a copy of target creature you control, except it isn't legendary. It gains haste until end of turn. Sacrifice it at the beginning of the next end step.
        Ability ability = new BeginningOfCombatTriggeredAbility(new GrayAndAdiraForeverJoinedEffect())
            .withInterveningIf(CastNoncreatureSpellThisTurnCondition.instance);
        ability.addTarget(new TargetControlledCreaturePermanent());
        this.addAbility(ability.addHint(CastNoncreatureSpellThisTurnCondition.getHint()), new SpellsCastWatcher());
    }

    private GrayAndAdiraForeverJoined(final GrayAndAdiraForeverJoined card) {
        super(card);
    }

    @Override
    public GrayAndAdiraForeverJoined copy() {
        return new GrayAndAdiraForeverJoined(this);
    }
}

class GrayAndAdiraForeverJoinedEffect extends CreateTokenCopyTargetEffect {

    GrayAndAdiraForeverJoinedEffect() {
        this.setIsntLegendary(true);
        staticText = "create a token that's a copy of target creature you control, except it isn't legendary. "
            + "It gains haste until end of turn. Sacrifice it at the beginning of the next end step";
    }

    private GrayAndAdiraForeverJoinedEffect(final GrayAndAdiraForeverJoinedEffect effect) {
        super(effect);
    }

    @Override
    public GrayAndAdiraForeverJoinedEffect copy() {
        return new GrayAndAdiraForeverJoinedEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        if (!super.apply(game, source)) {
            return false;
        }
        game.addEffect(new GainAbilityTargetEffect(HasteAbility.getInstance())
            .setTargetPointer(new FixedTargets(getAddedPermanents(), game)), source);
        this.sacrificeTokensCreatedAtNextEndStep(game, source);
        return true;
    }
}
