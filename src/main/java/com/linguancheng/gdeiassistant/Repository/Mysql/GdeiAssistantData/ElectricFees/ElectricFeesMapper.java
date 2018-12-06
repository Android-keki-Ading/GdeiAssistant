package com.gdeiassistant.gdeiassistant.Repository.Mysql.GdeiAssistantData.ElectricFees;

import com.gdeiassistant.gdeiassistant.Pojo.Entity.ElectricFees;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ElectricFeesMapper {

    public ElectricFees selectElectricFees(@Param("name") String name
            , @Param("number") Long number, @Param("year") Integer year);

    public void insertElectricFeesBatch(List<ElectricFees> electricFees);
}
