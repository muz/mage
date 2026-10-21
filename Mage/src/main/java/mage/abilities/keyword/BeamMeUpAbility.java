package mage.abilities.keyword;

import mage.abilities.costs.common.ReturnToHandChosenControlledPermanentCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.cards.Card;
import mage.constants.SpellAbilityCastMode;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.ObjectSourcePlayerPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.common.TargetControlledPermanent;

/**
 * @author muz
 */
public class BeamMeUpAbility extends CastFromGraveyardAbility {

    private static final FilterControlledCreaturePermanent filter = new FilterControlledCreaturePermanent();

    static {
        filter.add(BeamMeUpPredicate.instance);
    }

    public BeamMeUpAbility(Card card, String manaString) {
        super(card, new ManaCostsImpl<>(manaString), SpellAbilityCastMode.BEAM_ME_UP);
        this.addCost(new ReturnToHandChosenControlledPermanentCost(new TargetControlledPermanent(filter)));
    }

    private BeamMeUpAbility(final BeamMeUpAbility ability) {
        super(ability);
    }

    @Override
    public BeamMeUpAbility copy() {
        return new BeamMeUpAbility(this);
    }

    @Override
    public String getRule() {
        return name + " <i>(You may cast this card from your graveyard for " + getManaCosts().getText()
            + " if you also return a creature you control to its owner's hand. Then exile this spell.)</i>";
    }
}

enum BeamMeUpPredicate implements ObjectSourcePlayerPredicate<Permanent> {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<Permanent> input, Game game) {
        return !game.replaceEvent(GameEvent.getEvent(GameEvent.EventType.BEAM_UP,
            input.getObject().getId(), input.getSource(), input.getPlayerId()));
    }
}
