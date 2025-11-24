package com.example.Busbook.controller;

import com.example.Busbook.entity.Booking;
import com.example.Busbook.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @PostMapping("/book")
    public ResponseEntity<?> bookTicket(@RequestBody Booking booking) {

        if (booking.getPassengerName() == null || booking.getPassengerEmail() == null || booking.getTravelDate() == null || booking.getBusName() == null || booking.getSeatNo() == null) {

            return ResponseEntity.badRequest().body(Map.of("error","Missing required fields"));
        }

        boolean exists = bookingRepository.existsBySeatNoAndBusNameAndTravelDate(booking.getSeatNo(), booking.getBusName(), booking.getTravelDate());

        if (exists) {
            return ResponseEntity.status(409).body(Map.of("error","Seat already booked"));
        }

        Booking saved = bookingRepository.save(booking);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/checkSeat")
    public ResponseEntity<?> checkSeat(@RequestBody Map<String, Object> req) {

        Object seatNoObj = req.get("seatNo");
        String busName = (String) req.get("busName");
        String travelDate = (String) req.get("travelDate");

        if (seatNoObj == null || busName == null || travelDate == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
        }

        Integer seatNo;
        try {
            seatNo = Integer.parseInt(seatNoObj.toString());
            if (seatNo < 1 || seatNo > 40) {
                return ResponseEntity.badRequest().body(Map.of("error", "Seat number must be between 1 to 40"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid seat number"));
        }

        boolean exists = bookingRepository.existsBySeatNoAndBusNameAndTravelDate(
                seatNo, busName, travelDate
        );

        List<Integer> bookedSeats = bookingRepository
                .findByBusNameAndTravelDate(busName, travelDate)
                .stream()
                .map(Booking::getSeatNo)
                .collect(Collectors.toList());

        List<Integer> allSeats = new ArrayList<>();
        for (int i = 1; i <= 40; i++) allSeats.add(i);

        List<Integer> availableSeats = allSeats.stream()
                .filter(s -> !bookedSeats.contains(s))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("seatStatus", exists ? "UNAVAILABLE" : "AVAILABLE");
        response.put("availableSeats", availableSeats);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/view")
    public ResponseEntity<?> viewTicket(@RequestBody Map<String, String> request) {

        String bookIdStr = request.get("bookId");
        String busName = request.get("busName");

        if (bookIdStr == null || busName == null) {
            return ResponseEntity.badRequest().body(Map.of("error","Missing fields"));
        }

        Long bookId = Long.parseLong(bookIdStr);

        List<Booking> bookings = bookingRepository.findByBookIdAndBusName(bookId, busName);

        if (bookings.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error","No ticket found"));
        }

        return ResponseEntity.ok(bookings);
    }


    @PostMapping("/cancel")
    public ResponseEntity<?> cancel(@RequestBody Map<String, String> req) {

        String seatNoStr = req.get("seatNo");
        String busName = req.get("busName");
        String bookIdStr = req.get("bookId");

        if (seatNoStr == null || busName == null || bookIdStr == null) {
            return ResponseEntity.badRequest().body(Map.of("error","Missing fields"));
        }

        Integer seatNo;
        Long bookId;

        try {
            seatNo = Integer.parseInt(seatNoStr);
            bookId = Long.parseLong(bookIdStr);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error","Invalid number format"));
        }

        Optional<Booking> bookingOpt = bookingRepository.findBySeatNoAndBusNameAndBookId(seatNo, busName, bookId);

        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error","Booking not found"));
        }

        bookingRepository.delete(bookingOpt.get());
        return ResponseEntity.ok(Map.of("message", "Booking cancelled successfully"));
    }
}
