# Prepare Mechanic Implementation Plan

Status: implementation in progress. Slice 1, Slice 2, the initial Slice 3 code,
initial Slice 4 explicit cleanup, Slice 5 phasing support, the initial Slice 6/7
prepare-characteristics resolver work, and initial Slice 9 prepare-spell stack
metadata have been added on branch `prepare_mechanic` and statically checked;
runtime test execution still needs a local Maven-capable run. Later slices in
this document remain planned work until their code lands.

This document records the rule model, current Mage code facts, corrected design
decisions, and remaining questions for implementing the Magic: The Gathering
Prepare mechanic in this fork.

## Goal

Implement Prepare as a permanent designation that creates and exposes a castable
exiled copy of the prepare spell characteristics. Do not implement Prepare as
casting the inset `PrepareSpellCard` directly from the battlefield.

The required behavior is:

- a preparation card is a permanent card with alternative prepare spell
  characteristics,
- a permanent with a prepare spell can gain the prepared designation,
- when the permanent gains prepared, the controller creates a copy in exile with
  only the prepare spell characteristics,
- the permitted player casts that exiled copy,
- when that copy becomes cast, the source permanent loses prepared,
- the copy is only available while the linked source permanent remains a valid
  prepared permanent,
- each source permanent object can have at most one live associated prepare copy
  in exile at any time,
- stale copies are removed from exile, copied-card registration, and Prepare
  tracking when CR 722.3c no longer keeps them alive; dynamic cast permission
  must also make any missed duplicate or orphaned copy uncastable as a safety
  net.

## Rule Citations

Primary source: Wizards Comprehensive Rules, effective 2026-06-19.

Useful source links for the later implementation pass:

- Official rules page: https://magic.wizards.com/en/rules
- Current text file referenced by the official rules page:
  https://media.wizards.com/2026/downloads/MagicCompRules%2020260619.txt
- Community edge-case checklist reviewed while updating this plan:
  https://www.reddit.com/r/EDH/comments/1sz7b43/prepared_mechanic_deep_dive_a_comprehensive_guide/

Rules to cite in code comments, tests, and review:

- CR 722, "Preparation Cards", is the controlling rules section for preparation
  cards, prepare spells, and the prepared designation.
- CR 722.2a defines the inset frame as the prepare spell and establishes that an
  object "with a prepare spell" has those alternative characteristics.
- CR 722.2b states that the prepare spell's alternative characteristics are part
  of the object's copiable values.
- CR 722.2c states that a preparation card is only one card even though it has
  multiple sets of characteristics. This matters for deck construction,
  highlander checks, and "card" wording.
- CR 722.3a allows a permanent to gain the prepared designation only if it has
  the alternative characteristics of a prepare spell, and prevents gaining
  prepared again while it already has the designation.
- CR 722.3 prevents simply casting the object using the prepare spell's
  alternative characteristics; effects can create copies using those
  characteristics.
- CR 722.3c is the core lifecycle rule: when the permanent gains prepared or
  phases in prepared, that permanent's controller creates a copy in exile with
  only the prepare spell characteristics. For as long as the copy remains in
  exile, the prepared permanent's controller may cast it. The copy exists only
  while the permanent remains on the battlefield and remains prepared.
- CR 722.3c points to CR 601.2i for the moment when the source permanent loses
  prepared: after the casting process reaches the point where the prepare copy
  becomes cast.
- CR 722.3c is also the reason phasing must create a fresh copy: it explicitly
  says a copy is created when the permanent phases in prepared.
- CR 722.3d requires prepare-spell cast metadata to survive spell copying: if a
  prepare spell on the stack is copied, that copy is also a prepare spell and
  knows it was cast as a prepare spell for effects that care about that fact.
- CR 722.4 says a preparation card has only its normal characteristics in every
  zone. Searching a library, hand, graveyard, or battlefield object by the
  prepare spell's card type, name, or mana cost should fail unless another rule
  or effect specifically looks at alternative characteristics.
- CR 722.5 says an effect that instructs a player to choose a card name may
  choose a preparation card's alternative name. Name-prohibition effects such as
  "the chosen name can't be cast" therefore need to match the prepare copy's
  alternative name.
- CR 117.1a, CR 117.3b, CR 117.5, CR 603.6d, CR 614.1c, and CR 614.12 matter
  for enters-prepared priority. "Enters prepared" is an enters-the-battlefield
  replacement/static effect, not a triggered ability. The active player receives
  priority after the permanent spell resolves, but state-based actions and
  waiting triggers are put on the stack first.
- CR 601.2b says a player cannot apply two alternative methods or alternative
  costs to a single spell. Prepare itself is not an alternative cost; it is a
  cast permission from exile supplied by CR 722.3c. The copied spell should
  still use the normal cost-selection, additional-cost, cost-increase, and
  cost-reduction machinery.
- CR 108.2, CR 109.1, CR 200.3, CR 601.2a, CR 112.1a, CR 112.1b, CR 707.10,
  and CR 707.12 distinguish cards, copies of cards, copies of spells, and the
  parts those copies can have. A prepare copy is not an ordinary card in exile,
  but when it is cast it is a spell on the stack. "Cast a spell from exile"
  wording can apply; "play/cast a card from exile" or "instant or sorcery card"
  wording generally should not.
- CR 702.26b/702.26d matter for phase-out: a phased-out permanent is treated as
  though it does not exist and is not on the battlefield, but phasing does not
  make it change zones or become a new object. The prepared designation should
  remain on the source permanent while its live prepare copy stops existing.
- CR 704.5e normally makes copies of spells cease to exist outside the stack and
  copies of cards cease to exist outside the stack or battlefield. CR 722.3c
  creates a temporary exception only while the prepared permanent remains on the
  battlefield and has prepared, so the exception should stop applying while the
  source is phased out or after the source loses prepared.
- CR 400.7 says an object that moves from one zone to another becomes a new
  object with no memory of its previous existence unless an exception applies.
  Prepare should not treat a copied spell that was returned to hand, put into a
  library, or exiled by another effect as the same live CR 722.3c exile copy.
- CR 701.6a defines countering as removing the spell or ability from the stack
  so it does not resolve. A normal countered spell goes to its owner's
  graveyard unless the countering effect says otherwise.
- CR 702.62b defines a suspended card as a card in exile with suspend and a time
  counter on it. It does not override CR 704.5e for copied cards or copied
  spells.
- CR 700.7 says an ability using "this [quality]" to identify an object refers
  to that particular object even if the object no longer has that quality. This
  matters for effects copied onto noncreature permanents that still say "this
  creature becomes prepared."
- CR 707.2 defines copiable values and says other effects, including
  type-changing and text-changing effects, are not copied. A prepared
  designation is not a copiable value, and ordinary text/type-changing effects
  should not create or erase prepare spell alternative characteristics.
- CR 707.2b says that once an object has been copied, changing the copiable
  values of the original object will not cause the copy to change. A live
  prepare copy should stay the spell it was when created unless it is removed by
  the Prepare lifecycle.
- CR 608.2k says an ability effect that refers to a specific untargeted object
  previously referred to by that ability's cost or trigger condition still
  affects that object even if it has changed characteristics. This reinforces
  "this creature" self-reference behavior for become-prepared abilities.
- CR 702.140c says a resolving mutating creature spell with a legal target does
  not enter the battlefield; it merges with the target creature and becomes one
  object represented by more than one card or token.
- CR 702.140e says a mutated permanent has all abilities of each card and token
  that represents it, while its other characteristics come from the topmost card
  or token.
- CR 730, "Merging with Permanents", supplies the merged-permanent object model
  used by mutate. Prepare tracking should treat a merged permanent as the source
  permanent object, not as separate source objects for each component.
- CR 730.2 says merging places the object on top of or under the permanent, and
  that permanent becomes a merged permanent represented by all those components.
- CR 730.2a says a merged permanent has only the characteristics of its topmost
  component unless otherwise specified by the merging effect; this is a copiable
  effect.
- CR 730.2c says a merged permanent is the same object it was before, so
  continuous effects and similar object-tracking effects continue to apply.
- CR 730.3 says if a merged permanent leaves the battlefield, one permanent
  leaves the battlefield and each individual component is put into the
  appropriate zone.
- CR 601.3 matters because a player needs a rule or effect permitting casting
  from exile; for Prepare, that permission is supplied by CR 722.3c. CR 601.3
  also requires cast prohibitions to be honored, including prohibitions based on
  the prepare copy's name or characteristics.
- CR 112.2a says that when an effect instructs a player to create a copy of a
  card and says they may cast it, the owner of that copy is the player
  instructed to create it and given permission to cast it. For the exiled
  Prepare copy, use the controller who creates the copy as the card-owner /
  bookkeeping owner, but do not use that owner as the casting authority after
  control of the prepared permanent changes.
- CR 903.4e says alternative characteristics are included when determining
  Commander color identity. CR 903.5b still counts cards by their English card
  names for Commander singleton deck construction. Prepare spell names are not
  extra deck cards, but their mana symbols and rules text can matter to color
  identity.

Code-comment convention for implementation:

- When an engine branch exists because Prepare differs from ordinary cards,
  ordinary copied cards, Adventure/Omen-style spell options, or ordinary
  cast-from-exile effects, add a nearby code comment citing the exact CR rule
  that justifies the branch.
- Comments should be short and specific, for example "CR 722.3c: the live
  Prepare copy may exist in exile while linked to a prepared permanent" or
  "CR 722.3d: a stack copy of a prepare spell remains a prepare spell".
- Do not add broad rule essays to plumbing code. Cite the rule at the point
  where the behavior would otherwise look surprising or wrong to a future
  maintainer.

Implementation consequence: CR 722.3 and CR 722.3c rule out direct battlefield
casting of the inset spell object. Mage needs a real exiled copy object whose
own spell ability is cast from exile.

## Current Code Facts

Prepare-specific code:

- `Mage/src/main/java/mage/cards/PrepareCard.java` currently extends
  `CardImpl`, owns a `protected Card spellCard`, and returns it via
  `getSpellCard()`.
- `Mage/src/main/java/mage/cards/PrepareSpellCard.java` currently extends
  `CardImpl`, stores a parent `PrepareCard`, and creates a basic alternate
  `SpellAbility`.
- Local search found no current card-data or card-class consumer for
  `wasPrepareSpell` or wording like "cast as a prepare spell". The metadata
  path is still required by CR 722.3d for stack copies of prepare spells, but it
  is currently future-proofing rather than support for an existing printed-card
  reference in this checkout.
- `Mage/src/main/java/mage/game/permanent/Permanent.java` exposes
  `setPrepared(boolean prepared, Game game)`.
- `Mage/src/main/java/mage/game/permanent/PermanentImpl.java#setPrepared`
  currently only tracks the marker and UI info. It does not know the source
  `Ability`, so it cannot safely create source-linked copies by itself.
- `BecomePreparedSourceEffect`, `BecomePreparedTargetEffect`, and
  `EntersPreparedAbility` are the current entry points for setting prepared.
  The effects have the `Ability source` context needed for a source-aware
  lifecycle helper.

Shared spell-option code that is useful but dangerous:

- `CardWithSpellOption#getAbilities()` and `getAbilities(Game)` merge
  spell-card abilities into the main card's abilities.
- `PlayerImpl#getPlayableFromObjectAll` explicitly treats every
  `CardWithSpellOption` by checking `getSpellCard()` as a separately playable
  object, then checking the shared main-card abilities.
- Therefore, blindly changing `PrepareCard` to extend `CardWithSpellOption`
  risks leaking the real inset `PrepareSpellCard` as directly playable. That is
  not legal for Prepare.

Copy/exile/cast code facts:

- `GameState#copyCard` only accepts a main card and copies all card parts. That
  conflicts with CR 722.3c's "only the prepare spell characteristics" copy.
- `Exile` contains real `ExileZone` objects, and `PlayerImpl#getPlayable` scans
  exile zones to discover playable exiled cards. The prepare copy must be added
  to an actual exile zone, not only to `GameState.copiedCards` or `Zone.EXILED`.
- `PlayerImpl#cast` fires early `CAST_SPELL` events before/around announcement,
  then calls `spell.activate(...)`, and only fires `SPELL_CAST` after casting
  succeeds. The Prepare cleanup must use `SPELL_CAST`, not early `CAST_SPELL`.
- For `SPELL_CAST`, the event target id is the ability id. The event source id
  comes from the copied spell ability's source object. Prepare tracking must map
  `event.getSourceId()` to the exiled prepare-copy card id.

State/bookmark/simulation code facts:

- `GameState` explicitly warns that copied/restored game state must refer to
  objects by id, not by stable Java object identity.
- `GameImpl#bookmarkState` saves `state.copy()`, and `GameImpl#restoreState`
  restores that saved `GameState`. Turn rollback also stores `state.copy()` in
  `saveRollBackGameState` and restores through `restoreForRollBack`.
- `GameState#copy` deep-copies exile zones, battlefield, continuous effects,
  watchers, and `values`. `ExileZone` membership is id-based, so restoring exile
  membership requires the corresponding copied card id to resolve through
  `game.getCard(...)`.
- `PermanentImpl` copies both the `prepared` marker and phasing state, so those
  marker fields can survive `GameState` copies as long as the surrounding
  Prepare copy registration is restored coherently.
- `GameState#copy` currently shallow-copies `copiedCards` with `putAll`.
  Existing short-lived copied-card use may tolerate that, but Prepare copies can
  persist in exile across priority passes, playable simulations, bookmarks, and
  rollbacks. The Prepare implementation must not rely on shared mutable copied
  card instances across copied `GameState` objects.
- `GameImpl#getCard` resolves copied cards from `GameState.copiedCards`, then
  falls back to the `COPIED_CARD_KEY` LKI workaround stored in `GameState`
  values.
- The state-based action implementation for CR 704.5e scans
  `GameState.copiedCards` and copied-card LKI values, removes copied cards from
  exile, and moves copied cards to `Zone.OUTSIDE` unless they are validly on the
  stack, battlefield, or already outside. Prepare must add a narrow live-copy
  exemption there.
- `ContinuousEffects#copy` copies as-though effects. A Prepare cast-permission
  effect with only UUIDs, `MageObjectReference`s, and immutable/copyable fields
  will therefore survive bookmark and rollback state copies if its copy
  constructor preserves those fields.
- `SpellAbility#canActivate` and `PlayerImpl#getPlayableFromObjectAll` already
  query both `PLAY_FROM_NOT_OWN_HAND_ZONE` and `CAST_FROM_NOT_OWN_HAND_ZONE` for
  spells. `CAST_FROM_NOT_OWN_HAND_ZONE` is the cleaner Prepare hook because CR
  722.3c grants permission to cast a spell copy, not to play a card or land.

Mutate/merged-permanent code facts:

- `MutateAbility` documents the CR 702.140 model: a legal mutating creature
  spell merges with the target creature rather than entering the battlefield,
  the merged permanent has all component abilities, and its other
  characteristics come from the topmost component.
- `PermanentImpl#mutate` keeps the same permanent object/id for the target
  permanent, stores mutation component ids in `mutations` and
  `mutationsForView`, and records `topMutation` when the mutating spell is
  placed over the existing permanent.
- `PermanentImpl#mutate` copies abilities from the mutation component onto the
  same permanent with source id rewritten to the permanent id.
