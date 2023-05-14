package ru.yandex.yandexlavka.service.jsprit;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.yandexlavka.config.JSpritConfig;
import ru.yandex.yandexlavka.model.dto.CouriersGroupOrders;
import ru.yandex.yandexlavka.model.dto.OrderAssignResponse;
import ru.yandex.yandexlavka.model.entity.CourierDto;
import ru.yandex.yandexlavka.model.entity.GroupOrders;
import ru.yandex.yandexlavka.model.entity.OrderDto;
import ru.yandex.yandexlavka.model.entity.Region;
import ru.yandex.yandexlavka.service.VrpService;
import ru.yandex.yandexlavka.utils.Utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Validated
@Log
public class JSpritVrpServiceImpl implements VrpService {

    private final static String WEIGHT_INDEX_KEY = "weightIndex";
    private final static String AMOUNT_INDEX_KEY = "amountIndex";
    private final static String WEIGHT_CONSTRAINT = "weight";
    private final static String AMOUNT_CONSTRAINT = "amount";
    private final static String REGION_CONSTRAINT = "region";
    private final static String DELIVERY_DISTANCE_SAME_REGION = "sameRegion";
    private final static String DELIVERY_DISTANCE_FIRST_REGION = "firstRegion";
    private final static String DELIVERY_DISTANCE_NEXT_REGION = "nextRegion";
    private final static String DELIVERY_COST_FIRST_SHIPMENT = "first";
    private final static String DELIVERY_COST_NOT_FIRST_SHIPMENT = "notFirst";

    JSpritConfig jSpritConfig;

    private static OrderAssignResponse convertToOrderAssignResponse(LocalDate currentDate,
                                                                    List<CourierDto> couriers,
                                                                    List<OrderDto> orders,
                                                                    VehicleRoutingProblemSolution bestSolution) {
        Map<Long, CourierDto> couriersMap = couriers.stream()
                .collect(Collectors.toMap(CourierDto::getId, Function.identity()));
        Map<Long, OrderDto> ordersMap = orders.stream()
                .collect(Collectors.toMap(OrderDto::getId, Function.identity()));

        Collection<VehicleRoute> routes = bestSolution.getRoutes();
        Map<Long, List<GroupOrders>> deliveries = new HashMap<>();
        for (VehicleRoute route : routes) {
            Vehicle vehicle = route.getVehicle();
            List<List<DeliverShipment>> deliverGroups = new ArrayList<>();
            List<DeliverShipment> group = new ArrayList<>();
            for (TourActivity tourActivity : route.getTourActivities().getActivities()) {
                if (tourActivity instanceof PickupShipment) {
                    if (group.size() > 0) {
                        deliverGroups.add(group);
                        group = new ArrayList<>();
                    }
                } else if (tourActivity instanceof DeliverShipment) {
                    if (group.size() >= 0) {
                        group.add((DeliverShipment) tourActivity);
                    }
                }
            }
            if (group.size() > 0) deliverGroups.add(group);

            String vehicleId = vehicle.getId();
            String[] vehicleIdParts = vehicleId.split("_");
            long courierId = Long.parseLong(vehicleIdParts[1]);

            List<GroupOrders> groupsOrdersList = deliveries.getOrDefault(courierId, new ArrayList<>());
            CourierDto courierDto = couriersMap.get(courierId);
            for (List<DeliverShipment> deliverGroup : deliverGroups) {
                GroupOrders groupOrders = new GroupOrders(currentDate, courierDto);
                for (DeliverShipment deliverShipment : deliverGroup) {
                    Shipment shipment = (Shipment) deliverShipment.getJob();
                    long orderId = Long.parseLong(shipment.getId());
                    OrderDto orderDto = ordersMap.get(orderId);
                    long endTime = Double.valueOf(deliverShipment.getEndTime()).longValue();
                    OffsetDateTime assignedTime = OffsetDateTime.of(currentDate, LocalTime.ofSecondOfDay(endTime),
                            ZoneOffset.UTC);
                    orderDto.setAssignedTime(assignedTime);
                    orderDto.setGroupOrders(groupOrders);
                    groupOrders.addOrder(orderDto);
                }
                groupsOrdersList.add(groupOrders);
            }
            deliveries.put(courierId, groupsOrdersList);
        }
        List<CouriersGroupOrders> couriersGroupOrdersList = new ArrayList<>();
        for (Map.Entry<Long, List<GroupOrders>> courierDeliveries : deliveries.entrySet()) {
            Long courierId = courierDeliveries.getKey();
            List<GroupOrders> groupOrdersList = courierDeliveries.getValue();
            CouriersGroupOrders couriersGroupOrders = new CouriersGroupOrders(courierId, groupOrdersList);
            couriersGroupOrdersList.add(couriersGroupOrders);
        }

        return new OrderAssignResponse(currentDate, couriersGroupOrdersList);
    }

