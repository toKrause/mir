package org.mycore.mir.authorization;

import java.util.Objects;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mir.authorization.accesskeys.MIRAccessKeyManager;
import org.mycore.mir.authorization.accesskeys.MIRAccessKeyPair;

public class MIRKeyStrategyHelper {

    private static final Logger LOGGER = LogManager.getLogger();

    protected boolean checkObjectPermission(MCRObjectID objectId, String permission) {
        LOGGER.debug("check object {} permission {}.", objectId, permission);
        boolean isWritePermission = MCRAccessManager.PERMISSION_WRITE.equals(permission);
        boolean isReadPermission = MCRAccessManager.PERMISSION_READ.equals(permission);
        return (isWritePermission || isReadPermission) && userHasValidAccessKey(objectId, isReadPermission);
    }

    private static boolean userHasValidAccessKey(MCRObjectID objectId, boolean isReadPermission) {
        String userKey = getUserKey(objectId);
        if (userKey != null) {
            MIRAccessKeyPair keyPair = MIRAccessKeyManager.getKeyPair(objectId);
            if (keyPair != null) {
                if (userKey.equals(keyPair.getWriteKey()) || isReadPermission && userKey.equals(keyPair.getReadKey())) {
                    LOGGER.debug("Access granted. User has a key to access the resource {}.", objectId);
                    return true;
                }
                if (!userKey.equals(keyPair.getReadKey())) {
                    LOGGER.warn("Neither read nor write key matches. Remove access key from user.");
                    MIRAccessKeyManager.deleteAccessKey(objectId);
                }
            }
        }
        return false;
    }

    protected boolean checkDerivatePermission(MCRObjectID derivateId, MCRObjectID objectId, String permission) {
        LOGGER.debug("check derivate {}, object {} permission {}.", derivateId, objectId, permission);
        boolean isWritePermission = MCRAccessManager.PERMISSION_WRITE.equals(permission);
        boolean isReadPermission = MCRAccessManager.PERMISSION_READ.equals(permission);
        if ((isWritePermission || isReadPermission)) {
            return Stream.of(derivateId, objectId)
                .filter(Objects::nonNull)
                .filter(id -> MIRAccessKeyManager.getKeyPair(id) != null)
                .findFirst()
                .map(id -> userHasValidAccessKey(id, isReadPermission))
                .orElse(Boolean.FALSE);
        }
        return false;
    }

    private static String getUserKey(MCRObjectID objectId) {
        return MCRSessionMgr.getCurrentSession().getUserInformation()
            .getUserAttribute(MIRAccessKeyManager.ACCESS_KEY_PREFIX + objectId.toString());
    }

}