- `PermanentImpl#applyMutate` copies name, mana cost, color, types, subtypes,
  power/toughness, and similar non-ability characteristics from `topMutation`
  when the mutation is over the existing permanent.
- `PermanentImpl#moveToZone` moves mutation component cards along with the
  source permanent when a merged permanent leaves the battlefield.
- `EntersPreparedAbility` is implemented as an enters-the-battlefield effect,
  so a legal mutate merge should not run an "enters prepared" path for the
  mutating component because the mutating spell did not enter the battlefield.
- Current prepared marker storage is already on the right object for mutate:
  `PermanentImpl` stores `prepared` on the permanent object, and
  `PermanentImpl#mutate` keeps the same permanent object/id for the merged
  permanent. A prepared permanent that mutates therefore keeps the designation on
  the correct merged permanent object.
- Initial prepare eligibility is now resolver-backed:
  `PermanentImpl#setPrepared` checks
  `PrepareUtil.getPrepareSpellCharacteristics(this, game).isPresent()` instead
  of `getMainCard() instanceof PrepareCard`.
- The initial resolver treats copy provenance as authoritative and inspects the
  top component of merged permanents through `getMutateForView()`. Full
  copied-token gameplay coverage, mutate gameplay coverage, and edge cases
  where copy provenance cannot be resolved still need runtime-backed testing.

## Corrected Architecture

Use four separate concepts.

1. Physical preparation card

   The real `PrepareCard` object. This card can be cast as a permanent, enter
   the battlefield, gain prepared, lose prepared, leave the battlefield, and
   keep the printed prepare spell data.

2. Prepare spell blueprint

   The real `PrepareSpellCard` object represents the inset spell
   characteristics for card construction, rules text, costs, modes, effects,
   targets, and copy creation. It must not itself become the castable object
   just because the permanent is prepared.

3. Exiled prepare copy

   Add a standalone copied card class or helper path, preferably
   `PrepareSpellCopyCard extends CardImpl`. This object represents only the
   prepare spell characteristics. It has its own id, owner/controller, zone,
   spell ability, costs, modes, targets, effects, and copy metadata. It has no
   parent `PrepareCard` relationship and must not implement behavior that makes
   it a multi-part physical preparation card.

   A source permanent object must have at most one live associated exiled
   prepare copy at a time. Reprepare, phase-in, duplicate events, and stale
   state recovery must not leave multiple copies associated with the same source
   permanent.

4. Source-aware prepare state

   Add a source-aware helper, for example `PrepareUtil.setPrepared(...)`, that
   handles CR 722.3a and CR 722.3c together. Keep
   `PermanentImpl#setPrepared(boolean, Game)` marker-only or treat it as an
   internal low-level setter. Existing effects should call the helper because
   they have the `Ability source` argument.

## Key Design Decisions

### Use Prepare-Specific Rendering/Search Hooks, Not CardWithSpellOption

Resolved decision: do not make `PrepareCard` extend `CardWithSpellOption` as the
primary implementation path.

Use a Prepare-specific blueprint/interface path:

- keep `PrepareCard`/`PrepareSpellCard` as a Prepare-specific pairing,
- add a minimal Prepare-specific interface such as `HasPrepareSpell` only if it
  makes shared hooks cleaner,
- keep the existing card-authoring API:
  `this.getSpellCard().getSpellAbility().addEffect(...)`,
- expose spell text/search/copy data through explicit Prepare branches,
- do not inherit generic `CardWithSpellOption` playable scanning,
- use `PrepareSpellCopyCard` for the actual exiled castable object.

Why:

- `CardWithSpellOption#getAbilities()` merges spell-card abilities into the
  main card ability list.
- `PlayerImpl#getPlayableFromObjectAll` explicitly asks every
  `CardWithSpellOption` for its `getSpellCard()` and checks that spell card as
  a separately playable object.
- That behavior is correct for Adventure/Omen-style spell options, but Prepare
  must not expose the real inset `PrepareSpellCard` as castable. CR 722.3 says
  only the exiled copy created by Prepare may be cast.

Required explicit Prepare hooks:

- card repository metadata should include the prepare spell name and mana cost
  where spell-option metadata is currently expected,
- card text search should include the prepare spell name and rules text,
- rules rendering should show the prepare spell name, type, cost, and rules on
  the parent card,
- copy creation should use resolved prepare spell characteristics to make a
  standalone `PrepareSpellCopyCard`,
- playable discovery should see only the standalone exiled copy, not the real
  blueprint object.

Fallback only if the Prepare-specific path proves too invasive:

- `PrepareCard` may extend `CardWithSpellOption` only if the implementation also
  adds hard Prepare-specific guards:
  - `getPlayableFromObjectAll` must not expose the real `PrepareSpellCard` from
    normal zones,
  - `getAbilities()` aggregation must not make prepare-spell abilities behave as
    permanent abilities,
  - direct `PrepareSpellCard` `canActivate`/cast paths must fail unless the
    object is the standalone exiled copy,
  - focused tests must prove the real inset spell is not castable from the
    battlefield, hand, library, graveyard, or as a generic spell option.

### Do Not Use GameState#copyCard For The Prepare Copy

`GameState#copyCard(parentPrepareCard, ...)` copies the main card and all card
parts. That is wrong for CR 722.3c because the exile copy has only the prepare
spell characteristics.

Add a focused copy path:

- copy from resolved prepare spell characteristics, not the parent
  `PrepareCard`; for a physical preparation card this can be backed by its
  `PrepareSpellCard`, but copied permanents, token copies, and merged
  permanents should use a resolved characteristics snapshot/result object rather
  than assuming a physical `PrepareSpellCard` instance,
- create a standalone `PrepareSpellCopyCard`,
- assign/finalize the copied card's fresh id before final source-id repair,
- set the copied card's owner/bookkeeping owner to the controller who creates
  the exiled copy under CR 722.3c/112.2a; set controller fields as needed for
  Mage bookkeeping, but do not use the copy's stored owner/controller as the
  casting authority,
- copy spell ability, modes, targets, costs, name, card types, subtypes, rules,
  color, and other spell characteristics from the resolved characteristics,
- set every copied ability's source id, including the spell ability, to the
  copied card id,
- assert that the copied spell ability's source id is the copied card id,
- mark the card as a copy where Mage needs copy metadata,
- register it in `GameState.copiedCards`,
- set zone state to `Zone.EXILED`,
- add it to an actual `ExileZone`.

### Rewrite Copied Ability Source Ids To The Exiled Copy

Resolved decision: the exiled `PrepareSpellCopyCard` owns its copied abilities.
After the copy has its final id, every copied ability must have
`sourceId == prepareCopy.getId()`.

This is a hard invariant, not just a cleanup preference.

Why:

- CR 722.3c creates a copy in exile with only the prepare spell
  characteristics, and CR 601.2i/722.3c clear the source permanent's prepared
  designation when that copy becomes cast. The engine therefore needs the cast
  event to identify the exiled copy, not the blueprint `PrepareSpellCard`.
- `PlayerImpl#cast` copies the selected `SpellAbility`, then looks up the card
  with `game.getCard(ability.getSourceId())`. If the source id still points at
  the blueprint spell card, the cast path will reason about the wrong object.
- `SPELL_CAST` events keep `source.getSourceId()` as `event.getSourceId()`.
  Prepare cleanup should map that event source id directly to the tracked
  exiled prepare copy id.
- `SpellAbility#spellCanBeActivatedNow` also starts from `game.getObject(sourceId)`
  and checks the object/main-card ids for play-from-exile permission, so the
  copied ability must point at the standalone exiled copy for playability to be
  correct.

Mage already has suitable repair hooks:

- `CardImpl#assignNewId()` rewrites the card's ability source ids to the new
  card id.
- `CardImpl#addAbility(...)` and `CardImpl#replaceSpellAbility(...)` set the
  added ability source id to the receiving card id.
- `AbilityImpl#setSourceId(...)` cascades to subabilities and watchers.
- Ability copying deep-copies costs, modes, targets, effects, subabilities, and
  watchers, so the important additional requirement is assigning those copied
  objects to the exiled copy after the copy id is final.

Required construction behavior:

- create or copy the standalone `PrepareSpellCopyCard`,
- copy the resolved prepare spell ability and install it with
  `replaceSpellAbility(...)` or an equivalent path that sets the source id to
  the copy card,
- after the copy has its final id, explicitly repair all ability source ids with
  `copy.getAbilities().setSourceId(copy.getId())` or an equivalent helper,
- explicitly repair `copy.getSpellAbility().setSourceId(copy.getId())` if the
  cached spell-ability field may not be the same instance seen through
  `getAbilities()`,
- fail fast in tests or validation if `copy.getSpellAbility().getSourceId()` is
  not the copy id,
- fail fast if any copied ability on the exiled copy has a different source id.

Do not rely on the current `PrepareSpellCard` copy constructor for this. It
preserves the parent `PrepareCard` relationship and starts from the blueprint
object id, which is exactly what the standalone exiled copy must avoid.

### Preserve Prepare-Spell Cast Metadata

Resolved decision: add a small explicit metadata path, tentatively named
`wasPrepareSpell`, for stack spells that were cast from a CR 722.3c prepare
copy. The exact field/API name can change during implementation, but the
semantic invariant should not.

Local audit result: no current local card data or card class appears to consume
this metadata today. A search across `Utils/mtg-cards-data.txt`,
`Mage.Sets/src/mage/cards`, and `Mage/src/main/java` for `wasPrepareSpell`,
"cast as a prepare spell", "prepare spell", and "prepared spell" found only the
existing CR 722.3a comments in `PermanentImpl`. Therefore this metadata is not
blocking a known current card implementation, but CR 722.3d makes it part of
the rules model and cheap enough to include before future cards or generic
copy-spell effects need it.

Implementation consequences:

- Set the metadata when a `PrepareSpellCopyCard` is successfully cast from
  exile and becomes a spell on the stack.
- Preserve the metadata when that spell is copied on the stack. Under
  CR 722.3d, the stack copy is also a prepare spell and knows it was cast as a
  prepare spell.
- Keep this metadata separate from the live exiled-copy lifecycle. It should
  not decide whether the source permanent is prepared, whether a live copy
  exists in exile, or whether a player may cast the copy. Those decisions remain
  keyed to `PrepareCopyInfo`, source object identity, zone state, and the
  successful-cast event.
- Do not make the first implementation depend on a real printed consumer. Use a
  focused synthetic/regression test for spell-copy preservation if no current
  card in Mage observes "was cast as a prepare spell".

### Do Not Centralize Lifecycle In PermanentImpl#setPrepared

`PermanentImpl#setPrepared(boolean, Game)` lacks source context. It cannot know
which ability caused the prepare event, which player should be associated with
the copy, or which source reference should own cleanup metadata.

Add a helper with source context:

```java
PrepareUtil.setPrepared(Permanent permanent, boolean prepared, Ability source, Game game)
```

Expected helper behavior when setting prepared true:

- reject null permanent/source/game inputs,
- reject permanents for which no prepare spell characteristics can be resolved,
- if the permanent is already prepared, do not create a new copy from the
  repeated prepare event; clean up duplicate tracked copies if possible, keep
  the existing valid copy if one exists, and return,
- set the marker through the low-level permanent setter,
- create a `MageObjectReference` for the exact source permanent object,
- remove any stale or duplicate tracked prepare copy for that source object,
- create the standalone prepare copy from the resolved prepare spell
  characteristics,
- register the copy in game state and an exile zone,
- store a serializable `PrepareCopyInfo` in game state keyed by source permanent
  id plus zone-change counter,
- add a custom-duration cast permission effect for the copied card and record
  enough identity to discard that exact effect during cleanup,
- preserve the existing "Prepared" UI marker.

Expected helper behavior when setting prepared false:

- no-op if the permanent is already not prepared,
- clear the marker through the low-level permanent setter,
- remove the tracked exiled copy if it still exists in exile,
- clear the game-state tracking entry,
- let the custom-duration cast permission effect expire/discard itself.

All current callers should route through this helper:

- `BecomePreparedSourceEffect#apply`
- `BecomePreparedTargetEffect#apply`
- the enter-prepared path used by `EntersPreparedAbility`

### Resolve Prepare Spell Characteristics From Copiable Values

Resolved decision: do not require the source permanent to be a physical
`PrepareCard` at the point it becomes prepared.

Why:

- CR 722.2b makes the existence and values of the prepare spell's alternative
  characteristics part of the object's copiable values.
- The CR 722.3c example explicitly shows a token copy of a preparation creature
  becoming prepared and creating an exiled copy with only the prepare spell
  characteristics, not the token-copy modifications.
- Mage copy effects already preserve copy provenance through `copyFrom` on
  copied permanents and token-copy paths, so the Prepare helper should use that
  provenance rather than assuming `permanent.getMainCard()` is itself a
  `PrepareCard`.

Add a resolver that returns a resolved prepare-spell characteristics object.
Prefer an API like:

```java
Optional<PrepareSpellCharacteristics> PrepareUtil.getPrepareSpellCharacteristics(Permanent permanent, Game game)
```

where `PrepareSpellCharacteristics` is an immutable/copyable snapshot containing
the spell name, mana cost, color, card types, subtypes, supertypes, rules text,
spell ability, modes, targets, effects, and any other spell characteristics
needed to build the exiled copy. For a physical preparation card this snapshot
can be sourced from its `PrepareSpellCard`; for copied/token/merged permanents
it must represent the object's current prepare-spell copiable values without
leaking other copy-effect exceptions such as token color, power/toughness, or
creature subtypes.

The resolver should find prepare spell characteristics from:

- a physical `PrepareCard`,
- a `PermanentCard` wrapping a preparation card,
- a copied permanent whose `copyFrom` or active copy-effect blueprint has
  prepare spell characteristics,
- a token copy whose token provenance points back to a preparation card or
  preparation permanent,
- any future Mage copy path that records copyable values for a source object
  with a prepare spell.

If no prepare spell characteristics can be resolved at the moment a permanent
would gain prepared, the helper must reject the prepare event under CR 722.3a.

Once a copy has been created, do not clear prepared or remove the exiled copy
solely because the source permanent's current copiable values later stop
showing a prepare spell. CR 722.3c's ongoing condition for that already-created
copy is that the prepared permanent remains on the battlefield and has the
prepared designation. However, any later event that needs to create a fresh copy
from current characteristics, such as phase-in while prepared, must resolve a
prepare spell characteristics snapshot again; if no characteristics can be
resolved then, no fresh copy can be created.

### Mutated And Merged Permanents

Resolved decision: prepared is a designation on the merged permanent object, not
on an individual mutation component.

Rules basis:

- CR 702.140c makes a legal mutating creature spell merge with the target
  creature instead of entering the battlefield.
- CR 702.140e gives the resulting permanent all component abilities, but its
  non-ability characteristics come from the topmost component.
- CR 730.2c makes the merged permanent the same object it was before.
- CR 722.3a gates gaining prepared on whether the permanent has the alternative
  characteristics of a prepare spell.
- CR 722.3c keeps an already-created Prepare copy alive while the source
  permanent remains on the battlefield and prepared; it does not also require
  that the source permanent continue to expose prepare spell characteristics.

Implementation consequences:

- Keep the `prepared` marker on `PermanentImpl`; do not move prepared state to a
  mutation component or per-card structure.
- Replace the current `getMainCard() instanceof PrepareCard` eligibility check
  with the prepare-spell resolver so merged permanents use their current
  top-component prepare characteristics.
