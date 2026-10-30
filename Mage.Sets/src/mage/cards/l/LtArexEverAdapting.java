package mage.cards.l;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.StrangeNewWorldsValue;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.continuous.SetBasePowerToughnessSourceEffect;
import mage.abilities.hint.common.StrangeNewWorldsHint;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CardsImpl;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.PutCards;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.common.TargetCardInHand;

/**
 * @author muz
 */
public final class LtArexEverAdapting extends CardImpl {

    public LtArexEverAdapting(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{5}{G}{U}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.EDOSIAN);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // Strange new worlds -- Lt. Arex's power and toughness are each equal to the number of differently named lands you control.
        this.addAbility(new SimpleStaticAbility(Zone.ALL,
            new SetBasePowerToughnessSourceEffect(StrangeNewWorldsValue.instance)
            .setText("{this}'s power and toughness are each equal to the number of differently named lands you control"))
            .setAbilityWord(AbilityWord.STRANGE_NEW_WORLDS).addHint(StrangeNewWorldsHint.instance));

        // When Lt. Arex enters, draw three cards. You may put up to three land cards from your hand onto the battlefield tapped.
        Ability ability = new EntersBattlefieldTriggeredAbility(new DrawCardSourceControllerEffect(3));
        ability.addEffect(new LtArexEverAdaptingEffect());
        this.addAbility(ability);
    }

    private LtArexEverAdapting(final LtArexEverAdapting card) {
        super(card);
    }

    @Override
    public LtArexEverAdapting copy() {
        return new LtArexEverAdapting(this);
    }
}

class LtArexEverAdaptingEffect extends OneShotEffect {

    LtArexEverAdaptingEffect() {
        super(Outcome.PutLandInPlay);
        staticText = "you may put up to three land cards from your hand onto the battlefield tapped";
    }

    private LtArexEverAdaptingEffect(final LtArexEverAdaptingEffect effect) {
        super(effect);
    }

    @Override
    public LtArexEverAdaptingEffect copy() {
        return new LtArexEverAdaptingEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        TargetCard target = new TargetCardInHand(0, 3, StaticFilters.FILTER_CARD_LANDS);
        player.choose(outcome, player.getHand(), target, source, game);
        return PutCards.BATTLEFIELD_TAPPED.moveCards(player, new CardsImpl(target.getTargets()), source, game);
    }
}
