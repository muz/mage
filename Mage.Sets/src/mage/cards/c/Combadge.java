package mage.cards.c;

import mage.abilities.Ability;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.effects.common.CastSourceTriggeredAbility;
import mage.abilities.effects.common.CopySourceSpellEffect;
import mage.abilities.effects.common.combat.CantBeBlockedSourceEffect;
import mage.abilities.effects.common.continuous.GainAbilityAttachedEffect;
import mage.abilities.keyword.EquipAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AttachmentType;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.filter.StaticFilters;
import mage.game.Game;

import java.util.UUID;

/**
 * @author muz
 */
public final class Combadge extends CardImpl {

    public Combadge(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{3}");

        this.subtype.add(SubType.COMMUNICATOR);
        this.subtype.add(SubType.EQUIPMENT);

        // When you cast this spell, copy it.
        this.addAbility(new CastSourceTriggeredAbility(new CopySourceSpellEffect().setText("copy it")));

        // Equipped creature has "Whenever this creature attacks while you control another creature with a Communicator attached to it, this creature can't be blocked this turn."
        Ability ability = new AttacksTriggeredAbility(new CantBeBlockedSourceEffect(Duration.EndOfTurn))
            .withTriggerCondition(CombadgeCondition.instance)
            .withRuleTextReplacement(false);
        this.addAbility(new SimpleStaticAbility(new GainAbilityAttachedEffect(ability, AttachmentType.EQUIPMENT)));

        // Equip {2}
        this.addAbility(new EquipAbility(2));
    }

    private Combadge(final Combadge card) {
        super(card);
    }

    @Override
    public Combadge copy() {
        return new Combadge(this);
    }
}

enum CombadgeCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        return game.getBattlefield().getAllActivePermanents(
            StaticFilters.FILTER_PERMANENT_CREATURE, source.getControllerId(), game
        ).stream()
            .filter(creature -> !creature.getId().equals(source.getSourceId()))
            .flatMap(creature -> creature.getAttachments().stream())
            .map(game::getPermanent)
            .anyMatch(attachment -> attachment != null
                && attachment.isPhasedIn()
                && attachment.hasSubtype(SubType.COMMUNICATOR, game));
    }

    @Override
    public String toString() {
        return "you control another creature with a Communicator attached to it";
    }
}