- If a prepared permanent mutates, keep the prepared designation and the
  existing live Prepare copy. Mutate does not make the source permanent leave
  the battlefield, become a new object, or gain prepared again.
- Do not recreate, replace, or retarget the live Prepare copy just because a
  mutation component was placed over or under the source permanent. The copy was
  created from the prepare spell characteristics that existed when the permanent
  became prepared.
- If a nonprepared merged permanent later tries to gain prepared, the resolver
  must determine whether the current merged permanent has prepare spell
  characteristics. Because prepare spell characteristics are not merely
  abilities, a Prepare component underneath a non-Prepare top component should
  not be enough by itself.
- If the top component has prepare spell characteristics, the merged permanent
  can gain prepared and the copy should be made from those top-component prepare
  spell characteristics only. Abilities from lower mutation components do not
  get copied into the exiled Prepare spell unless they are part of the prepare
  spell characteristics being copied.
- If a lower component has a "becomes prepared" ability, that ability can exist
  on the merged permanent under the mutate rules, but it still fails CR 722.3a
  unless the merged permanent currently has a prepare spell.
- If a legal mutating spell has an "enters prepared" ability, that ability does
  not make the merged permanent prepared solely from the mutate merge, because
  the mutating spell does not enter the battlefield. If the mutate target is
  illegal and the spell resolves as a normal creature spell instead, ordinary
  enters-prepared handling applies.
- If a prepared merged permanent phases out, remove the live copy without
  clearing prepared as already planned. On phase-in, create a fresh copy only if
  the current merged permanent can resolve prepare spell characteristics from
  its current top component/copy provenance.
- If a prepared merged permanent leaves the battlefield, remove the live Prepare
  copy and clear tracking once for the merged permanent object. Do not try to
  keep separate Prepare copies for individual mutation components.

Resolver requirements:

- `PrepareUtil.getPrepareSpellCharacteristics(...)` must inspect
  merged-permanent state before falling back to the physical base card. In Mage
  terms, use the ordered `getMutateForView()` component ids, where available,
  to identify the current top component.
- If the top component id is the permanent id, use the base permanent's current
  card/copy provenance.
- If the top component id is a mutation component, resolve that component card
  from the game and inspect its card/copy provenance for prepare spell
  characteristics.
- Lower mutation components can contribute abilities, but they should not supply
  prepare spell characteristics unless they are also the top component under the
  merged-permanent characteristic rules.

### Track State In GameState, Not Static Maps

Prepare state must survive simulation copies, bookmarks, rollback, and saved
game cloning consistently.

Resolved decision: use a dedicated `GameState` field and helper methods for
Prepare tracking. A `GameState` value would be acceptable only if the stored
object is immutable or implements `Copyable`, but a typed field is clearer and
easier to audit because the state-based action, cast permission, zone cleanup,
and rollback paths all need the same indexed data.

Use a serializable/copyable state object such as `PrepareCopyInfo` containing:

- source permanent id,
- source permanent zone-change counter,
- source `MageObjectReference`,
- exiled prepare copy id,
- copy creator / copied-card owner id for Mage bookkeeping,
- no fixed allowed caster; the source permanent's current controller is
  resolved dynamically at cast/playability time,
- exile zone id,
- either the registered cast-permission effect id, or a stable permission key
  embedded in the `PrepareCastFromExileEffect` that lets cleanup discard every
  matching effect for this source/copy pair,
- a flag/metadata field identifying this card as a prepare copy.

Avoid static maps or utility-level mutable state.

Required `GameState` integration:

- add a `Map<PrepareSourceKey, PrepareCopyInfo>` or equivalent typed field,
- deep-copy that map in the `GameState` copy constructor,
- restore that map in `GameState#restore(...)`,
- clear that map in `clearOnGameRestart()`,
- expose narrow helpers such as `getPrepareCopyInfo(...)`,
  `putPrepareCopyInfo(...)`, `removePrepareCopyInfo(...)`,
  `findPrepareInfoByCopyId(...)`, and `isLivePrepareSpellCopy(...)`,
- keep `PrepareCopyInfo` immutable if possible; otherwise implement `Copyable`
  and make `CardUtil.deepCopyObject(...)` able to copy it if it ever lives in a
  state value or collection copied through that utility.

Do not depend on object identity surviving `GameState.copy()`. Store ids and
`MageObjectReference`s, and re-resolve the source permanent and exiled copy
against the current `Game` each time a lifecycle or cast-permission check runs.

The cast-permission effect identity must be explicit. Mage's `game.addEffect`
copies continuous effects and assigns the registered copy a fresh id, so do not
assume the pre-registration effect object's id is the id stored in
`ContinuousEffects`. If cleanup uses an effect id, add a narrow registration
helper that returns the registered effect id. If cleanup instead uses a stable
source/copy key, add a narrow helper that finds and discards all
`PrepareCastFromExileEffect` instances with that key. In both designs,
clearing Prepare tracking must not leave an unbounded custom-duration
as-though effect behind.

### Make Prepare Copy State Bookmark-Safe

Resolved decision: Prepare copy registration must be coherent as a single
`GameState` concern. A later state restore should restore either the whole
prepared state or none of it; it must not restore only the marker, only the
exile-zone id, or only the copied card object.

When a permanent is prepared, these pieces must be updated together:

- the permanent's prepared marker,
- the standalone `PrepareSpellCopyCard` in copied-card registration,
- the copied card's `Zone.EXILED` entry in zone state,
- the actual `ExileZone` membership,
- the `PrepareCopyInfo` mapping,
- the custom `CAST_FROM_NOT_OWN_HAND_ZONE` permission effect.

When a bookmark or rollback restores to before the permanent became prepared,
all six pieces should be absent. When it restores to after the permanent became
prepared, all six pieces should be present and internally consistent. Avoid
"repairing" missing pieces during restore unless the repair is part of a
well-scoped consistency check; restore should primarily be data restoration, not
rule execution.

`GameState.copiedCards` needs special care:

- existing `GameState#copy` shallow-copies `copiedCards`; that is risky for
  Prepare because the exiled copy can persist and later be mutated by playable
  simulation, target selection, casting, or cleanup,
- the registration helper should ensure registered Prepare copies are copied
  independently when a `GameState` is copied,
- the least invasive implementation is a copied-card copy helper that preserves
  current behavior for existing copied-card mechanics but deep-copies
  `PrepareSpellCopyCard` entries by id,
- if a broader `copiedCards` deep-copy is attempted, audit multi-part copied
  cards first; naively copying every entry independently can break internal
  part relationships for split, double-faced, Adventure, or Omen-style copied
  cards.

Resolved decision: do not store Prepare-created copies under
`GameState.COPIED_CARD_KEY`.

Why:

- `COPIED_CARD_KEY` is a generic copied-card LKI workaround. `GameImpl#getCard`
  falls back to it after a card has left `GameState.copiedCards`.
- That fallback is dangerous for Prepare. Once the CR 722.3c exception stops
  applying, the Prepare copy should cease to exist like other copied objects
  under CR 704.5e. A stale `COPIED_CARD_KEY` value would make
  `game.getCard(copyId)` keep returning a copied card object after it should no
  longer be a durable card.
- Avoiding `COPIED_CARD_KEY` is the cleanest way to make `Delay`, `Remand`,
  `Memory Lapse`, `Sink into Stupor`, `Feather, the Redeemed`, and
  `Pull from Eternity`-style interactions fail to find a destination-zone
  "card" for the consumed Prepare copy.

Implementation consequences:

- Register the live Prepare copy in `GameState.copiedCards`, zone state, a real
  `ExileZone`, and `PrepareCopyInfo`.
- Do not call `setValue(GameState.COPIED_CARD_KEY + copyId, ...)` for
  `PrepareSpellCopyCard`.
- Do not rely on `GameState#copyCard(...)` for Prepare registration, because it
  always writes the `COPIED_CARD_KEY` backup and copies the wrong object shape
  for CR 722.3c.
- While the copy is live in exile or on the stack, `game.getCard(copyId)` should
  resolve through `GameState.copiedCards`.
- After the copy has been consumed and removed from `copiedCards`,
  `game.getCard(copyId)` should return `null`.
- The CR 704.5e state-based-action exemption only needs to protect live valid
  Prepare copies in `GameState.copiedCards`; there should be no
  `COPIED_CARD_KEY` entry for copied-card SBA cleanup to scan.
- If a future engine path proves it needs last-known information for a consumed
  Prepare copy, add a Prepare-specific LKI snapshot that is not returned by
  `GameImpl#getCard` and is not treated as a card in any zone. Do not re-use
  `COPIED_CARD_KEY` for that purpose.

The as-though effect should be bookmark-safe by construction:

- store only ids, source references, and other copyable/immutable fields,
- implement a normal copy constructor,
- perform no prompts or irreversible side effects in `applies(...)`,
- dynamically validate against `PrepareCopyInfo` every time it is queried,
- call `discard()` once the tracked copy is no longer live so
  `ContinuousEffectsList#removeInactiveEffects(...)` can clean it up.

Self-discard from `applies(...)` is acceptable because existing Mage
as-though effects use that pattern for stale permissions, but it must be the
only mutation performed from playability checks. Do not remove cards from
zones, edit `PrepareCopyInfo`, or mutate player cast-cost side channels from
`PrepareCastFromExileEffect#applies(...)`; the lifecycle helpers and
state-based cleanup own those changes.

Playable simulations should be treated as hostile to hidden side effects. The
Prepare as-though effect should use `CAST_FROM_NOT_OWN_HAND_ZONE` and should not
write player cast-cost side channels from `applies(...)`; the copied spell's
normal spell ability should own timing, costs, target selection, and mana
payment.

Do not implement Prepare's permission by calling
`AsThoughEffectImpl.allowCardToPlayWithoutMana(...)` or any helper that seeds
`Player.castSourceIdCosts`, `Player.castSourceIdManaCosts`, or
`Player.castSourceIdWithAlternateMana`. `PlayerImpl#getPlayableFromObjectSingle`
copies those maps back from the simulation player to the real player after
as-though checks, so a permission effect that mutates them during playability
can leak alternate-cost state into the real cast. Prepare grants zone
permission only; it does not choose or replace costs.

### Maintain One Live Copy Per Source Permanent

Resolved invariant: one source permanent object can have zero or one live
associated prepare copy in exile.

Implementation consequences:

- Key `PrepareCopyInfo` by source permanent id plus source zone-change counter.
- Treat that key as owning a single copy id.
- Before creating a copy for a source key, remove any existing tracked copy for
  that key if it is still in exile.
- If the existing tracked copy is no longer in exile, clear the stale tracking
  entry before creating the replacement.
- `PrepareCastFromExileEffect` should approve only the currently tracked copy id
  for that source key. Any duplicate or orphaned prepare copy must be
  uncastable even before cleanup runs.
- Phase-in should use a copy-creation helper that enforces this invariant, so
  duplicate `PHASED_IN` handling cannot create multiple live copies.
- Re-preparing after the prior copy was cast should create a fresh copy only
  after the previous tracking entry has been cleared.
- Repeated "becomes prepared" effects while the permanent is already prepared
  must not create a replacement copy; CR 722.3a says the permanent cannot gain
  prepared again while it already has it. The duplicate cleanup path may remove
  extras but should not use that repeated event as authority to create another
  copy.

### Centralize Prepare Copy Registration In GameState

Resolved decision: add a dedicated `GameState` helper, with a `Game` wrapper if
needed, for registering and unregistering standalone Prepare copies in exile.

`PrepareUtil` should own rule orchestration: deciding when a permanent gains or
loses prepared, when phasing removes/recreates the copy, and which source
permanent a copy is linked to. It should not manually mutate scattered engine
state for copied-card registration.

The `GameState`/`Game` helper should own these operations as one atomic-looking
registration path:

- create/register the standalone `PrepareSpellCopyCard`,
- add the copy to `GameState.copiedCards` so `game.getCard(copyId)` can find it,
- set the copy's zone to `Zone.EXILED`,
- add the copy to an actual `ExileZone` so playable discovery can see it,
- set or update `PrepareCopyInfo` for the source permanent id plus zone-change
  counter,
- mark the copy as protected by CR 722.3c from normal copied-card state-based
  cleanup while the linked source remains valid,
- deliberately skip the generic `GameState.COPIED_CARD_KEY` LKI workaround so a
  consumed Prepare copy cannot later be found as a stale card.

Suggested API shape:

```java
Card createPrepareSpellCopyInExile(
    PrepareSpellCharacteristics characteristics,
    Permanent sourcePermanent,
    UUID copyCreatorId,
    Game game
);

void removePrepareSpellCopy(UUID copyId, Game game);

boolean isLivePrepareSpellCopy(UUID copyId, Game game);
```

The exact signatures can change during implementation, but the ownership
boundary should not: `PrepareUtil` calls this helper; it should not directly
coordinate `copiedCards`, zone state, exile-zone membership, and
`PrepareCopyInfo` itself.

### Remove Prepare Copies With Zone-Aware Semantics

Resolved decision: Prepare copy removal should be handled by the dedicated
registration helper, and it must branch on the copied card's current zone.

Rules basis:

- CR 722.3c makes the exiled prepare copy exist only while the linked permanent
  remains on the battlefield and prepared.
- CR 722.3c also clears prepared at the CR 601.2i point when the copy becomes
  cast.
- CR 704.5e normally makes copied cards cease to exist outside the stack or
  battlefield, but CR 722.3c is a temporary exception for the live linked
  prepare copy in exile.

Implementation consequences:

- Add a copied-card state-based-action exemption for live, valid Prepare copies
  in exile. The generic CR 704.5e copied-card cleanup must not remove a prepare
  copy while `isLivePrepareSpellCopy(copyId, game)` is true.
- When CR 722.3c stops keeping the copy alive because the source permanent left
  the battlefield, became a new object, lost prepared before casting, or phased
  out, call the dedicated removal helper.
- If the tracked copy is still in `Zone.EXILED`, the removal helper should:
  - remove the card id from the actual `ExileZone`,
  - set the copy's zone to `Zone.OUTSIDE`,
  - remove the live copy entry from `GameState.copiedCards`,
  - clear the `PrepareCopyInfo` entry for the source permanent key,
  - assert or clean up any accidental `GameState.COPIED_CARD_KEY + copyId`
    value, because Prepare registration should never create one.
- If the tracked copy is already in `Zone.STACK`, the removal helper must not
  physically remove it from the stack. This is the successful-cast case: clear
  the source permanent's prepared marker and `PrepareCopyInfo`, then let normal
  stack resolution and copied-card cleanup finish the copied spell.
- If the tracked copy is already `Zone.OUTSIDE`, missing, or absent from all
  exile zones, treat the operation as stale-state cleanup: clear `PrepareCopyInfo`
  and any custom-duration Prepare permission state, but do not report an engine
  error.
- If duplicate prepare-copy-looking cards somehow exist, only the currently
  tracked copy id is authoritative. Remove or make uncastable orphaned exile
  copies opportunistically, but do not touch unrelated copied cards.

Why:

- `Exile#removeCard(...)` only removes exile-zone membership. It does not update
  copied-card registration, zone state, Prepare tracking, or LKI state.
- `PlayerImpl#cast` and `ZonesHandler` move the copied card to `Zone.STACK`
  before the later `SPELL_CAST` cleanup decision. Removing the copied card after
  that point would destroy the spell that was successfully cast.
