package com.example.Busbook.repository;

import com.example.Busbook.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

        List<Booking> findByBookIdAndBusName(Long bookId, String busName);

        boolean existsBySeatNoAndBusNameAndTravelDate(
                        Integer seatNo,
                        String busName,
                        String travelDate);

        Optional<Booking> findBySeatNoAndBusNameAndBookId(
                        Integer seatNo,
                        String busName,
                        Long bookId);
}
