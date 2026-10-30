package mage.cards.t;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.FightTargetsEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.common.TargetOpponentsCreaturePermanent;

import java.util.UUID;

/**
 * @author muz
 */
public final class TPringNeverParted extends CardImpl {

    public TPringNeverParted(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.VULCAN);
        this.subtype.add(SubType.CITIZEN);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Kal-if-fee -- When T'Pring enters, choose another target creature you control and target creature an opponent controls. You may have the chosen creatures fight each other. Whenever a creature that fought this way dies this turn, you draw a card.
        Ability ability = new EntersBattlefieldTriggeredAbility(new TPringNeverPartedEffect());
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_ANOTHER_TARGET_CREATURE_YOU_CONTROL));
        ability.addTarget(new TargetOpponentsCreaturePermanent());
        this.addAbility(ability.withFlavorWord("Kal-if-fee"));
    }

    private TPringNeverParted(final TPringNeverParted card) {
        super(card);
    }

    @Override
    public TPringNeverParted copy() {
        return new TPringNeverParted(this);
    }
}

class TPringNeverPartedEffect extends OneShotEffect {

    TPringNeverPartedEffect() {
        super(Outcome.Benefit);
        staticText = "choose another target creature you control and target creature an opponent controls. "
            + "You may have the chosen creatures fight each other. "
            + "Whenever a creature that fought this way dies this turn, you draw a card";
    }

    private TPringNeverPartedEffect(final TPringNeverPartedEffect effect) {
        super(effect);
    }

    @Override
    public TPringNeverPartedEffect copy() {
        return new TPringNeverPartedEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        Permanent first = game.getPermanent(source.getTargets().get(0).getFirstTarget());
        Permanent second = game.getPermanent(source.getTargets().get(1).getFirstTarget());
        if (player == null || first == null || second == null
            || !source.getTargets().get(0).isLegal(source, game)
            || !source.getTargets().get(1).isLegal(source, game)
            || !player.chooseUse(Outcome.Benefit, "Have the chosen creatures fight each other?", source, game)) {
            return false;
        }
        MageObjectReference firstReference = new MageObjectReference(first, game);
        MageObjectReference secondReference = new MageObjectReference(second, game);
        if (!new FightTargetsEffect().apply(game, source)) {
            return false;
        }
        game.addDelayedTriggeredAbility(
            new TPringNeverPartedDelayedTriggeredAbility(firstReference, secondReference), source);
        return true;
    }
}

class TPringNeverPartedDelayedTriggeredAbility extends DelayedTriggeredAbility {

    private final Set<MageObjectReference> creatures;

    TPringNeverPartedDelayedTriggeredAbility(MageObjectReference first, MageObjectReference second) {
        super(new DrawCardSourceControllerEffect(1), Duration.EndOfTurn, false);
        creatures = new HashSet<>(Arrays.asList(first, second));
        setTriggerPhrase("Whenever a creature that fought this way dies this turn, ");
    }

    private TPringNeverPartedDelayedTriggeredAbility(final TPringNeverPartedDelayedTriggeredAbility ability) {
        super(ability);
        creatures = new HashSet<>(ability.creatures);
    }

    @Override
    public TPringNeverPartedDelayedTriggeredAbility copy() {
        return new TPringNeverPartedDelayedTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        ZoneChangeEvent zEvent = (ZoneChangeEvent) event;
        return zEvent.isDiesEvent()
            && creatures.stream().anyMatch(creature -> creature.refersTo(zEvent.getTarget(), game));
    }
}
