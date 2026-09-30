package mage.cards.u;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.assignment.common.CardTypeAssignment;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.CastFromEverywhereSourceCondition;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.filter.common.FilterNonlandCard;
import mage.game.Game;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;
import mage.util.CardUtil;

import java.util.Set;
import java.util.UUID;

/**
 * @author muz
 */
public final class UldarosTheorix extends CardImpl {

    public UldarosTheorix(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}{B}{B}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ELDER, SubType.SPHINX);
        this.power = new MageInt(5);
        this.toughness = new MageInt(5);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // When Uldaros Theorix enters, if you cast him, exile up to one target nonland card of each card type from your graveyard. Copy those cards. You may cast any number of spells with total mana value 6 or less from among the copies without paying their mana costs.
        Ability ability = new EntersBattlefieldTriggeredAbility(new UldarosTheorixEffect())
                .withInterveningIf(UldarosTheorixCondition.instance);
        ability.addTarget(new UldarosTheorixTarget());
        this.addAbility(ability);
    }

    private UldarosTheorix(final UldarosTheorix card) {
        super(card);
    }

    @Override
    public UldarosTheorix copy() {
        return new UldarosTheorix(this);
    }
}

enum UldarosTheorixCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        return CastFromEverywhereSourceCondition.instance.apply(game, source);
    }

    @Override
    public String toString() {
        return "you cast him";
    }
}

class UldarosTheorixEffect extends OneShotEffect {

    UldarosTheorixEffect() {
        super(Outcome.PlayForFree);
        staticText = "exile up to one target nonland card of each card type from your graveyard. "
                + "Copy those cards. You may cast any number of spells with total mana value 6 or less "
                + "from among the copies without paying their mana costs";
    }

    private UldarosTheorixEffect(final UldarosTheorixEffect effect) {
        super(effect);
    }

    @Override
    public UldarosTheorixEffect copy() {
        return new UldarosTheorixEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        Cards cards = new CardsImpl(getTargetPointer().getTargets(game, source));
        cards.retainZone(Zone.GRAVEYARD, game);
        player.moveCards(cards, Zone.EXILED, source, game);
        cards.retainZone(Zone.EXILED, game);
        Cards copies = new CardsImpl();
        for (Card card : cards.getCards(game)) {
            copies.add(game.copyCard(card.getMainCard(), source, source.getControllerId()));
        }
        CardUtil.castMultipleWithAttributeForFree(player, source, game, copies,
                StaticFilters.FILTER_CARD, Integer.MAX_VALUE, new UldarosTheorixTracker());
        return true;
    }
}

class UldarosTheorixTarget extends TargetCardInYourGraveyard {

    private static final CardTypeAssignment assignment = new CardTypeAssignment(CardType.values());

    UldarosTheorixTarget() {
        super(0, Integer.MAX_VALUE, new FilterNonlandCard("up to one nonland card of each card type from your graveyard"));
    }

    private UldarosTheorixTarget(final UldarosTheorixTarget target) {
        super(target);
    }

    @Override
    public UldarosTheorixTarget copy() {
        return new UldarosTheorixTarget(this);
    }

    @Override
    public Set<UUID> possibleTargets(UUID playerId, Ability source, Game game) {
        Set<UUID> possible = super.possibleTargets(playerId, source, game);
        Cards chosen = new CardsImpl(getTargets());
        possible.removeIf(id -> {
            Cards combined = chosen.copy();
            combined.add(id);
            return assignment.hasSharedRoles(combined, game);
        });
        return possible;
    }
}

class UldarosTheorixTracker implements CardUtil.SpellCastTracker {

    private int totalManaValue;

    @Override
    public boolean checkCard(Card card, Game game) {
        return totalManaValue + card.getManaValue() <= 6;
    }

    @Override
    public void addCard(Card card, Ability source, Game game) {
        // Count the spell actually cast, so an adventure or split half uses its own mana value.
        totalManaValue += game.getStack().stream()
                .filter(Spell.class::isInstance)
                .map(Spell.class::cast)
                .filter(spell -> spell.getMainCard().getId().equals(card.getMainCard().getId()))
                .mapToInt(Spell::getManaValue)
                .findFirst().orElse(card.getManaValue());
    }
}