- Existing copied-card cleanup already knows how to remove ordinary copied cards
  under CR 704.5e. Prepare should only add the CR 722.3c live-copy exemption and
  a focused helper for the moments when that exception no longer applies.

### Countered, Returned, Or Moved Prepare Copies

Resolved decision: after a prepared spell copy has been cast from exile, effects
that counter it, return it to hand, put it into a library, exile it, or otherwise
move it off the stack must not create a durable card object in the destination
zone.

Rules sequence:

1. The player casts the live Prepare copy from exile using CR 722.3c's cast
   permission.
2. When the casting process reaches CR 601.2i and the spell becomes cast,
   CR 722.3c immediately makes the source permanent lose prepared. The
   `PrepareCopyInfo` entry should be cleared at this point, but the copied spell
   must remain on the stack.
3. If an effect counters the copied spell, CR 701.6a removes it from the stack
   and it does not resolve.
4. If an effect returns the copied spell to hand, puts it into a library, exiles
   it, or otherwise moves it off the stack, the copied spell leaves the only zone
   where it can legally continue to exist as a copied spell.
5. There is no longer a live CR 722.3c Prepare exception: the source permanent
   already lost prepared when the spell became cast.
6. The destination effect does not create a new durable card. CR 704.5e makes
   copied spells cease to exist outside the stack, and copied cards cease to
   exist outside the stack or battlefield.

Intuition: treat this like the token zone-change model. The Prepare-created
object is only allowed to exist in exile because CR 722.3c specifically keeps
that source-linked copy there. Once the player casts it, it changes from the
live exiled Prepare copy into a copied spell on the stack, and the source
permanent loses prepared. If another effect later tries to move that copied
object again, such as Delay trying to exile the countered spell, the object does
not become a new durable exiled/suspended object. It either has already been
removed by the stack-copy path or must cease to exist under copied-object
cleanup.

Specific interactions:

- `Delay`: counters the copied spell, but the copy must not become a suspended
  card. CR 702.62b defines a suspended card as a card in exile with suspend and
  a time counter on it, but suspend does not override CR 704.5e for copied
  spells/cards.
- `Remand`: counters the copied spell, but the copy is not put into its owner's
  hand. Remand still countered the spell, so Remand's draw effect proceeds
  normally if Remand resolved.
- `Memory Lapse`, `Lapse of Certainty`, `Hinder`, and similar effects: counter
  the copied spell, but the copy is not put on top of, on the bottom of, or
  shuffled into its owner's library.
- `Sink into Stupor`, `Brutal Expulsion`, and similar "return target spell"
  effects: the copied spell is returned/removed from the stack and does not
  resolve, but it was not countered. The copy must not appear as a durable card
  in its owner's hand.

Implementation consequences:

- Do not recreate the Prepare copy after a counter/return/move effect removes
  the copied spell from the stack. The source permanent has already consumed
  prepared.
- Do not keep a copied card that was moved by such an effect associated with the
  source permanent. It is not a live Prepare copy and must not be accepted by
  `isLivePrepareSpellCopy(...)`.
- Do not leave a `GameState.COPIED_CARD_KEY` backup for the consumed Prepare
  copy. After the copied spell has left the stack and copied-card cleanup has
  run, `game.getCard(copyId)` should not be able to resurrect it as a stale
  card object.
- Do not exempt these moved copied cards from CR 704.5e cleanup. The only
  copied-card-in-exile exemption is the live CR 722.3c copy linked to a source
  permanent that is still on the battlefield, phased in, and prepared.
- A Prepare-created copied card must not migrate from the live CR 722.3c exile
  context into a second durable zone context such as a hand, library, graveyard,
  command zone, or suspend exile zone. The attempted zone change consumes no new
  persistent object for the moving effect to manage.
- If Mage's counter path removes copied spells directly from the stack without
  moving their copied card to exile, that is acceptable for this interaction:
  the spell is countered, does not resolve, and the source remains unprepared.
- If Mage's return-to-hand path removes copied spells directly from the stack
  without moving their copied card to hand, that is acceptable for this
  interaction: the spell leaves the stack, was not countered, does not resolve,
  and the source remains unprepared.
- If a future path temporarily places the copied card in the named destination
  before cleanup, the next copied-object cleanup must move it to `Zone.OUTSIDE`,
  remove it from any real zone collection, remove any copied-card registration,
  and leave no hand/library/exile/suspend playability behind.
- Existing non-copy spells must keep ordinary behavior: real cards countered by
  Remand should return to hand, real cards countered by Memory Lapse/Hinder
  should move to the library destination, real cards countered by Delay should
  move to the suspend exile zone with time counters and suspend, and real card
  spells returned by Sink into Stupor should return to hand.

Mage code facts to preserve:

- `CounterTargetWithReplacementEffect` delegates to
  `game.getStack().counter(..., putIt)`, so Remand, Memory Lapse, Lapse of
  Certainty, Hinder, and similar cards use the same stack-copy behavior as
  Delay.
- `DelayEffect` counters with `PutCards.EXILED` and then calls
  `SuspendAbility.addTimeCountersAndSuspend(spell.getMainCard(), 3, ...)`.
- `SuspendAbility.addTimeCountersAndSuspend(...)` already returns false for
  `card.isCopy()`.
- `ExileSpellWithTimeCountersEffect` similarly refuses to suspend copied cards.
- `PlayerImpl#cast` marks the stack `Spell` as a copy when the cast card is a
  copied card, and `Spell#counter` removes copied spells from the stack without
  moving the underlying copied card to another zone.
- `ReturnToHandTargetEffect` already detects copied spells, removes them from
  the stack, and only moves non-copy card objects to hand.

### Cast Permission Follows The Source Permanent's Current Controller

Resolved decision: cast permission tracks the current controller of the prepared
source permanent.

CR 722.3c says the permanent's controller creates the exiled copy when the
permanent gains prepared or phases in prepared. The later cast permission is
phrased dynamically: for as long as the copy remains in exile, the prepared
permanent's controller may cast it.

Implementation consequences:

- `PrepareCopyInfo` should store the source permanent reference and copy id, not
  a fixed allowed caster as the source of truth.
- The copy should still record the controller who created it as the copied-card
  owner/bookkeeping owner under CR 112.2a. This is separate from the dynamic
  casting permission.
- `PrepareCastFromExileEffect` should compare `playerId` to
  `sourcePermanent.getControllerId()` at playability/cast time.
- Changing control of the prepared permanent should not create or remove the
  copy. It should immediately change which player can cast the existing copy.
- Tests must prove the original preparing player loses permission after losing
  control, the new controller gains permission while the permanent remains
  prepared, and the copied card's bookkeeping owner does not become the casting
  authority.

### Additional Edge-Case Audit Decisions

The Reddit deep-dive linked in the source list is useful as a checklist, not as
rules authority. The implementation should use the CR citations above as the
source of truth and cover these cases explicitly.

#### Card, Spell, And Copy Wording

Resolved decision: the live object in exile is a copied object created by
Prepare, not a normal physical card in exile. When that object is cast, it is a
spell on the stack. This distinction must be reflected in triggers,
restrictions, cost modifications, targeting, and destination effects.

Implementation consequences:

- Effects that care about casting a spell from exile should see the cast
  prepare copy. Examples to cover are `The Thirteenth Doctor`, `Passionate
  Archaeologist`, and `Doc Aurlock, Grizzled Genius`-style wording.
- Effects that care about playing or casting a card from exile should not see
  the prepare copy as a played card. `Prosper, Tome-Bound`-style wording should
  not trigger from casting a prepare copy.
- Effects that require an instant or sorcery card should not see the prepare
  copy as a physical instant or sorcery card. `Eye of the Storm`-style wording
  should not exile or copy it as a card.
- Effects that try to exile, return, or otherwise move "that card" after a spell
  resolves or is countered should not find a durable card object for a prepare
  copy. `Feather, the Redeemed`-style wording and `Pull from Eternity`-style
  targeting must fail for the copied object unless Mage has a separate
  live-card object that the CR actually allows.
- Because Mage will represent the live exiled copy as a `CardImpl` in an
  `ExileZone` for playability, card-targeting and card-filtering code needs an
  explicit guard. Add a narrow helper such as
  `PrepareUtil.isDurableExiledCardForCardWording(...)` or
  `PrepareUtil.isLivePrepareSpellCopy(...)` plus caller-specific wording, then
  use it in generic exiled-card targeting paths such as
  `TargetCard#getAllPossibleTargetInExile` and `TargetCardInExile#possibleTargets`.
  The guard must reject live Prepare copies for "target exiled card" and
  "instant or sorcery card in exile" wording without hiding those copies from
  the separate playable-discovery path that offers CR 722.3c casting.
- Magecraft-like "whenever you cast or copy an instant or sorcery spell" should
  not trigger when Prepare creates the exiled copy, because CR 722.3c creates a
  copy in exile rather than copying a spell on the stack. It should trigger
  when the prepare copy is later cast if the copied spell has the appropriate
  type.
- `Twinning Staff`-style effects should not create an extra copy from the
  initial CR 722.3c copy creation. If an effect later copies the spell while it
  is on the stack, that ordinary spell-copy interaction is outside the Prepare
  lifecycle.

#### Timing, Costs, And Repeated Prepare Attempts

Resolved decision: Prepare is cast permission, not an alternative cost and not a
triggered delayed action.

Implementation consequences:

- The cast-permission effect must only grant permission to cast the copied spell
  from exile. It must let normal Mage spell casting handle alternative costs,
  additional costs, cost reductions, cost increases, targets, timing,
  restrictions, and replacement effects.
- `Doc Aurlock, Grizzled Genius`-style cost reductions for spells cast from
  exile should apply through the normal cost pipeline.
- Effects that grant broader timing permission, such as
  `Vedalken Orrery`-style effects, should work through existing timing
  machinery. Prepare should not implement timing rules itself.
- An "enters prepared" permanent should enter with the prepared marker and live
  exile copy already established. If the permanent spell resolves onto an empty
  stack during its controller's main phase, that controller should receive the
  first opportunity to cast a sorcery-speed prepare spell before opponents can
  remove the source. If triggers are waiting to be put on the stack, normal
  priority/trigger ordering applies and sorcery-speed prepare spells still
  require an empty stack.
- If an ability instructs an already prepared permanent to become prepared, the
  ability should still have been activated or resolved normally and any costs
  should remain paid. The only no-op is the CR 722.3a result: do not create a
  second copy, do not replace the current copy, and do not reset permission
  state.
- If casting the prepare copy is also an event that can make the source
  prepared again, the source must lose prepared at the successful-cast point
  first. A later resolving trigger may make the same source prepared again and
  create a fresh copy, preserving the one-live-copy invariant.

#### Names, Restrictions, And Non-Battlefield Characteristics

Resolved decision: the physical preparation card is one card for deck and
non-battlefield purposes, but the prepare spell copy has its own copied spell
name and characteristics while it exists.

Implementation consequences:

- Name-choice UI and card-name validators should allow a preparation card's
  alternative prepare name under CR 722.5. If a player chooses that alternative
  name for `Meddling Mage`-style restrictions, casting the prepare copy with
  that name should be prohibited by the normal restriction engine.
- Card search, tutors, and filters in hand/library/graveyard should inspect the
  preparation card's normal characteristics under CR 722.4, not the prepare
  spell's name, card type, or mana cost. For example, a library search for an
  instant or sorcery card should not find a preparation creature solely because
  it has an instant or sorcery prepare spell.
- Battlefield characteristics of the source permanent should remain its normal
  characteristics. Devotion or "mana symbols among permanents you control"
  effects should not count mana symbols from the inset prepare spell on the
  source permanent, while Commander color identity should include alternative
  characteristics under CR 903.4e.
- Deck-construction and format-ban checks should treat the preparation card as
  one physical card. Singleton/highlander and banned-list checks should not
  treat the prepare spell name as a second card, although color identity checks
  must include alternative characteristics where the format requires that.

#### Source Changes After The Copy Exists

Resolved decision: the exiled prepare copy is a snapshot of the prepare spell
characteristics from the moment the source became prepared or phased in
prepared. Later changes to the source permanent do not rewrite that existing
copy. Later events that create a fresh copy must use the source's current
prepare spell characteristics at that later moment.

Implementation consequences:

- A prepared permanent that later loses abilities, changes types, or has its
  text changed should keep its prepared marker and existing live copy as long as
  it remains on the battlefield, phased in, and prepared.
- `Darksteel Mutation`-style type/ability removal should not erase prepare
  spell alternative characteristics merely because those characteristics are
  not abilities on the permanent. If an unprepared preparation permanent is
  later instructed to become prepared, the resolver should still be able to find
  the prepare spell unless an actual copy/characteristic effect has changed the
  object's copiable values.
- `Mirrorform`/copy-effect style changes should not modify an existing exiled
  copy. If the source later becomes unprepared and then gains prepared again,
  the new copy should be built from the source's current prepare spell
  characteristics at that new event.
- If an effect attempts to make a permanent prepared while it does not currently
  have prepare spell characteristics, that effect fails to create a copy under
  CR 722.3a. If the permanent later gains prepare spell characteristics, the
  earlier failed effect should not retroactively create a copy.
- Text-changing or text-box-exchange effects, including `Exchange of Words` or
  Deadpool-style text-box swaps, should not by themselves add, remove, or swap
  the inset prepare spell characteristics unless Mage models a specific
  copyable-values effect that does so. The resolver should not infer prepare
  spell characteristics only from normal rules text.
- The prepared designation itself is not copiable. Copying a prepared permanent
  should create an unprepared object with prepare spell characteristics in its
  copiable values. That copy can later become prepared and create its own linked
  exiled copy.
- A noncreature permanent can become prepared if it has prepare spell
  characteristics and an effect actually instructs that permanent to become
  prepared. Myrkul-style noncreature token copies are a useful regression case.
  Targeting restrictions still apply, but "this creature becomes prepared"
  self-reference should not add a hidden creature-type requirement once the
  ability is resolving for its own source.
- Blinking, reanimating, or otherwise returning the source permanent creates a
  new object. Any old live copy tied to the previous object must be removed when
  the old source leaves. If the new object enters prepared, it creates a new
  copy through the ordinary enters-prepared path.

## Implementation Steps

### 1. Introduce Prepare-Specific Helpers And Metadata

Add a small Prepare utility package/class close to existing card mechanics.
Responsibilities:

- validate whether a permanent can gain prepared under CR 722.3a,
- find the prepare spell characteristics from the permanent's current object,
  merged-permanent state, copy provenance, and current copiable values,
- build `PrepareCopyInfo`,
- create/register/remove the exiled copy,
- answer "is this copied card still the live prepare copy for this permanent?",
- answer "may this player cast this copied card from exile right now?".

Keep this code centralized so `BecomePrepared...` effects, watchers, and
as-though effects share one validity check.

Do not use the CR 722.3a "already prepared" check as an activation restriction
unless the printed ability explicitly requires an unprepared permanent. Let the
ability resolve normally, then have the helper no-op only the become-prepared
result if the permanent is already prepared.

### 2. Build `PrepareSpellCopyCard`

Create a standalone copied card implementation.

Required invariants:

- extends `CardImpl`,
- `getMainCard()` is itself,
- no parent `PrepareCard`,
- not a `CardWithSpellOption`,
- not a `SpellOptionCard` whose methods delegate to the physical card,
- all abilities, especially the spell ability, have the copy card id as source
  id after the copy id is final,
