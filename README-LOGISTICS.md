# Logistics (Delivery) Module

Endpoints:
- Delivery Orders: /api/delivery-orders
- Delivery Plans: /api/delivery-plans
  - /{planId}/orders       (GET, POST ids[], DELETE /{planOrderId})
  - /{planId}/shippers     (GET, POST, DELETE /{planShipperId})
  - /{planId}/trips        (GET)
  - /{planId}/generate-trips (POST)

Frontend pages:
- DeliveryPlanList.jsx
- DeliveryPlanDetail.jsx
    