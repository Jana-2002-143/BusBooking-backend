package com.example.Busbook.controller;

import com.example.Busbook.entity.Booking;
import com.example.Busbook.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @PostMapping("/book")
    public ResponseEntity<?> bookTicket(@RequestBody Booking booking) {

        if (booking.getPassengerName() == null || booking.getPassengerEmail() == null || booking.getTravelDate() == null || booking.getBusName() == null || booking.getSeatNo() == null) {

            return ResponseEntity.badRequest().body("Missing required fields");
        }

        boolean exists = bookingRepository.existsBySeatNoAndBusNameAndTravelDate(booking.getSeatNo(), booking.getBusName(), booking.getTravelDate());

        if (exists) {
            return ResponseEntity.status(409).body("Seat already booked");
        }

        Booking saved = bookingRepository.save(booking);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/checkSeat")
    public ResponseEntity<?> checkSeat(@RequestBody Map<String, String> req) {

        String seatNoStr = req.get("seatNo");
        String busName = req.get("busName");
        String travelDate = req.get("travelDate");

        if (seatNoStr == null || busName == null || travelDate == null) {
            return ResponseEntity.badRequest().body("Missing fields");
        }

        Integer seatNo;
        try {
            seatNo = Integer.parseInt(seatNoStr);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid seat number");
        }

        boolean exists = bookingRepository.existsBySeatNoAndBusNameAndTravelDate(seatNo, busName, travelDate);

        if (exists) {
            return ResponseEntity.ok("UNAVAILABLE");
        } else {
            return ResponseEntity.ok("AVAILABLE");
        }
    }

    @PostMapping("/view")
    public ResponseEntity<?> viewTicket(@RequestBody Map<String, String> request) {

        String bookIdStr = request.get("bookId");
        String busName = request.get("busName");

        if (bookIdStr == null || busName == null) {
            return ResponseEntity.badRequest().body("Missing fields");
        }

        Long bookId = Long.parseLong(bookIdStr);

        List<Booking> bookings = bookingRepository.findByBookIdAndBusName(bookId, busName);

        if (bookings.isEmpty()) {
            return ResponseEntity.status(404).body("No ticket found");
        }

        return ResponseEntity.ok(bookings);
    }


    @PostMapping("/cancel")
    public ResponseEntity<?> cancel(@RequestBody Map<String, String> req) {

        String seatNoStr = req.get("seatNo");
        String busName = req.get("busName");
        String bookIdStr = req.get("bookId");

        if (seatNoStr == null || busName == null || bookIdStr == null) {
            return ResponseEntity.badRequest().body("Missing fields");
        }

        Integer seatNo;
        Long bookId;

        try {
            seatNo = Integer.parseInt(seatNoStr);
            bookId = Long.parseLong(bookIdStr);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid number format");
        }

        Optional<Booking> bookingOpt = bookingRepository.findBySeatNoAndBusNameAndBookId(seatNo, busName, bookId);

        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Booking not found");
        }

        bookingRepository.delete(bookingOpt.get());
        return ResponseEntity.ok("Booking cancelled successfully");
    }
}
