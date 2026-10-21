package mage.cards.k;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.effects.common.DoIfCostPaid;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AsThoughEffectType;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;

import java.util.UUID;

/**
 * @author muz
 */
public final class KhanVengefulHeart extends CardImpl {

    public KhanVengefulHeart(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WARRIOR);
        this.power = new MageInt(5);
        this.toughness = new MageInt(3);

        // When Khan enters, you may sacrifice another creature or artifact. If you do, draw two cards.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new DoIfCostPaid(
            new DrawCardSourceControllerEffect(2), new SacrificeTargetCost(StaticFilters.FILTER_CONTROLLED_ANOTHER_CREATURE_OR_ARTIFACT)
        )));

        // You may cast this card from exile.
        this.addAbility(new SimpleStaticAbility(Zone.EXILED, new KhanVengefulHeartEffect()));
    }

    private KhanVengefulHeart(final KhanVengefulHeart card) {
        super(card);
    }

    @Override
    public KhanVengefulHeart copy() {
        return new KhanVengefulHeart(this);
    }
}

class KhanVengefulHeartEffect extends AsThoughEffectImpl {

    KhanVengefulHeartEffect() {
        super(AsThoughEffectType.CAST_FROM_NOT_OWN_HAND_ZONE, Duration.EndOfGame, Outcome.Benefit);
        staticText = "you may cast this card from exile";
    }

    private KhanVengefulHeartEffect(final KhanVengefulHeartEffect effect) {
        super(effect);
    }

    @Override
    public KhanVengefulHeartEffect copy() {
        return new KhanVengefulHeartEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID affectedControllerId, Game game) {
        Card card = game.getCard(objectId);
        return objectId.equals(source.getSourceId()) && card != null && card.isOwnedBy(affectedControllerId)
            && game.getState().getZone(objectId) == Zone.EXILED;
    }
}
