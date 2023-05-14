package ru.yandex.yandexlavka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "jsprit.vehicle")
public class JSpritConfig {

    private Map<String, Integer> capacity;
    private Map<String, String> types;
    private Map<String, Map<String, Integer>> constraints;

    private JSpritVehicleDeliveryConfig delivery;

    public JSpritConfig(JSpritVehicleDeliveryConfig delivery) {
        this.delivery = delivery;
    }

    public Map<String, Integer> getCapacity() {
        return capacity;
    }

    public void setCapacity(Map<String, Integer> capacity) {
        this.capacity = capacity;
    }

    public Map<String, Integer> getDeliveryCost() {
        return delivery.getCost();
    }

    public Map<String, Map<String, Integer>> getDeliveryDistance() {
        return delivery.getDistance();
    }

    public void setDelivery(JSpritVehicleDeliveryConfig deliveryConfig) {
        this.delivery = deliveryConfig;
    }

    public Map<String, String> getTypes() {
        return types;
    }

    public void setTypes(Map<String, String> types) {
        this.types = types;
    }

    public Map<String, Map<String, Integer>> getConstraints() {
        return constraints;
    }

    public void setConstraints(Map<String, Map<String, Integer>> constraints) {
        this.constraints = constraints;
    }

    @Component
    @ConfigurationProperties(prefix = "jsprit.vehicle.delivery")
    public static class JSpritVehicleDeliveryConfig {
        private Map<String, Integer> cost;
        private Map<String, Map<String, Integer>> distance;

        public Map<String, Integer> getCost() {
            return cost;
        }

        public void setCost(Map<String, Integer> cost) {
            this.cost = cost;
        }

        public Map<String, Map<String, Integer>> getDistance() {
            return distance;
        }

        public void setDistance(Map<String, Map<String, Integer>> distance) {
            this.distance = distance;
        }
    }
}


