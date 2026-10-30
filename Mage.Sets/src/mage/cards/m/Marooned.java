package mage.cards.m;

import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.DoIfCostPaid;
import mage.abilities.effects.common.DontUntapInControllersUntapStepEnchantedEffect;
import mage.abilities.effects.common.TapEnchantedEffect;
import mage.abilities.keyword.CastFromGraveyardAbility;
import mage.abilities.keyword.EnchantAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SpellAbilityCastMode;
import mage.constants.SubType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author muz
 */
public final class Marooned extends CardImpl {

    public Marooned(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{U}");

        this.subtype.add(SubType.AURA);

        // Enchant artifact or creature
        TargetPermanent auraTarget = new TargetPermanent(StaticFilters.FILTER_PERMANENT_ARTIFACT_OR_CREATURE);
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.Detriment));
        this.addAbility(new EnchantAbility(auraTarget));

        // When this Aura enters, you may pay {3}. If you do, tap enchanted permanent.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new DoIfCostPaid(
            new TapEnchantedEffect("permanent"), new GenericManaCost(3)
        )));

        // Enchanted permanent doesn't untap during its controller's untap step and can't be beamed up.
        Ability ability = new SimpleStaticAbility(new DontUntapInControllersUntapStepEnchantedEffect("permanent"));
        ability.addEffect(new MaroonedEffect());
        this.addAbility(ability);
    }

    private Marooned(final Marooned card) {
        super(card);
    }

    @Override
    public Marooned copy() {
        return new Marooned(this);
    }
}

class MaroonedEffect extends ContinuousRuleModifyingEffectImpl {

    MaroonedEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Detriment);
        staticText = "and can't be beamed up";
    }

    private MaroonedEffect(final MaroonedEffect effect) {
        super(effect);
    }

    @Override
    public MaroonedEffect copy() {
        return new MaroonedEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.PAY_RETURN_TO_HAND_COST;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        Object targetAbility = getValue("targetAbility");
        if (!(targetAbility instanceof SpellAbility)
            || ((SpellAbility) targetAbility).getSpellAbilityCastMode() != SpellAbilityCastMode.BEAM_ME_UP
            || !CastFromGraveyardAbility.isGraveyardCastCost((SpellAbility) targetAbility, event.getData(), game)) {
            return false;
        }
        Permanent aura = source.getSourcePermanentIfItStillExists(game);
        return aura != null && event.getTargetId().equals(aura.getAttachedTo());
    }
}
