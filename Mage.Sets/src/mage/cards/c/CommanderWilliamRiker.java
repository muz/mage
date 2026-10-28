package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.game.Game;
import mage.players.Player;
import mage.players.PlayerList;
import mage.target.common.TargetOpponent;

import java.util.UUID;

/**
 * @author muz
 */
public final class CommanderWilliamRiker extends CardImpl {

    public CommanderWilliamRiker(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{R}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Vigilance, haste
        this.addAbility(VigilanceAbility.getInstance());
        this.addAbility(HasteAbility.getInstance());

        // {1}, {T}: You and target opponent each draw two cards.
        Ability ability = new SimpleActivatedAbility(
            new CommanderWilliamRikerEffect(), new ManaCostsImpl<>("{1}")
        );
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetOpponent());
        this.addAbility(ability);
    }

    private CommanderWilliamRiker(final CommanderWilliamRiker card) {
        super(card);
    }

    @Override
    public CommanderWilliamRiker copy() {
        return new CommanderWilliamRiker(this);
    }
}

class CommanderWilliamRikerEffect extends OneShotEffect {

    CommanderWilliamRikerEffect() {
        super(Outcome.DrawCard);
        staticText = "you and target opponent each draw two cards";
    }

    private CommanderWilliamRikerEffect(final CommanderWilliamRikerEffect effect) {
        super(effect);
    }

    @Override
    public CommanderWilliamRikerEffect copy() {
        return new CommanderWilliamRikerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        UUID targetId = getTargetPointer().getFirst(game, source);
        PlayerList players = game.getPlayerList().copy();
        players.setCurrent(game.getActivePlayerId());
        for (UUID playerId : players) {
            if (!playerId.equals(source.getControllerId()) && !playerId.equals(targetId)) {
                continue;
            }
            Player player = game.getPlayer(playerId);
            if (player != null) {
                player.drawCards(2, source, game);
            }
        }
        return true;
    }
}
