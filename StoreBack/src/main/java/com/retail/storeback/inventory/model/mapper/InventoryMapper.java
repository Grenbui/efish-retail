package com.retail.storeback.inventory.model.mapper;

import com.retail.storeback.inventory.model.InventoryLog;
import com.retail.storeback.inventory.model.InventoryStock;
import com.retail.storeback.inventory.model.dto.InventoryLogResponse;
import com.retail.storeback.inventory.model.dto.InventoryStockResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    InventoryStockResponse toStockResponse(InventoryStock stock);

    InventoryLogResponse toLogResponse(InventoryLog log);

    List<InventoryLogResponse> toLogResponseList(List<InventoryLog> logs);
}
