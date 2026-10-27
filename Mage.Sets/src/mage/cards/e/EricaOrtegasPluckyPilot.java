package mage.cards.e;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.BecomesTappedSourceTriggeredAbility;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.PutCardFromHandOntoBattlefieldEffect;
import mage.abilities.effects.common.ReturnToHandChosenControlledPermanentEffect;
import mage.abilities.keyword.HasteAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledLandPermanent;
import mage.game.Game;

import java.util.UUID;

/**
 * @author muz
 */
public final class EricaOrtegasPluckyPilot extends CardImpl {

    public EricaOrtegasPluckyPilot(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.PILOT);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Haste
        this.addAbility(HasteAbility.getInstance());

        // Whenever Erica Ortegas becomes tapped, you may put a land card from your hand onto the battlefield. If you do, return a land you control to its owner's hand at the beginning of the next end step.
        this.addAbility(new BecomesTappedSourceTriggeredAbility(new EricaOrtegasPluckyPilotEffect()));
    }

    private EricaOrtegasPluckyPilot(final EricaOrtegasPluckyPilot card) {
        super(card);
    }

    @Override
    public EricaOrtegasPluckyPilot copy() {
        return new EricaOrtegasPluckyPilot(this);
    }
}

class EricaOrtegasPluckyPilotEffect extends OneShotEffect {

    EricaOrtegasPluckyPilotEffect() {
        super(Outcome.Benefit);
        staticText = "you may put a land card from your hand onto the battlefield. If you do, return a land you control to its owner's hand at the beginning of the next end step";
    }

    private EricaOrtegasPluckyPilotEffect(final EricaOrtegasPluckyPilotEffect effect) {
        super(effect);
    }

    @Override
    public EricaOrtegasPluckyPilotEffect copy() {
        return new EricaOrtegasPluckyPilotEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        if (new PutCardFromHandOntoBattlefieldEffect(StaticFilters.FILTER_CARD_LAND_A).apply(game, source)) {
            game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(
                new ReturnToHandChosenControlledPermanentEffect(new FilterControlledLandPermanent())
            ), source);
        }
        return true;
    }
}