- subabilities and watchers copied from the resolved characteristics also have
  the copy card id as source id,
- copied modes/targets/effects are independent copies,
- copied modes/targets/effects are not shared mutable state with the
  characteristics snapshot or physical `PrepareSpellCard`,
- metadata clearly identifies the object as a Prepare-created copy, not as an
  ordinary physical card or as the real inset `PrepareSpellCard`,
- moving the copy to stack/graveyard/exile follows normal copied-spell/card
  handling.

This avoids the current `PrepareSpellCard` copy constructor, which preserves a
parent reference and is not suitable for an independent exiled copy.

Recommended validation:

- assert `copy.getSpellAbility().getSourceId().equals(copy.getId())`,
- assert every ability in `copy.getAbilities()` has source id equal to
  `copy.getId()`,
- assert casting the prepare copy produces a `SPELL_CAST` event whose source id
  is the exiled copy id stored in `PrepareCopyInfo`.

### 3. Register The Copy In Exile

When the source permanent becomes prepared:

- remove or clear any existing prepare copy tracked for the same source
  permanent id plus zone-change counter,
- call the centralized Prepare copy registration helper,
- have that helper create or reuse a deterministic exile zone name such as
  `"Prepare"`,
- have that helper add the copy to `copiedCards`, set `Zone.EXILED`, add the
  copy to the `ExileZone`, and write `PrepareCopyInfo`,
- ensure `PlayerImpl#getPlayable` can discover the copy by scanning exile zones.

Do not rely on zone state alone. The UI/playability path iterates real exile
zones. Do not hand-code this registration sequence in every Prepare lifecycle
entry point.

### 4. Add Cast Permission From Exile

Add a Prepare-specific `AsThoughEffect`, likely:

```java
PrepareCastFromExileEffect extends AsThoughEffectImpl
```

Recommended settings:

- `AsThoughEffectType.CAST_FROM_NOT_OWN_HAND_ZONE`,
- `Duration.Custom`,
- target/object id is the exiled prepare-copy id.

The effect should approve casting only if:

- the requested object id equals the tracked copy id,
- the object is still in `Zone.EXILED`,
- the card exists in an actual exile zone,
- the player id matches the source prepared permanent's current controller,
- the source `MageObjectReference` resolves to the same permanent object,
- that permanent is on the battlefield,
- that permanent is prepared,
- the permanent is phased in,
- the source permanent's tracked prepare copy is still the requested copy.

If multiple prepare-copy-looking cards somehow exist in exile for the same
source permanent, only the copy id currently stored in `PrepareCopyInfo` is
castable. The validity check may self-discard stale permission effects, but it
should not remove orphaned cards or edit game-state tracking from
`applies(...)`; explicit lifecycle cleanup and the copied-card state-based
action should handle physical cleanup.

The effect should not implement timing rules itself. Let the copied spell's
normal `SpellAbility#spellCanBeActivatedNow` path handle instant/sorcery timing,
cost payment, targets, restrictions, and replacement effects.

The effect should also not bypass normal cast prohibitions or cost modifiers.
Name restrictions, "can't cast" effects, exile spell cost reductions, additional
costs, and alternative-cost choices should all be handled by the ordinary cast
pipeline after Prepare only answers "may this player cast this object from this
zone at all?"

Do not use `PLAY_FROM_NOT_OWN_HAND_ZONE` for the primary Prepare implementation.
That broader hook is for effects that may allow either land plays or spell
casts. Prepare never allows playing a land; CR 722.3c says the controller may
cast the exiled copy.

### 5. Clear Prepared On Successful Cast

Use `SPELL_CAST`, not early `CAST_SPELL`.

Implementation options:

- preferably add a game watcher such as `PrepareSpellCastWatcher`, because
  Mage handles watchers before delayed and normal triggers for the same event,
  or
- add a custom effect/listener tied to the cast permission effect only if it can
  run before any ordinary cast trigger or watcher could observe the source
  permanent's prepared marker.

Required behavior:

- on `SPELL_CAST`, read `event.getSourceId()` as the copied card id,
- if that id is a tracked live prepare copy, resolve the source permanent from
  `PrepareCopyInfo`,
- call the source-aware prepare helper to clear prepared,
- tolerate the source permanent already being absent or unprepared,
- do not clear prepared for illegal, cancelled, or failed casts,
- ensure the prepared marker is gone before normal "when a spell is cast" style
  triggers inspect game state for that `SPELL_CAST` event.

Rationale:

- `PlayerImpl#cast` fires `SPELL_CAST` after `spell.activate(...)` succeeds.
- CR 722.3c/601.2i cares about the point where the spell becomes cast, not the
  start of announcement.
- Mage's `GameState#handleEvent` calls watchers before delayed and normal
  triggers, so a watcher-based cleanup is the most direct fit for making the
  marker disappear at the successful-cast event before triggered abilities are
  checked.

### 6. Clean Up On Zone Changes And State Changes

The copy must be removed or invalidated when:

- prepared is cleared,
- the copy becomes cast,
- the source permanent leaves the battlefield,
- the source permanent becomes a new object,
- the source permanent phases out,
- the source permanent phases in prepared and needs a fresh copy under
  CR 722.3c,
- game rollback/bookmark restoration removes the preparation event.

Do not clean up merely because the source permanent's current characteristics no
longer expose a prepare spell after the copy has already been created. For an
existing live copy, CR 722.3c tracks the source permanent remaining on the
battlefield and retaining prepared. Only events that must create a new copy,
such as phase-in while prepared, need to resolve current prepare spell
characteristics again.

Do not clean up merely because the prepared permanent mutates. Mutate is a
characteristic/object-representation change on the same permanent, not a
battlefield-exit event for the source permanent.

Add explicit cleanup, not only dynamic rejection.

Recommended shape:

- `PrepareUtil.clearPrepared(...)` calls the zone-aware removal helper when
  clearing the marker.
- A `ZONE_CHANGE` watcher or equivalent hook observes the source permanent
  leaving the battlefield and clears/removes the linked copy.
- A `PHASED_OUT` watcher or equivalent hook removes the linked copy without
  clearing the prepared marker.
- A `PHASED_IN` watcher or equivalent hook creates a fresh linked copy if the
  permanent is still prepared.
- The custom as-though effect dynamically rejects stale copies as a safety net.
- Cleanup should enforce that each source permanent key has no more than one
  live exiled copy after any prepare, unprepare, phase, cast, or zone-change
  transition.
- The cleanup path must not remove the copy after it has moved from exile to the
  stack as part of being cast; at that point the marker should be cleared, but
  the spell on the stack must remain.
- The generic copied-card state-based cleanup must skip live valid Prepare
  copies in exile, because CR 722.3c is the specific rule keeping them there.
- If the source permanent leaves after the prepare copy has already been cast,
  the stack spell remains independent and should resolve or be countered
  normally. Leaving the battlefield before the copy is cast removes the live
  exile copy; leaving after successful cast does not remove the copied spell
  from the stack.
- If the source permanent leaves and then returns, treat the returned permanent
  as a new object. Do not reconnect the old `PrepareCopyInfo` or old exiled
  copy to the returned object.

### 7. Handle Phasing Explicitly

CR 722.3c creates a copy when a permanent gains prepared or phases in prepared.
The implementation must not ignore the phase-in half of that sentence.

Resolved rule decision:

- Phase-out should remove or invalidate the current exiled prepare copy.
- Phase-out should not clear the source permanent's prepared designation.
- Phase-in should create a fresh exiled prepare copy if the permanent phases in
  prepared and still has a prepare spell.
- Phase-in should not reuse the pre-phase-out copy id.

Why:

- A phased-out permanent is treated as though it does not exist and is treated
  as though it is not on the battlefield, so the CR 722.3c condition that keeps
  the copy in exile is no longer satisfied.
- Phasing does not make the permanent change zones or become a new object, so
  the prepared designation remains on the source permanent.
- CR 722.3c explicitly creates a copy when the permanent phases in prepared.

Mage phasing points confirmed by local code:

- `GameEvent.EventType.PHASED_IN`
- `GameEvent.EventType.PHASED_OUT`
- `SourcePhaseInTriggeredAbility`
- `PhaseInTriggeredAbility`
- permanent `isPhasedIn()` handling
- `PermanentImpl#phaseOut(...)` fires `PHASED_OUT` after setting `phasedIn` to
  false,
- `PermanentImpl#phaseIn(...)` queues `PHASED_IN` as a simultaneous event after
  setting `phasedIn` to true.

Implementation consequence:

- Do not call the normal "unprepare" path on phase-out, because that would clear
  the prepared marker incorrectly.
- Add separate helper paths for "remove prepare copy because source is currently
  unavailable" and "clear prepared because the designation is consumed/lost".
- The as-though effect should require `sourcePermanent.isPhasedIn()`.
- The phase-in path should remove any stale tracked copy before creating the
  fresh one and should guard against duplicate live copies.

### 8. Preserve Rules Text, Search, And Rendering

Prepare cards currently populate prepare spell effects via:

```java
this.getSpellCard().getSpellAbility().addEffect(...)
```

The implementation must keep that card-authoring API or provide a mechanical
migration path with minimal card churn.

Required checks:

- parent card rules text displays the prepare spell name, cost, type, and text,
- deckbuilder/repository text search can find text that appears only on the
  prepare spell,
- the real `PrepareSpellCard` remains available as a blueprint for card
  construction,
- rules aggregation does not make prepare spell effects act as permanent
  abilities.

Use explicit Prepare branches for this rather than accepting direct-cast leakage
from `CardWithSpellOption`.

## Iterative Plan Of Attack

Implement Prepare in vertical slices. Each slice should leave the engine in a
coherent state, add focused tests for the invariant it introduces, and avoid
pulling in later mechanics until the current slice is green. Prefer one commit
per completed slice.

General sequencing rules:

- Keep `PermanentImpl#setPrepared(boolean, Game)` as marker/UI plumbing until
  the helper owns lifecycle behavior.
- Route all effect entry points through `PrepareUtil` as soon as the helper
  exists; do not add per-card Prepare hacks.
- Avoid `CardWithSpellOption` playability inheritance throughout. If rendering
  later needs shared spell-option metadata, add explicit Prepare branches
  instead of reusing Adventure/Omen casting behavior.
- Add tests at the same time as the behavior they protect. Do not defer all
  tests to the end; many Prepare bugs will only show up through playability,
  state-based cleanup, or rollback simulation.
- After each slice, run `git -c core.fsmonitor=false diff --check` and the
  focused `PrepareTest` cases that exist so far. Run a compile-oriented Maven
  check after touching shared engine classes such as `GameState`,
  `PlayerImpl`, or copied-card state-based cleanup.

### Slice 0: Preflight And Baseline

Goal: establish the exact starting behavior and avoid debugging pre-existing
failures as Prepare regressions.

Work:

- Confirm the current branch status and note any uncommitted work before code
  changes.
- Identify the first physical-card fixtures to use. Start with simple
  preparation cards such as `Elite Interceptor` before using X-cost or unusual
  cards.
- Run or at least attempt the smallest relevant baseline test command available
  in this checkout.
- Create `PrepareTest` only if the test package layout is clear; otherwise
  inspect neighboring keyword tests before adding files.

Done when:

- The implementation target files and first fixture cards are known.
- Any baseline test failures are recorded separately from Prepare work.
- No behavior changes have been made yet.

### Slice 1: Physical Prepare Characteristics And Copy Construction

Goal: build a standalone `PrepareSpellCopyCard` from a real physical
preparation card, without yet solving copied permanents, tokens, mutate, or
phasing.

Work:

- Add `PrepareSpellCharacteristics` or an equivalent immutable/copyable result
  object.
- Implement the physical-card resolver path:
  `PrepareCard` / `PermanentCard` wrapping a `PrepareCard` ->
  `PrepareSpellCharacteristics`.
- Fix `PrepareCard` copy construction so copied blueprints have an independent
  `spellCard` rather than losing it or sharing the original card's inset spell.
- Fix/rework `PrepareSpellCard` copying so any copied blueprint is re-parented
  to the copied `PrepareCard`; do not preserve a parent pointer to the original
  physical card.
- Add `PrepareSpellCopyCard` as a standalone `CardImpl` copy object.
- Build the copy from the resolved characteristics, not from the parent
  permanent card.
- Assign/finalize the copy id before repairing source ids.
- Ensure the copied spell ability, subabilities, modes, targets, effects, and
  watchers point at the copied card id where source ids are required.
- Set the copied card owner/bookkeeping owner to the controller who creates the
  copy. Do not treat that owner as the future casting authority.
- Mark or type the object so later code can distinguish a Prepare-created copy
  from a physical card, the inset `PrepareSpellCard`, and unrelated copied-card
  objects.

Tests:

- Add a focused construction/invariant test if the helper can be reached without
  a full game flow.
- Otherwise, defer test assertions to Slice 2 but keep this slice small enough
  to inspect manually.
- Required assertions once testable: `copy.getMainCard() == copy`, no parent
  `PrepareCard`, no `CardWithSpellOption`, spell ability source id equals copy
  id, every copied ability source id equals copy id, and the copy is not
  reported as a physical card for card-specific interactions.
- Copy a `PrepareCard` blueprint or restored card and assert the copied
  `PrepareCard` has its own inset spell blueprint, with the inset spell's
  parent pointing at the copied card rather than the original.

Done when:

- A physical `PrepareCard` can produce a standalone prepare-spell copy object.
- The copy has only prepare spell characteristics.
- No normal game flow exposes the real inset `PrepareSpellCard` as castable.

Do not include yet:

- Copy registration in exile.
- Cast permission.
- Copied/token/mutate source resolution.

### Slice 2: GameState Tracking And Exile Registration

Goal: when a physical source permanent becomes prepared, create exactly one live
linked copy in a real exile zone and keep it alive under CR 722.3c.

Work:

- Add `PrepareSourceKey` and `PrepareCopyInfo`, or equivalent typed state.
- Add a dedicated `GameState` field and narrow helpers:
  `putPrepareCopyInfo`, `getPrepareCopyInfo`,
  `findPrepareInfoByCopyId`, `removePrepareCopyInfo`, and
  `isLivePrepareSpellCopy`.
- Deep-copy the Prepare state in the `GameState` copy constructor.
- Restore it in `GameState#restore(...)`.
- Clear it in `clearOnGameRestart()`.
- Add a central registration helper that:
  - creates the `PrepareSpellCopyCard`,
  - adds it to `GameState.copiedCards`,
  - deliberately does not add a `GameState.COPIED_CARD_KEY` backup,
  - sets the card zone to `Zone.EXILED`,
  - adds the id to a real `ExileZone`,
  - records `PrepareCopyInfo`,
  - records permission-effect identity once Slice 3 adds the effect.
- Add a narrow CR 704.5e state-based-action exemption for live valid Prepare
  copies in exile while they are present in `GameState.copiedCards`.
- Route `BecomePreparedSourceEffect`, `BecomePreparedTargetEffect`, and
  `EntersPreparedAbility` through `PrepareUtil.setPrepared(...)`.
- Keep the old permanent marker setter as the low-level marker/UI operation.

Tests:

- Enters-prepared physical permanent creates one exiled copy.
- The copy is present in an actual `ExileZone` and resolves through
  `game.getCard(copyId)`.
