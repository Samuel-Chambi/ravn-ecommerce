package com.ravn.ecommerce.application.usecases.refund;

import com.ravn.ecommerce.application.dto.response.RefundResponse;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.repositories.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetUserRefundsUseCase implements UseCase<Long, List<RefundResponse>> {

    private final RefundRepository repository;

    @Override
    public List<RefundResponse> execute(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(RefundResponse::toDto)
                .toList();
    }
}
