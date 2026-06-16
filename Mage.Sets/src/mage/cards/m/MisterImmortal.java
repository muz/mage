package mage.cards.m;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ReturnSourceFromGraveyardToBattlefieldEffect;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.game.ExileZone;
import mage.game.Game;
import mage.players.Player;
import mage.util.CardUtil;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;

/**
 *
 * @author muz
 */
public final class MisterImmortal extends CardImpl {

    public MisterImmortal(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.MUTANT);
        this.subtype.add(SubType.HERO);
        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // {2}{G}: Return this card from your graveyard or from exile to the battlefield tapped.
        this.addAbility(new SimpleActivatedAbility(
            Zone.GRAVEYARD,
            new ReturnSourceFromGraveyardToBattlefieldEffect(true, false)
                .setText("Return this card from your graveyard or from exile to the battlefield tapped"),
            new ManaCostsImpl<>("{2}{G}")
        ));
        this.addAbility(new SimpleActivatedAbility(
            Zone.EXILED,
            new MisterImmortalEffect(),
            new ManaCostsImpl<>("{2}{G}")
        ));
    }

    private MisterImmortal(final MisterImmortal card) {
        super(card);
    }

    @Override
    public MisterImmortal copy() {
        return new MisterImmortal(this);
    }
}

class MisterImmortalEffect extends OneShotEffect {

    public MisterImmortalEffect() {
        super(Outcome.Benefit);
        staticText = "Return this card from your graveyard or from exile to the battlefield tapped";
    }

    private MisterImmortalEffect(final MisterImmortalEffect effect) {
        super(effect);
    }

    @Override
    public MisterImmortalEffect copy() {
        return new MisterImmortalEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        UUID exileZoneId = CardUtil.getExileZoneId(game, source.getSourceId(), source.getStackMomentSourceZCC());
        ExileZone exileZone = game.getExile().getExileZone(exileZoneId);
        if (exileZone != null && exileZone.contains(source.getSourceId())) {
            Card card = game.getCard(source.getSourceId());
            if (card != null
                    && controller.moveCards(card, Zone.BATTLEFIELD, source, game, true, false, true, null)) {
                return true;
            }
        }
        return false;
    }
}
