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

    // ---------------------------------------------------------
    // BOOK TICKET
    // ---------------------------------------------------------
    @PostMapping("/book")
    public ResponseEntity<?> bookTicket(@RequestBody Booking booking) {

        if (booking.getPassengerName() == null ||
                booking.getPassengerEmail() == null ||
                booking.getTravelDate() == null ||
                booking.getBusName() == null ||
                booking.getSeatNo() == null) {

            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
        }

        // Check if seat already booked
        boolean exists = bookingRepository.existsBySeatNoAndBusNameAndTravelDate(
                booking.getSeatNo(),
                booking.getBusName(),
                booking.getTravelDate()
        );

        if (exists) {
            return ResponseEntity.status(409).body(Map.of("error", "Seat already booked"));
        }

        Booking saved = bookingRepository.save(booking);

        // Return booking ID only (clean response)
        return ResponseEntity.ok(Map.of(
                "message", "Booking successful",
                "bookId", saved.getBookId()
        ));
    }

    // ---------------------------------------------------------
    // CHECK SEAT (FIXED VERSION)
    // ---------------------------------------------------------
    @PostMapping("/checkSeat")
    public ResponseEntity<?> checkSeat(@RequestBody Map<String, Object> req) {

        Object seatNoObj = req.get("seatNo");
        String busName = (String) req.get("busName");
        String travelDate = (String) req.get("travelDate");

        if (seatNoObj == null || busName == null || travelDate == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
        }

        // Safe convert seatNo (can be number or string)
        Integer seatNo;
        try {
            seatNo = Integer.parseInt(seatNoObj.toString());
            if (seatNo < 1 || seatNo > 28) {
                return ResponseEntity.badRequest().body(Map.of("error", "Seat number must be between 1–40"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid seat number"));
        }

        // Is seat booked?
        boolean exists = bookingRepository.existsBySeatNoAndBusNameAndTravelDate(
                seatNo, busName, travelDate
        );

        // Fetch booked seats
        List<Integer> bookedSeats = bookingRepository
                .findByBusNameAndTravelDate(busName, travelDate)
                .stream()
                .map(Booking::getSeatNo)
                .collect(Collectors.toList());

        // All seats 1-28
        List<Integer> allSeats = new ArrayList<>();
        for (int i = 1; i <= 28; i++) allSeats.add(i);

        // Available seats
        List<Integer> availableSeats = allSeats.stream()
                .filter(s -> !bookedSeats.contains(s))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "seatStatus", exists ? "UNAVAILABLE" : "AVAILABLE",
                "availableSeats", availableSeats
        ));
    }

    // ---------------------------------------------------------
    // VIEW TICKET
    // ---------------------------------------------------------
    @PostMapping("/view")
    public ResponseEntity<?> viewTicket(@RequestBody Map<String, Object> req) {

        Object bookIdObj = req.get("bookId");
        String busName = (String) req.get("busName");

        if (bookIdObj == null || busName == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
        }

        Long bookId;
        try {
            bookId = Long.parseLong(bookIdObj.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid book ID"));
        }

        List<Booking> bookings = bookingRepository.findByBookIdAndBusName(bookId, busName);

        if (bookings.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "No ticket found"));
        }

        return ResponseEntity.ok(bookings);
    }

    // ---------------------------------------------------------
    // CANCEL TICKET
    // ---------------------------------------------------------
    @PostMapping("/cancel")
    public ResponseEntity<?> cancel(@RequestBody Map<String, Object> req) {

        Object seatNoObj = req.get("seatNo");
        String busName = (String) req.get("busName");
        Object bookIdObj = req.get("bookId");

        if (seatNoObj == null || busName == null || bookIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
        }

        Integer seatNo;
        Long bookId;
        try {
            seatNo = Integer.parseInt(seatNoObj.toString());
            bookId = Long.parseLong(bookIdObj.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid number format"));
        }

        Optional<Booking> bookingOpt =
                bookingRepository.findBySeatNoAndBusNameAndBookId(seatNo, busName, bookId);

        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Booking not found"));
        }

        bookingRepository.delete(bookingOpt.get());
        return ResponseEntity.ok(Map.of("message", "Booking cancelled successfully"));
    }
}
