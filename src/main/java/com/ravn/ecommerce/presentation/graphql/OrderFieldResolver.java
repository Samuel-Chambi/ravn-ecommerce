package com.ravn.ecommerce.presentation.graphql;

import com.ravn.ecommerce.application.dto.response.AddressResponse;
import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.dto.response.UserSummaryResponse;
import com.ravn.ecommerce.application.repositories.AddressRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.domain.model.user.Address;
import com.ravn.ecommerce.domain.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OrderFieldResolver {

        private final UserRepository userRepository;
        private final AddressRepository addressRepository;

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

        @BatchMapping(typeName = "Order", field = "shippingAddress")
        public Map<OrderResponse, AddressResponse> shippingAddresses(List<OrderResponse> orders) {
                List<Long> addressIds = orders.stream()
                                .map(OrderResponse::getShippingAddressId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .toList();

                log.debug("BatchMapping: resolving 'shippingAddress' field for {} orders (fetching {} unique addresses)",
                                orders.size(), addressIds.size());

                Map<Long, AddressResponse> addressMap = addressIds.isEmpty()
                                ? Map.of()
                                : addressRepository.findAllById(addressIds).stream()
                                                .collect(Collectors.toMap(
                                                                Address::getId,
                                                                AddressResponse::toDto));

                Map<OrderResponse, AddressResponse> result = new HashMap<>();
                for (OrderResponse order : orders) {
                        result.put(order, order.getShippingAddressId() != null
                                        ? addressMap.get(order.getShippingAddressId())
                                        : null);
                }
                return result;
        }
}
