package com.matiasugluck.deremate_backend.repository;

import com.matiasugluck.deremate_backend.entity.Delivery;
import com.matiasugluck.deremate_backend.graphql.input.DeliveryFilterInput;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryRepositoryCustomImpl implements DeliveryRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Delivery> findDeliveriesByFilter(DeliveryFilterInput filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Delivery> query = cb.createQuery(Delivery.class);
        Root<Delivery> delivery = query.from(Delivery.class);
        List<Predicate> predicates = new ArrayList<>();

        // Lógica de filtro por estado (sin cambios)
        if (filter.status() != null) {
            predicates.add(cb.equal(delivery.get("status"), filter.status()));
        }

        // Lógica de filtro por ID de usuario (sin cambios)
        if (filter.assignedToUserId() != null) {
            Join<Object, Object> route = delivery.join("route");
            Join<Object, Object> user = route.join("assignedTo");
            predicates.add(cb.equal(user.get("id"), filter.assignedToUserId()));
        }

        // --- NUEVA LÓGICA PARA BUSCAR ASIGNADOS / NO ASIGNADOS ---
        if (filter.isAssigned() != null) {
            Join<Object, Object> route = delivery.join("route");
            if (Boolean.FALSE.equals(filter.isAssigned())) {
                // Si isAssigned es false, busca donde assignedTo es NULL
                predicates.add(cb.isNull(route.get("assignedTo")));
            } else { // Si isAssigned es true
                // Si isAssigned es true, busca donde assignedTo NO es NULL
                predicates.add(cb.isNotNull(route.get("assignedTo")));
            }
        }
        // -----------------------------------------------------------

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        return entityManager.createQuery(query).getResultList();
    }
}