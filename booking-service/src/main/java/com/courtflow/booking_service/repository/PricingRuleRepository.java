package com.courtflow.booking_service.repository;

import com.courtflow.booking_service.entity.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    @Query("""
        SELECT pr FROM PricingRule pr
        WHERE (pr.dayType = :dayType OR pr.dayType = 'ALL')
          AND :slotStartTime >= pr.startTime
          AND :slotStartTime < pr.endTime
        ORDER BY pr.dayType ASC
        LIMIT 1
    """)
    Optional<PricingRule> findMatchingRule(
        @Param("dayType") PricingRule.DayType dayType,
        @Param("slotStartTime") LocalTime slotStartTime
    );
}