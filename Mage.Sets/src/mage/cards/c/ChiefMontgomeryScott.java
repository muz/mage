package mage.cards.c;

import java.util.UUID;
import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.dynamicvalue.common.StrangeNewWorldsValue;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.hint.common.StrangeNewWorldsHint;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.FilterCard;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.events.EntersTheBattlefieldEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.common.TargetCardInHand;

/**
 * @author muz
 */
public final class ChiefMontgomeryScott extends CardImpl {

    public ChiefMontgomeryScott(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}{U}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Strange new worlds -- {6}, {T}: You may put a creature or Spacecraft card from your hand onto the battlefield.
        // It enters with X additional +1/+1 counters on it if it's a creature and X additional charge counters on it
        // if it's a Spacecraft, where X is the number of differently named lands you control.
        Ability ability = new SimpleActivatedAbility(new ChiefMontgomeryScottEffect(), new ManaCostsImpl<>("{6}"));
        ability.addCost(new TapSourceCost());
        this.addAbility(ability.setAbilityWord(AbilityWord.STRANGE_NEW_WORLDS).addHint(StrangeNewWorldsHint.instance));
    }

    private ChiefMontgomeryScott(final ChiefMontgomeryScott card) {
        super(card);
    }

    @Override
    public ChiefMontgomeryScott copy() {
        return new ChiefMontgomeryScott(this);
    }
}

class ChiefMontgomeryScottEffect extends OneShotEffect {

    private static final FilterCard filter = new FilterCard("creature or Spacecraft card");

    static {
        filter.add(Predicates.or(CardType.CREATURE.getPredicate(), SubType.SPACECRAFT.getPredicate()));
    }

    ChiefMontgomeryScottEffect() {
        super(Outcome.PutCardInPlay);
        staticText = "you may put a creature or Spacecraft card from your hand onto the battlefield. "
            + "It enters with X additional +1/+1 counters on it if it's a creature and X additional charge counters on it "
            + "if it's a Spacecraft, where X is the number of differently named lands you control";
    }

    private ChiefMontgomeryScottEffect(final ChiefMontgomeryScottEffect effect) {
        super(effect);
    }

    @Override
    public ChiefMontgomeryScottEffect copy() {
        return new ChiefMontgomeryScottEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        TargetCard target = new TargetCardInHand(0, 1, filter);
        player.choose(outcome, player.getHand(), target, source, game);
        Card card = game.getCard(target.getFirstTarget());
        if (card == null) {
            return false;
        }
        ChiefMontgomeryScottCountersEffect effect = new ChiefMontgomeryScottCountersEffect(new MageObjectReference(card, game));
        effect.init(source, game);
        // Keep this instance so the entry replacement can be discarded even if the card cannot enter.
        game.getState().addEffect(effect, source);
        try {
            return player.moveCards(card, Zone.BATTLEFIELD, source, game);
        } finally {
            effect.discard();
        }
    }
}

class ChiefMontgomeryScottCountersEffect extends ReplacementEffectImpl {

    private final MageObjectReference mor;

    ChiefMontgomeryScottCountersEffect(MageObjectReference mor) {
        super(Duration.Custom, Outcome.BoostCreature);
        this.mor = mor;
    }

    private ChiefMontgomeryScottCountersEffect(final ChiefMontgomeryScottCountersEffect effect) {
        super(effect);
        this.mor = effect.mor;
    }

    @Override
    public ChiefMontgomeryScottCountersEffect copy() {
        return new ChiefMontgomeryScottCountersEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        Permanent permanent = ((EntersTheBattlefieldEvent) event).getTarget();
        return permanent != null && mor.refersTo(permanent, game);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Permanent permanent = ((EntersTheBattlefieldEvent) event).getTarget();
        int amount = StrangeNewWorldsValue.instance.calculate(game, source, this);
        if (permanent.isCreature(game)) {
            permanent.addCounters(CounterType.P1P1.createInstance(amount), source.getControllerId(), source, game, event.getAppliedEffects());
        }
        if (permanent.hasSubtype(SubType.SPACECRAFT, game)) {
            permanent.addCounters(CounterType.CHARGE.createInstance(amount), source.getControllerId(), source, game, event.getAppliedEffects());
        }
        return false;
    }
}
