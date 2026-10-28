package mage.cards.q;

import mage.MageInt;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.effects.mana.AddManaOfAnyColorEffect;
import mage.abilities.keyword.MenaceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.game.Game;
import mage.players.Player;

import java.util.UUID;

/**
 * @author muz
 */
public final class QonosQuartermaster extends CardImpl {

    public QonosQuartermaster(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{R}");

        this.subtype.add(SubType.KLINGON);
        this.subtype.add(SubType.WARRIOR);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Menace
        this.addAbility(new MenaceAbility());

        // Whenever this creature attacks, add one mana of any color. Until end of turn, you don't lose this mana as steps and phases end.
        this.addAbility(new AttacksTriggeredAbility(new QonosQuartermasterManaEffect()));
    }

    private QonosQuartermaster(final QonosQuartermaster card) {
        super(card);
    }

    @Override
    public QonosQuartermaster copy() {
        return new QonosQuartermaster(this);
    }
}

class QonosQuartermasterManaEffect extends AddManaOfAnyColorEffect {

    QonosQuartermasterManaEffect() {
        staticText += ". Until end of turn, you don't lose this mana as steps and phases end";
    }

    private QonosQuartermasterManaEffect(final QonosQuartermasterManaEffect effect) {
        super(effect);
    }

    @Override
    public QonosQuartermasterManaEffect copy() {
        return new QonosQuartermasterManaEffect(this);
    }

    @Override
    protected void addManaToPool(Player player, Mana manaToAdd, Game game, Ability source) {
        player.getManaPool().addMana(manaToAdd, game, source, true);
    }
}
