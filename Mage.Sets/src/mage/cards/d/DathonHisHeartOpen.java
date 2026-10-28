package mage.cards.d;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.ActivateAsSorceryActivatedAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DamagePlayersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.choices.Choice;
import mage.choices.ChoiceImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.TargetController;
import mage.counters.Counter;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.target.common.TargetControlledPermanent;

import java.util.LinkedHashSet;
import java.util.UUID;

/**
 * @author muz
 */
public final class DathonHisHeartOpen extends CardImpl {

    public DathonHisHeartOpen(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{G}{U}{R}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.TAMARIAN);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Whenever you cast an enchantment spell, draw a card. If it's a Saga spell, Dathon deals 2 damage to each opponent.
        this.addAbility(new SpellCastControllerTriggeredAbility(
            new DathonHisHeartOpenCastEffect(), StaticFilters.FILTER_SPELL_AN_ENCHANTMENT, false
        ));

        // {2}: Choose a counter on target enchantment you control. Put another counter of that kind on it. Activate only as a sorcery.
        Ability ability = new ActivateAsSorceryActivatedAbility(new DathonHisHeartOpenCounterEffect(), new GenericManaCost(2));
        ability.addTarget(new TargetControlledPermanent(StaticFilters.FILTER_CONTROLLED_PERMANENT_ENCHANTMENT));
        this.addAbility(ability);
    }

    private DathonHisHeartOpen(final DathonHisHeartOpen card) {
        super(card);
    }

    @Override
    public DathonHisHeartOpen copy() {
        return new DathonHisHeartOpen(this);
    }
}

class DathonHisHeartOpenCastEffect extends OneShotEffect {

    DathonHisHeartOpenCastEffect() {
        super(Outcome.DrawCard);
        staticText = "draw a card. If it's a Saga spell, {this} deals 2 damage to each opponent";
    }

    private DathonHisHeartOpenCastEffect(final DathonHisHeartOpenCastEffect effect) {
        super(effect);
    }

    @Override
    public DathonHisHeartOpenCastEffect copy() {
        return new DathonHisHeartOpenCastEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        controller.drawCards(1, source, game);
        Spell spell = (Spell) getValue("spellCast");
        if (spell != null && spell.hasSubtype(SubType.SAGA, game)) {
            new DamagePlayersEffect(2, TargetController.OPPONENT).apply(game, source);
        }
        return true;
    }
}

class DathonHisHeartOpenCounterEffect extends OneShotEffect {

    DathonHisHeartOpenCounterEffect() {
        super(Outcome.Benefit);
        staticText = "choose a counter on target enchantment you control. Put another counter of that kind on it";
    }

    private DathonHisHeartOpenCounterEffect(final DathonHisHeartOpenCounterEffect effect) {
        super(effect);
    }

    @Override
    public DathonHisHeartOpenCounterEffect copy() {
        return new DathonHisHeartOpenCounterEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (controller == null || permanent == null || permanent.getCounters(game).isEmpty()) {
            return false;
        }
        Choice choice = new ChoiceImpl(true);
        choice.setMessage("Choose a counter");
        choice.setChoices(new LinkedHashSet<>(permanent.getCounters(game).keySet()));
        if (!controller.choose(outcome, choice, game)) {
            return false;
        }
        permanent.addCounters(new Counter(choice.getChoice()), source.getControllerId(), source, game);
        return true;
    }
}
