package me.alikuxac.vortexia.addon.storage.network;

import me.alikuxac.vortexia.addon.storage.network.StorageNode.NodeType;
import org.bukkit.Location;
import java.util.UUID;

public class CloudDriveNode extends AbstractStorageNode {

    private String cloudId; // "player:uuid" or "channel:name"
    private boolean personal;
    private UUID owner;

    public CloudDriveNode(Location location, UUID owner) {
        super(location);
        this.owner = owner;
        this.personal = true;
        this.cloudId = "player:" + owner.toString();
    }

    @Override
    public NodeType getType() {
        return NodeType.CLOUD_DRIVE;
    }

    public String getCloudId() {
        return cloudId;
    }

    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }

    public boolean isPersonal() {
        return personal;
    }

    public void setPersonal(boolean personal) {
        this.personal = personal;
    }

    public UUID getOwner() {
        return owner;
    }
}
