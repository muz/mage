package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.FilterCard;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.ObjectSourcePlayerPredicate;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.permanent.token.OfficerToken;
import mage.players.Player;
import mage.util.CardUtil;
import mage.watchers.common.DilemmasFacedWatcher;

import java.util.UUID;

/**
 * @author muz
 */
public final class CaptainBenjaminSisko extends CardImpl {

    public CaptainBenjaminSisko(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // At the beginning of your end step, create a 1/1 red Officer creature token for each dilemma you've faced this turn.
        this.addAbility(new BeginningOfEndStepTriggeredAbility(new CaptainBenjaminSiskoTokenEffect()), new DilemmasFacedWatcher());

        // {2}{W}{U}{B}{R}{G}, {T}: Exile the top five cards of your library. You may cast any number of spells with mana value less than or equal to the number of Officers you control from among those cards without paying their mana costs.
        Ability ability = new SimpleActivatedAbility(new CaptainBenjaminSiskoCastEffect(), new ManaCostsImpl<>("{2}{W}{U}{B}{R}{G}"));
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);
    }

    private CaptainBenjaminSisko(final CaptainBenjaminSisko card) {
        super(card);
    }

    @Override
    public CaptainBenjaminSisko copy() {
        return new CaptainBenjaminSisko(this);
    }
}

class CaptainBenjaminSiskoTokenEffect extends OneShotEffect {

    CaptainBenjaminSiskoTokenEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "create a 1/1 red Officer creature token for each dilemma you've faced this turn";
    }

    private CaptainBenjaminSiskoTokenEffect(final CaptainBenjaminSiskoTokenEffect effect) {
        super(effect);
    }

    @Override
    public CaptainBenjaminSiskoTokenEffect copy() {
        return new CaptainBenjaminSiskoTokenEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        DilemmasFacedWatcher watcher = game.getState().getWatcher(DilemmasFacedWatcher.class);
        int amount = watcher == null ? 0 : watcher.getCount(source.getControllerId());
        return new CreateTokenEffect(new OfficerToken(), amount).apply(game, source);
    }
}

class CaptainBenjaminSiskoCastEffect extends OneShotEffect {

    private static final FilterCard filter = new FilterCard("spell with mana value less than or equal to the number of Officers you control");

    static {
        filter.add(CaptainBenjaminSiskoPredicate.instance);
    }

    CaptainBenjaminSiskoCastEffect() {
        super(Outcome.PlayForFree);
        staticText = "exile the top five cards of your library. You may cast any number of spells with mana value less than "
            + "or equal to the number of Officers you control from among those cards without paying their mana costs";
    }

    private CaptainBenjaminSiskoCastEffect(final CaptainBenjaminSiskoCastEffect effect) {
        super(effect);
    }

    @Override
    public CaptainBenjaminSiskoCastEffect copy() {
        return new CaptainBenjaminSiskoCastEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        UUID exileId = UUID.randomUUID();
        controller.moveCardsToExile(controller.getLibrary().getTopCards(game, 5), source, game, true,
            exileId, CardUtil.getSourceName(game, source));
        game.processAction();
        ExileZone exile = game.getExile().getExileZone(exileId);
        if (exile == null) {
            return true;
        }
        Cards cards = new CardsImpl(exile.getCards(game));
        CardUtil.castMultipleWithAttributeForFree(controller, source, game, cards, filter);
        return true;
    }
}

enum CaptainBenjaminSiskoPredicate implements ObjectSourcePlayerPredicate<Card> {
    instance;

    private static final FilterControlledPermanent filter = new FilterControlledPermanent("Officers you control");

    static {
        filter.add(SubType.OFFICER.getPredicate());
    }

    @Override
    public boolean apply(ObjectSourcePlayer<Card> input, Game game) {
        return input.getObject().getManaValue() <= game.getBattlefield().count(
            filter, input.getPlayerId(), input.getSource(), game
        );
    }
}
