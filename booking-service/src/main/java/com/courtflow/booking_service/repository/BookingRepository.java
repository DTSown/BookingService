package com.courtflow.booking_service.repository;

import com.courtflow.booking_service.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Tìm kiếm các đơn đặt theo số điện thoại (sắp xếp đơn mới nhất lên đầu)
    List<Booking> findByCustomerPhoneOrderByCreatedAtDesc(String customerPhone);

    // Dùng FETCH JOIN để giải quyết bài toán N+1 Query khi lấy Booking kèm BookingDetail
    @Query("""
        SELECT DISTINCT b FROM Booking b 
        LEFT JOIN FETCH b.details bd 
        LEFT JOIN FETCH bd.court 
        LEFT JOIN FETCH bd.timeSlot 
        WHERE b.id = :id
    """)
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT b FROM Booking b 
        LEFT JOIN FETCH b.details bd 
        LEFT JOIN FETCH bd.court 
        LEFT JOIN FETCH bd.timeSlot 
        WHERE (:status IS NULL OR b.status = :status)
          AND (:date IS NULL OR bd.bookingDate = :date)
          AND (:phone IS NULL OR b.customerPhone = :phone)
        ORDER BY b.createdAt DESC
    """)
    List<Booking> findAllWithDetailsAndFilters(
        @Param("status") Booking.BookingStatus status,
        @Param("date") java.time.LocalDate date,
        @Param("phone") String phone
    );

    List<Booking> findByStatusAndCreatedAtBefore(Booking.BookingStatus status, LocalDateTime expiryTime);

    @Modifying
    @Query("""
        UPDATE Booking b 
        SET b.status = :newStatus 
        WHERE b.status = :currentStatus 
          AND b.createdAt < :expiryTime
    """)
    int expirePendingBookings(
        @Param("currentStatus") Booking.BookingStatus currentStatus,
        @Param("newStatus") Booking.BookingStatus newStatus,
        @Param("expiryTime") LocalDateTime expiryTime
    );
}