- A live valid Prepare copy survives generic copied-card state-based cleanup.
- Repeated "becomes prepared" while already prepared does not create a second
  copy.
- No `COPIED_CARD_KEY + copyId` value is written for the live Prepare copy.
- Copied-card cleanup does not remove the live Prepare copy from `copiedCards`
  while the source remains a valid prepared permanent.
- If a become-prepared effect resolves while the permanent is already prepared,
  costs and non-Prepare parts of that effect are not undone; only the
  become-prepared result is a no-op.

Done when:

- A physical prepared permanent has exactly one tracked live copy in exile.
- No stale or duplicate copy is created by repeated prepare events.
- State-based cleanup no longer immediately removes the live exiled copy.

Do not include yet:

- Permission to cast the copy.
- Successful-cast consumption.
- Phasing or zone-leave cleanup beyond what is required to keep state coherent.

### Slice 3: Cast Permission And Successful-Cast Consumption

Goal: allow the prepared permanent's current controller to cast the linked copy
from exile, then clear prepared at the successful-cast event without removing
the spell from the stack.

Current implementation notes:

- `PrepareCastFromExileEffect` lives under
  `mage.abilities.effects.common.asthought` and is keyed by the source
  permanent `MageObjectReference` plus the current prepare-copy id.
- Permission uses `CAST_FROM_NOT_OWN_HAND_ZONE` with `Duration.Custom`.
- The effect validates against `PrepareCopyInfo`, copied-card registration, the
  real exile zone, source permanent object identity, prepared marker,
  phased-in status, and the source permanent's current controller.
- `applies(...)` does not prompt, edit zones, edit `PrepareCopyInfo`, or write
  alternate-cost state. It only validates and self-discards if the tracked
  source/copy relationship is gone.
- Because `GameImpl#addEffect` copies effects and assigns a fresh effect id,
  `PrepareUtil` records the registered permission effect id by scanning for the
  matching prepare-copy id after registration. Cleanup discards all matching
  permission effects, not only the first match.
- Successful-cast consumption is a central `GameState#handleEvent` hook on
  `SPELL_CAST`, immediately after ordinary watchers and before delayed/normal
  triggers. It uses `event.getSourceId()` as the prepare-copy id.
- The consumption path deliberately does not call the live-exile predicate,
  because by `SPELL_CAST` the copied card has already moved from exile to the
  stack. It clears the source permanent's prepared marker and tracking without
  removing the copied card from `copiedCards` or the stack.
- This slice currently has static verification only in this session. Runtime
  `PrepareTest` execution remains required.

Work:

- Add `PrepareCastFromExileEffect` using
  `AsThoughEffectType.CAST_FROM_NOT_OWN_HAND_ZONE` and `Duration.Custom`.
- Make the effect validate every query against `PrepareCopyInfo`, the exile
  zone, source `MageObjectReference`, source battlefield presence, prepared
  marker, phased-in status, and current source controller.
- Ensure `applies(...)` performs no prompts, no zone edits, no
  `PrepareCopyInfo` edits, and no player cast-cost side effects. It may
  self-discard stale permission effects.
- Add a registration helper that can either return the registered effect id or
  later find effects by a stable source/copy permission key.
- Record the effect identity/key in `PrepareCopyInfo`.
- Add `PrepareSpellCastWatcher` or equivalent watcher-based cleanup on
  `SPELL_CAST`.
- On `SPELL_CAST`, read `event.getSourceId()` as the copied card id. If it is a
  tracked live Prepare copy, clear the source permanent's prepared marker and
  tracking.
- Preserve these hard cast/event invariants:
  - the copied card is cast from `Zone.EXILED`,
  - the pre-cast `CAST_SPELL` event and successful `SPELL_CAST` event both
    report `event.getZone() == Zone.EXILED`,
  - `event.getSourceId()` is the `PrepareSpellCopyCard` id,
  - `event.getTargetId()` remains the normal stack spell / spell ability id
    expected by existing spell-cast triggers,
  - the stack `Spell#getFromZone()` is `Zone.EXILED`,
  - the stack `Spell#getCard()` is the standalone copied card and
    `Spell#getCard().isCopy()` is true,
  - the copied `SpellAbility#getSourceId()` is the copied card id, not the
    source permanent id and not the inset `PrepareSpellCard` blueprint id.
- In the successful-cast path, do not remove the copied spell from the stack.
  Only remove/clear the exile tracking and permission state.
- Ensure failed, cancelled, illegal, or replaced casts do not clear prepared.
- Set or preserve the prepare-spell cast metadata described above once the
  spell is successfully cast, so CR 722.3d stack-copy behavior has a stable
  source of truth.

Tests:

- Prepared copy is playable from exile at legal timing. Initial end-to-end
  coverage has been added with `Elite Interceptor` / `Rejoinder`.
- Sorcery prepare spells obey normal sorcery timing; instant prepare spells use
  instant timing.
- "Enters prepared" establishes the live copy before the next priority window.
  On an empty stack in the controller's main phase, the controller can cast a
  sorcery-speed prepare copy before opponents can remove the source. If waiting
  triggers are put on the stack first, sorcery-speed timing still requires an
  empty stack.
- Prepare is not treated as an alternative cost. Existing alternative costs,
  additional costs, cost reducers, cost increases, and cast prohibitions still
  run through the ordinary cast pipeline.
- A `Meddling Mage`-style prohibition naming the prepare spell's alternative
  name prevents casting the prepare copy.
- Casting the copy resolves its spell effect. Initial coverage asserts the
  target taps and the controller draws.
- The `SPELL_CAST` event source id is the exiled copy id.
- The `SPELL_CAST` event zone and the stack spell's `fromZone` are both
  `Zone.EXILED`.
- Spell-from-exile triggers that inspect `Spell#getFromZone()` or
  `event.getZone()` both see the Prepare cast as a spell cast from exile.
- Card-from-exile triggers still reject it because the stack spell's card is a
  copy.
- Successful cast clears prepared and `PrepareCopyInfo`. Initial coverage added.
- Successful cast does not remove the copied spell from the stack before it can
  resolve. Initial coverage is indirect through spell resolution.
- Failed/cancelled/illegal cast does not clear prepared.
- A same-event cast trigger observes the source permanent after the prepared
  marker has been cleared, if this can be expressed in tests.
- If the prepare copy itself is the event that later makes the source prepared
  again, the source first loses prepared for the cast copy and then receives one
  fresh copy from the later resolving trigger/effect.

Done when:

- The basic physical-card Prepare loop works end to end:
  enter/gain prepared -> copy in exile -> cast copy -> source loses prepared ->
  spell resolves.
- The real inset `PrepareSpellCard` is still not directly castable.

Do not include yet:

- Control-change behavior unless it naturally falls out of dynamic validation.
- Phasing.
- Copied/token/mutate sources.

### Slice 4: Explicit Cleanup, Control Changes, And Rollback Safety

Goal: make lifecycle cleanup robust and prove Prepare state survives simulation,
bookmark, and rollback boundaries coherently.

Current implementation notes:

- `PrepareUtil` now has a zone-aware Prepare-copy removal helper:
  `Zone.EXILED` removes the card from the real exile zone, clears copied-card
  registration, sets the copy outside, clears tracking, and discards permission;
  `Zone.STACK` clears tracking/permission but leaves the stack spell and copied
  card registration alone.
- `GameState#handleEvent` calls `PrepareUtil.handlePrepareZoneChange(...)`
  after the successful-cast consumption hook and before delayed/normal
  triggers.
- `handlePrepareZoneChange(...)` removes the live exile copy when the tracked
  source permanent moves from battlefield to a non-battlefield zone, citing CR
  722.3c.
- Initial test coverage covers a prepared `Elite Interceptor` being returned to
  hand by `Unsummon` before `Rejoinder` is cast. It asserts tracking and exile
  cleanup.
- Initial helper-level coverage also verifies explicit unprepare removes the
  exiled copy, copied-card registration, and `PrepareCopyInfo`.
- Initial control-change coverage uses `Skycoach Conductor` / `All Aboard` and
  `Act of Treason` to prove cast permission follows the source permanent's
  current controller rather than the copied-card owner.
- Rollback/bookmark-specific assertions and simulation mutation checks remain
  to be implemented.

Work:

- Implement zone-aware removal for tracked Prepare copies:
  - if in `Zone.EXILED`, remove from actual exile zone, set `Zone.OUTSIDE`,
    remove live copied-card registration, clear `PrepareCopyInfo`, and discard
    permission effects,
  - if in `Zone.STACK`, clear marker/tracking/permission but leave the stack
    spell alone,
  - if missing or already `Zone.OUTSIDE`, clear stale tracking and permission
    state without error.
- Add cleanup hooks for explicit unprepare and source permanent leaving the
  battlefield.
- Enforce the one-live-copy invariant after prepare, unprepare, cast, and
  zone-change transitions.
- Finish the cast-permission effect identity cleanup path so stale
  custom-duration effects do not accumulate.
- Confirm `GameState#copy` does not share mutable `PrepareSpellCopyCard`
  instances across simulations/bookmarks. Deep-copy Prepare copies by id in the
  least invasive way.
- Confirm `GameState#restore(...)` restores the marker, copied card, exile zone
  membership, `PrepareCopyInfo`, and permission effect consistently.
- Implement dynamic control-change permission: permission follows
  `sourcePermanent.getControllerId()`, not the copied-card owner.

Tests:

- Unpreparing removes the exiled copy and permission effect. Initial coverage
  added for the copy/tracking cleanup path; direct permission-effect removal
  still needs runtime-oriented coverage.
- Source leaves the battlefield before the copy is cast: copy is removed or at
  least uncastable immediately, then physically cleaned up. Initial coverage
  added for the ordinary zone-change path.
- Source leaves the battlefield after the copy has been successfully cast: the
  source was already unprepared, no live exile copy remains, and the copied
  spell on the stack is not removed by source cleanup.
- Source leaves and returns before the copy is cast: the old copy is removed
  for the old object; any returned object is a new source and creates a new copy
  only through normal enters/gain-prepared handling.
- Control changes: old controller loses permission, new controller gains
  permission, copy is not recreated, bookkeeping owner does not become casting
  authority. Initial playable-permission coverage added.
- Bookmark/rollback to before prepare removes marker, copied card, exile
  membership, tracking, and permission effect.
- Bookmark/rollback to after prepare restores all of those pieces coherently.
- Modifying/casting a copy in a simulation does not mutate the restored saved
  copy.

Done when:

- Basic Prepare is not only functional, but stable across Mage's playable
  simulation and rollback model.
- No stale playable UI entry remains after unprepare, source-zone change, cast,
  or rollback.

Do not include yet:

- Phasing.
- Copied/token/mutate source expansion.
- Destination-changing counter/return tests unless basic cleanup already covers
  them cheaply.

### Slice 5: Phasing

Goal: implement the CR 722.3c phase-out/phase-in lifecycle without clearing the
prepared marker on phase-out.

Current implementation notes:

- `PrepareUtil.handlePreparePhaseChange(...)` is called from
  `GameState#handleEvent` after successful-cast and zone-change cleanup, before
  delayed/normal triggers.
- `PHASED_OUT` resolves the phased source permanent, removes the linked
  prepare copy and permission state, and deliberately leaves the permanent's
  prepared marker intact.
- `PHASED_IN` resolves the phased-in source permanent, checks that it is still
  prepared, resolves current prepare spell characteristics, and creates a fresh
  copy through the same one-live-copy helper used by normal prepare.
- Duplicate `PHASED_IN` handling is a no-op if a valid live copy is already
  tracked for the source, so duplicate events cannot churn or multiply copies.
- `PermanentImpl#phaseOut(...)` fires `PHASED_OUT` immediately after setting
  `phasedIn` to false; `PermanentImpl#phaseIn(...)` queues `PHASED_IN` as a
  simultaneous event after setting `phasedIn` to true.
- `PrepareCastFromExileEffect` already rejects phased-out source permanents via
  `sourcePermanent.isPhasedIn()`, so any missed cleanup still leaves the copy
  uncastable.
- The phase-in path uses a synthetic static source ability only to register the
  custom-duration cast permission effect after the original prepare source
  ability is no longer on the event. The ability is sourced to the permanent id
  and controlled by the permanent's current controller.
- This slice currently has static verification only in this session. Runtime
  `PrepareTest` execution remains required.

Work:

- Add a `PHASED_OUT` watcher or equivalent hook that removes the live exiled
  copy and permission state while leaving the source permanent prepared.
- Add a `PHASED_IN` watcher or equivalent hook that creates a fresh copy if the
  permanent is still prepared and current prepare spell characteristics can be
  resolved.
- Ensure phase-in uses the same one-live-copy registration helper as normal
  prepare.
- Ensure the phase-in copy gets a fresh copy id.
- Ensure `PrepareCastFromExileEffect` rejects phased-out source permanents.

Tests:

- Prepared permanent phases out: prepared marker remains, live copy disappears
  or becomes uncastable and is cleaned up. Initial coverage asserts
  `PrepareCopyInfo` removal, copied-card registration removal, `Zone.OUTSIDE`
  state for the old copy id, and zero exile visibility.
- Prepared permanent phases in: fresh copy is created with a new id. Initial
  coverage added.
- Duplicate `PHASED_IN` handling cannot create multiple live copies or replace
  the already-created fresh copy. Initial coverage added.
- If phase-in cannot resolve current prepare spell characteristics, no fresh
  copy is created and no stale previous copy returns.

Done when:

- Phasing matches the documented rule model and preserves one live copy per
  source permanent.

Do not include yet:

- Mutate-specific phase-in behavior beyond whatever the current physical
  resolver can support.

### Slice 6: Copied And Token Preparation Permanents

Goal: let copied permanents and token copies become prepared when their
copiable values include prepare spell characteristics, while copying only the
prepare spell characteristics into exile.

Current implementation notes:

- `PrepareUtil.getPrepareSpellCharacteristics(...)` now resolves through
  `MageObject#getCopyFrom()` before physical card fallback. This makes copy
  provenance authoritative: a non-Prepare permanent copying a preparation card
  can expose prepare spell characteristics, while a physical preparation
  permanent copying a non-Prepare object cannot.
- `PermanentImpl#setPrepared(...)` now uses the resolver for the CR 722.3a
  marker gate instead of checking the backing card class directly.
- Initial helper-level coverage asserts both copy-provenance directions.
- Full end-to-end copied permanent and token-copy gameplay coverage remains
  required before this slice is complete.

Work:

- Expand `PrepareSpellCharacteristics` resolution beyond physical
  `PrepareCard`.
- Inspect `Permanent#getCopyFrom()`, `PermanentToken#getCopyFrom()`, token copy
  source card data, and active `CopyEffect` blueprints as needed.
- Resolve prepare spell characteristics from the copied object's copiable
  values, not from current non-copy continuous effects.
- Ignore other copy-effect exceptions for the exiled Prepare copy as required
  by CR 722.3c. The Croaking Counterpart-style token copy should not make the
  exiled prepare spell green, 1/1, a creature, or a Frog.
- Keep rejection behavior for permanents whose provenance cannot produce
  prepare spell characteristics.
- Do not copy the prepared designation. Copy effects copy prepare spell
  alternative characteristics, not the live prepared marker or live exiled copy.
- Permit noncreature token copies or other noncreature permanents to become
  prepared if their copiable values include prepare spell characteristics and an
  effect legally instructs that permanent to become prepared.

Tests:

