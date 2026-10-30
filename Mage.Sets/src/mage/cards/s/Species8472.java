package mage.cards.s;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.StrangeNewWorldsValue;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.abilities.effects.common.GainLifeEffect;
import mage.abilities.effects.common.combat.CantBeBlockedByCreaturesSourceEffect;
import mage.abilities.hint.common.StrangeNewWorldsHint;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.StackAbility;
import mage.game.stack.StackObject;

import java.util.UUID;

/**
 * @author muz
 */
public final class Species8472 extends CardImpl {

    public Species8472(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{G}{G}");
        this.subtype.add(SubType.INSECT);
        this.subtype.add(SubType.BEAST);
        this.power = new MageInt(7);
        this.toughness = new MageInt(5);

        // Strange new worlds -- When this creature enters, you gain life equal to the number of differently named lands you control.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new GainLifeEffect(StrangeNewWorldsValue.instance))
            .setAbilityWord(AbilityWord.STRANGE_NEW_WORLDS).addHint(StrangeNewWorldsHint.instance));

        // This creature can't be blocked by artifact creatures.
        this.addAbility(new SimpleStaticAbility(new CantBeBlockedByCreaturesSourceEffect(
            StaticFilters.FILTER_PERMANENTS_ARTIFACT_CREATURE, Duration.WhileOnBattlefield
        )));

        // This creature can't be the target of abilities from artifact sources your opponents control.
        this.addAbility(new SimpleStaticAbility(new Species8472Effect()));
    }

    private Species8472(final Species8472 card) {
        super(card);
    }

    @Override
    public Species8472 copy() {
        return new Species8472(this);
    }
}

class Species8472Effect extends ContinuousRuleModifyingEffectImpl {

    Species8472Effect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "{this} can't be the target of abilities from artifact sources your opponents control";
    }

    private Species8472Effect(final Species8472Effect effect) {
        super(effect);
    }

    @Override
    public Species8472Effect copy() {
        return new Species8472Effect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.TARGET;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (!source.getSourceId().equals(event.getTargetId())) {
            return false;
        }
        StackObject stackObject = game.getStack().getStackObject(event.getSourceId());
        if (!(stackObject instanceof StackAbility)
            || !game.getOpponents(source.getControllerId()).contains(stackObject.getControllerId())) {
            return false;
        }
        MageObject abilitySource = ((StackAbility) stackObject).getSourceObject(game);
        return abilitySource != null && abilitySource.isArtifact(game);
    }
}
