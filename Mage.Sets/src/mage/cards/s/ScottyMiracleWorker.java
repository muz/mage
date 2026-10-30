package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.common.ReturnToHandChosenControlledPermanentCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.BeamMeUpAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CardWithHalves;
import mage.cards.CardWithSpellOption;
import mage.cards.TransformingDoubleFacedCard;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.FilterPermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.MageObjectReferencePredicate;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetPermanent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author muz
 */
public final class ScottyMiracleWorker extends CardImpl {

    public ScottyMiracleWorker(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // During your main phase, each card in your graveyard has beam me up. The beam me up cost is equal to that spell's mana cost. Scotty can't be beamed up to cast a spell this way.
        this.addAbility(new SimpleStaticAbility(new ScottyMiracleWorkerEffect()));
    }

    private ScottyMiracleWorker(final ScottyMiracleWorker card) {
        super(card);
    }

    @Override
    public ScottyMiracleWorker copy() {
        return new ScottyMiracleWorker(this);
    }
}

class ScottyMiracleWorkerEffect extends ContinuousEffectImpl {

    ScottyMiracleWorkerEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "during your main phase, each card in your graveyard has beam me up. "
            + "The beam me up cost is equal to that spell's mana cost. {this} can't be beamed up to cast a spell this way";
    }

    private ScottyMiracleWorkerEffect(final ScottyMiracleWorkerEffect effect) {
        super(effect);
    }

    @Override
    public ScottyMiracleWorkerEffect copy() {
        return new ScottyMiracleWorkerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null || !game.isActivePlayer(player.getId()) || !game.isMainPhase()) {
            return false;
        }
        for (Card card : player.getGraveyard().getCards(game)) {
            List<Card> parts = new ArrayList<>();
            if (card instanceof TransformingDoubleFacedCard) {
                parts.add(((TransformingDoubleFacedCard) card).getLeftHalfCard());
            } else if (card instanceof CardWithHalves) {
                parts.add(((CardWithHalves) card).getLeftHalfCard());
                parts.add(((CardWithHalves) card).getRightHalfCard());
            } else {
                parts.add(card);
                if (card instanceof CardWithSpellOption) {
                    parts.add(((CardWithSpellOption) card).getSpellCard());
                }
            }
            for (Card part : parts) {
                if (part.getSpellAbility() == null) {
                    continue;
                }
                BeamMeUpAbility ability = new BeamMeUpAbility(part, part.getManaCost().getText());
                for (Cost cost : ability.getCosts()) {
                    if (cost instanceof ReturnToHandChosenControlledPermanentCost) {
                        TargetPermanent target = (TargetPermanent) cost.getTargets().get(0);
                        FilterPermanent filter = target.getFilter().copy();
                        filter.add(Predicates.not(new MageObjectReferencePredicate(source.getSourceId(), game)));
                        target.replaceFilter(filter);
                    }
                }
                ability.setAbilityName(part.getName());
                game.getState().addOtherAbility(part, ability);
            }
        }
        return true;
    }
}
