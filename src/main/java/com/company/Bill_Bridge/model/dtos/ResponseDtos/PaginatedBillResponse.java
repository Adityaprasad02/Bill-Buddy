package com.company.Bill_Bridge.model.dtos.ResponseDtos;

import java.util.List;

public record PaginatedBillResponse(
        List<FetchAllBills> content,
        int currentPage,
        int totalPages,
        long totalElements
) {
}
