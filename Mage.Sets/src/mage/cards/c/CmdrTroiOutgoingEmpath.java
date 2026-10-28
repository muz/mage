package mage.cards.c;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.FilterOpponent;
import mage.filter.StaticFilters;
import mage.filter.predicate.other.ActivePlayerPredicate;
import mage.game.Game;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.players.Player;
import mage.target.TargetPlayer;
import mage.util.CardUtil;

/**
 * @author muz
 */
public final class CmdrTroiOutgoingEmpath extends CardImpl {

    private static final FilterOpponent filter = new FilterOpponent("opponent whose turn it is");

    static {
        filter.add(ActivePlayerPredicate.instance);
    }

    public CmdrTroiOutgoingEmpath(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.BETAZOID);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // {T}: Target opponent whose turn it is may cast a spell from their hand without paying its mana cost. If they do, you copy that spell. You may choose new targets for the copy. If they don't, you draw a card.
        Ability ability = new SimpleActivatedAbility(new CmdrTroiOutgoingEmpathEffect(), new TapSourceCost());
        ability.addTarget(new TargetPlayer(filter));
        this.addAbility(ability);
    }

    private CmdrTroiOutgoingEmpath(final CmdrTroiOutgoingEmpath card) {
        super(card);
    }

    @Override
    public CmdrTroiOutgoingEmpath copy() {
        return new CmdrTroiOutgoingEmpath(this);
    }
}

class CmdrTroiOutgoingEmpathEffect extends OneShotEffect {

    CmdrTroiOutgoingEmpathEffect() {
        super(Outcome.Benefit);
        staticText = "target opponent whose turn it is may cast a spell from their hand without paying its mana cost. "
            + "If they do, you copy that spell. You may choose new targets for the copy. "
            + "If they don't, you draw a card";
    }

    private CmdrTroiOutgoingEmpathEffect(final CmdrTroiOutgoingEmpathEffect effect) {
        super(effect);
    }

    @Override
    public CmdrTroiOutgoingEmpathEffect copy() {
        return new CmdrTroiOutgoingEmpathEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        Player controller = game.getPlayer(source.getControllerId());
        if (opponent == null || controller == null) {
            return false;
        }
        if (!CardUtil.castSpellWithAttributesForFree(opponent, source, game, new CardsImpl(opponent.getHand()),
            StaticFilters.FILTER_CARD, CmdrTroiOutgoingEmpathTracker.instance)) {
            controller.drawCards(1, source, game);
        }
        return true;
    }
}

enum CmdrTroiOutgoingEmpathTracker implements CardUtil.SpellCastTracker {
    instance;

    @Override
    public boolean checkCard(Card card, Game game) {
        return true;
    }

    @Override
    public void addCard(Card card, Ability source, Game game) {
        for (StackObject stackObject : game.getStack()) {
            if (stackObject instanceof Spell && !stackObject.isCopy()
                && ((Spell) stackObject).getCard().getMainCard().getId().equals(card.getMainCard().getId())) {
                stackObject.createCopyOnStack(game, source, source.getControllerId(), true);
                return;
            }
        }
    }
}
