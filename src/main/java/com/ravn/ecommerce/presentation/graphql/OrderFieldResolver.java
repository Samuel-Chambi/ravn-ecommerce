package com.ravn.ecommerce.presentation.graphql;

import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.dto.response.UserSummaryResponse;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.domain.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OrderFieldResolver {

    private final UserRepository userRepository;

    @BatchMapping(typeName = "Order", field = "client")
    public Map<OrderResponse, UserSummaryResponse> clients(List<OrderResponse> orders) {
        List<Long> userIds = orders.stream()
                .map(OrderResponse::getUserId)
                .distinct()
                .toList();

        log.debug("BatchMapping: resolving 'client' field for {} orders (fetching {} unique users)",
                orders.size(), userIds.size());

        Map<Long, UserSummaryResponse> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(
                        User::getId,
                        UserSummaryResponse::fromDomain));

        return orders.stream()
                .collect(Collectors.toMap(
                        order -> order,
                        order -> userMap.get(order.getUserId())));
    }
}
