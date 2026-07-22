package ru.practice.subsidies.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "quota_balances")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuotaBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quota_balanc_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "resident_id")
    private Resident resident;

    private Integer year;
    private Integer available;
    private Integer issued;
    private Integer refunded;
    private Integer used;

    public Integer getRemaining() {
        return available - issued + refunded;
    }

    public void minusAvailable() { available--; }

    public void plusAvailable() { available++; }

    public void minusIssued() { issued--; }

    public void plusIssued() { issued++; }

    public void minusRefunded() { refunded--; }

    public void plusRefunded() { refunded++; }

    public void plusUsed() { used++; }

    public void  minusUsed() { used--; }
}
