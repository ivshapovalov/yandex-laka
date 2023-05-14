package ru.yandex.yandexlavka.service.jsprit;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;

import java.util.Map;

public class MultiVehicleTypeCosts implements VehicleRoutingTransportCosts {

    final Map<String, double[][][]> matrices;

    public MultiVehicleTypeCosts(Map<String, double[][][]> matrices) {
        this.matrices = matrices;
    }

    @Override
    public double getBackwardTransportCost(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
        return 0d;
    }

    @Override
    public double getBackwardTransportTime(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
        if (vehicle == null) return 0d;

        double[][][] matrix = matrices.get(vehicle.getType().getTypeId());
        return matrix[from.getIndex()][to.getIndex()][1];
    }

    @Override
    public double getTransportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
        if (vehicle == null) return 0d;

        double[][][] matrix = matrices.get(vehicle.getType().getTypeId());

        double time = matrix[from.getIndex()][to.getIndex()][1];
        double distance = matrix[from.getIndex()][to.getIndex()][0];
        VehicleTypeImpl.VehicleCostParams costParams = vehicle.getType().getVehicleCostParams();
        return costParams.perDistanceUnit * distance + costParams.perTransportTimeUnit * time;

    }

    @Override
    public double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
        if (vehicle == null) return 0d;

        double[][][] matrix = matrices.get(vehicle.getType().getTypeId());
        return matrix[from.getIndex()][to.getIndex()][1];

    }

    @Override
    public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
        if (vehicle == null) return 0d;

        double[][][] matrix = matrices.get(vehicle.getType().getTypeId());
        return matrix[from.getIndex()][to.getIndex()][0];
    }
}
