package mage.cards.e;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.LandfallAbility;
import mage.abilities.condition.common.SourceHasCounterCondition;
import mage.abilities.decorator.ConditionalOneShotEffect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.effects.mana.AddManaToManaPoolSourceControllerEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.Game;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author muz
 */
public final class ExploratoryMission extends CardImpl {

    public ExploratoryMission(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{R}{R}");

        // Landfall -- Whenever a land you control enters, put a quest counter on this enchantment. Then exile cards from the top of your library until you exile a nonland card. You may play that card this turn. If this enchantment has five or more quest counters on it, add {R}{R}.
        Ability ability = new LandfallAbility(new AddCountersSourceEffect(CounterType.QUEST.createInstance()));
        ability.addEffect(new ExploratoryMissionEffect());
        ability.addEffect(new ConditionalOneShotEffect(new AddManaToManaPoolSourceControllerEffect(Mana.RedMana(2)),
            new SourceHasCounterCondition(CounterType.QUEST, 5), "if {this} has five or more quest counters on it, add {R}{R}"));
        this.addAbility(ability);
    }

    private ExploratoryMission(final ExploratoryMission card) {
        super(card);
    }

    @Override
    public ExploratoryMission copy() {
        return new ExploratoryMission(this);
    }
}

class ExploratoryMissionEffect extends OneShotEffect {

    ExploratoryMissionEffect() {
        super(Outcome.Benefit);
        staticText = "then exile cards from the top of your library until you exile a nonland card. You may play that card this turn";
    }

    private ExploratoryMissionEffect(final ExploratoryMissionEffect effect) {
        super(effect);
    }

    @Override
    public ExploratoryMissionEffect copy() {
        return new ExploratoryMissionEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        for (Card card : player.getLibrary().getCards(game)) {
            player.moveCards(card, Zone.EXILED, source, game);
            if (game.getState().getZone(card.getId()) == Zone.EXILED && !card.isLand(game)) {
                CardUtil.makeCardPlayable(game, source, card, false, Duration.EndOfTurn, false);
                break;
            }
        }
        return true;
    }
}
