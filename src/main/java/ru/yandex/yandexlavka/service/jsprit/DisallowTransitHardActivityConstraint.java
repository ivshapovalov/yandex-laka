package ru.yandex.yandexlavka.service.jsprit;

import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DisallowTransitHardActivityConstraint implements HardActivityConstraint {

    public DisallowTransitHardActivityConstraint() {
    }

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        if (newAct instanceof DeliverShipment) {
            List<TourActivity> tourActivities = iFacts.getRoute().getActivities();
            if (tourActivities.size() != 0) {
                int pickupInsertionIndex = iFacts.getRelatedActivityContext().getInsertionIndex();
                int deliveryInsertionIndex = iFacts.getActivityContext().getInsertionIndex();
                List<Integer> activityIds = tourActivities.stream().map(tourActivity -> {
                    if (tourActivity instanceof PickupShipment) {
                        return Integer.parseInt(((PickupShipment) tourActivity).getJob().getId());
                    } else {
                        return -1 * Integer.parseInt(((DeliverShipment) tourActivity).getJob().getId());
                    }
                }).collect(Collectors.toList());
                String currentShipmentId = ((DeliverShipment) newAct).getJob().getId();

                activityIds.add(deliveryInsertionIndex, -1 * Integer.parseInt(currentShipmentId));
                activityIds.add(pickupInsertionIndex, Integer.parseInt(currentShipmentId));
                List<List<Integer>> groups = new ArrayList<>();
                List<Integer> group = new ArrayList<>();
                boolean failed = false;
                for (int activityId : activityIds) {
                    if (activityId > 0) {
                        if (group.size() > 0) {
                            if (group.get(group.size() - 1) < 0) {
                                groups.add(group);
                                group = new ArrayList<>();
                            }
                        }
                        group.add(activityId);
                    } else {
                        if (!group.contains(-1 * activityId)) {
                            failed = true;
                            break;
                        }
                        group.add(activityId);
                    }
                }
                if (failed) {
                    return ConstraintsStatus.NOT_FULFILLED;
                } else {
                    return ConstraintsStatus.FULFILLED;
                }
            }
        }
        return ConstraintsStatus.FULFILLED;
    }
}
