package mage.cards.t;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.condition.common.IsStepCondition;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.combat.ReselectDefenderAttackedByTargetEffect;
import mage.abilities.keyword.FlashAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.PhaseStep;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetAttackingCreature;
import mage.target.targetpointer.FixedTarget;

/**
 * @author muz
 */
public final class TuvokChiefTacticalOfficer extends CardImpl {

    public TuvokChiefTacticalOfficer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.VULCAN);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // Flash
        this.addAbility(FlashAbility.getInstance());

        // When Tuvok enters during the declare attackers step, choose up to two target attacking creatures. For each of them, you may reselect which player or permanent that creature is attacking.
        Ability ability = new EntersBattlefieldTriggeredAbility(new TuvokChiefTacticalOfficerEffect())
            .withTriggerCondition(new IsStepCondition(PhaseStep.DECLARE_ATTACKERS, false));
        ability.addTarget(new TargetAttackingCreature(0, 2));
        this.addAbility(ability);
    }

    private TuvokChiefTacticalOfficer(final TuvokChiefTacticalOfficer card) {
        super(card);
    }

    @Override
    public TuvokChiefTacticalOfficer copy() {
        return new TuvokChiefTacticalOfficer(this);
    }
}

class TuvokChiefTacticalOfficerEffect extends OneShotEffect {

    TuvokChiefTacticalOfficerEffect() {
        super(Outcome.Benefit);
        staticText = "choose up to two target attacking creatures. For each of them, "
            + "you may reselect which player or permanent that creature is attacking";
    }

    private TuvokChiefTacticalOfficerEffect(final TuvokChiefTacticalOfficerEffect effect) {
        super(effect);
    }

    @Override
    public TuvokChiefTacticalOfficerEffect copy() {
        return new TuvokChiefTacticalOfficerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        for (UUID targetId : getTargetPointer().getTargets(game, source)) {
            Permanent permanent = game.getPermanent(targetId);
            if (permanent == null || !controller.chooseUse(outcome,
                "Reselect which player or permanent " + permanent.getLogName() + " is attacking?", source, game)) {
                continue;
            }
            new ReselectDefenderAttackedByTargetEffect(true)
                .setTargetPointer(new FixedTarget(permanent, game)).apply(game, source);
        }
        return true;
    }
}
