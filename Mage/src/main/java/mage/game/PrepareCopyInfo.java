package mage.game;

import mage.MageObjectReference;
import mage.util.Copyable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public final class PrepareCopyInfo implements Serializable, Copyable<PrepareCopyInfo> {

    private static final long serialVersionUID = 1L;

    private final MageObjectReference sourcePermanentReference;
    private final UUID copyId;
    private final UUID exileZoneId;
    private final UUID copyCreatorId;
    private UUID permissionEffectId;

    public PrepareCopyInfo(MageObjectReference sourcePermanentReference, UUID copyId, UUID exileZoneId, UUID copyCreatorId) {
        this.sourcePermanentReference = Objects.requireNonNull(sourcePermanentReference);
        this.copyId = Objects.requireNonNull(copyId);
        this.exileZoneId = Objects.requireNonNull(exileZoneId);
        this.copyCreatorId = Objects.requireNonNull(copyCreatorId);
    }

    private PrepareCopyInfo(final PrepareCopyInfo info) {
        this.sourcePermanentReference = info.sourcePermanentReference;
        this.copyId = info.copyId;
        this.exileZoneId = info.exileZoneId;
        this.copyCreatorId = info.copyCreatorId;
        this.permissionEffectId = info.permissionEffectId;
    }

    @Override
    public PrepareCopyInfo copy() {
        return new PrepareCopyInfo(this);
    }

    public MageObjectReference getSourcePermanentReference() {
        return sourcePermanentReference;
    }

    public UUID getSourcePermanentId() {
        return sourcePermanentReference.getSourceId();
    }

    public UUID getCopyId() {
        return copyId;
    }

    public UUID getExileZoneId() {
        return exileZoneId;
    }

    public UUID getCopyCreatorId() {
        return copyCreatorId;
    }

    public UUID getPermissionEffectId() {
        return permissionEffectId;
    }

    public void setPermissionEffectId(UUID permissionEffectId) {
        this.permissionEffectId = permissionEffectId;
    }
}
