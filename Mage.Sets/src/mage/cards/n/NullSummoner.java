package mage.cards.n;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.CastFromEverywhereSourceCondition;
import mage.abilities.condition.common.ThresholdCondition;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.effects.AsThoughManaEffect;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.AsThoughEffectType;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.ManaType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.ManaPoolItem;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.common.TargetOpponent;
import mage.util.CardUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author muz
 */
public final class NullSummoner extends CardImpl {

    public NullSummoner(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}{B}");
        this.subtype.add(SubType.HUMAN, SubType.WARLOCK);
        this.power = new MageInt(4);
        this.toughness = new MageInt(2);

        // When this creature enters, if you cast it, target opponent reveals their hand. You choose a nonland card from it. Exile that card.
        Ability ability = new EntersBattlefieldTriggeredAbility(new NullSummonerExileEffect())
                .withInterveningIf(CastFromEverywhereSourceCondition.instance);
        ability.addTarget(new TargetOpponent());
        this.addAbility(ability);

        // Threshold -- As long as there are seven or more cards in your graveyard, you may cast the exiled card, and mana of any type can be spent to cast that spell.
        ability = new SimpleStaticAbility(new NullSummonerCastEffect());
        ability.addEffect(new NullSummonerManaEffect());
        this.addAbility(ability.setAbilityWord(AbilityWord.THRESHOLD));
    }

    static String linkKey(UUID id, int zcc) {
        return "NullSummoner:" + id + ':' + zcc;
    }

    static boolean matchesExiledCard(UUID objectId, Ability source, Game game, boolean allowStack) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent == null || !ThresholdCondition.instance.apply(game, source)) {
            return false;
        }
        Object value = game.getState().getValue(linkKey(permanent.getId(), permanent.getZoneChangeCounter(game)));
        if (!(value instanceof Set)) {
            return false;
        }
        Set<MageObjectReference> references = (Set<MageObjectReference>) value;
        Card card = game.getCard(CardUtil.getMainCardId(game, objectId));
        if (card == null) {
            return false;
        }
        Zone zone = game.getState().getZone(card.getId());
        return references.stream().anyMatch(reference ->
                zone == Zone.EXILED && reference.refersTo(card, game)
                        || allowStack && zone == Zone.STACK && reference.refersTo(card, game, 1));
    }

    private NullSummoner(final NullSummoner card) {
        super(card);
    }

    @Override
    public NullSummoner copy() {
        return new NullSummoner(this);
    }
}

class NullSummonerExileEffect extends OneShotEffect {

    NullSummonerExileEffect() {
        super(Outcome.Exile);
        staticText = "target opponent reveals their hand. You choose a nonland card from it. Exile that card";
    }

    private NullSummonerExileEffect(final NullSummonerExileEffect effect) {
        super(effect);
    }

    @Override
    public NullSummonerExileEffect copy() {
        return new NullSummonerExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (player == null || opponent == null) {
            return false;
        }
        opponent.revealCards(source, opponent.getHand(), game);
        TargetCard target = new TargetCard(Zone.HAND, StaticFilters.FILTER_CARD_NON_LAND).withNotTarget(true);
        if (opponent.getHand().count(StaticFilters.FILTER_CARD_NON_LAND, game) == 0
                || !player.choose(outcome, opponent.getHand(), target, source, game)) {
            return true;
        }
        Card card = game.getCard(target.getFirstTarget());
        UUID exileId = CardUtil.getExileZoneId(game, source.getSourceId(), source.getStackMomentSourceZCC());
        if (card != null && player.moveCardsToExile(card, source, game, true, exileId, "Null Summoner")
                && game.getState().getZone(card.getId()) == Zone.EXILED) {
            String key = NullSummoner.linkKey(source.getSourceId(), source.getStackMomentSourceZCC());
            Object value = game.getState().getValue(key);
            Set<MageObjectReference> references = value instanceof Set
                    ? new HashSet<>((Set<MageObjectReference>) value) : new HashSet<>();
            references.add(new MageObjectReference(card, game));
            game.getState().setValue(key, references);
        }
        return true;
    }
}

class NullSummonerCastEffect extends AsThoughEffectImpl {

    NullSummonerCastEffect() {
        super(AsThoughEffectType.CAST_FROM_NOT_OWN_HAND_ZONE, Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "as long as there are seven or more cards in your graveyard, you may cast the exiled card";
    }

    private NullSummonerCastEffect(final NullSummonerCastEffect effect) {
        super(effect);
    }

    @Override
    public NullSummonerCastEffect copy() {
        return new NullSummonerCastEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public boolean applies(UUID objectId, Ability affectedAbility, Ability source, Game game, UUID playerId) {
        return source.isControlledBy(playerId) && affectedAbility instanceof SpellAbility
                && NullSummoner.matchesExiledCard(objectId, source, game, false);
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID playerId, Game game) {
        return false;
    }
}

class NullSummonerManaEffect extends AsThoughEffectImpl implements AsThoughManaEffect {

    NullSummonerManaEffect() {
        super(AsThoughEffectType.SPEND_OTHER_MANA, Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = ", and mana of any type can be spent to cast that spell";
    }

    private NullSummonerManaEffect(final NullSummonerManaEffect effect) {
        super(effect);
    }

    @Override
    public NullSummonerManaEffect copy() {
        return new NullSummonerManaEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID playerId, Game game) {
        return source.isControlledBy(playerId) && NullSummoner.matchesExiledCard(objectId, source, game, true);
    }

    @Override
    public ManaType getAsThoughManaType(ManaType manaType, ManaPoolItem mana, UUID playerId, Ability source, Game game) {
        return mana.getFirstAvailable();
    }
}
