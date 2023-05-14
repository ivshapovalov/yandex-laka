package ru.yandex.yandexlavka.service.jsprit;

import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MaxLocationsHardActivityConstraint implements HardActivityConstraint {
    private final Map<String, Integer> locationsConstraint;

    public MaxLocationsHardActivityConstraint(Map<String, Integer> locationsConstraint) {
        this.locationsConstraint = locationsConstraint;
    }

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        if (nextAct instanceof End) {
            return ConstraintsStatus.FULFILLED;
        } else if (newAct instanceof PickupShipment) {
            List<Integer> locationIndices = iFacts.getRoute().getActivities().stream()
                    .map(activity -> activity.getLocation().getIndex()).toList();
            int insertionIndex;
            if (nextAct instanceof End) {
                insertionIndex = locationIndices.size() - 1;
            } else {
                insertionIndex = iFacts.getRoute().getActivities().indexOf(nextAct);
            }
            int previousDepotIndex = 0;
            int nextDepotIndex = locationIndices.size();
            if (nextAct instanceof PickupShipment || newAct instanceof End) {
                if (prevAct instanceof Start || prevAct instanceof DeliverShipment) {
                    previousDepotIndex = insertionIndex;
                } else {
                    for (int i = insertionIndex; i >= 0; i--) {
                        if (locationIndices.get(i) != 0) {
                            previousDepotIndex = i + 1;
                            break;
                        }
                    }
                }
                boolean pickupEnds = false;
                for (int i = insertionIndex + 1; i < locationIndices.size(); i++) {
                    if (locationIndices.get(i) != 0 && !pickupEnds) {
                        pickupEnds = true;
                    } else if (locationIndices.get(i) == 0 && pickupEnds) {
                        nextDepotIndex = i;
                        break;
                    }
                }
            } else if (nextAct instanceof DeliverShipment) {
                for (int i = insertionIndex; i >= 0; i--) {
                    if (locationIndices.get(i) == 0) {
                        previousDepotIndex = i;
                        break;
                    }
                }
                for (int i = insertionIndex + 1; i < locationIndices.size(); i++) {
                    if (locationIndices.get(i) == 0) {
                        nextDepotIndex = i;
                        break;
                    }
                }
            }
            if (previousDepotIndex != nextDepotIndex) {
                Set<Integer> locationsIndicesBetweenDepot =
                        locationIndices.stream()
                                .skip(previousDepotIndex + 1)
                                .limit(nextDepotIndex - previousDepotIndex - 1)
                                .filter(location -> location != 0)
                                .collect(Collectors.toSet());

                String vehicleTypeId = iFacts.getNewVehicle().getType().getTypeId().toLowerCase(Locale.ROOT);
                Integer maxLocationsOfVehicleType = locationsConstraint.get(vehicleTypeId);

                int currentLocationIndex = ((Shipment) ((PickupShipment) newAct).getJob()).getDeliveryLocation().getIndex();
                if (!locationsIndicesBetweenDepot.contains(currentLocationIndex)
                        && maxLocationsOfVehicleType != null
                        && locationsIndicesBetweenDepot.size() >= maxLocationsOfVehicleType) {
                    return ConstraintsStatus.NOT_FULFILLED;
                }
            }
        }
        return ConstraintsStatus.FULFILLED;
    }
}
