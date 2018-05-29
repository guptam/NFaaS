package com.hortonworks.faas.nfaas.core.helper;

import com.hortonworks.faas.nfaas.config.EntityState;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServiceReferencingComponentEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ControllerServiceFacadeHelper extends BaseFacadeHelper {

    private static final Logger logger = LoggerFactory.getLogger(ControllerServiceFacadeHelper.class);

    /**
     * Check for the reference component. check for the state else sleep for 10
     * sec
     *
     * @param controllerServiceEntity
     * @param state
     */
    private void checkReferenceComponentStatus(ControllerServiceEntity controllerServiceEntity, String state) {
        int count = 0;
        int innerCount = 0;
        ControllerServiceEntity cse = null;

        while (true && count < WAIT_IN_SEC) {
            cse = controllerService.getLatestControllerServiceEntity(controllerServiceEntity);

            Set<ControllerServiceReferencingComponentEntity> referencingComponents = cse.getComponent()
                    .getReferencingComponents();

            for (ControllerServiceReferencingComponentEntity csrRefComp : referencingComponents) {

                if (!state.equalsIgnoreCase(csrRefComp.getComponent().getState())) {
                    break;
                }
                innerCount++;
            }

            if (referencingComponents.size() == innerCount) {
                break;
            }

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {

            }
            count++;
            innerCount = 0;
        }

    }


    /**
     * Stop and Un deploy the controller Services.
     *
     * @param controllerServicesEntity
     */
    private void stopAndUnDeployControllerServices(ControllerServicesEntity controllerServicesEntity) {

        Set<ControllerServiceEntity> controllerServicesEntities = controllerServicesEntity.getControllerServices();

        ControllerServiceEntity cse = null;

        for (ControllerServiceEntity controllerServiceEntity : controllerServicesEntities) {
            logger.info("stopAndUnDeployControllerServices Starts for --> "
                    + controllerServiceEntity.getComponent().getName());
            cse = stopRefrencingComponents(controllerServiceEntity);
            cse = disableControllerService(cse);
            cse = deleteControllerService(cse);
            logger.info("stopAndUnDeployControllerServices Ends for --> "
                    + controllerServiceEntity.getComponent().getName());

        }

    }

    /**
     * Method is used to enable the controller services
     *
     * @param cse
     * @return
     */
    private void enableAllControllerServices(ControllerServicesEntity controllerServicesEntity) {
        Set<ControllerServiceEntity> controllerServicesEntities = controllerServicesEntity.getControllerServices();
        ControllerServiceEntity cse = null;
        for (ControllerServiceEntity controllerServiceEntity : controllerServicesEntities) {
            if (EntityState.INVALID.getState().equalsIgnoreCase(controllerServiceEntity.getComponent().getState())) {
                logger.error("Controller Services is in invalid state.. Please validate --> "
                        + controllerServiceEntity.getComponent().getName());
                continue;
            }
            logger.info("Controller Services Enable Starts --> " + controllerServiceEntity.getComponent().getName());
            cse = enableControllerService(controllerServiceEntity);
            logger.debug(cse.toString());
            logger.info("Controller Services Enable Ends   --> " + controllerServiceEntity.getComponent().getName());
        }
    }

    /**
     * Method is used to enable the controller services
     *
     * @param cse
     * @return
     */
    private void disableAllControllerServices(ControllerServicesEntity controllerServicesEntity) {
        Set<ControllerServiceEntity> controllerServicesEntities = controllerServicesEntity.getControllerServices();
        ControllerServiceEntity cse = null;
        for (ControllerServiceEntity controllerServiceEntity : controllerServicesEntities) {
            logger.info(
                    "disableAllControllerServices Starts for --> " + controllerServiceEntity.getComponent().getName());
            cse = stopRefrencingComponents(controllerServiceEntity);
            cse = disableControllerService(cse);
            logger.info(
                    "disableAllControllerServices Ends for --> " + controllerServiceEntity.getComponent().getName());
        }
    }

    /**
     * Method is used to enable the controller services
     *
     * @param cse
     * @return
     */
    private void deleteAllControllerServices(ControllerServicesEntity controllerServicesEntity) {
        Set<ControllerServiceEntity> controllerServicesEntities = controllerServicesEntity.getControllerServices();
        ControllerServiceEntity cse = null;
        for (ControllerServiceEntity controllerServiceEntity : controllerServicesEntities) {
            logger.info(
                    "deleteAllControllerServices Starts for --> " + controllerServiceEntity.getComponent().getName());
            cse = deleteControllerService(controllerServiceEntity);
            logger.info("deleteAllControllerServices Ends for --> " + controllerServiceEntity.getComponent().getName()
                    + cse.toString());
        }
    }


}