package mage.cards.k;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.combat.GoadTargetEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.predicate.permanent.GoadedPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.target.common.TargetOpponentsCreaturePermanent;
import mage.target.targetpointer.FixedTargets;

/**
 * @author muz
 */
public final class KangSuspiciousCaptain extends CardImpl {

    public KangSuspiciousCaptain(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.KLINGON);
        this.subtype.add(SubType.WARRIOR);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // When Kang enters, choose target creature an opponent controls. Kang and that creature are goaded for as long as you control Kang.
        Ability ability = new EntersBattlefieldTriggeredAbility(new KangSuspiciousCaptainEffect());
        ability.addTarget(new TargetOpponentsCreaturePermanent());
        this.addAbility(ability);

        // Whenever one or more goaded creatures attack, put a +1/+1 counter on target creature you control.
        this.addAbility(new KangSuspiciousCaptainTriggeredAbility());
    }

    private KangSuspiciousCaptain(final KangSuspiciousCaptain card) {
        super(card);
    }

    @Override
    public KangSuspiciousCaptain copy() {
        return new KangSuspiciousCaptain(this);
    }
}

class KangSuspiciousCaptainEffect extends OneShotEffect {

    KangSuspiciousCaptainEffect() {
        super(Outcome.Detriment);
        staticText = "choose target creature an opponent controls. "
            + "{this} and that creature are goaded for as long as you control {this}";
    }

    private KangSuspiciousCaptainEffect(final KangSuspiciousCaptainEffect effect) {
        super(effect);
    }

    @Override
    public KangSuspiciousCaptainEffect copy() {
        return new KangSuspiciousCaptainEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent kang = source.getSourcePermanentIfItStillExists(game);
        Permanent target = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (kang == null || target == null || !kang.isControlledBy(source.getControllerId())) {
            return false;
        }
        game.addEffect(new GoadTargetEffect(Duration.WhileControlled)
            .setTargetPointer(new FixedTargets(Arrays.asList(kang, target), game)), source);
        return true;
    }
}

class KangSuspiciousCaptainTriggeredAbility extends TriggeredAbilityImpl {

    KangSuspiciousCaptainTriggeredAbility() {
        super(Zone.BATTLEFIELD, new AddCountersTargetEffect(CounterType.P1P1.createInstance()));
        setTriggerPhrase("Whenever one or more goaded creatures attack, ");
        addTarget(new TargetControlledCreaturePermanent());
    }

    private KangSuspiciousCaptainTriggeredAbility(final KangSuspiciousCaptainTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public KangSuspiciousCaptainTriggeredAbility copy() {
        return new KangSuspiciousCaptainTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DECLARED_ATTACKERS;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return game.getCombat().getAttackers().stream()
            .map(game::getPermanent)
            .filter(Objects::nonNull)
            .anyMatch(permanent -> GoadedPredicate.instance.apply(permanent, game));
    }
}
