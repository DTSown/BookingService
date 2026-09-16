package com.courtflow.booking_service.repository;

import com.courtflow.booking_service.entity.Booking;
import com.courtflow.booking_service.entity.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {

    @Query("""
        SELECT COUNT(bd) > 0 FROM BookingDetail bd
        WHERE bd.court.id = :courtId
          AND bd.bookingDate = :bookingDate
          AND bd.timeSlot.id = :slotId
          AND bd.booking.status IN (:activeStatuses)
    """)
    boolean isSlotBooked(
        @Param("courtId") Long courtId,
        @Param("bookingDate") LocalDate bookingDate,
        @Param("slotId") Integer slotId,
        @Param("activeStatuses") List<Booking.BookingStatus> activeStatuses
    );

    @Query("""
        SELECT bd.timeSlot.id FROM BookingDetail bd
        WHERE bd.court.id = :courtId
          AND bd.bookingDate = :bookingDate
          AND bd.booking.status IN (:activeStatuses)
    """)
    List<Integer> findBookedSlotIds(
        @Param("courtId") Long courtId,
        @Param("bookingDate") LocalDate bookingDate,
        @Param("activeStatuses") List<Booking.BookingStatus> activeStatuses
    );

    @Query("""
        SELECT bd FROM BookingDetail bd
        JOIN FETCH bd.court
        JOIN FETCH bd.booking
        WHERE bd.booking.status = :status
          AND bd.bookingDate BETWEEN :startDate AND :endDate
        ORDER BY bd.bookingDate ASC
    """)
    List<BookingDetail> findConfirmedDetailsBetweenDates(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("status") Booking.BookingStatus status
    );
}