- Copy of a physical preparation permanent can become prepared and creates a
  castable exiled copy. Initial helper-level coverage asserts resolver and
  marker eligibility; full castable-copy coverage remains required.
- Token copy of a preparation permanent can become prepared and creates a
  castable exiled copy.
- Copy of an already prepared permanent is not itself prepared and does not
  inherit the original source's live exiled copy.
- Physical preparation permanent copying a non-Prepare object cannot gain
  prepared. Initial helper-level coverage added.
- A noncreature token copy with prepare spell characteristics can become
  prepared when a legal effect instructs that object to become prepared.
- Copy/token modifications that are not part of the prepare spell
  characteristics do not leak into the exiled copy.
- The one-live-copy invariant still holds for copied/token sources.

Done when:

- The physical-card implementation no longer depends on
  `permanent.getMainCard() instanceof PrepareCard`.
- The documented CR 722.3c token-copy example shape is represented in tests.

Do not include yet:

- Mutate-specific top-component resolution.

### Slice 7: Mutate And Merged Permanents

Goal: make Prepare eligibility and lifecycle correct for merged permanents
without moving prepared state off the permanent object.

Current implementation notes:

- The initial resolver inspects `Permanent#getMutateForView()`. If the top
  component id is not the permanent id, it resolves prepare spell
  characteristics from that top component only.
- If the top component is the permanent id, the resolver falls back to the
  source permanent's own card/copy provenance.
- Lower mutation components do not supply prepare spell characteristics through
  this resolver unless they are the current top component.
- This covers the core CR 730.2a eligibility gate statically, but mutate
  gameplay tests still need to prove merge-over, merge-under, phase-in, and
  enters-prepared behavior.

Work:

- Replace the current `getMainCard() instanceof PrepareCard` eligibility check
  with `PrepareSpellCharacteristics` resolution everywhere.
- Teach the resolver to inspect `getMutateForView()` ordering and identify the
  current top component.
- If the top component id is the permanent id, use the base permanent's
  card/copy provenance.
- If the top component id is a mutation component, resolve that component card
  from the game and inspect its card/copy provenance.
- Do not let lower components supply prepare spell characteristics unless they
  are the top component.
- Preserve prepared marker and the existing live copy when an already-prepared
  permanent mutates.
- Preserve the existing live copy when the prepared source later loses
  abilities, changes types, or has ordinary text-changing effects applied.
- If an unprepared source has its current copiable values changed into an object
  with prepare spell characteristics, gaining prepared should use those current
  characteristics. If the current characteristics do not have a prepare spell,
  the gain-prepared event fails without creating a delayed or retroactive copy.
- Do not create a Prepare copy merely because a mutating component with
  `EntersPreparedAbility` legally merged; it did not enter the battlefield.
- Keep ordinary enters-prepared behavior when a mutating spell's target becomes
  illegal and it resolves as a normal creature spell.

Tests:

- Prepared source mutates over and under: keeps prepared, keeps one existing
  copy, and does not recreate the copy.
- Prepared source is affected by type/ability/text-changing effects after copy
  creation: the source remains prepared, keeps the existing copy, and the copy's
  spell characteristics do not change.
- Unprepared source gains or loses prepare spell characteristics before a
  become-prepared effect resolves: the helper uses the current characteristics
  at resolution and never creates a retroactive copy for an earlier failed
  event.
- Nonprepared merged permanent with a preparation top component can gain
  prepared and creates a copy from top-component prepare spell characteristics.
- Preparation component underneath a non-Prepare top component cannot supply
  prepare spell characteristics by itself.
- Legal mutate with enters-prepared does not make the merged permanent prepared
  solely from the merge.
- Illegal mutate target resolves as normal creature spell and ordinary
  enters-prepared applies.
- Prepared merged permanent leaving the battlefield removes exactly one linked
  copy.
- Prepared merged permanent phase-out/phase-in follows the phasing slice, using
  current top-component characteristics on phase-in.

Done when:

- Mutate tests prove prepared is tracked on the merged permanent object and not
  on a component.
- New prepare eligibility for merged permanents follows top-component
  characteristics.

### Slice 8: Destination-Changing Counters And Return-Spell Effects

Goal: prove a cast Prepare copy cannot become a durable card in hand, library,
graveyard, exile, command, or suspend exile after it leaves the stack.

Work:

- Add tests first against current copied-spell behavior. The existing
  `Spell#counter`, `ReturnToHandTargetEffect`, and suspend helpers may already
  do most of the correct work.
- If a Prepare-specific exemption accidentally keeps moved copied cards alive,
  narrow the exemption to only the live CR 722.3c exile copy before it is cast.
- Ensure successful cast already cleared prepared and `PrepareCopyInfo`, so
  later counter/return/move effects cannot associate the moved copied spell with
  the source permanent.
- Preserve ordinary behavior for non-copy spells.

Tests:

- `Delay` counters the Prepare copy but does not suspend it.
- `Remand` counters the Prepare copy, does not put it into hand, and still
  draws a card.
- `Memory Lapse`, `Lapse of Certainty`, and `Hinder` counter the Prepare copy
  but do not put it into or shuffle it into a library.
- `Sink into Stupor` or `Brutal Expulsion` removes/returns the copied spell
  from the stack but does not create a durable hand card and is not treated as
  countering.
- After copied-card cleanup for each of those cases, assert
  `game.getCard(copyId) == null` and
  `game.getState().getValue(GameState.COPIED_CARD_KEY + copyId) == null`.
- Ordinary non-copy spells still go to the printed destinations for those same
  effects.

Done when:

- The only copied-card-in-exile exemption is the live linked Prepare copy
  before it is cast.
- No cast Prepare copy can reappear as a durable card after a destination
  change.

### Slice 9: Search, Rendering, Card/Spell Wording, And Metadata

Goal: make preparation cards visible and searchable correctly without leaking
direct castability of the inset spell, and prove card-vs-spell wording follows
the CR model.

Current implementation notes:

- `Spell` now has explicit `wasPrepareSpell()` metadata.
- `PlayerImpl#cast(...)` sets that metadata when the cast card is a
  `PrepareSpellCopyCard`.
- `Spell#copy()`, `Spell#copySpell(...)`, and `Spell#setCopy(true, copyFrom)`
  preserve the metadata for stack-spell copies under CR 722.3d.
- Initial helper-level coverage asserts the metadata survives spell-state copy
  and stack-copy provenance. Full stack-copy gameplay coverage remains
  required before this slice is complete.

Work:

- Add explicit Prepare branches wherever card repository metadata expects
  spell-option names, mana costs, or rules text.
- Add text/search coverage for prepare spell name and rules text.
- Ensure rendered rules show prepare spell name, cost, type, and rules on the
  parent card.
- Keep card search/tutor/deck-construction metadata separate from rendered
  prepare-spell text. Searches for cards in non-battlefield zones should use
  normal card characteristics; name-choice and Commander color-identity paths
  need explicit alternative-characteristic handling.
- Add name-choice metadata so effects that choose a card name can choose the
  prepare spell's alternative name under CR 722.5.
- Ensure card search/tutor filters for library, hand, graveyard, and other
  normal zones use only the physical preparation card's normal characteristics
  under CR 722.4 unless the specific query is for name choice or color
  identity.
- Ensure Commander color identity includes prepare spell alternative
  characteristics while singleton/banned-list checks still treat the
  preparation card as one physical card.
- Add an explicit `PrepareCard`/prepare-characteristics branch to
  `ManaUtil#getColorIdentity(Card)`. The current code handles
  `CardWithSpellOption`, split cards, and double-faced cards; Prepare must not
  rely on the `CardWithSpellOption` branch because this plan deliberately keeps
  `PrepareCard` out of that hierarchy.
- Confirm the cast prepare copy is visible to spell-from-exile triggers and
  cost reducers but not card-from-exile or instant/sorcery-card effects.
- Add explicit card-wording guards for live Prepare copies in exiled-card target
  and filter paths, including `TargetCard#getAllPossibleTargetInExile` and
  `TargetCardInExile#possibleTargets`, while leaving playable discovery able to
  find the copy for CR 722.3c casting.
- Add stack-spell metadata support for CR 722.3d even though no current local
  card appears to consume "was cast as a prepare spell" metadata.
- Preserve current card-authoring API:
  `this.getSpellCard().getSpellAbility().addEffect(...)`.
- Do not make `PrepareCard` extend `CardWithSpellOption` unless all fallback
  guards in this document are implemented and tested.

Tests:

- Deckbuilder/repository search finds text that appears only on the prepare
  spell.
- Card rules rendering includes the prepare spell information.
- The real `PrepareSpellCard` is not exposed as playable from hand,
  battlefield, graveyard, library, or as a generic spell option.
- A library/hand/graveyard search for an instant or sorcery card does not find a
  preparation creature solely because its prepare spell is an instant or
  sorcery.
- Name-choice effects can choose the prepare spell's alternative name, and a
  prohibition on that name stops casting the prepare copy.
- `The Thirteenth Doctor`/`Passionate Archaeologist`/`Doc Aurlock`-style
  spell-from-exile wording sees the cast prepare copy where appropriate.
- `Prosper, Tome-Bound`-style card-from-exile wording, `Eye of the Storm`-style
  instant-or-sorcery-card wording, `Feather, the Redeemed`-style "that card"
  wording, and `Pull from Eternity`-style exiled-card targeting do not treat the
  prepare copy as a durable card.
- Magecraft-style "cast or copy an instant or sorcery spell" does not trigger
  from initial Prepare copy creation, but does trigger from casting an instant
  or sorcery prepare copy if the copied spell's type qualifies.
- Commander color identity includes alternative prepare characteristics, but
  Commander singleton/highlander checks do not count the prepare spell name as a
  separate card.
- Copying a cast prepare spell on the stack preserves its "prepare spell" /
  "was cast as a prepare spell" metadata under CR 722.3d, using a synthetic
  regression if no printed card in the local data consumes that metadata.

Done when:

- UI/search behavior is useful without compromising the rules implementation.

### Slice 10: Full Regression Sweep And Cleanup

Goal: stabilize the mechanic after all rules surfaces are implemented.

Work:

- Run the full `PrepareTest` suite.
- Run a compile-oriented check for `Mage` and `Mage.Tests`.
- Re-run nearby spell-option, copied-card, phasing, and mutate tests if they
  exist and are reasonably scoped.
- Review `PrepareUtil` and `GameState` APIs for accidental public surface area.
- Remove temporary assertions or debug logging that are not appropriate for
  normal test runs.
- Confirm no stale TODOs in the implementation contradict this plan.

Done when:

- All focused Prepare tests pass.
- Existing ordinary copied-card, Adventure/Omen, mutate, phasing, and
  counter/return behavior is not regressed by the new Prepare-specific paths.
- The implementation still matches the non-goals in this document.

## Tests To Add

Add focused tests under:

```text
Mage.Tests/src/test/java/org/mage/test/cards/abilities/keywords/PrepareTest.java
```

or another local test package matching existing conventions.

Good fixture cards already in this branch:

- `Elite Interceptor`: enters prepared; prepare spell `Rejoinder` is a sorcery
  that taps/untaps a target creature and draws a card.
- `Jadzi, Steward of Fate`: enters prepared and has an X prepare spell candidate
  for cost-copy coverage.
- `Yavimaya Bloomsage`, `Lorehold Archivist`, and similar cards exercise
  becoming prepared after the permanent is already on the battlefield.

Minimum tests:

1. Enters prepared creates a castable exiled copy

   Assert the permanent is prepared, the copy exists in an exile zone, the copy
   is playable from exile at legal timing, casting it resolves its effects, and
   the source permanent loses prepared once the copy becomes cast.

2. Real inset spell is not directly castable

   Assert the actual `PrepareSpellCard` object cannot be cast from the
   battlefield or exposed through inherited spell-option playability.

3. Not prepared means no castable copy

   Assert no prepare spell is playable while the permanent is unprepared, and no
   stale copy remains playable after unpreparing.

4. Source leaves battlefield invalidates the copy

   Prepare a permanent, remove/bounce/destroy/exile it before casting the copy,
   then assert the old copy is gone or uncastable.

5. Successful cast consumes prepared; failed cast does not

   Verify successful casting clears prepared on `SPELL_CAST`. Also cover an
   illegal/cancelled/failed cast path if the test framework can express it,
   proving early `CAST_SPELL` does not clear the marker. If practical, include
   a watcher/trigger-ordering assertion showing that normal cast triggers for
   the same `SPELL_CAST` event observe the source permanent after the prepared
   marker has already been cleared.

6. Control-change behavior

   Prepare a permanent under Player A's control, have Player B gain control of
   that source permanent, and assert Player B can cast the existing copy while
   Player A cannot. The copy should not be recreated solely because control
   changed.

7. Re-preparing creates a fresh copy

   Cast the first copy, make the permanent prepared again, and assert a new copy
   is created with a distinct id and the old copy is not reused.

8. Timing follows spell type

   Instant prepare spells should be castable at instant timing. Sorcery prepare
   spells should require normal sorcery timing.

9. X costs and copied costs work

   Use `Jadzi, Steward of Fate` or another X-cost prepare spell. Assert X can be
   chosen and paid and the result matches the chosen X.

10. Phasing

   Add coverage for a prepared permanent phasing out and phasing in prepared.
   Assert phase-out removes or invalidates the current copy without clearing
   prepared. Assert phase-in creates a fresh copy, with no duplicate live copies
   and the correct castability window.

11. At most one live copy per source

   Exercise duplicate-risk paths such as repeated prepare effects, phase-in, and
   stale tracking recovery. Assert there is never more than one live exiled copy
   associated with the same source permanent object, and only the tracked copy is
   castable.

12. Copied ability source ids

   Prepare a permanent and inspect the exiled copy before casting it. Assert the
   copy's spell ability and every copied ability use the exiled copy id as
   source id. Cast the copy and assert the `SPELL_CAST` cleanup path observes
   `event.getSourceId()` as the tracked copy id, then clears prepared on the
   source permanent.

13. Zone-aware removal semantics

   Assert a valid live prepare copy in exile survives generic copied-card
   state-based cleanup while its source permanent is still prepared. Then clear
   prepared, remove the source, and phase it out in separate cases; each should
   remove the copy from the exile zone, set the copy to `Zone.OUTSIDE`, remove
   the live copied-card registration, and clear `PrepareCopyInfo`.

14. Successful cast does not remove the stack spell

   Cast the prepare copy and assert the `SPELL_CAST` cleanup clears prepared and
   tracking without removing the copied spell from the stack. The spell should
   still resolve normally.

15. Bookmark and rollback coherence

   Prepare a permanent, take a bookmark, advance through playable checks or a
   simulated cast attempt, then restore. Assert the prepared marker,
   `PrepareCopyInfo`, `GameState.copiedCards`, zone state, exile-zone
   membership, and cast-permission effect all match the restored point. Assert
   no stale `PrepareCastFromExileEffect` remains after rollback to a point where
   no live Prepare copy exists. Also cover rollback to before the permanent
   became prepared and assert none of those pieces survive.

16. Copied-card object isolation across state copies

   Prepare a permanent, copy/bookmark game state, modify or cast the live exiled
   copy in the current state, then restore the saved state. Assert the restored
   exiled copy has the saved spell ability, source ids, modes, targets, zone,
   and castability state rather than any mutation that happened after the copy.

