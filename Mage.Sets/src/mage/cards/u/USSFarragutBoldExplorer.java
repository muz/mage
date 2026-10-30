package mage.cards.u;

import mage.abilities.Ability;
import mage.abilities.common.LandfallAbility;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.common.delayed.ReflexiveTriggeredAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.keyword.StationAbility;
import mage.abilities.keyword.StationLevelAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInHand;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author muz
 */
public final class USSFarragutBoldExplorer extends CardImpl {

    public USSFarragutBoldExplorer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{6}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SPACECRAFT);

        // Landfall -- Whenever a land you control enters, you may put a creature card from your hand onto the battlefield. That creature gains haste. At the beginning of the next end step, put it on the bottom of your library. When you do, draw a card.
        this.addAbility(new LandfallAbility(new USSFarragutBoldExplorerEffect()));

        // Station
        this.addAbility(new StationAbility());

        // STATION 7+
        // Flying
        // 4/4
        this.addAbility(new StationLevelAbility(7)
            .withLevelAbility(FlyingAbility.getInstance())
            .withPT(4, 4));
    }

    private USSFarragutBoldExplorer(final USSFarragutBoldExplorer card) {
        super(card);
    }

    @Override
    public USSFarragutBoldExplorer copy() {
        return new USSFarragutBoldExplorer(this);
    }
}

class USSFarragutBoldExplorerEffect extends OneShotEffect {

    USSFarragutBoldExplorerEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "you may put a creature card from your hand onto the battlefield. That creature gains haste. "
            + "At the beginning of the next end step, put it on the bottom of your library. When you do, draw a card";
    }

    private USSFarragutBoldExplorerEffect(final USSFarragutBoldExplorerEffect effect) {
        super(effect);
    }

    @Override
    public USSFarragutBoldExplorerEffect copy() {
        return new USSFarragutBoldExplorerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        TargetCardInHand target = new TargetCardInHand(0, 1, StaticFilters.FILTER_CARD_CREATURE);
        player.choose(Outcome.PutCreatureInPlay, target, source, game);
        Card card = game.getCard(target.getFirstTarget());
        if (card == null || !player.moveCards(card, Zone.BATTLEFIELD, source, game)) {
            return false;
        }
        Permanent permanent = CardUtil.getPermanentFromCardPutToBattlefield(card, game);
        if (permanent == null) {
            return false;
        }
        game.addEffect(new GainAbilityTargetEffect(HasteAbility.getInstance(), Duration.Custom)
            .setTargetPointer(new FixedTarget(permanent, game)), source);
        Effect effect = new USSFarragutBoldExplorerReturnEffect().setTargetPointer(new FixedTarget(permanent, game));
        game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(effect), source);
        return true;
    }
}

class USSFarragutBoldExplorerReturnEffect extends OneShotEffect {

    USSFarragutBoldExplorerReturnEffect() {
        super(Outcome.Benefit);
        staticText = "put it on the bottom of your library. When you do, draw a card";
    }

    private USSFarragutBoldExplorerReturnEffect(final USSFarragutBoldExplorerReturnEffect effect) {
        super(effect);
    }

    @Override
    public USSFarragutBoldExplorerReturnEffect copy() {
        return new USSFarragutBoldExplorerReturnEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (player == null || permanent == null || !permanent.isPhasedIn()) {
            return false;
        }
        if (!player.moveCardToLibraryWithInfo(permanent, source, game, Zone.BATTLEFIELD, false, true)) {
            return false;
        }
        game.processAction();
        game.fireReflexiveTriggeredAbility(new ReflexiveTriggeredAbility(new DrawCardSourceControllerEffect(1), false), source);
        return true;
    }
}
