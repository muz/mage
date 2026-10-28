package mage.cards.g;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.common.FilterControlledPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.NeurofluxHeirloomToken;
import mage.game.permanent.token.Token;
import mage.target.common.TargetControlledPermanent;
import mage.util.functions.EmptyCopyApplier;

import java.util.UUID;

/**
 * @author muz
 */
public final class GwyndalaTheProgeny extends CardImpl {

    private static final FilterControlledPermanent filter = new FilterControlledPermanent("Equipment you control");

    static {
        filter.add(SubType.EQUIPMENT.getPredicate());
    }

    public GwyndalaTheProgeny(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.VAU_N_AKAT);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // When Gwyndala enters, create a colorless Equipment artifact token named Neuroflux Heirloom with "Equipped creature gets +1/+1" and equip {2}. Attach it to Gwyndala.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new GwyndalaTheProgenyCreateEffect()));

        // {2}: Each Equipment token you control becomes a copy of target Equipment you control until end of turn.
        Ability ability = new SimpleActivatedAbility(new GwyndalaTheProgenyCopyEffect(), new GenericManaCost(2));
        ability.addTarget(new TargetControlledPermanent(filter));
        this.addAbility(ability);
    }

    private GwyndalaTheProgeny(final GwyndalaTheProgeny card) {
        super(card);
    }

    @Override
    public GwyndalaTheProgeny copy() {
        return new GwyndalaTheProgeny(this);
    }
}

class GwyndalaTheProgenyCreateEffect extends OneShotEffect {

    GwyndalaTheProgenyCreateEffect() {
        super(Outcome.BoostCreature);
        staticText = "create a colorless Equipment artifact token named Neuroflux Heirloom with "
            + "\"Equipped creature gets +1/+1\" and equip {2}. Attach it to {this}";
    }

    private GwyndalaTheProgenyCreateEffect(final GwyndalaTheProgenyCreateEffect effect) {
        super(effect);
    }

    @Override
    public GwyndalaTheProgenyCreateEffect copy() {
        return new GwyndalaTheProgenyCreateEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Token token = new NeurofluxHeirloomToken();
        token.putOntoBattlefield(1, game, source);
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent != null) {
            for (UUID tokenId : token.getLastAddedTokenIds()) {
                permanent.addAttachment(tokenId, source, game);
            }
        }
        return true;
    }
}

class GwyndalaTheProgenyCopyEffect extends OneShotEffect {

    GwyndalaTheProgenyCopyEffect() {
        super(Outcome.Copy);
        staticText = "each Equipment token you control becomes a copy of target Equipment you control until end of turn";
    }

    private GwyndalaTheProgenyCopyEffect(final GwyndalaTheProgenyCopyEffect effect) {
        super(effect);
    }

    @Override
    public GwyndalaTheProgenyCopyEffect copy() {
        return new GwyndalaTheProgenyCopyEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent equipment = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (equipment == null) {
            return false;
        }
        for (Permanent permanent : game.getBattlefield().getActivePermanents(source.getControllerId(), game)) {
            if (permanent.isControlledBy(source.getControllerId())
                && permanent.isToken() && permanent.hasSubtype(SubType.EQUIPMENT, game)) {
                game.copyPermanent(Duration.EndOfTurn, equipment, permanent.getId(), source, new EmptyCopyApplier());
            }
        }
        return true;
    }
}
