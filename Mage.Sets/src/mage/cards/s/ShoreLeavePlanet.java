package mage.cards.s;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTappedAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.CopyPermanentEffect;
import mage.abilities.mana.BlueManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.util.functions.CopyApplier;

import java.util.UUID;

/**
 * @author muz
 */
public final class ShoreLeavePlanet extends CardImpl {

    public ShoreLeavePlanet(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        this.subtype.add(SubType.PLANET);

        // This land enters tapped.
        this.addAbility(new EntersBattlefieldTappedAbility());

        // {T}: Add {U}.
        this.addAbility(new BlueManaAbility());

        // {4}{U}: This land becomes a copy of target creature you control until end of turn, except it isn't legendary and it's a Robot artifact in addition to its other types.
        Ability ability = new SimpleActivatedAbility(new CopyPermanentEffect(
            StaticFilters.FILTER_CONTROLLED_CREATURE, new ShoreLeavePlanetCopyApplier(), true
        ).setDuration(Duration.EndOfTurn).setText("{this} becomes a copy of target creature you control until end of turn, "
            + "except it isn't legendary and it's a Robot artifact in addition to its other types"), new ManaCostsImpl<>("{4}{U}"));
        ability.addTarget(new TargetControlledCreaturePermanent());
        this.addAbility(ability);
    }

    private ShoreLeavePlanet(final ShoreLeavePlanet card) {
        super(card);
    }

    @Override
    public ShoreLeavePlanet copy() {
        return new ShoreLeavePlanet(this);
    }
}

class ShoreLeavePlanetCopyApplier extends CopyApplier {

    @Override
    public boolean apply(Game game, MageObject blueprint, Ability source, UUID copyToObjectId) {
        blueprint.removeSuperType(SuperType.LEGENDARY);
        blueprint.addCardType(CardType.ARTIFACT);
        blueprint.addSubType(SubType.ROBOT);
        return true;
    }
}
