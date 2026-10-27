package mage.cards.k;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.effects.common.ChooseACardNameEffect;
import mage.abilities.effects.common.LoseLifeTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author muz
 */
public final class KaiWinnFaithlessLeader extends CardImpl {

    public KaiWinnFaithlessLeader(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.BAJORAN);
        this.subtype.add(SubType.CLERIC);
        this.power = new MageInt(3);
        this.toughness = new MageInt(1);

        // As Kai Winn enters, choose a nonland card name.
        this.addAbility(new AsEntersBattlefieldAbility(new ChooseACardNameEffect(ChooseACardNameEffect.TypeOfName.NON_LAND_NAME)));

        // Whenever an opponent casts a spell with the chosen name or activates an ability of a source with the chosen name, that player loses 3 life.
        this.addAbility(new KaiWinnFaithlessLeaderTriggeredAbility());
    }

    private KaiWinnFaithlessLeader(final KaiWinnFaithlessLeader card) {
        super(card);
    }

    @Override
    public KaiWinnFaithlessLeader copy() {
        return new KaiWinnFaithlessLeader(this);
    }
}

class KaiWinnFaithlessLeaderTriggeredAbility extends TriggeredAbilityImpl {

    KaiWinnFaithlessLeaderTriggeredAbility() {
        super(Zone.BATTLEFIELD, new LoseLifeTargetEffect(3).setText("that player loses 3 life"));
        setTriggerPhrase("Whenever an opponent casts a spell with the chosen name or activates an ability of a source with the chosen name, ");
    }

    private KaiWinnFaithlessLeaderTriggeredAbility(final KaiWinnFaithlessLeaderTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public KaiWinnFaithlessLeaderTriggeredAbility copy() {
        return new KaiWinnFaithlessLeaderTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.SPELL_CAST
            || event.getType() == GameEvent.EventType.ACTIVATED_ABILITY
            || event.getType() == GameEvent.EventType.MANA_ABILITY_ACTIVATED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!game.getOpponents(getControllerId()).contains(event.getPlayerId())) {
            return false;
        }
        MageObject object;
        if (event.getType() == GameEvent.EventType.SPELL_CAST) {
            object = game.getSpell(event.getTargetId());
        } else if (event.getZone() == Zone.BATTLEFIELD) {
            object = game.getPermanentOrLKIBattlefield(event.getSourceId());
        } else {
            object = game.getObject(event.getSourceId());
        }
        String name = (String) game.getState().getValue(getSourceId() + ChooseACardNameEffect.INFO_KEY);
        if (object == null || name == null || !CardUtil.haveSameNames(object, name, game)) {
            return false;
        }
        getEffects().setTargetPointer(new FixedTarget(event.getPlayerId()));
        return true;
    }
}