    private Map<String, VehicleType> getVehicleTypes() {
        Map<String, String> vehicleTypeNames = jSpritConfig.getTypes();
        Integer weightIndex = jSpritConfig.getCapacity().get(WEIGHT_INDEX_KEY);
        Integer amountIndex = jSpritConfig.getCapacity().get(AMOUNT_INDEX_KEY);
        Map<String, Integer> vehicleWeightConstraints = jSpritConfig.getConstraints().get(WEIGHT_CONSTRAINT);
        Map<String, Integer> vehicleAmountConstraints = jSpritConfig.getConstraints().get(AMOUNT_CONSTRAINT);
        Map<String, VehicleType> vehicleTypes = new HashMap<>();
        for (String vehicleTypeName : vehicleTypeNames.values()) {
            VehicleType vehicleType = VehicleTypeImpl.Builder.newInstance(vehicleTypeName)
                    .addCapacityDimension(weightIndex, vehicleWeightConstraints.get(vehicleTypeName))
                    .addCapacityDimension(amountIndex, vehicleAmountConstraints.get(vehicleTypeName))
                    .build();
            vehicleTypes.put(vehicleTypeName, vehicleType);
        }
        return vehicleTypes;
    }

    @Override
    public OrderAssignResponse solve(LocalDate currentDate,
                                     @NotEmpty List<CourierDto> couriers,
                                     @NotEmpty List<OrderDto> orders) {
        List<Integer> distinctOrdersRegions =
                orders.stream().map(order -> order.getRegion().getId()).distinct().toList();
        List<Integer> distinctCouriersRegions =
                couriers.stream().flatMap(courier -> courier.getRegions().stream())
                        .map(Region::getId)
                        .distinct().toList();
        List<Integer> distinctRegions = new ArrayList<>(distinctCouriersRegions);
        distinctRegions.addAll(distinctOrdersRegions);
        distinctRegions = distinctRegions.stream().distinct().sorted().collect(Collectors.toList());
        distinctRegions.add(0, 0);

        MultiVehicleTypeCosts multiVehicleTypeCosts = new MultiVehicleTypeCosts(getIndexMatrices(distinctRegions.size()));

        Location locationDepot = Location.Builder.newInstance().setId("0").setIndex(0).build();

        Map<String, VehicleType> vehicleTypes = getVehicleTypes();

        List<Vehicle> vehicles = convertCouriersToVehicles(couriers, vehicleTypes, locationDepot);
        List<Shipment> shipments = convertOrdersToShipments(orders, distinctRegions, locationDepot);

        Map<String, Integer> vehicleRegionConstraints = jSpritConfig.getConstraints().get(REGION_CONSTRAINT);

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addAllVehicles(vehicles);
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        vrpBuilder.setRoutingCost(multiVehicleTypeCosts);
        vrpBuilder.addAllJobs(shipments);
        VehicleRoutingProblem problem = vrpBuilder.build();

        StateManager stateManager = new StateManager(problem);
        ConstraintManager constraintManager = new ConstraintManager(problem, stateManager);
        constraintManager.addConstraint(new MaxLocationsHardActivityConstraint(vehicleRegionConstraints),
                ConstraintManager.Priority.CRITICAL);
        constraintManager.addConstraint(new DisallowTransitHardActivityConstraint(),
                ConstraintManager.Priority.CRITICAL);
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(problem)
                .addCoreStateAndConstraintStuff(true)
                .setProperty(Jsprit.Parameter.MIN_UNASSIGNED, "0")
                .setProperty(Jsprit.Parameter.MAX_TRANSPORT_COSTS, Double.toString(1.0E8))
                .setStateAndConstraintManager(stateManager, constraintManager)
                .buildAlgorithm();

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.CONCISE);
        SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);

        return convertToOrderAssignResponse(currentDate, couriers, orders, bestSolution);
    }

    private List<Shipment> convertOrdersToShipments(List<OrderDto> orders,
                                                    List<Integer> distinctRegions,
                                                    Location depotLocation) {

        Integer weightIndex = jSpritConfig.getCapacity().get(WEIGHT_INDEX_KEY);
        Integer amountIndex = jSpritConfig.getCapacity().get(AMOUNT_INDEX_KEY);

        return orders.stream().map(order -> {
            int regionId = order.getRegion().getId();
            int locationIndex = distinctRegions.indexOf(regionId);
            Location orderLocation =
                    Location.Builder.newInstance().setId(String.valueOf(locationIndex)).setIndex(locationIndex).build();

            List<TimeWindow> deliveryHoursTimeWindows = order.getDeliveryHours().stream()
                    .map(line -> Utils.convertStringIntervalToSecondsInterval(line)).collect(Collectors.toList());

            Shipment shipment = Shipment.Builder.newInstance(String.valueOf(order.getId()))
                    .addSizeDimension(weightIndex, Float.valueOf(order.getWeight()).intValue())
                    .addSizeDimension(amountIndex, 1)
                    .addRequiredSkill(String.valueOf(regionId))
                    .setPickupLocation(depotLocation)
                    .setDeliveryLocation(orderLocation)
                    .addAllDeliveryTimeWindows(deliveryHoursTimeWindows)
                    .build();
            return shipment;
        }).collect(Collectors.toList());
    }

    private List<Vehicle> convertCouriersToVehicles(
            List<CourierDto> couriers,
            Map<String, VehicleType> vehicleTypes,
            Location locationDepot) {

        return couriers.stream().flatMap(courier -> {
            List<TimeWindow> workingHoursTimeWindows = courier.getWorkingHours().stream()
                    .map(line -> Utils.convertStringIntervalToSecondsInterval(line)).collect(Collectors.toList());

            VehicleType vehicleType = vehicleTypes.get(courier.getCourierType().toString().toLowerCase(Locale.ROOT));
            long courierId = courier.getId();
            int counter = 1;
            List<Vehicle> courierVehicles = new ArrayList<>();
            for (TimeWindow timeWindow : workingHoursTimeWindows) {
                String vehicleId = vehicleType.getTypeId() + "_" + courierId + "_" + counter++;
                List<String> locationIds = courier.getRegions().stream()
                        .map(region -> String.valueOf(region.getId())).collect(Collectors.toList());

                Vehicle vehicle = VehicleImpl.Builder.newInstance(vehicleId)
                        .setStartLocation(locationDepot)
                        .setType(vehicleType)
                        .addAllSkills(locationIds)
                        .setEarliestStart(timeWindow.getStart())
                        .setLatestArrival(timeWindow.getEnd())
                        .build();
                courierVehicles.add(vehicle);
            }
            return courierVehicles.stream();
        }).collect(Collectors.toList());
    }

    private Map<String, double[][][]> getIndexMatrices(int locationsAmount) {

        Map<String, double[][][]> matrices = new HashMap<>();

        Map<String, String> vehicleTypeNames = jSpritConfig.getTypes();

        for (String vehicleTypeName : vehicleTypeNames.values()) {
            Map<String, Integer> vehicleDistanceParams = jSpritConfig.getDeliveryDistance().get(vehicleTypeName);
            Integer deliveryCostFirstShipment = jSpritConfig.getDeliveryCost().get(DELIVERY_COST_FIRST_SHIPMENT);
            Integer deliveryCostNotFirstShipment = jSpritConfig.getDeliveryCost().get(DELIVERY_COST_NOT_FIRST_SHIPMENT);

            double[][][] matrix = new double[locationsAmount][locationsAmount][2];

            for (int rowIndex = 0; rowIndex < locationsAmount; rowIndex++) {
                for (int colIndex = 0; colIndex < locationsAmount; colIndex++) {
                    if (rowIndex == 0 && colIndex == 0) {
                        matrix[rowIndex][colIndex][0] = 0;
                        matrix[rowIndex][colIndex][1] = 0;
                    } else if (rowIndex == colIndex) {
                        matrix[rowIndex][colIndex][0] = deliveryCostNotFirstShipment;
                        matrix[rowIndex][colIndex][1] = vehicleDistanceParams.get(DELIVERY_DISTANCE_SAME_REGION);
                    } else if (rowIndex == 0) {
                        matrix[rowIndex][colIndex][0] = deliveryCostFirstShipment;
                        matrix[rowIndex][colIndex][1] = vehicleDistanceParams.get(DELIVERY_DISTANCE_FIRST_REGION);
                    } else if (colIndex == 0) {
                        matrix[rowIndex][colIndex][0] = 0;
                        matrix[rowIndex][colIndex][1] = 0;
                    } else {
                        matrix[rowIndex][colIndex][0] = deliveryCostNotFirstShipment;
                        matrix[rowIndex][colIndex][1] = vehicleDistanceParams.get(DELIVERY_DISTANCE_NEXT_REGION);

                    }
                }
            }
            matrices.put(vehicleTypeName, matrix);
        }
        return matrices;
    }

}
