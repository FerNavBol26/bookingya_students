package com.project.bookingya.entities;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "room")
public class RoomEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private Integer maxGuests;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal nightlyPrice;

    @Column(nullable = false)
    private Boolean available;

    public void setPricePerNight(double price) {
        this.nightlyPrice = BigDecimal.valueOf(price);
    }

    public double getPricePerNight() {
        return this.nightlyPrice.doubleValue();
    }
}