17. Copied or token preparation permanents

   Create a copy or token copy of a permanent with a prepare spell, make that
   copied permanent prepared, and assert it creates a castable exiled copy using
   only the prepare spell characteristics from the copied object's copiable
   values. Assert copy modifications that are not part of the prepare spell
   characteristics do not leak into the exiled copy. Also assert the one-live-copy
   invariant still holds for copied/token sources.

18. Destination-changing counters do not preserve a cast Prepare copy

   Cast a Prepare copy from exile, respond with `Delay`, `Remand`,
   `Memory Lapse`, and `Hinder` in separate cases, and assert the copied spell
   is countered and does not resolve. Assert the source permanent already lost
   prepared when the copy became cast, no `PrepareCopyInfo` remains, no live
   Prepare copy remains in exile, and no copied card becomes durable in exile,
   hand, or library. If implementation details allow the copied card to appear
   in the named destination briefly, assert the next copied-object cleanup
   removes it under CR 704.5e. For `Remand`, also assert the controller of
   Remand still draws a card.

19. Return-target-spell effects do not preserve a cast Prepare copy

   Cast a Prepare copy from exile, target it with `Sink into Stupor` or
   `Brutal Expulsion`, and assert the copied spell leaves the stack and does not
   resolve, but is not treated as countered and does not become a durable card in
   hand. Assert the source permanent remains unprepared and no live
   `PrepareCopyInfo` remains.

20. Ordinary destination-changing effects remain unchanged

   Counter normal non-copy spells with `Delay`, `Remand`, `Memory Lapse`, and
   `Hinder`, and return a normal non-copy spell with `Sink into Stupor`. Assert
   real card spells move to their printed destinations and retain their normal
   downstream behavior. This protects the Prepare-specific copied-card cleanup
   from breaking ordinary counter/return behavior.

21. Prepared source mutates

   Prepare a permanent, then mutate another creature spell over and under it in
   separate cases. Assert the permanent keeps prepared, keeps exactly one live
   Prepare copy, does not recreate the copy, and the existing copy remains
   castable while the merged permanent remains on the battlefield and prepared.

22. Merged permanent gains prepared based on the top component

   Create a merged permanent where a preparation card is the top component, then
   have an effect make it prepared. Assert a live Prepare copy is created from
   the top component's prepare spell characteristics only. Create the inverse
   case where the preparation card is underneath a non-Prepare top component and
   assert a "becomes prepared" effect does not create a Prepare copy.

23. Enters-prepared mutation component

   Mutate a preparation creature with `EntersPreparedAbility` onto a legal
   target and assert the merged permanent does not become prepared solely from
   that merge. Then make the mutate target illegal so the spell resolves as a
   normal creature spell, and assert ordinary enters-prepared handling applies.

24. Prepared merged permanent leaves or phases

   Prepare a merged permanent, then make it leave the battlefield and assert the
   single live Prepare copy is removed once for the merged permanent. Also cover
   phase-out/phase-in: phase-out removes the live copy without clearing
   prepared, and phase-in creates a fresh copy only if the current top component
   can resolve prepare spell characteristics.

25. Enters-prepared priority and trigger ordering

   Resolve an enters-prepared permanent during its controller's main phase with
   an otherwise empty stack. Assert the copy exists before the next priority
   window and a sorcery-speed prepare spell is castable before opponents receive
   priority to remove the source. In a separate case with waiting triggered
   abilities, assert those triggers are put on the stack normally and
   sorcery-speed casting remains blocked while the stack is not empty.

26. Prepare is not an alternative cost

   Cast a prepare copy while applying ordinary cost reductions, cost increases,
   additional costs, and an available alternative cost if the test framework has
   suitable fixtures. Assert Prepare only supplies zone permission and the
   normal cost pipeline determines the payable cost.

27. Repeated prepare effects while already prepared

   Resolve or activate a become-prepared effect on an already prepared source.
   Assert any costs remain paid and any non-Prepare instructions still happen,
   but no second copy is created, the existing copy id is not replaced, and only
   the tracked copy remains castable.

28. Cast-copy trigger can reprepare the source

   Use a source whose trigger can make it prepared again when the prepare copy
   itself is cast, such as a "third spell each turn" shape. Assert `SPELL_CAST`
   first clears the old prepared marker and tracking, then the later resolving
   trigger can make the source prepared again with one fresh copy.

29. Name choice and cast prohibition

   Have a player choose the prepare spell's alternative name for a
   `Meddling Mage`-style effect. Assert the name can be chosen and that casting
   the prepare copy with that name is prohibited by normal cast restrictions.

30. Card-vs-spell wording from exile

   Cast a prepare copy from exile and assert spell-from-exile wording such as
   `The Thirteenth Doctor`, `Passionate Archaeologist`, or `Doc Aurlock,
   Grizzled Genius` applies where appropriate. Assert card-from-exile wording
   such as `Prosper, Tome-Bound`, instant-or-sorcery-card wording such as
   `Eye of the Storm`, "that card" delayed wording such as
   `Feather, the Redeemed`, and exiled-card targeting such as
   `Pull from Eternity` do not treat the prepare copy as a durable card.

31. Copy creation is not spell copying

   Make a permanent prepared while a magecraft-style or `Twinning Staff`-style
   effect is present. Assert the initial CR 722.3c copy creation does not count
   as copying a spell on the stack. Then cast an instant or sorcery prepare copy
   and assert ordinary cast-spell triggers see it if its type qualifies.

32. Prepared designation is not copied

   Copy an already prepared permanent. Assert the new object is not prepared,
   has no linked live exiled copy, and does not share the original source's
   `PrepareCopyInfo`. Then make the copy prepared through a legal effect and
   assert it creates its own independent copy.

33. Noncreature copied preparation permanent can become prepared

   Create a Myrkul-style noncreature token copy whose copiable values include a
   prepare spell. If an ability or effect legally instructs that object itself
   to become prepared, assert it gains prepared and creates a copy. Also assert
   creature-targeting effects still cannot target the noncreature token unless
   another effect makes it a legal target.

34. Source text/type/copy changes after copy creation

   Prepare a source, then apply `Darksteel Mutation`-style type/ability removal,
   ordinary text-changing effects, and a copy effect such as `Mirrorform` in
   separate cases. Assert the existing copy does not change and remains linked
   while the source remains prepared. After the source becomes unprepared, a
   later gain-prepared event should use the source's current prepare spell
   characteristics or fail if none can be resolved.

35. Text-box swaps do not grant prepare spell characteristics

   Apply `Exchange of Words` or a Deadpool-style text-box swap to an unprepared
   permanent, then try to make it prepared. Assert normal rules text alone is
   not enough to supply prepare spell alternative characteristics. Only actual
   current copiable values with prepare spell characteristics should pass
   CR 722.3a.

36. Blink and reanimation create a new source object

   Prepare a permanent, then blink or reanimate it before casting the copy.
   Assert the old copy is removed when the old source leaves. If the returned
   object enters prepared, assert it creates a new copy with a new source
   reference; otherwise assert no old copy remains playable.

37. Deck, search, and color identity metadata

   Assert searches in library/hand/graveyard use the physical preparation
   card's normal characteristics and do not find it solely by the prepare spell
   type. Assert card-name choice can choose the alternative name, Commander
   color identity includes alternative characteristics, and singleton/banned
   checks do not count the prepare spell name as a separate card.

Regression tests to consider:

- rules text and deckbuilder/repository search include prepare spell text,
- casting a prepare copy counts as casting the appropriate spell type,
- card-vs-spell wording for exile, name choice, and destination effects matches
  the CR model,
- deck construction, Commander color identity, and non-battlefield search
  metadata do not confuse the prepare spell with an extra physical card,
- copy effects preserve prepare spell characteristics as copiable values, and
  the exiled Prepare copy uses only those prepare spell characteristics,
- the prepared designation itself is not copied,
- source type/text/copy changes after copy creation do not rewrite an existing
  prepare copy,
- undo/bookmark simulation does not leave stale copy ids or stale playable UI,
- unprepare, successful cast, source-zone change, phase-out, and rollback do
  not accumulate stale custom-duration Prepare cast-permission effects,
- the Prepare as-though effect remains side-effect-free during
  `PlayerImpl#getPlayable` checks.

## Remaining Questions And Implementation Risks

No rules-policy questions currently block implementation. The remaining work is
to verify the chosen model against Mage's actual state, rollback, playability,
targeting, and copied-card cleanup paths as the slices land.

Rules interaction decisions now documented:

- phasing removes/recreates the live copy without clearing prepared,
- control changes move cast permission to the source permanent's current
  controller,
- one source permanent can have at most one live exiled Prepare copy,
- copied and token-copy preparation permanents can become prepared if their
  copiable values include prepare spell characteristics,
- the prepared designation itself is not copied,
- noncreature copied/token preparation permanents can become prepared if their
  copiable values include prepare spell characteristics and a legal effect
  instructs that object to become prepared,
- repeated prepare effects on an already prepared permanent resolve normally but
  do not create or replace the live copy,
- enters-prepared creates the copy before the next priority window, while normal
  trigger and timing rules still apply,
- Prepare is zone permission, not an alternative cost,
- cast permission and restrictions must honor the prepare spell copy's name,
  including names chosen under CR 722.5,
- card-vs-spell wording must distinguish spell-from-exile effects from
  card-from-exile, instant-or-sorcery-card, "that card", and exiled-card
  effects,
- deck/search metadata must not treat the prepare spell as an extra physical
  card, while Commander color identity must include alternative
  characteristics,
- counter/return/move effects such as Delay, Remand, Memory Lapse, Hinder, and
  Sink into Stupor do not create durable destination-zone cards from a cast
  Prepare copy,
- mutate preserves prepared and the live Prepare copy on the same source
  permanent object, but newly gaining prepared on a merged permanent requires
  the current top component to supply prepare spell characteristics,
- ordinary source type/text changes after copy creation do not rewrite the
  existing copy; later fresh-copy events use current prepare spell
  characteristics or fail if none can be resolved,
- blinking or reanimating the source creates a new source object and cannot
  reconnect to the old exiled copy.

Known implementation risks to answer with code and tests:

- `PrepareCard` and `PrepareSpellCard` copy construction currently does not
  provide an independently re-parented inset spell blueprint. Fix this before
  any Prepare-copy construction depends on copied/restored card blueprints.
- `GameState#copy` currently shallow-copies `copiedCards`. Prepare registration
  must deep-copy the standalone `PrepareSpellCopyCard` or otherwise prove
  rollback/playability simulation cannot mutate a shared object.
- `GameState.COPIED_CARD_KEY` is part of copied-card SBA cleanup today, but
  Prepare should not write it. Tests must prove no key is created and that a
  consumed Prepare copy cannot be found through the `GameImpl#getCard` fallback.
- The `PrepareCastFromExileEffect` must not mutate player alternate-cost maps
  during playability simulation. Tests should prove Prepare grants only zone
  permission and that normal cost/restriction machinery remains authoritative.
- Cast-event invariants must be tested directly: `event.getZone()`,
  `Spell#getFromZone()`, `event.getSourceId()`, copied ability source id, and
  `Spell#getCard().isCopy()` all drive existing exile/spell/card wording.
- The live exiled copy must be discoverable by playable scanning but rejected
  by generic exiled-card targeting/filtering such as `Pull from Eternity`.
- `ManaUtil#getColorIdentity(Card)` needs an explicit Prepare branch because
  `PrepareCard` should not inherit `CardWithSpellOption` just to reuse color
  identity aggregation.
- CR 722.3d "was cast as a prepare spell" metadata has no current local printed
  consumer found by search, but it should still be represented and preserved
  across stack spell copies so future effects and synthetic tests have a
  correct engine hook.

## Files Likely To Change

Core engine:

- `Mage/src/main/java/mage/cards/PrepareCard.java`
- `Mage/src/main/java/mage/cards/PrepareSpellCard.java`
- `Mage/src/main/java/mage/game/permanent/Permanent.java`
- `Mage/src/main/java/mage/game/permanent/PermanentImpl.java`

New or likely helper classes:

- `Mage/src/main/java/mage/cards/PrepareSpellCopyCard.java`
- `Mage/src/main/java/mage/abilities/effects/common/asthought/PrepareCastFromExileEffect.java`
- `Mage/src/main/java/mage/cards/PrepareUtil.java`
- `Mage/src/main/java/mage/game/GameState.java` for the equivalent event hook
- `Mage/src/main/java/mage/game/PrepareCopyInfo.java`

Potential shared touch points:

- `Mage/src/main/java/mage/game/GameState.java`
- `Mage/src/main/java/mage/game/Exile.java`
- `Mage/src/main/java/mage/players/PlayerImpl.java`
- `Mage/src/main/java/mage/cards/repository/CardInfo.java`
- `Mage/src/main/java/mage/filter/predicate/card/CardTextPredicate.java`
- `Mage/src/main/java/mage/abilities/effects/common/ChooseACardNameEffect.java`
- `Mage/src/main/java/mage/filter/predicate/mageobject/NamePredicate.java`
- `Mage/src/main/java/mage/filter/predicate/mageobject/ChosenNamePredicate.java`
- `Mage/src/main/java/mage/util/ManaUtil.java`
- `Mage/src/main/java/mage/cards/decks/DeckValidator.java`
- `Mage/src/main/java/mage/cards/decks/Constructed.java`
- rules/search rendering helpers that currently special-case spell-option card
  parts
- spell/card wording helpers and triggers such as
  `SpellCastControllerTriggeredAbility`, `PlayCardTriggeredAbility`, and
  `MagecraftAbility` should be audited with tests before changing shared
  behavior.

Tests:

- `Mage.Tests/src/test/java/org/mage/test/cards/abilities/keywords/PrepareTest.java`

## Verification Commands

Start with markdown/patch hygiene:

```sh
git -c core.fsmonitor=false diff --check
```

After implementation, run focused tests first:

```sh
mvn -pl Mage.Tests -Dtest=PrepareTest test
```

If shared engine classes are touched, also run a compile-oriented check:

```sh
mvn -pl Mage -am test -DskipTests
```

Adjust Maven commands if this checkout's module setup requires a different
invocation.

## Non-Goals

- Do not add per-card hacks for individual preparation cards.
- Do not cast the real `PrepareSpellCard` directly from the battlefield.
- Do not use `GameState#copyCard` to copy the parent `PrepareCard` for CR
  722.3c.
- Do not leave stale exiled copies playable.
- Do not allow more than one live prepare copy in exile for the same source
  permanent object.
- Do not treat the prepare spell as a second physical card for deck
  construction, banned-list checks, card-from-exile triggers, card-targeting
  effects, or non-battlefield card searches.
- Do not copy the prepared designation or share an original source's live copy
  with copied/token permanents.
- Do not model Prepare as an alternative cost.
- Do not clear prepared merely because the source permanent phased out.
- Do not rework Adventure/Omen infrastructure unless a small shared helper is
  clearly necessary.

## Implementation Summary

The later coding pass should implement Prepare around a source-linked,
standalone exiled copy. The physical `PrepareCard` owns prepare spell
characteristics as a blueprint. Becoming prepared uses a source-aware helper to
create a `PrepareSpellCopyCard` in a real exile zone and grants a
custom-duration cast permission from exile. A successful `SPELL_CAST` event for
that copy clears prepared on the source permanent. Zone changes, phasing, and
explicit unprepare effects must remove or invalidate the copy so no stale copy
remains playable.
