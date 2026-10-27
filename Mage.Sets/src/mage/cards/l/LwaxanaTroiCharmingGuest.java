package mage.cards.l;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SkipUntapOptionalAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.ReplacementEffectImpl;
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
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author muz
 */
public final class LwaxanaTroiCharmingGuest extends CardImpl {

    public LwaxanaTroiCharmingGuest(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.BETAZOID);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // You may choose not to untap Lwaxana Troi during your untap step.
        this.addAbility(new SkipUntapOptionalAbility());

        // {2}, {T}: Choose another target creature you control. For as long as Lwaxana Troi remains tapped, if a triggered ability of that creature triggers, that ability triggers an additional time.
        Ability ability = new SimpleActivatedAbility(new LwaxanaTroiCharmingGuestEffect(), new GenericManaCost(2));
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_ANOTHER_CREATURE_YOU_CONTROL));
        this.addAbility(ability);
    }

    private LwaxanaTroiCharmingGuest(final LwaxanaTroiCharmingGuest card) {
        super(card);
    }

    @Override
    public LwaxanaTroiCharmingGuest copy() {
        return new LwaxanaTroiCharmingGuest(this);
    }
}

class LwaxanaTroiCharmingGuestEffect extends ReplacementEffectImpl {

    private MageObjectReference creature;

    LwaxanaTroiCharmingGuestEffect() {
        super(Duration.Custom, Outcome.Benefit);
        staticText = "choose another target creature you control. For as long as {this} remains tapped, "
            + "if a triggered ability of that creature triggers, that ability triggers an additional time";
    }

    private LwaxanaTroiCharmingGuestEffect(final LwaxanaTroiCharmingGuestEffect effect) {
        super(effect);
        this.creature = effect.creature;
    }

    @Override
    public LwaxanaTroiCharmingGuestEffect copy() {
        return new LwaxanaTroiCharmingGuestEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null || isInactive(source, game)) {
            discard();
        } else {
            creature = new MageObjectReference(permanent, game);
        }
    }

    @Override
    public boolean isInactive(Ability source, Game game) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        return permanent == null || !permanent.isTapped();
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.NUMBER_OF_TRIGGERS;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return creature != null && !isInactive(source, game)
            && creature.refersTo(game.getPermanentOrLKIBattlefield(event.getSourceId()), game);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        event.setAmount(CardUtil.overflowInc(event.getAmount(), 1));
        return false;
    }
}
