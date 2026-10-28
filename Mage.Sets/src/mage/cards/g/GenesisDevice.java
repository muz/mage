package mage.cards.g;

import mage.abilities.Ability;
import mage.abilities.common.ActivateAsSorceryActivatedAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.search.SearchLibraryPutInPlayEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.FilterCard;
import mage.filter.StaticFilters;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInLibrary;
import mage.target.common.TargetSacrifice;

import java.util.UUID;

/**
 * @author muz
 */
public final class GenesisDevice extends CardImpl {

    private static final FilterCard filter = new FilterCard("basic land card or Planet card");

    static {
        filter.add(Predicates.or(
            Predicates.and(CardType.LAND.getPredicate(), SuperType.BASIC.getPredicate()),
            SubType.PLANET.getPredicate()
        ));
    }

    public GenesisDevice(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}{G}");
        this.supertype.add(SuperType.LEGENDARY);

        // When Genesis Device enters, search your library for a basic land card or Planet card, put it onto the battlefield tapped, then shuffle.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new SearchLibraryPutInPlayEffect(new TargetCardInLibrary(filter), true)));

        // {3}{G}{G}, {T}, Sacrifice Genesis Device: Sacrifice any number of lands you control, then search your library for up to that many land cards, put them onto the battlefield tapped, then shuffle. Activate only as a sorcery.
        Ability ability = new ActivateAsSorceryActivatedAbility(new GenesisDeviceEffect(), new ManaCostsImpl<>("{3}{G}{G}"));
        ability.addCost(new TapSourceCost());
        ability.addCost(new SacrificeSourceCost());
        this.addAbility(ability);
    }

    private GenesisDevice(final GenesisDevice card) {
        super(card);
    }

    @Override
    public GenesisDevice copy() {
        return new GenesisDevice(this);
    }
}

class GenesisDeviceEffect extends OneShotEffect {

    GenesisDeviceEffect() {
        super(Outcome.PutLandInPlay);
        staticText = "sacrifice any number of lands you control, then search your library for up to that many land cards, "
            + "put them onto the battlefield tapped, then shuffle";
    }

    private GenesisDeviceEffect(final GenesisDeviceEffect effect) {
        super(effect);
    }

    @Override
    public GenesisDeviceEffect copy() {
        return new GenesisDeviceEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetSacrifice target = new TargetSacrifice(0, Integer.MAX_VALUE, StaticFilters.FILTER_LANDS);
        int amount = 0;
        if (controller.choose(Outcome.Sacrifice, target, source, game)) {
            for (UUID targetId : target.getTargets()) {
                Permanent land = game.getPermanent(targetId);
                if (land != null && land.sacrifice(source, game)) {
                    amount++;
                }
            }
        }
        return new SearchLibraryPutInPlayEffect(new TargetCardInLibrary(0, amount, StaticFilters.FILTER_CARD_LANDS), true)
            .apply(game, source);
    }
}
