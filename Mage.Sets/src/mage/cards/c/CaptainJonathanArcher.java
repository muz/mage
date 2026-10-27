package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.ActivateAsSorceryActivatedAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.dynamicvalue.common.FederationCount;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.continuous.BoostTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.game.permanent.token.TokenImpl;
import mage.target.common.TargetControlledCreaturePermanent;

import java.util.UUID;

/**
 * @author muz
 */
public final class CaptainJonathanArcher extends CardImpl {

    public CaptainJonathanArcher(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // When Captain Archer enters, create Porthos, a legendary 1/1 green Dog creature token.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new CreateTokenEffect(new CaptainJonathanArcherToken())));

        // Federation -- {G}, {T}: Target creature you control gets +X/+X until end of turn, where X is the number of creature types among non-Borg creatures you control. Activate only as a sorcery.
        Ability ability = new ActivateAsSorceryActivatedAbility(new BoostTargetEffect(FederationCount.instance, FederationCount.instance)
            .setText("target creature you control gets +X/+X until end of turn, where X is the number of creature types among non-Borg creatures you control"),
            new ManaCostsImpl<>("{G}"));
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetControlledCreaturePermanent());
        this.addAbility(ability.setAbilityWord(AbilityWord.FEDERATION).addHint(FederationCount.instance.getHint()));
    }

    private CaptainJonathanArcher(final CaptainJonathanArcher card) {
        super(card);
    }

    @Override
    public CaptainJonathanArcher copy() {
        return new CaptainJonathanArcher(this);
    }
}

class CaptainJonathanArcherToken extends TokenImpl {

    CaptainJonathanArcherToken() {
        super("Porthos", "Porthos, a legendary 1/1 green Dog creature token");
        supertype.add(SuperType.LEGENDARY);
        cardType.add(CardType.CREATURE);
        color.setGreen(true);
        subtype.add(SubType.DOG);
        power = new MageInt(1);
        toughness = new MageInt(1);
    }

    private CaptainJonathanArcherToken(final CaptainJonathanArcherToken token) {
        super(token);
    }

    @Override
    public CaptainJonathanArcherToken copy() {
        return new CaptainJonathanArcherToken(this);
    }
}
